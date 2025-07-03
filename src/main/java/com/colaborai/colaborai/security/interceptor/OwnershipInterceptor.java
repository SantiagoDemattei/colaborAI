package com.colaborai.colaborai.security.interceptor;

import com.colaborai.colaborai.security.annotation.RequireOwnership;
import com.colaborai.colaborai.security.annotation.RequireProjectAdmin;
import com.colaborai.colaborai.security.annotation.RequireProjectMember;
import com.colaborai.colaborai.security.annotation.RequireProjectOwnership;
import com.colaborai.colaborai.security.annotation.RequireTaskAccess;
import com.colaborai.colaborai.security.service.SecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interceptor que maneja automáticamente la autorización basada en anotaciones
 */
@Component
public class OwnershipInterceptor implements HandlerInterceptor {

    private final SecurityService securityService;

    public OwnershipInterceptor(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        
        // Verificar anotación de ownership
        RequireOwnership requireOwnership = method.getAnnotation(RequireOwnership.class);
        if (requireOwnership != null) {
            Long userId = extractUserId(request, requireOwnership.userIdParam());
            if (userId == null) {
                throw new SecurityException("No se pudo determinar el propietario del recurso");
            }
            securityService.validateOwnership(userId, requireOwnership.message());
        }

        // Verificar anotación de administrador de proyecto
        RequireProjectAdmin requireProjectAdmin = method.getAnnotation(RequireProjectAdmin.class);
        if (requireProjectAdmin != null) {
            Long projectId = extractProjectId(request, requireProjectAdmin.projectIdParam());
            if (projectId == null) {
                throw new SecurityException("No se pudo determinar el ID del proyecto");
            }
            securityService.validateProjectAdmin(projectId, requireProjectAdmin.message());
        }

        // Verificar anotación de miembro de proyecto
        RequireProjectMember requireProjectMember = method.getAnnotation(RequireProjectMember.class);
        if (requireProjectMember != null) {
            Long projectId = extractProjectId(request, requireProjectMember.projectIdParam());
            if (projectId == null) {
                throw new SecurityException("No se pudo determinar el ID del proyecto");
            }
            securityService.validateProjectMember(projectId, requireProjectMember.message());
        }

        // Verificar anotación de propiedad de proyecto
        RequireProjectOwnership requireProjectOwnership = method.getAnnotation(RequireProjectOwnership.class);
        if (requireProjectOwnership != null) {
            Long projectId = extractProjectId(request, requireProjectOwnership.projectIdParam());
            if (projectId == null) {
                throw new SecurityException("No se pudo determinar el ID del proyecto");
            }
            securityService.validateProjectOwnership(projectId, requireProjectOwnership.message());
        }

        // Verificar anotación de acceso a tarea
        RequireTaskAccess requireTaskAccess = method.getAnnotation(RequireTaskAccess.class);
        if (requireTaskAccess != null) {
            Long taskId = extractTaskId(request, requireTaskAccess.taskIdParam());
            if (taskId == null) {
                throw new SecurityException("No se pudo determinar el ID de la tarea");
            }
            securityService.validateTaskAccess(taskId, requireTaskAccess.message());
        }
        
        return true;
    }

    private Long extractUserId(HttpServletRequest request, String paramName) {
        // Primero intentar obtener de path variables
        Long userId = extractFromPath(request.getRequestURI(), paramName);
        if (userId != null) {
            return userId;
        }

        // Luego intentar obtener de query parameters
        String userIdStr = request.getParameter(paramName);
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    private Long extractProjectId(HttpServletRequest request, String paramName) {
        // Patrones para extraer projectId del path
        String[] patterns = {
            "/projects/([0-9]+)",           // Para /api/projects/{projectId}
            "/project/([0-9]+)",           // Para /api/tasks/project/{projectId}
            "/tasks/project/([0-9]+)"      // Para /api/tasks/project/{projectId} más específico
        };

        String path = request.getRequestURI();
        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                try {
                    return Long.parseLong(matcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        // Si no se encuentra en el path, intentar en query parameters
        String projectIdStr = request.getParameter(paramName);
        if (projectIdStr != null && !projectIdStr.isEmpty()) {
            try {
                return Long.parseLong(projectIdStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    private Long extractTaskId(HttpServletRequest request, String paramName) {
        // Intentar extraer taskId del path usando patrones comunes
        String[] patterns = {
            "/tasks/([0-9]+)",
            "/task/([0-9]+)",
            "/" + paramName + "/([0-9]+)"
        };

        String path = request.getRequestURI();
        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                try {
                    return Long.parseLong(matcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        // Si no se encuentra en el path, intentar en query parameters
        String taskIdStr = request.getParameter(paramName);
        if (taskIdStr != null && !taskIdStr.isEmpty()) {
            try {
                return Long.parseLong(taskIdStr);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    private Long extractFromPath(String path, String paramName) {
        // Patrones comunes para extraer userId del path
        String[] patterns = {
            "/user/([0-9]+)",
            "/users/([0-9]+)", 
            "/pending/([0-9]+)",  // Para /api/connections/pending/{userId}
            "/sent/([0-9]+)",     // Para /api/connections/sent/{userId}
            "/" + paramName + "/([0-9]+)",
            "/([0-9]+)$"  // Para casos donde el ID está al final del path
        };

        for (String patternStr : patterns) {
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(path);
            if (matcher.find()) {
                try {
                    return Long.parseLong(matcher.group(1));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        return null;
    }
}
