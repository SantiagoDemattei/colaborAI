package com.colaborai.colaborai.dto;

import com.colaborai.colaborai.entity.ProjectMember;
import java.time.LocalDateTime;

public class ProjectMemberDTO {
    private Long id;
    private Long projectId;
    private UserDTO user;
    private ProjectMember.ProjectRole role;
    private LocalDateTime joinedAt;

    public ProjectMemberDTO() {}

    public ProjectMemberDTO(Long id, Long projectId, UserDTO user, 
                          ProjectMember.ProjectRole role, LocalDateTime joinedAt) {
        this.id = id;
        this.projectId = projectId;
        this.user = user;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public ProjectMember.ProjectRole getRole() {
        return role;
    }

    public void setRole(ProjectMember.ProjectRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
