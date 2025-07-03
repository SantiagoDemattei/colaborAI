package com.colaborai.colaborai.controller;

import com.colaborai.colaborai.dto.ProjectMemberDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.security.annotation.RequireProjectAdmin;
import com.colaborai.colaborai.security.annotation.RequireProjectMember;
import com.colaborai.colaborai.security.service.SecurityService;
import com.colaborai.colaborai.service.ProjectMemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final SecurityService securityService;

    public ProjectMemberController(ProjectMemberService projectMemberService, SecurityService securityService) {
        this.projectMemberService = projectMemberService;
        this.securityService = securityService;
    }

    @RequireProjectAdmin(message = "Solo los administradores pueden agregar miembros")
    @PostMapping
    public ProjectMemberDTO addMember(@PathVariable Long projectId, 
                                    @RequestBody Map<String, Object> request) {
        Long userId = ((Number) request.get("userId")).longValue();
        Long ownerId = securityService.getCurrentUserId(); // Usar usuario actual
        return projectMemberService.addMemberToProject(projectId, userId, ownerId);
    }

    @RequireProjectAdmin(message = "Solo los administradores pueden cambiar roles")
    @PutMapping("/{memberId}/role")
    public ProjectMemberDTO updateMemberRole(@PathVariable Long projectId,
                                           @PathVariable Long memberId,
                                           @RequestBody Map<String, Object> request) {
        String role = (String) request.get("role");
        Long ownerId = securityService.getCurrentUserId(); // Usar usuario actual
        return projectMemberService.updateMemberRole(projectId, memberId, role, ownerId);
    }

    @RequireProjectAdmin(message = "Solo los administradores pueden remover miembros")
    @DeleteMapping("/{memberId}")
    public void removeMember(@PathVariable Long projectId,
                           @PathVariable Long memberId) {
        Long ownerId = securityService.getCurrentUserId(); // Usar usuario actual
        projectMemberService.removeMemberFromProject(projectId, memberId, ownerId);
    }

    @RequireProjectMember(message = "Solo los miembros pueden ver la lista de miembros")
    @GetMapping
    public List<ProjectMemberDTO> getProjectMembers(@PathVariable Long projectId) {
        return projectMemberService.getProjectMembers(projectId);
    }

    @RequireProjectAdmin(message = "Solo los administradores pueden ver usuarios disponibles")
    @GetMapping("/available")
    public List<UserDTO> getAvailableUsers(@PathVariable Long projectId) {
        Long ownerId = securityService.getCurrentUserId(); // Usar usuario actual
        return projectMemberService.getAvailableUsersForProject(projectId, ownerId);
    }

    @RequireProjectMember(message = "Solo los miembros pueden verificar permisos de modificación")
    @GetMapping("/can-modify")
    public boolean canUserModifyProject(@PathVariable Long projectId, 
                                      @RequestParam Long userId) {
        // Solo permitir verificar permisos del usuario actual o si es admin del proyecto
        Long currentUserId = securityService.getCurrentUserId();
        if (!currentUserId.equals(userId) && !securityService.isCurrentUserProjectAdmin(projectId)) {
            throw new SecurityException("No tienes permisos para verificar los permisos de este usuario");
        }
        return projectMemberService.canUserModifyProject(projectId, userId);
    }

    @RequireProjectMember(message = "Solo los miembros pueden verificar permisos de asignación")
    @GetMapping("/can-assign-tasks")
    public boolean canUserAssignTasks(@PathVariable Long projectId, 
                                    @RequestParam Long userId) {
        // Solo permitir verificar permisos del usuario actual o si es admin del proyecto
        Long currentUserId = securityService.getCurrentUserId();
        if (!currentUserId.equals(userId) && !securityService.isCurrentUserProjectAdmin(projectId)) {
            throw new SecurityException("No tienes permisos para verificar los permisos de este usuario");
        }
        return projectMemberService.canUserAssignTasks(projectId, userId);
    }

    @RequireProjectMember(message = "Solo los miembros pueden verificar membresía")
    @GetMapping("/is-member")
    public boolean isUserProjectMember(@PathVariable Long projectId, 
                                     @RequestParam Long userId) {
        // Solo permitir verificar membresía del usuario actual o si es admin del proyecto
        Long currentUserId = securityService.getCurrentUserId();
        if (!currentUserId.equals(userId) && !securityService.isCurrentUserProjectAdmin(projectId)) {
            throw new SecurityException("No tienes permisos para verificar la membresía de este usuario");
        }
        return projectMemberService.isUserProjectMember(projectId, userId);
    }
}
