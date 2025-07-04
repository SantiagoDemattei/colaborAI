package com.colaborai.colaborai.service.impl;

import com.colaborai.colaborai.dto.ProjectDTO;
import com.colaborai.colaborai.entity.Project;
import com.colaborai.colaborai.entity.ProjectMember;
import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.repository.ProjectRepository;
import com.colaborai.colaborai.repository.ProjectMemberRepository;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.service.ProjectService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository,
                            ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    private ProjectDTO toDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setOwnerId(project.getOwner() != null ? project.getOwner().getId() : null);
        return dto;
    }

    private ProjectDTO toDTO(Project project, Long currentUserId) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setOwnerId(project.getOwner() != null ? project.getOwner().getId() : null);
        
        // Determinar el rol del usuario actual
        if (currentUserId != null) {
            if (project.getOwner() != null && project.getOwner().getId().equals(currentUserId)) {
                dto.setCurrentUserRole(ProjectMember.ProjectRole.OWNER);
            } else {
                // Buscar si el usuario es miembro del proyecto
                User currentUser = userRepository.findById(currentUserId).orElse(null);
                if (currentUser != null) {
                    Optional<ProjectMember> membership = projectMemberRepository
                        .findByProjectAndUser(project, currentUser);
                    if (membership.isPresent()) {
                        dto.setCurrentUserRole(membership.get().getRole());
                    }
                }
            }
        }
        
        return dto;
    }

    private Project toEntity(ProjectDTO dto) {
        Project project = new Project();
        project.setId(dto.getId());
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setCreatedAt(dto.getCreatedAt() != null ? dto.getCreatedAt() : LocalDate.now());
        if (dto.getOwnerId() != null) {
            User owner_user = userRepository.findById(dto.getOwnerId())
                    .orElseThrow(() -> new IllegalArgumentException("Owner not found"));
            project.setOwner(owner_user);
        }
        return project;
    }

    @Override
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        Project project = toEntity(projectDTO);
        Project saved = projectRepository.save(project);
        
        // Crear automáticamente el owner como miembro OWNER del proyecto
        if (saved.getOwner() != null) {
            ProjectMember ownerMember = new ProjectMember(saved, saved.getOwner(), 
                                                        ProjectMember.ProjectRole.OWNER);
            projectMemberRepository.save(ownerMember);
        }
        
        return toDTO(saved);
    }

    @Override
    public List<ProjectDTO> getProjectsByOwner(Long ownerId) {
        List<Project> projects = projectRepository.findByOwnerId(ownerId);
        return projects.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> getAllUserProjects(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        
        // Obtener proyectos como propietario
        List<Project> ownedProjects = projectRepository.findByOwnerId(userId);
        
        // Obtener proyectos como miembro usando el repository de ProjectMember
        List<Project> memberProjects = projectMemberRepository.findProjectsByUser(user);
        
        // Combinar ambas listas evitando duplicados
        Set<Project> allProjects = new HashSet<>();
        allProjects.addAll(ownedProjects);
        allProjects.addAll(memberProjects);
        
        return allProjects.stream().map(project -> toDTO(project, userId)).collect(Collectors.toList());
    }

    @Override
    public ProjectDTO getProjectById(Long id) {
        Optional<Project> project = projectRepository.findById(id);
        return project.map(this::toDTO).orElse(null);
    }

    @Override
    public ProjectDTO updateProject(Long id, ProjectDTO projectDTO) {
        Optional<Project> existing = projectRepository.findById(id);
        if (existing.isPresent()) {
            Project project = existing.get();
            project.setName(projectDTO.getName());
            project.setDescription(projectDTO.getDescription());
            Project updated = projectRepository.save(project);
            return toDTO(updated);
        }
        return null;
    }

    @Override
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }
}