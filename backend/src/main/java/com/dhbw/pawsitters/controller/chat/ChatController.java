package com.dhbw.pawsitters.controller.chat;

import com.dhbw.pawsitters.model.chat.ChatMessage;
import com.dhbw.pawsitters.model.chat.ChatMessageRequest;
import com.dhbw.pawsitters.model.sitting.SittingRequest;
import com.dhbw.pawsitters.model.user.AppUser;
import com.dhbw.pawsitters.service.chat.ChatService;
import com.dhbw.pawsitters.service.user.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private AppUserService userService;

    @GetMapping("/contacts")
    public List<SittingRequest> getContacts(@RequestParam Long userId) {
        AppUser user = userService.getUserById(userId);
        return chatService.getChatContacts(user);
    }

    @GetMapping("/messages/{requestId}")
    public List<ChatMessage> getMessages(@PathVariable Long requestId, @RequestParam Long userId) {
        AppUser user = userService.getUserById(userId);
        return chatService.getMessages(requestId, user);
    }

    @PostMapping("/messages/{requestId}")
    public ChatMessage sendMessage(@PathVariable Long requestId, @RequestParam Long userId, @RequestBody ChatMessageRequest messageRequest) {
        AppUser user = userService.getUserById(userId);
        return chatService.sendMessage(requestId, user, messageRequest.getContent());
    }
}
