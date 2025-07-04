package com.colaborai.colaborai.service;

import com.colaborai.colaborai.dto.TaskDTO;
import com.colaborai.colaborai.dto.UserDTO;
import java.util.List;

public interface TaskService {
    TaskDTO createTask(TaskDTO taskDTO, Long projectId, Long createdById);
    List<TaskDTO> getTasksByProject(Long projectId);
    TaskDTO getTaskById(Long id);
    TaskDTO updateTask(Long id, TaskDTO taskDTO, Long userId);
    void deleteTask(Long id, Long userId);
    TaskDTO assignTask(Long taskId, Long assigneeId, Long userId);
    List<UserDTO> getAssignableUsers(Long projectId);
    List<TaskDTO> getTasksByAssignee(Long userId);
    boolean canUserAccessTask(Long taskId, Long userId);
    
    // Métodos para dependencias entre tareas
    TaskDTO addTaskDependency(Long taskId, Long dependsOnTaskId, Long userId);
    TaskDTO removeTaskDependency(Long taskId, Long dependsOnTaskId, Long userId);
    List<TaskDTO> getTaskDependencies(Long taskId);
    List<TaskDTO> getTaskDependents(Long taskId);
}