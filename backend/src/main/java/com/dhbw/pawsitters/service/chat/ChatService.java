package com.dhbw.pawsitters.service.chat;

import com.dhbw.pawsitters.model.chat.ChatMessage;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.UnitOfWork;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ChatService {

    @Autowired
    private UnitOfWork unitOfWork;

    public ChatMessage sendMessage(Long requestId, AppUser sender, String content) {
        SittingRequest request = unitOfWork.getById(SittingRequest.class, requestId);

        if (!canChat(request, sender)) {
            throw new RuntimeException("Cannot send message. Request must be ACCEPTED and you must be a participant.");
        }

        ChatMessage message = ChatMessage.builder()
                .sittingRequest(request)
                .sender(sender)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();

        return unitOfWork.save(message);
    }

    public List<ChatMessage> getMessages(Long requestId, AppUser user) {
        SittingRequest request = unitOfWork.getById(SittingRequest.class, requestId);

        // We allow reading messages even if status is COMPLETED or CANCELLED, 
        // as long as the user was a participant.
        if (!isParticipant(request, user)) {
            throw new RuntimeException("You are not a participant of this request.");
        }

        return unitOfWork.getByPropertiesSorted(
                ChatMessage.class,
                Map.of("sittingRequest.id", requestId),
                "timestamp",
                true
        );
    }

    public List<SittingRequest> getChatContacts(AppUser user) {
        // Contacts are requests where the user is either requester or sitter,
        // and status is ACCEPTED (active chat) or PENDING (maybe? User said ACCEPTED until COMPLETED/CANCELLED).
        // User said: "from acceptance of request until finished".
        // Let's include ACCEPTED for active chat.
        
        List<SittingRequest> asRequester = unitOfWork.getByProperties(SittingRequest.class, Map.of(
                "requester", user,
                "status", SittingRequest.RequestStatus.ACCEPTED
        ));

        List<SittingRequest> asSitter = unitOfWork.getByProperties(SittingRequest.class, Map.of(
                "sitter", user,
                "status", SittingRequest.RequestStatus.ACCEPTED
        ));

        return Stream.concat(asRequester.stream(), asSitter.stream())
                .distinct()
                .collect(Collectors.toList());
    }

    public boolean canChat(SittingRequest request, AppUser user) {
        return request.getStatus() == SittingRequest.RequestStatus.ACCEPTED && isParticipant(request, user);
    }

    private boolean isParticipant(SittingRequest request, AppUser user) {
        return (request.getRequester() != null && request.getRequester().getId().equals(user.getId())) ||
               (request.getSitter() != null && request.getSitter().getId().equals(user.getId()));
    }
}
