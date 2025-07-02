package com.colaborai.colaborai.repository;

import com.colaborai.colaborai.entity.UserConnection;
import com.colaborai.colaborai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConnectionRepository extends JpaRepository<UserConnection, Long> {
    
    List<UserConnection> findByRequesterAndStatus(User requester, UserConnection.ConnectionStatus status);
    
    List<UserConnection> findByReceiverAndStatus(User receiver, UserConnection.ConnectionStatus status);
    
    @Query("SELECT uc FROM UserConnection uc WHERE " +
           "(uc.requester = :user OR uc.receiver = :user) AND uc.status = :status")
    List<UserConnection> findByUserAndStatus(@Param("user") User user, 
                                           @Param("status") UserConnection.ConnectionStatus status);
    
    @Query("SELECT uc FROM UserConnection uc WHERE " +
           "((uc.requester = :user1 AND uc.receiver = :user2) OR " +
           "(uc.requester = :user2 AND uc.receiver = :user1))")
    Optional<UserConnection> findConnectionBetweenUsers(@Param("user1") User user1, 
                                                      @Param("user2") User user2);
    
    @Query("SELECT uc FROM UserConnection uc WHERE " +
           "(uc.requester = :user OR uc.receiver = :user) AND uc.status = :status")
    List<UserConnection> findConnectionsByUserAndStatus(@Param("user") User user, @Param("status") UserConnection.ConnectionStatus status);
}
