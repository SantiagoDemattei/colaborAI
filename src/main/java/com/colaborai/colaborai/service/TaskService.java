package com.colaborai.colaborai.service;

import com.colaborai.colaborai.dto.TaskDTO;
import java.util.List;

public interface TaskService {
    TaskDTO createTask(TaskDTO taskDTO, Long projectId);
    List<TaskDTO> getTasksByProject(Long projectId);
    TaskDTO getTaskById(Long id);
    TaskDTO updateTask(Long id, TaskDTO taskDTO);
    void deleteTask(Long id);
}