package com.ondrejkomarek.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface JavaAnnotation
{
}

/*
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JavaFieldAnnotation{
}
*/
