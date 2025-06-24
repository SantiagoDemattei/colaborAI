package com.colaborai.colaborai.service.impl;

import com.colaborai.colaborai.dto.TaskDTO;
import com.colaborai.colaborai.entity.Project;
import com.colaborai.colaborai.entity.Task;
import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.entity.TaskStatus;
import com.colaborai.colaborai.repository.ProjectRepository;
import com.colaborai.colaborai.repository.TaskRepository;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.service.TaskService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository, 
                           UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    private TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate());
        dto.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        dto.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        return dto;
    }

    private Task toEntity(TaskDTO dto, Project project) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : TaskStatus.PENDING);
        task.setDueDate(dto.getDueDate());
        task.setProject(project);
        if(dto.getAssigneeId() != null) {
            User assignee = userRepository.findById(dto.getAssigneeId())
                     .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
            task.setAssignee(assignee);
        }
        return task;
    }

    @Override
    public TaskDTO createTask(TaskDTO taskDTO, Long projectId) {
        Project project = projectRepository.findById(projectId).orElseThrow();
        Task task = toEntity(taskDTO, project);
        Task saved = taskRepository.save(task);
        return toDTO(saved);
    }

    @Override
    public List<TaskDTO> getTasksByProject(Long projectId) {
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public TaskDTO getTaskById(Long id) {
        Optional<Task> task = taskRepository.findById(id);
        return task.map(this::toDTO).orElse(null);
    }

    @Override
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) {
        Optional<Task> existing = taskRepository.findById(id);
        if (existing.isPresent()) {
            Task task = existing.get();
            task.setTitle(taskDTO.getTitle());
            task.setDescription(taskDTO.getDescription());
            task.setStatus(taskDTO.getStatus());
            task.setDueDate(taskDTO.getDueDate());
            Task updated = taskRepository.save(task);
            return toDTO(updated);
        }
        return null;
    }

    @Override
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}