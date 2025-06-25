package com.colaborai.colaborai.dto;

import com.colaborai.colaborai.entity.UserConnection;
import java.time.LocalDateTime;

public class UserConnectionDTO {
    private Long id;
    private UserDTO requester;
    private UserDTO receiver;
    private UserConnection.ConnectionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime acceptedAt;

    public UserConnectionDTO() {}

    public UserConnectionDTO(Long id, UserDTO requester, UserDTO receiver, 
                           UserConnection.ConnectionStatus status, 
                           LocalDateTime createdAt, LocalDateTime acceptedAt) {
        this.id = id;
        this.requester = requester;
        this.receiver = receiver;
        this.status = status;
        this.createdAt = createdAt;
        this.acceptedAt = acceptedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserDTO getRequester() {
        return requester;
    }

    public void setRequester(UserDTO requester) {
        this.requester = requester;
    }

    public UserDTO getReceiver() {
        return receiver;
    }

    public void setReceiver(UserDTO receiver) {
        this.receiver = receiver;
    }

    public UserConnection.ConnectionStatus getStatus() {
        return status;
    }

    public void setStatus(UserConnection.ConnectionStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
}
