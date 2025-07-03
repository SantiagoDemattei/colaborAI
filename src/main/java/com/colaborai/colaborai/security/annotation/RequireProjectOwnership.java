package com.colaborai.colaborai.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para indicar que un endpoint requiere que el usuario autenticado
 * sea el propietario del proyecto especificado por el parámetro id
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireProjectOwnership {
    /**
     * Nombre del parámetro que contiene el ID del proyecto
     */
    String projectIdParam() default "id";
    
    /**
     * Mensaje de error personalizado cuando no se tiene acceso
     */
    String message() default "No tienes permisos para modificar este proyecto";
}
