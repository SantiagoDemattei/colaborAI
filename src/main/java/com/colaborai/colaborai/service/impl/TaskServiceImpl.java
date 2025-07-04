package com.colaborai.colaborai.service.impl;

import com.colaborai.colaborai.dto.TaskDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.entity.Project;
import com.colaborai.colaborai.entity.Task;
import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.entity.TaskStatus;
import com.colaborai.colaborai.entity.TaskPriority;
import com.colaborai.colaborai.repository.ProjectRepository;
import com.colaborai.colaborai.repository.TaskRepository;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.repository.ProjectMemberRepository;
import com.colaborai.colaborai.service.TaskService;
import com.colaborai.colaborai.service.ProjectMemberService;
import com.colaborai.colaborai.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectMemberService projectMemberService;
    private final NotificationService notificationService;

    public TaskServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository, 
                           UserRepository userRepository, ProjectMemberRepository projectMemberRepository,
                           ProjectMemberService projectMemberService, NotificationService notificationService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectMemberService = projectMemberService;
        this.notificationService = notificationService;
    }

    private UserDTO toUserDTO(User user) {
        if (user == null) return null;
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
    }

    private TaskDTO toDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setAssigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null);
        dto.setProjectId(task.getProject() != null ? task.getProject().getId() : null);
        dto.setProjectName(task.getProject() != null ? task.getProject().getName() : null);
        dto.setCreatedByName(task.getCreatedBy() != null ? task.getCreatedBy().getUsername() : null);
        dto.setAssignee(toUserDTO(task.getAssignee()));
        dto.setCreatedBy(toUserDTO(task.getCreatedBy()));
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }

    private Task toEntity(TaskDTO dto, Project project, User createdBy) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : TaskStatus.PENDING);
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : TaskPriority.MEDIUM);
        task.setDueDate(dto.getDueDate());
        task.setProject(project);
        task.setCreatedBy(createdBy);
        if(dto.getAssigneeId() != null) {
            User assignee = userRepository.findById(dto.getAssigneeId())
                     .orElseThrow(() -> new IllegalArgumentException("Assignee not found"));
            task.setAssignee(assignee);
        }
        return task;
    }

    @Override
    public TaskDTO createTask(TaskDTO taskDTO, Long projectId, Long createdById) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        User createdBy = userRepository.findById(createdById)
            .orElseThrow(() -> new IllegalArgumentException("Usuario creador no encontrado"));

        // Verificar permisos para crear tareas
        if (!projectMemberService.canUserAssignTasks(projectId, createdById)) {
            throw new IllegalArgumentException("No tienes permisos para crear tareas en este proyecto");
        }

        // Si se asigna a alguien, verificar que sea miembro del proyecto
        if (taskDTO.getAssigneeId() != null) {
            if (!projectMemberService.isUserProjectMember(projectId, taskDTO.getAssigneeId())) {
                throw new IllegalArgumentException("Solo puedes asignar tareas a miembros del proyecto");
            }
        }

        Task task = toEntity(taskDTO, project, createdBy);
        Task saved = taskRepository.save(task);

        // Notificar al asignado si es diferente del creador
        if (saved.getAssignee() != null && !saved.getAssignee().getId().equals(createdById)) {
            notificationService.createNotification(
                saved.getAssignee().getId(),
                "Nueva tarea asignada",
                "Te han asignado la tarea: " + saved.getTitle() + " en el proyecto " + project.getName(),
                "TASK_ASSIGNMENT",
                saved.getId()
            );
        }

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
    public TaskDTO updateTask(Long id, TaskDTO taskDTO, Long userId) {
        Optional<Task> existing = taskRepository.findById(id);
        if (existing.isPresent()) {
            Task task = existing.get();

            // Verificar permisos para actualizar la tarea
            if (!projectMemberService.canUserAssignTasks(task.getProject().getId(), userId)) {
                throw new IllegalArgumentException("No tienes permisos para actualizar tareas en este proyecto");
            }

            task.setTitle(taskDTO.getTitle());
            task.setDescription(taskDTO.getDescription());
            task.setStatus(taskDTO.getStatus());
            task.setPriority(taskDTO.getPriority());
            task.setDueDate(taskDTO.getDueDate());
            task.setUpdatedAt(LocalDateTime.now());

            Task updated = taskRepository.save(task);
            return toDTO(updated);
        }
        return null;
    }

    @Override
    public void deleteTask(Long id, Long userId) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));

        // Verificar permisos para eliminar la tarea
        if (!projectMemberService.canUserAssignTasks(task.getProject().getId(), userId)) {
            throw new IllegalArgumentException("No tienes permisos para eliminar tareas en este proyecto");
        }

        taskRepository.deleteById(id);
    }

    @Override
    public TaskDTO assignTask(Long taskId, Long assigneeId, Long userId) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Tarea no encontrada"));

        // Verificar permisos para asignar tareas
        if (!projectMemberService.canUserAssignTasks(task.getProject().getId(), userId)) {
            throw new IllegalArgumentException("No tienes permisos para asignar tareas en este proyecto");
        }

        // Verificar que el asignado es miembro del proyecto
        if (!projectMemberService.isUserProjectMember(task.getProject().getId(), assigneeId)) {
            throw new IllegalArgumentException("Solo puedes asignar tareas a miembros del proyecto");
        }

        User assignee = userRepository.findById(assigneeId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario asignado no encontrado"));

        User oldAssignee = task.getAssignee();
        task.setAssignee(assignee);
        task.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);

        // Notificar al nuevo asignado si es diferente del usuario que asigna
        if (!assignee.getId().equals(userId)) {
            notificationService.createNotification(
                assignee.getId(),
                "Tarea asignada",
                "Te han asignado la tarea: " + task.getTitle(),
                "TASK_ASSIGNMENT",
                task.getId()
            );
        }

        // Notificar al asignado anterior si había uno y es diferente
        if (oldAssignee != null && !oldAssignee.getId().equals(assigneeId) && !oldAssignee.getId().equals(userId)) {
            notificationService.createNotification(
                oldAssignee.getId(),
                "Tarea reasignada",
                "La tarea: " + task.getTitle() + " ha sido reasignada a " + assignee.getUsername(),
                "TASK_ASSIGNMENT",
                task.getId()
            );
        }

        return toDTO(saved);
    }

    @Override
    public List<UserDTO> getAssignableUsers(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        List<User> members = projectMemberRepository.findUsersByProject(project);
        return members.stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> getTasksByAssignee(Long userId) {
        List<Task> tasks = taskRepository.findByAssigneeId(userId);
        return tasks.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public boolean canUserAccessTask(Long taskId, Long userId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            return false;
        }
        
        Task task = taskOpt.get();
        
        // El usuario puede acceder a la tarea si:
        // 1. Es el creador de la tarea
        if (task.getCreatedBy() != null && task.getCreatedBy().getId().equals(userId)) {
            return true;
        }
        
        // 2. Es el asignado de la tarea
        if (task.getAssignee() != null && task.getAssignee().getId().equals(userId)) {
            return true;
        }
        
        // 3. Es miembro del proyecto al que pertenece la tarea
        if (task.getProject() != null) {
            return projectMemberService.isUserProjectMember(task.getProject().getId(), userId);
        }
        
        return false;
    }
}