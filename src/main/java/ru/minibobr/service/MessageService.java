package ru.minibobr.service;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Service
public class MessageService {

    private final MessageSource messageSource;

    public MessageService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public Map<String, String> getAllMessages() {
        Properties properties = loadProperties();
        Map<String, String> messages = new LinkedHashMap<>();
        for (String key : properties.stringPropertyNames()) {
            messages.put(key, messageSource.getMessage(key, null, key, java.util.Locale.getDefault()));
        }
        return messages;
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("messages.properties")) {
            if (input != null) {
                properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load messages.properties", e);
        }
        return properties;
    }
}
