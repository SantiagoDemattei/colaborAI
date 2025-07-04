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

}