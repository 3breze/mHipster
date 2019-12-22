package com.oul.mHipster.service;

import com.oul.mHipster.model.Attribute;
import com.oul.mHipster.model.Entity;
import com.oul.mHipster.model.RelationAttribute;
import com.oul.mHipster.util.ClassUtils;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class EntityMapper{
    public Entity buildEntity(Class<?> clazz) {



        ClassUtils.getClassName(clazz);
        ClassUtils.getPackageName(clazz);



        //determine fields declared in this class only (no fields of superclass)
        Field[] fields = clazz.getDeclaredFields();
        List<Attribute> attributes = new ArrayList<>();
        //print field names paired with their values
        for (Field field : fields) {
            Annotation annM2O = field.getAnnotation(ManyToOne.class);
            Annotation annM2M = field.getAnnotation(ManyToMany.class);
            if (annM2O != null) {
                System.out.println("Logika za m2o");
                RelationAttribute attr = new RelationAttribute();
                attributes.add(attr);
            }
            if (annM2M != null) {
                System.out.println("Logika za m2m");
                RelationAttribute attr = new RelationAttribute();
                attributes.add(attr);
            }

            result.append(field.getName());

            result.append(field.getType());

        }
    }
}
