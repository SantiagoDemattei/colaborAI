package com.colaborai.colaborai.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.colaborai.colaborai.entity.TaskStatus;
import com.colaborai.colaborai.entity.TaskPriority;

public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private Long projectId;
    private Long assigneeId;
    private LocalDate dueDate;
    private TaskStatus status;
    private TaskPriority priority;
    private String projectName;
    private String createdByName;
    private UserDTO assignee;
    private UserDTO createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Dependencias de tareas
    private List<Long> dependsOnTaskIds;
    private List<TaskDTO> dependsOnTasks;
    private List<Long> dependentTaskIds;
    private List<TaskDTO> dependentTasks;
    private boolean canBeCompleted;
    
    // Campos para análisis CPM-PERT
    private int estimatedDuration; // Duración estimada en días
    private int earliestStart; // Inicio más temprano (ES)
    private int earliestFinish; // Fin más temprano (EF)
    private int latestStart; // Inicio más tardío (LS)
    private int latestFinish; // Fin más tardío (LF)
    private int totalFloat; // Holgura total
    private boolean isCritical; // Si la tarea está en el camino crítico
    private int criticalPathPosition; // Posición en el camino crítico

    public TaskDTO() {
    }

    public TaskDTO(Long id, String title, String description, Long projectId, Long assigneeId, LocalDate dueDate, TaskStatus status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
        this.dueDate = dueDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public UserDTO getAssignee() {
        return assignee;
    }

    public void setAssignee(UserDTO assignee) {
        this.assignee = assignee;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Long> getDependsOnTaskIds() {
        return dependsOnTaskIds;
    }

    public void setDependsOnTaskIds(List<Long> dependsOnTaskIds) {
        this.dependsOnTaskIds = dependsOnTaskIds;
    }

    public List<TaskDTO> getDependsOnTasks() {
        return dependsOnTasks;
    }

    public void setDependsOnTasks(List<TaskDTO> dependsOnTasks) {
        this.dependsOnTasks = dependsOnTasks;
    }

    public List<Long> getDependentTaskIds() {
        return dependentTaskIds;
    }

    public void setDependentTaskIds(List<Long> dependentTaskIds) {
        this.dependentTaskIds = dependentTaskIds;
    }

    public List<TaskDTO> getDependentTasks() {
        return dependentTasks;
    }

    public void setDependentTasks(List<TaskDTO> dependentTasks) {
        this.dependentTasks = dependentTasks;
    }

    public boolean isCanBeCompleted() {
        return canBeCompleted;
    }

    public void setCanBeCompleted(boolean canBeCompleted) {
        this.canBeCompleted = canBeCompleted;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public int getEarliestStart() {
        return earliestStart;
    }

    public void setEarliestStart(int earliestStart) {
        this.earliestStart = earliestStart;
    }

    public int getEarliestFinish() {
        return earliestFinish;
    }

    public void setEarliestFinish(int earliestFinish) {
        this.earliestFinish = earliestFinish;
    }

    public int getLatestStart() {
        return latestStart;
    }

    public void setLatestStart(int latestStart) {
        this.latestStart = latestStart;
    }

    public int getLatestFinish() {
        return latestFinish;
    }

    public void setLatestFinish(int latestFinish) {
        this.latestFinish = latestFinish;
    }

    public int getTotalFloat() {
        return totalFloat;
    }

    public void setTotalFloat(int totalFloat) {
        this.totalFloat = totalFloat;
    }

    public boolean isCritical() {
        return isCritical;
    }

    public void setCritical(boolean critical) {
        isCritical = critical;
    }

    public int getCriticalPathPosition() {
        return criticalPathPosition;
    }

    public void setCriticalPathPosition(int criticalPathPosition) {
        this.criticalPathPosition = criticalPathPosition;
    }

}