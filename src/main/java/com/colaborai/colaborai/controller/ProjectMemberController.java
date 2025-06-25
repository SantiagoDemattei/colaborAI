package com.colaborai.colaborai.controller;

import com.colaborai.colaborai.dto.ProjectMemberDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.service.ProjectMemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects/{projectId}/members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @PostMapping
    public ProjectMemberDTO addMember(@PathVariable Long projectId, 
                                    @RequestBody Map<String, Object> request) {
        Long userId = ((Number) request.get("userId")).longValue();
        Long ownerId = ((Number) request.get("ownerId")).longValue();
        return projectMemberService.addMemberToProject(projectId, userId, ownerId);
    }

    @PutMapping("/{memberId}/role")
    public ProjectMemberDTO updateMemberRole(@PathVariable Long projectId,
                                           @PathVariable Long memberId,
                                           @RequestBody Map<String, Object> request) {
        String role = (String) request.get("role");
        Long ownerId = ((Number) request.get("ownerId")).longValue();
        return projectMemberService.updateMemberRole(projectId, memberId, role, ownerId);
    }

    @DeleteMapping("/{memberId}")
    public void removeMember(@PathVariable Long projectId,
                           @PathVariable Long memberId,
                           @RequestParam Long ownerId) {
        projectMemberService.removeMemberFromProject(projectId, memberId, ownerId);
    }

    @GetMapping
    public List<ProjectMemberDTO> getProjectMembers(@PathVariable Long projectId) {
        return projectMemberService.getProjectMembers(projectId);
    }

    @GetMapping("/available")
    public List<UserDTO> getAvailableUsers(@PathVariable Long projectId, 
                                         @RequestParam Long ownerId) {
        return projectMemberService.getAvailableUsersForProject(projectId, ownerId);
    }

    @GetMapping("/can-modify")
    public boolean canUserModifyProject(@PathVariable Long projectId, 
                                      @RequestParam Long userId) {
        return projectMemberService.canUserModifyProject(projectId, userId);
    }

    @GetMapping("/can-assign-tasks")
    public boolean canUserAssignTasks(@PathVariable Long projectId, 
                                    @RequestParam Long userId) {
        return projectMemberService.canUserAssignTasks(projectId, userId);
    }

    @GetMapping("/is-member")
    public boolean isUserProjectMember(@PathVariable Long projectId, 
                                     @RequestParam Long userId) {
        return projectMemberService.isUserProjectMember(projectId, userId);
    }
}
