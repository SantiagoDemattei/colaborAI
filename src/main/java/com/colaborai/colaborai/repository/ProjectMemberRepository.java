package com.colaborai.colaborai.repository;

import com.colaborai.colaborai.entity.ProjectMember;
import com.colaborai.colaborai.entity.Project;
import com.colaborai.colaborai.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    
    List<ProjectMember> findByProject(Project project);
    
    List<ProjectMember> findByUser(User user);
    
    Optional<ProjectMember> findByProjectAndUser(Project project, User user);
    
    @Query("SELECT pm.user FROM ProjectMember pm WHERE pm.project = :project")
    List<User> findUsersByProject(@Param("project") Project project);
    
    @Query("SELECT pm.project FROM ProjectMember pm WHERE pm.user = :user")
    List<Project> findProjectsByUser(@Param("user") User user);
    
    boolean existsByProjectAndUser(Project project, User user);
}
