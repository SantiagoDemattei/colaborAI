package com.colaborai.colaborai.service.impl;

import com.colaborai.colaborai.dto.TaskDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.entity.Project;
import com.colaborai.colaborai.entity.Task;
import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.entity.TaskStatus;
import com.colaborai.colaborai.repository.ProjectRepository;
import com.colaborai.colaborai.repository.TaskRepository;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.repository.ProjectMemberRepository;
import com.colaborai.colaborai.service.TaskService;
import com.colaborai.colaborai.service.ProjectMemberService;
import com.colaborai.colaborai.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
        
        // Añadir información de dependencias
        dto.setDependsOnTaskIds(task.getDependsOn().stream()
                .map(Task::getId)
                .collect(Collectors.toList()));
        dto.setDependentTaskIds(task.getDependents().stream()
                .map(Task::getId)
                .collect(Collectors.toList()));
        dto.setCanBeCompleted(task.canBeCompleted());
        dto.setEstimatedDuration(task.getEstimatedDuration() != null ? task.getEstimatedDuration() : 1);
        
        return dto;
    }

    private Task toEntity(TaskDTO dto, Project project, User createdBy) {
        Task task = new Task();
        task.setId(dto.getId());
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : TaskStatus.PENDING);
        task.setPriority(dto.getPriority());
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

    @Override
    public TaskDTO addTaskDependency(Long taskId, Long dependsOnTaskId, Long userId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        Optional<Task> dependsOnTaskOpt = taskRepository.findById(dependsOnTaskId);
        
        if (!taskOpt.isPresent() || !dependsOnTaskOpt.isPresent()) {
            throw new IllegalArgumentException("Una o ambas tareas no existen");
        }
        
        Task task = taskOpt.get();
        Task dependsOnTask = dependsOnTaskOpt.get();
        
        // Verificar que el usuario tenga acceso a ambas tareas
        if (!canUserAccessTask(taskId, userId) || !canUserAccessTask(dependsOnTaskId, userId)) {
            throw new SecurityException("No tienes permisos para modificar estas tareas");
        }
        
        // Verificar que ambas tareas pertenezcan al mismo proyecto
        if (!task.getProject().getId().equals(dependsOnTask.getProject().getId())) {
            throw new IllegalArgumentException("No se pueden crear dependencias entre tareas de diferentes proyectos");
        }
        
        // Verificar que no se cree una dependencia circular
        if (wouldCreateCircularDependency(task, dependsOnTask)) {
            throw new IllegalArgumentException("Esta dependencia crearía una referencia circular");
        }
        
        task.addDependency(dependsOnTask);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        
        return toDTO(task);
    }

    @Override
    public TaskDTO removeTaskDependency(Long taskId, Long dependsOnTaskId, Long userId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        Optional<Task> dependsOnTaskOpt = taskRepository.findById(dependsOnTaskId);
        
        if (!taskOpt.isPresent() || !dependsOnTaskOpt.isPresent()) {
            throw new IllegalArgumentException("Una o ambas tareas no existen");
        }
        
        Task task = taskOpt.get();
        Task dependsOnTask = dependsOnTaskOpt.get();
        
        // Verificar que el usuario tenga acceso a la tarea
        if (!canUserAccessTask(taskId, userId)) {
            throw new SecurityException("No tienes permisos para modificar esta tarea");
        }
        
        task.removeDependency(dependsOnTask);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        
        return toDTO(task);
    }

    @Override
    public List<TaskDTO> getTaskDependencies(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new IllegalArgumentException("La tarea no existe");
        }
        
        Task task = taskOpt.get();
        return task.getDependsOn().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskDTO> getTaskDependents(Long taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (!taskOpt.isPresent()) {
            throw new IllegalArgumentException("La tarea no existe");
        }
        
        Task task = taskOpt.get();
        return task.getDependents().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Método auxiliar para detectar dependencias circulares
    private boolean wouldCreateCircularDependency(Task task, Task dependsOnTask) {
        return hasPath(dependsOnTask, task);
    }

    private boolean hasPath(Task from, Task to) {
        if (from.equals(to)) {
            return true;
        }
        
        for (Task dependency : from.getDependsOn()) {
            if (hasPath(dependency, to)) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    public List<TaskDTO> getCriticalPath(Long projectId) {
        List<Task> projectTasks = taskRepository.findByProjectId(projectId);
        if (projectTasks.isEmpty()) {
            return new ArrayList<>();
        }

        // Calcular CPM
        Map<Long, TaskDTO> taskMap = calculateCPM(projectTasks);
        
        // Encontrar el camino crítico
        return findCriticalPath(taskMap, projectTasks);
    }

    @Override
    public List<TaskDTO> getCriticalTasks(Long projectId) {
        List<Task> projectTasks = taskRepository.findByProjectId(projectId);
        if (projectTasks.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, TaskDTO> taskMap = calculateCPM(projectTasks);
        
        return taskMap.values().stream()
                .filter(TaskDTO::isCritical)
                .collect(Collectors.toList());
    }

    // Método auxiliar para calcular CPM
    private Map<Long, TaskDTO> calculateCPM(List<Task> tasks) {
        Map<Long, TaskDTO> taskMap = new HashMap<>();
        
        // Convertir tareas a DTOs con cálculos CPM
        for (Task task : tasks) {
            TaskDTO dto = toDTO(task);
            taskMap.put(task.getId(), dto);
        }

        // Forward pass - calcular ES y EF
        calculateForwardPass(taskMap, tasks);
        
        // Backward pass - calcular LS y LF
        calculateBackwardPass(taskMap, tasks);
        
        // Calcular holgura y marcar tareas críticas
        calculateFloatAndCritical(taskMap);
        
        return taskMap;
    }

    private void calculateForwardPass(Map<Long, TaskDTO> taskMap, List<Task> tasks) {
        // Ordenamiento topológico para procesar tareas en orden correcto
        List<Task> sortedTasks = topologicalSort(tasks);
        
        for (Task task : sortedTasks) {
            TaskDTO dto = taskMap.get(task.getId());
            int maxPredecessorEF = 0;
            
            // Encontrar el mayor EF de las tareas predecesoras
            for (Task dependency : task.getDependsOn()) {
                TaskDTO depDto = taskMap.get(dependency.getId());
                if (depDto != null) {
                    maxPredecessorEF = Math.max(maxPredecessorEF, depDto.getEarliestFinish());
                }
            }
            
            dto.setEarliestStart(maxPredecessorEF);
            dto.setEarliestFinish(maxPredecessorEF + dto.getEstimatedDuration());
        }
    }

    private void calculateBackwardPass(Map<Long, TaskDTO> taskMap, List<Task> tasks) {
        // Encontrar tareas finales (sin sucesores)
        List<Task> finalTasks = tasks.stream()
                .filter(task -> task.getDependents().isEmpty())
                .collect(Collectors.toList());
        
        // Encontrar el mayor EF del proyecto
        int projectFinish = taskMap.values().stream()
                .mapToInt(TaskDTO::getEarliestFinish)
                .max()
                .orElse(0);
        
        // Inicializar LF de tareas finales
        for (Task task : finalTasks) {
            TaskDTO dto = taskMap.get(task.getId());
            dto.setLatestFinish(projectFinish);
            dto.setLatestStart(projectFinish - dto.getEstimatedDuration());
        }
        
        // Procesar en orden inverso
        List<Task> reverseSortedTasks = topologicalSort(tasks);
        Collections.reverse(reverseSortedTasks);
        
        for (Task task : reverseSortedTasks) {
            TaskDTO dto = taskMap.get(task.getId());
            if (dto.getLatestFinish() == 0) { // Si no se ha calculado aún
                int minSuccessorLS = Integer.MAX_VALUE;
                
                for (Task dependent : task.getDependents()) {
                    TaskDTO depDto = taskMap.get(dependent.getId());
                    if (depDto != null) {
                        minSuccessorLS = Math.min(minSuccessorLS, depDto.getLatestStart());
                    }
                }
                
                if (minSuccessorLS != Integer.MAX_VALUE) {
                    dto.setLatestFinish(minSuccessorLS);
                    dto.setLatestStart(minSuccessorLS - dto.getEstimatedDuration());
                }
            }
        }
    }

    private void calculateFloatAndCritical(Map<Long, TaskDTO> taskMap) {
        for (TaskDTO dto : taskMap.values()) {
            // Calcular holgura total
            dto.setTotalFloat(dto.getLatestStart() - dto.getEarliestStart());
            
            // Una tarea es crítica si su holgura es 0
            dto.setCritical(dto.getTotalFloat() == 0);
        }
    }

    private List<TaskDTO> findCriticalPath(Map<Long, TaskDTO> taskMap, List<Task> tasks) {
        List<TaskDTO> criticalPath = new ArrayList<>();
        
        // Encontrar tareas críticas
        List<TaskDTO> criticalTasks = taskMap.values().stream()
                .filter(TaskDTO::isCritical)
                .sorted((a, b) -> Integer.compare(a.getEarliestStart(), b.getEarliestStart()))
                .collect(Collectors.toList());
        
        // Establecer posiciones en el camino crítico
        for (int i = 0; i < criticalTasks.size(); i++) {
            criticalTasks.get(i).setCriticalPathPosition(i + 1);
        }
        
        return criticalTasks;
    }

    private List<Task> topologicalSort(List<Task> tasks) {
        Map<Long, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, task -> task));
        
        Map<Long, Integer> inDegree = new HashMap<>();
        
        // Calcular grado de entrada para cada tarea
        for (Task task : tasks) {
            inDegree.put(task.getId(), task.getDependsOn().size());
        }
        
        Queue<Task> queue = new LinkedList<>();
        List<Task> result = new ArrayList<>();
        
        // Agregar tareas sin dependencias a la cola
        for (Task task : tasks) {
            if (inDegree.get(task.getId()) == 0) {
                queue.offer(task);
            }
        }
        
        // Procesar tareas
        while (!queue.isEmpty()) {
            Task current = queue.poll();
            result.add(current);
            
            // Reducir grado de entrada de tareas dependientes
            for (Task dependent : current.getDependents()) {
                int newInDegree = inDegree.get(dependent.getId()) - 1;
                inDegree.put(dependent.getId(), newInDegree);
                
                if (newInDegree == 0) {
                    queue.offer(dependent);
                }
            }
        }
        
        return result;
    }
}