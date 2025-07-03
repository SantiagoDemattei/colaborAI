package com.colaborai.colaborai.service;

import com.colaborai.colaborai.dto.ProjectMemberDTO;
import com.colaborai.colaborai.dto.UserDTO;
import java.util.List;

public interface ProjectMemberService {
    ProjectMemberDTO addMemberToProject(Long projectId, Long userId, Long ownerId);
    ProjectMemberDTO updateMemberRole(Long projectId, Long memberId, String role, Long ownerId);
    void removeMemberFromProject(Long projectId, Long memberId, Long ownerId);
    List<ProjectMemberDTO> getProjectMembers(Long projectId);
    List<UserDTO> getAvailableUsersForProject(Long projectId, Long ownerId);
    boolean canUserModifyProject(Long projectId, Long userId);
    boolean canUserAssignTasks(Long projectId, Long userId);
    boolean isUserProjectMember(Long projectId, Long userId);
    boolean isUserProjectOwner(Long projectId, Long userId);
}
