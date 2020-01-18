package com.oul.mHipster.service.impl;

import com.oul.mHipster.layersConfig.Layer;
import com.oul.mHipster.layersConfig.LayersConfig;
import com.oul.mHipster.layersConfig.enums.LayerName;
import com.oul.mHipster.model.*;
import com.oul.mHipster.service.EntityManagerFactory;
import com.oul.mHipster.service.EntityModelBuilder;
import com.oul.mHipster.service.GenerateLayerStrategy;
import com.oul.mHipster.service.JavaFileMakerService;
import com.oul.mHipster.util.ClassUtils;
import com.squareup.javapoet.TypeSpec;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Previse nadleznosti za jedan servis!
 * Trebalo bi ga razdvojiti na najmanje 2 servisa gde bi se jedan bavio skeniranjem modela a drugi izgradnjom layera
 * jer su procesi nezavisni jedan od drugog.
 *
 * Ja bih cak razdvojio na 3 servisa gde bi prvi skenirao model, drugi logicki gradio layere tj modele layera na osnovu
 * kojih se mogu izgraditi java fajlovi a treci samo na osnovu modela layera gradio konkretne java fajlove.
 * Na taj nacin bi postigao da segmenti plugina mogu nezavisno da se grade i odrzavaju, imali bi jedino integraciju
 * preko interfejsa i ona bi bila bas pogodna za mockovanje pri testiranju (mogao bi da zakucas izgled layera i da testiras build
 * java klasa, ili da zakucas izgled domaina i da testiras kako ce izgraditi model layera). Najvaznije, zavisnost izmedju
 * komponenti sistema ce biti mala i imaces mogucnost da prepises komponentu bez da izmenis pola projekta.
 *
 * Dodatno, zbog preglednosti i lakseg reuse-ovanja pomocnog koda servis bi trebao samo da implementira metode iz
 * interfejsa. Dodatne pomocne metode (sve privatne metode) treba podeliti u dve grupe:
 *
 * 1. grupa koja se odnosi na isti poslovni proces (npr. findRelation, resolveRelation...) njih bi trebalo izmestiti iz klase
 * ali ostaviti usko povezane sa servisom. Najbolje resenje za to je apstraktna nadklasa, koja bi se dalje mogla reuse-ovati
 * kroz projekat ako uocis potrebu za slicnim servisom koji ipak ne bi mergeovao sa ovim.
 *
 * 2. grupa koja se odnosi na na druge alate (npr. resolveTypeArgument), takve bi stvari trebalo izdvajati u util klase,
 * u ovom slucaju mogli bi imati ReflectionUtil servis koji bi imao dve metode:
 *      boolean isParameterizedType(Field field);
 *      Class<T> getParameterizedType(Field field);
 */
public class EntityModelBuilderImpl implements EntityModelBuilder {

    private LayersConfig layersConfig;
    private JavaFileMakerService javaFileMakerService;
    private final GenerateLayerStrategyFactory generateLayerStrategyFactory = new GenerateLayerStrategyFactory();

    public EntityModelBuilderImpl(LayersConfig layersConfig) {
        this.layersConfig = layersConfig;
        this.javaFileMakerService = new JavaFileMakerServiceImpl();
    }

    @Override
    public Entity mapSourceToEntity(Class<?> clazz) {
        Entity.Builder builder = Entity.builder();
        builder.infoFields(clazz);

        Field[] fields = clazz.getDeclaredFields();
        builder.attributes(Arrays.stream(fields).map(field -> findRelation(field, clazz)).collect(Collectors.toList()));

        return builder.build();
    }

    private Attribute findRelation(Field field, Class clazz) {
        Annotation annM2M = field.getAnnotation(ManyToMany.class);
        Annotation annO2M = field.getAnnotation(OneToMany.class);
        Annotation annO2O = field.getAnnotation(OneToOne.class);
        Annotation annM2O = field.getAnnotation(ManyToOne.class);
        //onaj drugi case:
        //manytoone owner je uvek onaj drugi
        //ako ima mappedBy owner je onaj drugi

        //ti si owner:
        //kad ti je mappedBy prazan i nisi manytoone
        return Stream.of(annM2M, annO2M, annO2O, annM2O).filter(Objects::nonNull)
                .map(annotation -> resolveRelation(annotation, field, clazz))
                .findFirst()
                .orElse(new Attribute(field.getType(), field.getName()));
    }

    private Attribute resolveRelation(Annotation annotation, Field field, Class clazz) {
        Class<? extends Annotation> type = annotation.annotationType();

        for (Method method : type.getDeclaredMethods()) {
            Object value = null;
            try {
                value = method.invoke(annotation, (Object[]) null);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            if (method.getName().equals("mappedBy") && !value.equals("")) {
                ParameterizedType genericType = (ParameterizedType) field.getGenericType();
                Class<?> relationDomainClass = (Class<?>) genericType.getActualTypeArguments()[0];
                return new RelationAttribute(field.getType(), field.getName(), ClassUtils.getClassName(field.getType()),
                        relationDomainClass.getSimpleName(), RelationType.valueOf(ClassUtils.getClassName(type).toUpperCase()));
            } else if (method.getName().equals("mappedBy") && value.equals("")) {
                Class<?> typeArgument = resolveTypeArgument(field);
                return new RelationAttribute(field.getType(), field.getName(), ClassUtils.getClassName(typeArgument),
                        clazz.getSimpleName(), RelationType.valueOf(ClassUtils.getClassName(type).toUpperCase()));
            }
        }
        return new RelationAttribute(field.getType(), field.getName(), ClassUtils.getClassName(field.getType()),
                clazz.getSimpleName(), RelationType.MANYTOONE);
    }

    /**
     * Ne proveravaj tip na ovaj nacin jer imas previse opcija parametrizovanih kolekcija.
     *
     * U ovom slucaju lepsa provera bi bila:
     * field.getType().isAssignableFrom(Collection.class);
     */
    private Class<?> resolveTypeArgument(Field field) {
        if (field.getType().toString().equals("interface java.util.List")) {
            ParameterizedType genericType = (ParameterizedType) field.getGenericType();
            return (Class<?>) genericType.getActualTypeArguments()[0];
        }
        return field.getType();
    }

    @Override
    public void buildLayers(SourceDomainLayer sourceDomainLayer) {
        sourceDomainLayer.getEntities().forEach(entity -> {
            Map<String, ClassNamingInfo> layersMap = buildLayerClass(entity, sourceDomainLayer);
            entity.setLayers(layersMap);
        });

        EntityManagerFactory entityManagerFactory = EntityManagerFactoryImpl.getInstance();
        entityManagerFactory.createEntityManager(sourceDomainLayer);

        sourceDomainLayer.getEntities().forEach(entityModel -> layersConfig.getLayers().forEach(layer -> {
            GenerateLayerStrategy generateLayerStrategy = generateLayerStrategyFactory.getLayerStrategy(LayerName.valueOf(layer.getName()));
            TypeSpec typeSpec = generateLayerStrategy.generate(entityModel);
            // Potrebne izmene jer ce jedan entity imati niz TypeSpecova
            // Izmene trebaju i u javaFileMakerService gde se setuje packageName
            if (layer.getName().equals(LayerName.SERVICE_IMPL.toString()))
                entityModel.setTypeSpec(typeSpec);
        }));

        javaFileMakerService.makeJavaFiles(sourceDomainLayer);
    }

    @Override
    public Map<String, ClassNamingInfo> buildLayerClass(Entity entity, SourceDomainLayer sourceDomainLayer) {
        return layersConfig.getLayers().stream().collect(Collectors.toMap(Layer::getName, layer -> {
            String className = entity.getClassName() + layer.getNamingSuffix();
            String instanceName = ClassUtils.instanceNameBuilder(className);
            String packageName = sourceDomainLayer.getRootPackageName() + "." + layer.getPackageName();
            return new ClassNamingInfo(className, instanceName, packageName);
        }));
    }

}
