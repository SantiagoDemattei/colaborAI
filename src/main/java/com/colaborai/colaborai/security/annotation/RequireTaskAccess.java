package com.colaborai.colaborai.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para indicar que un endpoint requiere que el usuario autenticado
 * tenga permisos para modificar la tarea especificada
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireTaskAccess {
    /**
     * Nombre del parámetro que contiene el ID de la tarea
     */
    String taskIdParam() default "taskId";
    
    /**
     * Mensaje de error personalizado cuando no se tiene acceso
     */
    String message() default "No tienes permisos para acceder a esta tarea";
}
