package com.colaborai.colaborai.controller;

import com.colaborai.colaborai.dto.TaskDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.service.TaskService;
import com.colaborai.colaborai.security.annotation.RequireProjectMember;
import com.colaborai.colaborai.security.annotation.RequireTaskAccess;
import com.colaborai.colaborai.security.service.SecurityService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final SecurityService securityService;

    public TaskController(TaskService taskService, SecurityService securityService) {
        this.taskService = taskService;
        this.securityService = securityService;
    }

    @PostMapping("/project/{projectId}")
    @RequireProjectMember
    public TaskDTO createTask(@RequestBody Map<String, Object> request, @PathVariable Long projectId) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle((String) request.get("title"));
        taskDTO.setDescription((String) request.get("description"));
        taskDTO.setDueDate(request.get("dueDate") != null ? 
            java.time.LocalDate.parse((String) request.get("dueDate")) : null);
        if (request.get("priority") != null) {
            taskDTO.setPriority(com.colaborai.colaborai.entity.TaskPriority.valueOf((String) request.get("priority")));
        }
        if (request.get("assigneeId") != null) {
            Object assigneeIdObj = request.get("assigneeId");
            if (assigneeIdObj instanceof Number) {
                taskDTO.setAssigneeId(((Number) assigneeIdObj).longValue());
            } else if (assigneeIdObj instanceof String) {
                taskDTO.setAssigneeId(Long.parseLong((String) assigneeIdObj));
            }
        }
        
        // Obtener el ID del usuario autenticado del SecurityService
        Long createdById = securityService.getCurrentUserId();
        
        return taskService.createTask(taskDTO, projectId, createdById);
    }

    @GetMapping("/project/{projectId}")
    @RequireProjectMember
    public List<TaskDTO> getTasksByProject(@PathVariable Long projectId) {
        return taskService.getTasksByProject(projectId);
    }

    @GetMapping("/{id}")
    @RequireTaskAccess
    public TaskDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}")
    @RequireTaskAccess
    public TaskDTO updateTask(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle((String) request.get("title"));
        taskDTO.setDescription((String) request.get("description"));
        taskDTO.setDueDate(request.get("dueDate") != null ? 
            java.time.LocalDate.parse((String) request.get("dueDate")) : null);
        if (request.get("status") != null) {
            taskDTO.setStatus(com.colaborai.colaborai.entity.TaskStatus.valueOf((String) request.get("status")));
        }
        if (request.get("priority") != null) {
            taskDTO.setPriority(com.colaborai.colaborai.entity.TaskPriority.valueOf((String) request.get("priority")));
        }
        
        // Obtener el ID del usuario autenticado del SecurityService
        Long userId = securityService.getCurrentUserId();
        
        return taskService.updateTask(id, taskDTO, userId);
    }

    @DeleteMapping("/{id}")
    @RequireTaskAccess
    public void deleteTask(@PathVariable Long id) {
        // Obtener el ID del usuario autenticado del SecurityService
        Long userId = securityService.getCurrentUserId();
        
        taskService.deleteTask(id, userId);
    }

    @PutMapping("/{taskId}/assign")
    @RequireTaskAccess
    public TaskDTO assignTask(@PathVariable Long taskId, @RequestBody Map<String, Object> request) {
        Long assigneeId = ((Number) request.get("assigneeId")).longValue();
        
        // Obtener el ID del usuario autenticado del SecurityService
        Long userId = securityService.getCurrentUserId();
        
        return taskService.assignTask(taskId, assigneeId, userId);
    }

    @GetMapping("/project/{projectId}/assignable-users")
    @RequireProjectMember
    public List<UserDTO> getAssignableUsers(@PathVariable Long projectId) {
        return taskService.getAssignableUsers(projectId);
    }

    @GetMapping("/my-tasks")
    public List<TaskDTO> getMyAssignedTasks() {
        Long userId = securityService.getCurrentUserId();
        return taskService.getTasksByAssignee(userId);
    }

    @GetMapping("/priorities")
    public com.colaborai.colaborai.entity.TaskPriority[] getTaskPriorities() {
        return com.colaborai.colaborai.entity.TaskPriority.values();
    }
}