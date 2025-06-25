package com.colaborai.colaborai.service.impl;

import com.colaborai.colaborai.dto.UserConnectionDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.entity.UserConnection;
import com.colaborai.colaborai.repository.UserConnectionRepository;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.service.UserConnectionService;
import com.colaborai.colaborai.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserConnectionServiceImpl implements UserConnectionService {

    private final UserConnectionRepository userConnectionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public UserConnectionServiceImpl(UserConnectionRepository userConnectionRepository,
                                   UserRepository userRepository,
                                   NotificationService notificationService) {
        this.userConnectionRepository = userConnectionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    private UserDTO toUserDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
    }

    private UserConnectionDTO toDTO(UserConnection connection) {
        return new UserConnectionDTO(
            connection.getId(),
            toUserDTO(connection.getRequester()),
            toUserDTO(connection.getReceiver()),
            connection.getStatus(),
            connection.getCreatedAt(),
            connection.getAcceptedAt()
        );
    }

    @Override
    public UserConnectionDTO sendConnectionRequest(Long requesterId, String usernameOrEmail) {
        User requester = userRepository.findById(requesterId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario solicitante no encontrado"));

        User receiver = userRepository.findByUsername(usernameOrEmail)
            .orElse(userRepository.findByEmail(usernameOrEmail)
                .orElseThrow(() -> new IllegalArgumentException("Usuario receptor no encontrado")));

        if (requester.getId().equals(receiver.getId())) {
            throw new IllegalArgumentException("No puedes enviarte una solicitud a ti mismo");
        }

        // Verificar si ya existe una conexión
        Optional<UserConnection> existingConnection = userConnectionRepository
            .findConnectionBetweenUsers(requester, receiver);

        if (existingConnection.isPresent()) {
            throw new IllegalArgumentException("Ya existe una conexión entre estos usuarios");
        }

        UserConnection connection = new UserConnection(requester, receiver);
        UserConnection saved = userConnectionRepository.save(connection);

        // Crear notificación
        notificationService.createNotification(
            receiver.getId(),
            "Nueva solicitud de conexión",
            requester.getUsername() + " te ha enviado una solicitud de conexión",
            "CONNECTION_REQUEST",
            saved.getId()
        );

        return toDTO(saved);
    }

    @Override
    public UserConnectionDTO acceptConnectionRequest(Long connectionId, Long userId) {
        UserConnection connection = userConnectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud de conexión no encontrada"));

        if (!connection.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permisos para aceptar esta solicitud");
        }

        if (connection.getStatus() != UserConnection.ConnectionStatus.PENDING) {
            throw new IllegalArgumentException("Esta solicitud ya ha sido procesada");
        }

        connection.setStatus(UserConnection.ConnectionStatus.ACCEPTED);
        connection.setAcceptedAt(LocalDateTime.now());
        UserConnection saved = userConnectionRepository.save(connection);

        // Notificar al solicitante
        notificationService.createNotification(
            connection.getRequester().getId(),
            "Solicitud de conexión aceptada",
            connection.getReceiver().getUsername() + " ha aceptado tu solicitud de conexión",
            "CONNECTION_REQUEST",
            saved.getId()
        );

        return toDTO(saved);
    }

    @Override
    public UserConnectionDTO rejectConnectionRequest(Long connectionId, Long userId) {
        UserConnection connection = userConnectionRepository.findById(connectionId)
            .orElseThrow(() -> new IllegalArgumentException("Solicitud de conexión no encontrada"));

        if (!connection.getReceiver().getId().equals(userId)) {
            throw new IllegalArgumentException("No tienes permisos para rechazar esta solicitud");
        }

        if (connection.getStatus() != UserConnection.ConnectionStatus.PENDING) {
            throw new IllegalArgumentException("Esta solicitud ya ha sido procesada");
        }

        connection.setStatus(UserConnection.ConnectionStatus.REJECTED);
        UserConnection saved = userConnectionRepository.save(connection);

        return toDTO(saved);
    }

    @Override
    public List<UserConnectionDTO> getPendingRequests(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<UserConnection> connections = userConnectionRepository
            .findByReceiverAndStatus(user, UserConnection.ConnectionStatus.PENDING);

        return connections.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<UserConnectionDTO> getSentRequests(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<UserConnection> connections = userConnectionRepository
            .findByRequesterAndStatus(user, UserConnection.ConnectionStatus.PENDING);

        return connections.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getConnectedUsers(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        List<User> connectedUsers = userConnectionRepository.findConnectedUsers(user);
        return connectedUsers.stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    @Override
    public boolean areUsersConnected(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario 1 no encontrado"));
        User user2 = userRepository.findById(user2Id)
            .orElseThrow(() -> new IllegalArgumentException("Usuario 2 no encontrado"));

        Optional<UserConnection> connection = userConnectionRepository
            .findConnectionBetweenUsers(user1, user2);

        return connection.isPresent() && 
               connection.get().getStatus() == UserConnection.ConnectionStatus.ACCEPTED;
    }
}
