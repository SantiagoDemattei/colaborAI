package com.colaborai.colaborai.controller;

import com.colaborai.colaborai.dto.TaskDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/project/{projectId}")
    public TaskDTO createTask(@RequestBody Map<String, Object> request, @PathVariable Long projectId) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle((String) request.get("title"));
        taskDTO.setDescription((String) request.get("description"));
        taskDTO.setDueDate(request.get("dueDate") != null ? 
            java.time.LocalDate.parse((String) request.get("dueDate")) : null);
        if (request.get("assigneeId") != null) {
            taskDTO.setAssigneeId(((Number) request.get("assigneeId")).longValue());
        }
        Long createdById = ((Number) request.get("createdById")).longValue();
        return taskService.createTask(taskDTO, projectId, createdById);
    }

    @GetMapping("/project/{projectId}")
    public List<TaskDTO> getTasksByProject(@PathVariable Long projectId) {
        return taskService.getTasksByProject(projectId);
    }

    @GetMapping("/{id}")
    public TaskDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}")
    public TaskDTO updateTask(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle((String) request.get("title"));
        taskDTO.setDescription((String) request.get("description"));
        taskDTO.setDueDate(request.get("dueDate") != null ? 
            java.time.LocalDate.parse((String) request.get("dueDate")) : null);
        if (request.get("status") != null) {
            taskDTO.setStatus(com.colaborai.colaborai.entity.TaskStatus.valueOf((String) request.get("status")));
        }
        Long userId = ((Number) request.get("userId")).longValue();
        return taskService.updateTask(id, taskDTO, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id, @RequestParam Long userId) {
        taskService.deleteTask(id, userId);
    }

    @PutMapping("/{taskId}/assign")
    public TaskDTO assignTask(@PathVariable Long taskId, @RequestBody Map<String, Object> request) {
        Long assigneeId = ((Number) request.get("assigneeId")).longValue();
        Long userId = ((Number) request.get("userId")).longValue();
        return taskService.assignTask(taskId, assigneeId, userId);
    }

    @GetMapping("/project/{projectId}/assignable-users")
    public List<UserDTO> getAssignableUsers(@PathVariable Long projectId) {
        return taskService.getAssignableUsers(projectId);
    }
}