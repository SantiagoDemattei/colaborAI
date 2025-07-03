package com.colaborai.colaborai.service;

import com.colaborai.colaborai.dto.UserConnectionDTO;
import com.colaborai.colaborai.dto.UserDTO;
import java.util.List;

public interface UserConnectionService {
    UserConnectionDTO sendConnectionRequest(Long requesterId, String usernameOrEmail);
    UserConnectionDTO acceptConnectionRequest(Long connectionId, Long userId);
    UserConnectionDTO rejectConnectionRequest(Long connectionId, Long userId);
    List<UserConnectionDTO> getPendingRequests(Long userId);
    List<UserConnectionDTO> getSentRequests(Long userId);
    List<UserDTO> getConnectedUsers(Long userId);
    List<UserConnectionDTO> getAcceptedConnections(Long userId);
    boolean areUsersConnected(Long user1Id, Long user2Id);
}
