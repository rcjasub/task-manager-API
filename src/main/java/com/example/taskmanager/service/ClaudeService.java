package com.example.taskmanager.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import com.example.taskmanager.dto.TaskSuggestion;
import com.example.taskmanager.exception.ClaudeParseException;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ClaudeService {

    private final AnthropicClient client;
    private final ObjectMapper objectMapper;

    public ClaudeService(@Value("${anthropic.api-key}") String apiKey) {
        this.client = (apiKey == null || apiKey.isBlank())
                ? null
                : AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    /**
     * Sends a plain-English task description to Claude and returns a structured
     * TaskSuggestion. Nothing is persisted.
     */
    public TaskSuggestion suggestTask(String userDescription) {
        if (client == null) {
            throw new ClaudeParseException("ANTHROPIC_API_KEY is not configured — AI suggestions are unavailable.");
        }
        String systemPrompt = """
                You are a task management assistant. The user will describe a task in plain English.
                Your job is to extract structured task data and return it as a single JSON object.

                Rules:
                - Return ONLY valid JSON. No markdown, no backticks, no explanation.
                - Today's date is %s. Use it to infer relative due dates like "Friday" or "next week".
                - "title": short and actionable, max 80 characters.
                - "description": one sentence elaborating on the task, or null if nothing to add.
                - "dueDate": ISO-8601 date string (YYYY-MM-DD) inferred from the description, or null if unclear.
                - "priority": one of LOW, MEDIUM, HIGH. Infer from urgency or deadline proximity.
                - "status": always TODO for a new suggested task.

                Example output:
                {
                  "title": "Finish quarterly report",
                  "description": "Complete and review the Q3 quarterly report for submission.",
                  "dueDate": "2024-06-21",
                  "priority": "HIGH",
                  "status": "TODO"
                }
                """.formatted(LocalDate.now());

        MessageCreateParams params = MessageCreateParams.builder()
                .model(Model.CLAUDE_SONNET_4_5)
                .maxTokens(512L)
                .system(systemPrompt)
                .addUserMessage(userDescription)
                .build();

        Message message = client.messages().create(params);

        String rawJson = message.content().stream()
                .filter(block -> block.isText())
                .map(block -> block.asText().text())
                .findFirst()
                .orElseThrow(() -> new ClaudeParseException("Claude returned no text content"));

        return parseTaskSuggestion(rawJson.trim());
    }

    private TaskSuggestion parseTaskSuggestion(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);

            String title       = node.path("title").asText(null);
            String description = node.path("description").isNull() ? null
                                 : node.path("description").asText(null);
            String dueDateStr  = node.path("dueDate").isNull() ? null
                                 : node.path("dueDate").asText(null);
            String priorityStr = node.path("priority").asText(null);
            String statusStr   = node.path("status").asText(null);

            LocalDate dueDate = (dueDateStr != null) ? LocalDate.parse(dueDateStr) : null;
            Priority priority = parseEnum(Priority.class, priorityStr);
            Status status     = parseEnum(Status.class, statusStr);

            return new TaskSuggestion(title, description, dueDate, priority, status);

        } catch (Exception e) {
            throw new ClaudeParseException(
                "Failed to parse Claude's response as a task suggestion: " + e.getMessage(), e);
        }
    }

    private <E extends Enum<E>> E parseEnum(Class<E> enumClass, String value) {
        if (value == null || value.isBlank()) {
            throw new ClaudeParseException(
                "Claude returned a null or missing value for " + enumClass.getSimpleName() +
                ". Valid values: " + java.util.Arrays.toString(enumClass.getEnumConstants()));
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ClaudeParseException(
                "Claude returned an unrecognised value '" + value + "' for " +
                enumClass.getSimpleName() + ". Valid values: " +
                java.util.Arrays.toString(enumClass.getEnumConstants()));
        }
    }
}