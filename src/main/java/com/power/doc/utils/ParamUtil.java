package com.power.doc.utils;

import com.thoughtworks.qdox.model.JavaAnnotation;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.power.common.util.StringUtil;
import com.power.doc.builder.ProjectDocConfigBuilder;
import com.power.doc.constants.DocGlobalConstants;
import com.power.doc.constants.DocTags;
import com.power.doc.model.ApiParam;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;

/**
 * @author <a href="mailto:cqmike0315@gmail.com">chenqi</a>
 * @version 1.0
 */
public class ParamUtil {

    public static JavaClass handleSeeEnum(ApiParam param, JavaField javaField,
        ProjectDocConfigBuilder builder, boolean jsonRequest,
        Map<String, String> tagsMap) {
        JavaClass seeEnum = JavaClassUtil.getSeeEnum(javaField, builder);
        if (Objects.isNull(seeEnum)) {
            return null;
        }
        param.setType(DocGlobalConstants.ENUM);
        Object value = JavaClassUtil.getEnumValue(seeEnum, !jsonRequest);
        param.setValue(String.valueOf(value));
        param.setEnumValues(JavaClassUtil.getEnumValues(seeEnum));
        param.setEnumInfo(JavaClassUtil.getEnumInfo(seeEnum, builder));
        // Override old value
        if (tagsMap.containsKey(DocTags.MOCK) && StringUtil.isNotEmpty(tagsMap.get(DocTags.MOCK))) {
            param.setValue(tagsMap.get(DocTags.MOCK));
        }
        return seeEnum;
    }

    public static JavaClass handleAnnotationEnum(ApiParam param, JavaField javaField,
        ProjectDocConfigBuilder builder, boolean jsonRequest, Map<String, String> tagsMap,
        List<JavaAnnotation> annotations) {
        JavaAnnotation annotation = annotations.stream()
            .filter(
                e -> "IsEnumWithValidate".equals(e.getType().getSimpleName()) || "IsEnum".equals(
                    e.getType().getSimpleName())).findAny()
            .orElse(null);
        JavaClass enumClass = null;
        if (annotation != null && annotation.getProperty("enumType") != null) {
            String classSimpleName = String.valueOf(
                annotation.getProperty("enumType").getParameterValue());
            if (classSimpleName != null) {
                String[] names = classSimpleName.split("\\.");
                classSimpleName = names[names.length - 2];
            }
            List<String> imports = javaField.getDeclaringClass().getSource().getImports();
            String finalClassSimpleName = classSimpleName;
            String fullClassName = imports.stream().filter(e -> {
                String[] names = e.split("\\.");
                return names[names.length - 1].equals(finalClassSimpleName);
            }).findAny().orElse(null);
            if (fullClassName != null) {
                enumClass = builder.getJavaProjectBuilder().getClassByName(fullClassName);
            }
        }
        if (Objects.isNull(enumClass)) {
            return null;
        }
//        Object value = JavaClassUtil.getEnumValue(enumClass, !jsonRequest);
        param.setEnumValues(JavaClassUtil.getEnumValues(enumClass));
        param.setEnumInfo(JavaClassUtil.getEnumInfo(enumClass, builder));
        // Override old value
        if (tagsMap.containsKey(DocTags.MOCK) && StringUtil.isNotEmpty(tagsMap.get(DocTags.MOCK))) {
            param.setValue(tagsMap.get(DocTags.MOCK));
        }
        return enumClass;
    }
}
