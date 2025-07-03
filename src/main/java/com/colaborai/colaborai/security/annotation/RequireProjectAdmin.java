package com.colaborai.colaborai.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para indicar que un endpoint requiere que el usuario autenticado
 * tenga permisos de administración sobre el proyecto especificado
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireProjectAdmin {
    /**
     * Nombre del parámetro que contiene el ID del proyecto
     */
    String projectIdParam() default "projectId";
    
    /**
     * Mensaje de error personalizado cuando no se tiene acceso
     */
    String message() default "No tienes permisos de administración sobre este proyecto";
}
