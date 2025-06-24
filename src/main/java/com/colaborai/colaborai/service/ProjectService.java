package com.colaborai.colaborai.service;

import com.colaborai.colaborai.dto.ProjectDTO;
import java.util.List;

public interface ProjectService {
    ProjectDTO createProject(ProjectDTO projectDTO);
    List<ProjectDTO> getProjectsByOwner(Long ownerId);
    ProjectDTO getProjectById(Long id);
    ProjectDTO updateProject(Long id, ProjectDTO projectDTO);
    void deleteProject(Long id);
}