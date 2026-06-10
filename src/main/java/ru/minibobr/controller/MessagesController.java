package ru.minibobr.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.minibobr.service.MessageService;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessagesController {

    private final MessageService messageService;

    public MessagesController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public Map<String, String> getMessages() {
        return messageService.getAllMessages();
    }
}
