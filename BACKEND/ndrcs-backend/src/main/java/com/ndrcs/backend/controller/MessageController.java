package com.ndrcs.backend.controller;

import com.ndrcs.backend.dto.MessageActionRequest;
import com.ndrcs.backend.dto.SendMessageRequest;
import com.ndrcs.backend.model.Message;
import com.ndrcs.backend.service.MessageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public String sendMessage(@RequestBody SendMessageRequest request) {
        return messageService.sendMessage(request);
    }

    @GetMapping("/inbox")
    public List<Message> getInbox(@RequestParam String role, @RequestParam Long profileId) {
        return messageService.getInbox(role, profileId);
    }

    @PostMapping("/action")
    public String performAction(@RequestBody MessageActionRequest request) {
        return messageService.performMessageAction(request);
    }
}