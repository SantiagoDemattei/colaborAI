package com.colaborai.colaborai.controller;

import com.colaborai.colaborai.dto.UserConnectionDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.security.annotation.RequireOwnership;
import com.colaborai.colaborai.security.service.SecurityService;
import com.colaborai.colaborai.service.UserConnectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/connections")
public class UserConnectionController {

    private final UserConnectionService userConnectionService;
    private final SecurityService securityService;

    public UserConnectionController(UserConnectionService userConnectionService, SecurityService securityService) {
        this.userConnectionService = userConnectionService;
        this.securityService = securityService;
    }

    @PostMapping("/request")
    public UserConnectionDTO sendConnectionRequest(@RequestBody Map<String, Object> request) {
        // El requesterId debe ser el usuario actual autenticado
        Long requesterId = securityService.getCurrentUserId();
        String usernameOrEmail = (String) request.get("usernameOrEmail");
        return userConnectionService.sendConnectionRequest(requesterId, usernameOrEmail);
    }

    @PutMapping("/{connectionId}/accept")
    @RequireOwnership(userIdParam = "userId", message = "No tienes permisos para aceptar esta conexión")
    public UserConnectionDTO acceptConnectionRequest(@PathVariable Long connectionId,
                                                   @RequestParam Long userId) {
        return userConnectionService.acceptConnectionRequest(connectionId, userId);
    }

    @PutMapping("/{connectionId}/reject")
    @RequireOwnership(userIdParam = "userId", message = "No tienes permisos para rechazar esta conexión")
    public UserConnectionDTO rejectConnectionRequest(@PathVariable Long connectionId,
                                                   @RequestParam Long userId) {
        return userConnectionService.rejectConnectionRequest(connectionId, userId);
    }

    @GetMapping("/pending")
    public List<UserConnectionDTO> getPendingRequests() {
        // Obtener el usuario autenticado
        Long userId = securityService.getCurrentUserId();
        return userConnectionService.getPendingRequests(userId);
    }

    @GetMapping("/sent")
    public List<UserConnectionDTO> getSentRequests() {
        // Obtener el usuario autenticado
        Long userId = securityService.getCurrentUserId();
        return userConnectionService.getSentRequests(userId);
    }

    @GetMapping("/users")
    public List<UserDTO> getConnectedUsers() {
        // Obtener el usuario autenticado
        Long userId = securityService.getCurrentUserId();
        return userConnectionService.getConnectedUsers(userId);
    }

    @GetMapping("/accepted")
    public List<UserConnectionDTO> getAcceptedConnections() {
        // Obtener el usuario autenticado
        Long userId = securityService.getCurrentUserId();
        return userConnectionService.getAcceptedConnections(userId);
    }

    @GetMapping("/check")
    public boolean areUsersConnected(@RequestParam Long user1Id, @RequestParam Long user2Id) {
        // Verificar que el usuario actual sea uno de los dos usuarios
        Long currentUserId = securityService.getCurrentUserId();
        if (!currentUserId.equals(user1Id) && !currentUserId.equals(user2Id)) {
            throw new SecurityException("No tienes permisos para verificar esta conexión");
        }
        return userConnectionService.areUsersConnected(user1Id, user2Id);
    }
}
