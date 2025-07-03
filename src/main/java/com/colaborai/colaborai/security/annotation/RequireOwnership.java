package com.colaborai.colaborai.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para indicar que un endpoint requiere que el usuario autenticado
 * sea el propietario del recurso especificado por el parámetro userId
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireOwnership {
    /**
     * Nombre del parámetro que contiene el ID del usuario propietario del recurso
     */
    String userIdParam() default "userId";
    
    /**
     * Mensaje de error personalizado cuando no se tiene acceso
     */
    String message() default "No tienes permisos para acceder a este recurso";
}
