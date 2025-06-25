package com.colaborai.colaborai.controller;

import com.colaborai.colaborai.dto.UserConnectionDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.service.UserConnectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
public class UserConnectionController {

    private final UserConnectionService userConnectionService;

    public UserConnectionController(UserConnectionService userConnectionService) {
        this.userConnectionService = userConnectionService;
    }

    @PostMapping("/request")
    public UserConnectionDTO sendConnectionRequest(@RequestBody Map<String, Object> request) {
        Long requesterId = ((Number) request.get("requesterId")).longValue();
        String usernameOrEmail = (String) request.get("usernameOrEmail");
        return userConnectionService.sendConnectionRequest(requesterId, usernameOrEmail);
    }

    @PutMapping("/{connectionId}/accept")
    public UserConnectionDTO acceptConnectionRequest(@PathVariable Long connectionId,
                                                   @RequestParam Long userId) {
        return userConnectionService.acceptConnectionRequest(connectionId, userId);
    }

    @PutMapping("/{connectionId}/reject")
    public UserConnectionDTO rejectConnectionRequest(@PathVariable Long connectionId,
                                                   @RequestParam Long userId) {
        return userConnectionService.rejectConnectionRequest(connectionId, userId);
    }

    @GetMapping("/pending/{userId}")
    public List<UserConnectionDTO> getPendingRequests(@PathVariable Long userId) {
        return userConnectionService.getPendingRequests(userId);
    }

    @GetMapping("/sent/{userId}")
    public List<UserConnectionDTO> getSentRequests(@PathVariable Long userId) {
        return userConnectionService.getSentRequests(userId);
    }

    @GetMapping("/users/{userId}")
    public List<UserDTO> getConnectedUsers(@PathVariable Long userId) {
        return userConnectionService.getConnectedUsers(userId);
    }

    @GetMapping("/check")
    public boolean areUsersConnected(@RequestParam Long user1Id, @RequestParam Long user2Id) {
        return userConnectionService.areUsersConnected(user1Id, user2Id);
    }
}
