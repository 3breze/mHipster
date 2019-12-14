package tryit;

import com.squareup.javapoet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main {
    public static void main(String[] args) {
        Map<String, String> map = new HashMap<>();
        map.put("domainClazz", "Consumer");
        map.put("domain", "consumer");
        map.put("responseClazz", "ConsumerResponseDto");
        map.put("optional", "optionalCustomer");
        map.put("dao", "customerDao");

        String regex = "\\$\\{(.*?)}";
        String findByIdMethodBody = "Optional&lt;${domainClazz}&gt; ${optional} = ${dao}.findById(id);\n" +
                "                        if (${optional}.isEmpty()) {\n" +
                "                        throw new ResourceNotFoundException(\"${domainClazz} not found!\");\n" +
                "                        }\n" +
                "                        ${domainClazz} ${domain} = ${optional}.get();\n" +
                "                        return new ${responseClazz}(${domain});";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(findByIdMethodBody);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, map.get(matcher.group(1)));
        }
        matcher.appendTail(sb);

        String codeBlock = sb.toString();

        String codeBlock1 = codeBlock.replaceAll("&lt;", "<");
        String codeBlock2 = codeBlock1.replaceAll("&gt;", ">");


        // u zavisnosti od m2m i m2o veza
        FieldSpec customerDao = FieldSpec
                .builder(ClassName.get("com.whatever.dao", "CustomerDao"), "customerDao")
                .addModifiers(Modifier.PRIVATE)
                .addAnnotation(Autowired.class)
                .build();

        TypeName requestDtoClass = ClassName.get("com.whatever.domain.request", "CustomerRequestDto");
        ParameterSpec param = ParameterSpec
                .builder(requestDtoClass,
                        "customerRequestDto")
                .build();

        TypeName responseDtoClass = ClassName.get("com.whatever.domain.response", "CustomerResponseDto");

        TypeSpec serviceClass = TypeSpec
                .classBuilder("CustomerServiceImpl")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Service.class)
                .addField(customerDao)
                .addMethod(MethodSpec
                        .methodBuilder("save")
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(param)
                        .returns(responseDtoClass)
                        .addStatement(codeBlock2)
                        .build())
                .build();

        JavaFile javaFile = JavaFile
                .builder("lol.kek", serviceClass)
                .indent("    ")
                .build();


        try {
            javaFile.writeTo(System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
