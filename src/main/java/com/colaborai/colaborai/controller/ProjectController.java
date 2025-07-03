package com.colaborai.colaborai.controller;

import com.colaborai.colaborai.dto.ProjectDTO;
import com.colaborai.colaborai.service.ProjectService;
import com.colaborai.colaborai.security.annotation.RequireProjectOwnership;
import com.colaborai.colaborai.security.annotation.RequireProjectMember;
import com.colaborai.colaborai.security.service.SecurityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final SecurityService securityService;

    public ProjectController(ProjectService projectService, SecurityService securityService) {
        this.projectService = projectService;
        this.securityService = securityService;
    }

    @PostMapping
    public ProjectDTO createProject(@RequestBody ProjectDTO projectDTO) {
        // Obtener el ID del usuario autenticado del SecurityService
        Long ownerId = securityService.getCurrentUserId();
        projectDTO.setOwnerId(ownerId);
        
        return projectService.createProject(projectDTO);
    }

    @GetMapping("/owner")
    public List<ProjectDTO> getProjectsByOwner() {
        // Obtener el ID del usuario autenticado del SecurityService
        Long ownerId = securityService.getCurrentUserId();
        
        return projectService.getProjectsByOwner(ownerId);
    }

    @GetMapping("/user")
    public List<ProjectDTO> getAllUserProjects() {
        // Obtener el ID del usuario autenticado del SecurityService
        Long userId = securityService.getCurrentUserId();
        
        return projectService.getAllUserProjects(userId);
    }

    @GetMapping("/{id}")
    @RequireProjectMember
    public ProjectDTO getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id);
    }

    @PutMapping("/{id}")
    @RequireProjectOwnership
    public ProjectDTO updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        return projectService.updateProject(id, projectDTO);
    }

    @DeleteMapping("/{id}")
    @RequireProjectOwnership
    public void deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
    }
}