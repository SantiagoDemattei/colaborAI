package com.colaborai.colaborai.service.impl;

import com.colaborai.colaborai.dto.ProjectMemberDTO;
import com.colaborai.colaborai.dto.UserDTO;
import com.colaborai.colaborai.entity.Project;
import com.colaborai.colaborai.entity.ProjectMember;
import com.colaborai.colaborai.entity.User;
import com.colaborai.colaborai.repository.ProjectMemberRepository;
import com.colaborai.colaborai.repository.ProjectRepository;
import com.colaborai.colaborai.repository.UserRepository;
import com.colaborai.colaborai.repository.UserConnectionRepository;
import com.colaborai.colaborai.service.ProjectMemberService;
import com.colaborai.colaborai.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final UserConnectionRepository userConnectionRepository;
    private final NotificationService notificationService;

    public ProjectMemberServiceImpl(ProjectMemberRepository projectMemberRepository,
                                  ProjectRepository projectRepository,
                                  UserRepository userRepository,
                                  UserConnectionRepository userConnectionRepository,
                                  NotificationService notificationService) {
        this.projectMemberRepository = projectMemberRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.userConnectionRepository = userConnectionRepository;
        this.notificationService = notificationService;
    }

    private UserDTO toUserDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
    }

    private ProjectMemberDTO toDTO(ProjectMember member) {
        return new ProjectMemberDTO(
            member.getId(),
            member.getProject().getId(),
            toUserDTO(member.getUser()),
            member.getRole(),
            member.getJoinedAt()
        );
    }

    @Override
    public ProjectMemberDTO addMemberToProject(Long projectId, Long userId, Long ownerId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar que el owner puede agregar miembros
        if (!canUserModifyProject(projectId, ownerId)) {
            throw new IllegalArgumentException("No tienes permisos para agregar miembros a este proyecto");
        }

        // Verificar que el usuario no es ya miembro
        if (projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalArgumentException("El usuario ya es miembro de este proyecto");
        }

        // Verificar que están conectados o es el mismo usuario
        if (!ownerId.equals(userId) && !areUsersConnected(ownerId, userId)) {
            throw new IllegalArgumentException("Solo puedes agregar usuarios con los que estás conectado");
        }

        ProjectMember member = new ProjectMember(project, user, ProjectMember.ProjectRole.MEMBER);
        ProjectMember saved = projectMemberRepository.save(member);

        // Crear notificación
        notificationService.createNotification(
            userId,
            "Agregado a proyecto",
            "Has sido agregado al proyecto: " + project.getName(),
            "PROJECT_INVITATION",
            projectId
        );

        return toDTO(saved);
    }

    @Override
    public ProjectMemberDTO updateMemberRole(Long projectId, Long memberId, String role, Long ownerId) {
        ProjectMember member = projectMemberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));

        if (!member.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("El miembro no pertenece a este proyecto");
        }

        // Solo el owner puede cambiar roles
        if (!member.getProject().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Solo el propietario puede cambiar roles");
        }

        // No se puede cambiar el rol del owner
        if (member.getRole() == ProjectMember.ProjectRole.OWNER) {
            throw new IllegalArgumentException("No se puede cambiar el rol del propietario");
        }

        member.setRole(ProjectMember.ProjectRole.valueOf(role));
        ProjectMember saved = projectMemberRepository.save(member);

        return toDTO(saved);
    }

    @Override
    public void removeMemberFromProject(Long projectId, Long memberId, Long ownerId) {
        ProjectMember member = projectMemberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Miembro no encontrado"));

        if (!member.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("El miembro no pertenece a este proyecto");
        }

        // Solo el owner puede remover miembros
        if (!member.getProject().getOwner().getId().equals(ownerId)) {
            throw new IllegalArgumentException("Solo el propietario puede remover miembros");
        }

        // No se puede remover al owner
        if (member.getRole() == ProjectMember.ProjectRole.OWNER) {
            throw new IllegalArgumentException("No se puede remover al propietario del proyecto");
        }

        projectMemberRepository.delete(member);
    }

    @Override
    public List<ProjectMemberDTO> getProjectMembers(Long projectId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        List<ProjectMember> members = projectMemberRepository.findByProject(project);
        return members.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> getAvailableUsersForProject(Long projectId, Long ownerId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        User owner = userRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Obtener usuarios conectados
        List<User> connectedUsers = userConnectionRepository.findConnectedUsers(owner);
        
        // Obtener usuarios que ya son miembros del proyecto
        List<User> currentMembers = projectMemberRepository.findUsersByProject(project);
        
        // Filtrar usuarios conectados que no son miembros
        return connectedUsers.stream()
            .filter(user -> !currentMembers.contains(user))
            .map(this::toUserDTO)
            .collect(Collectors.toList());
    }

    @Override
    public boolean canUserModifyProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        // El owner siempre puede modificar
        if (project.getOwner().getId().equals(userId)) {
            return true;
        }

        // Verificar si es admin del proyecto
        Optional<ProjectMember> membership = projectMemberRepository
            .findByProjectAndUser(project, userRepository.findById(userId).orElse(null));

        return membership.isPresent() && 
               membership.get().getRole() == ProjectMember.ProjectRole.ADMIN;
    }

    @Override
    public boolean canUserAssignTasks(Long projectId, Long userId) {
        return canUserModifyProject(projectId, userId);
    }

    @Override
    public boolean isUserProjectMember(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // El owner siempre es miembro
        if (project.getOwner().getId().equals(userId)) {
            return true;
        }

        return projectMemberRepository.existsByProjectAndUser(project, user);
    }

    private boolean areUsersConnected(Long user1Id, Long user2Id) {
        User user1 = userRepository.findById(user1Id).orElse(null);
        User user2 = userRepository.findById(user2Id).orElse(null);
        
        if (user1 == null || user2 == null) {
            return false;
        }

        Optional<com.colaborai.colaborai.entity.UserConnection> connection = 
            userConnectionRepository.findConnectionBetweenUsers(user1, user2);

        return connection.isPresent() && 
               connection.get().getStatus() == com.colaborai.colaborai.entity.UserConnection.ConnectionStatus.ACCEPTED;
    }
}
