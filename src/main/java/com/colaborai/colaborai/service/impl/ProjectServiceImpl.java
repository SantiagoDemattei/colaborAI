package com.colaborai.colaborai.service.impl;

import com.colaborai.colaborai.dto.ProjectDTO;
import com.colaborai.colaborai.entity.Project;
import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.repository.ProjectRepository;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.service.ProjectService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
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
        return toDTO(saved);
    }

    @Override
    public List<ProjectDTO> getProjectsByOwner(Long ownerId) {
        List<Project> projects = projectRepository.findByOwnerId(ownerId);
        return projects.stream().map(this::toDTO).collect(Collectors.toList());
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