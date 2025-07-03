package com.colaborai.colaborai.security.service;

import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.service.ProjectMemberService;
import com.colaborai.colaborai.service.TaskService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio centralizado para validaciones de seguridad
 */
@Service
public class SecurityService {

    private final UserRepository userRepository;
    private final ProjectMemberService projectMemberService;
    private final TaskService taskService;

    public SecurityService(UserRepository userRepository, ProjectMemberService projectMemberService, TaskService taskService) {
        this.userRepository = userRepository;
        this.projectMemberService = projectMemberService;
        this.taskService = taskService;
    }

    /**
     * Obtiene el usuario actualmente autenticado
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("Usuario no autenticado");
        }

        String username = authentication.getName();
        
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new SecurityException("Usuario autenticado no encontrado en el sistema"));
    }

    /**
     * Obtiene el ID del usuario actualmente autenticado
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }

    /**
     * Verifica si el usuario actual es el propietario del recurso
     */
    public boolean isCurrentUserOwner(Long resourceOwnerId) {
        if (resourceOwnerId == null) {
            return false;
        }
        
        try {
            Long currentUserId = getCurrentUserId();
            return currentUserId.equals(resourceOwnerId);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Valida que el usuario actual sea el propietario del recurso, lanza excepción si no
     */
    public void validateOwnership(Long resourceOwnerId) {
        validateOwnership(resourceOwnerId, "No tienes permisos para acceder a este recurso");
    }

    /**
     * Valida que el usuario actual sea el propietario del recurso con mensaje personalizado
     */
    public void validateOwnership(Long resourceOwnerId, String errorMessage) {
        if (!isCurrentUserOwner(resourceOwnerId)) {
            throw new SecurityException(errorMessage);
        }
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && !"anonymousUser".equals(authentication.getName());
    }

    /**
     * Obtiene el usuario actual de forma segura (puede retornar null)
     */
    public Optional<User> getCurrentUserSafe() {
        try {
            return Optional.of(getCurrentUser());
        } catch (SecurityException e) {
            return Optional.empty();
        }
    }

    /**
     * Verifica si el usuario actual es administrador del proyecto (owner o admin)
     */
    public boolean isCurrentUserProjectAdmin(Long projectId) {
        try {
            Long currentUserId = getCurrentUserId();
            return projectMemberService.canUserModifyProject(projectId, currentUserId);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Verifica si el usuario actual es miembro del proyecto
     */
    public boolean isCurrentUserProjectMember(Long projectId) {
        try {
            Long currentUserId = getCurrentUserId();
            return projectMemberService.isUserProjectMember(projectId, currentUserId);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Valida que el usuario actual sea administrador del proyecto
     */
    public void validateProjectAdmin(Long projectId) {
        validateProjectAdmin(projectId, "No tienes permisos de administración sobre este proyecto");
    }

    /**
     * Valida que el usuario actual sea administrador del proyecto con mensaje personalizado
     */
    public void validateProjectAdmin(Long projectId, String errorMessage) {
        if (!isCurrentUserProjectAdmin(projectId)) {
            throw new SecurityException(errorMessage);
        }
    }

    /**
     * Valida que el usuario actual sea miembro del proyecto
     */
    public void validateProjectMember(Long projectId) {
        validateProjectMember(projectId, "No tienes acceso a este proyecto");
    }

    /**
     * Valida que el usuario actual sea miembro del proyecto con mensaje personalizado
     */
    public void validateProjectMember(Long projectId, String errorMessage) {
        if (!isCurrentUserProjectMember(projectId)) {
            throw new SecurityException(errorMessage);
        }
    }

    /**
     * Verifica si el usuario actual tiene acceso a la tarea
     * (es miembro del proyecto al que pertenece la tarea)
     */
    public boolean isCurrentUserTaskAccessible(Long taskId) {
        try {
            Long currentUserId = getCurrentUserId();
            return taskService.canUserAccessTask(taskId, currentUserId);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Valida que el usuario actual tenga acceso a la tarea
     */
    public void validateTaskAccess(Long taskId) {
        validateTaskAccess(taskId, "No tienes permisos para acceder a esta tarea");
    }

    /**
     * Valida que el usuario actual tenga acceso a la tarea con mensaje personalizado
     */
    public void validateTaskAccess(Long taskId, String errorMessage) {
        if (!isCurrentUserTaskAccessible(taskId)) {
            throw new SecurityException(errorMessage);
        }
    }

    /**
     * Verifica si el usuario actual es propietario del proyecto
     */
    public boolean isCurrentUserProjectOwner(Long projectId) {
        try {
            Long currentUserId = getCurrentUserId();
            return projectMemberService.isUserProjectOwner(projectId, currentUserId);
        } catch (SecurityException e) {
            return false;
        }
    }

    /**
     * Valida que el usuario actual sea propietario del proyecto
     */
    public void validateProjectOwnership(Long projectId) {
        validateProjectOwnership(projectId, "No tienes permisos para modificar este proyecto");
    }

    /**
     * Valida que el usuario actual sea propietario del proyecto con mensaje personalizado
     */
    public void validateProjectOwnership(Long projectId, String errorMessage) {
        if (!isCurrentUserProjectOwner(projectId)) {
            throw new SecurityException(errorMessage);
        }
    }
}
