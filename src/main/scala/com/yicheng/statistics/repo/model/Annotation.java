package com.yicheng.statistics.repo.model;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;

/**
 * 定义常用Json序列化/反序列化注释，简化Json注释的说明。
 */
public @interface Annotation {
    //********************************************************************************
    // 序列化JSON原始数据，且提升到上级对象的属性内。
    //********************************************************************************
    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonRawValue
    @JsonUnwrapped
    public @interface SerUnwrappedRaw {}

    //********************************************************************************
    // 容器对象反序列化时指定容器内容的格式。容器对象包括：Map/Array/List/Option等。
    //********************************************************************************
    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(contentAs = Boolean.class)
    public @interface DeserValueBool {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(contentAs = Integer.class)
    public @interface DeserValueInt {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(contentAs = Long.class)
    public @interface DeserValueLong {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(contentAs = Float.class)
    public @interface DeserValueFloat {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(contentAs = Double.class)
    public @interface DeserValueDouble {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(contentAs = String.class)
    public @interface DeserValueString {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(contentAs = Date.class)
    public @interface DeserValueDate {}

    //********************************************************************************
    // 容器对象反序列化时指定容器键值的格式。带键值容器对象包括：Map等。
    //********************************************************************************
    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = Boolean.class)
    public @interface DeserKeyBool {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = Integer.class)
    public @interface DeserKeyInt {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = Long.class)
    public @interface DeserKeyLong {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = Float.class)
    public @interface DeserKeyFloat {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = Double.class)
    public @interface DeserKeyDouble {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = String.class)
    public @interface DeserKeyString {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = Date.class)
    public @interface DeserKeyDate {}

    //********************************************************************************
    // Map对象反序列化时指定Map的格式。
    //********************************************************************************
    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = Integer.class, contentAs = Float.class)
    public @interface DeserMapIntFloat {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = Integer.class, contentAs = Integer.class)
    public @interface DeserMapIntInt {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @JsonDeserialize(keyAs = Long.class, contentAs = Integer.class)
    public @interface DeserMapLongInt {}

    //********************************************************************************
    // 容器内Map对象反序列化时指定Map的格式。
    //********************************************************************************
    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    public @interface DeserInnerMap {
        public Class<?> keyAs() default Void.class;
        public Class<?> contentAs() default Void.class;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @DeserInnerMap(keyAs = Integer.class, contentAs = Float.class)
    public @interface DeserInnerIntFloat {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @DeserInnerMap(keyAs = Integer.class)
    public @interface DeserInnerKeyInt {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @DeserInnerMap(contentAs = Integer.class)
    public @interface DeserInnerValueInt {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @DeserInnerMap(keyAs = Integer.class, contentAs = Integer.class)
    public @interface DeserInnerIntInt {}

    @Retention(RetentionPolicy.RUNTIME)
    @JacksonAnnotationsInside
    @DeserInnerMap(keyAs = Long.class, contentAs = Integer.class)
    public @interface DeserInnerLongInt {}

}
