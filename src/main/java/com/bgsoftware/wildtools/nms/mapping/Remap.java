package com.bgsoftware.wildtools.nms.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Remaps.class)
public @interface Remap {

    String classPath();

    String name();

    Type type();

    String remappedName() default "";

    enum Type {

        FIELD,
        METHOD

    }


}
