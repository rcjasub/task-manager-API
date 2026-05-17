package com.example.taskmanager.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.services.blocking.MessageService;
import com.anthropic.models.messages.TextBlock;
import com.example.taskmanager.dto.TaskSuggestion;
import com.example.taskmanager.exception.ClaudeParseException;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaudeServiceTest {

    @Mock private AnthropicClient anthropicClient;
    @Mock private MessageService  messageService;
    @Mock private Message         message;

    // Constructed directly — no Spring context, no reflection, no dummy API key
    private ClaudeService claudeService;

    @BeforeEach
    void setUp() {
        claudeService = new ClaudeService(anthropicClient);
        when(anthropicClient.messages()).thenReturn(messageService);
        when(messageService.create(any(MessageCreateParams.class))).thenReturn(message);
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void suggestTask_validJson_returnsParsedSuggestion() {
        stubClaudeResponse("""
                {
                  "title": "Finish quarterly report",
                  "description": "Complete and submit the Q3 report.",
                  "dueDate": "2026-05-23",
                  "priority": "HIGH",
                  "status": "TODO"
                }
                """);

        TaskSuggestion result = claudeService.suggestTask("finish the quarterly report before Friday");

        assertThat(result.getTitle()).isEqualTo("Finish quarterly report");
        assertThat(result.getDescription()).isEqualTo("Complete and submit the Q3 report.");
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2026, 5, 23));
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        assertThat(result.getStatus()).isEqualTo(Status.TODO);
    }

    @Test
    void suggestTask_nullDueDate_returnsNullDueDate() {
        stubClaudeResponse("""
                {
                  "title": "Vague task",
                  "description": null,
                  "dueDate": null,
                  "priority": "LOW",
                  "status": "TODO"
                }
                """);

        TaskSuggestion result = claudeService.suggestTask("do something sometime");

        assertThat(result.getDueDate()).isNull();
        assertThat(result.getDescription()).isNull();
        assertThat(result.getPriority()).isEqualTo(Priority.LOW);
    }

    @Test
    void suggestTask_lowercaseEnums_parsedCaseInsensitively() {
        stubClaudeResponse("""
                {
                  "title": "Lowercase enums",
                  "description": null,
                  "dueDate": null,
                  "priority": "medium",
                  "status": "todo"
                }
                """);

        TaskSuggestion result = claudeService.suggestTask("some task");

        assertThat(result.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(result.getStatus()).isEqualTo(Status.TODO);
    }

    // ── Parse failure paths ───────────────────────────────────────────────────

    @Test
    void suggestTask_unrecognisedPriorityValue_throwsClaudeParseException() {
        stubClaudeResponse("""
                {
                  "title": "Bad priority",
                  "description": null,
                  "dueDate": null,
                  "priority": "URGENT",
                  "status": "TODO"
                }
                """);

        assertThatThrownBy(() -> claudeService.suggestTask("something urgent"))
                .isInstanceOf(ClaudeParseException.class)
                .hasMessageContaining("URGENT")
                .hasMessageContaining("Priority");
    }

    @Test
    void suggestTask_missingPriorityField_throwsClaudeParseException() {
        stubClaudeResponse("""
                {
                  "title": "No priority",
                  "description": null,
                  "dueDate": null,
                  "status": "TODO"
                }
                """);

        assertThatThrownBy(() -> claudeService.suggestTask("task without priority"))
                .isInstanceOf(ClaudeParseException.class)
                .hasMessageContaining("Priority");
    }

    @Test
    void suggestTask_malformedJson_throwsClaudeParseException() {
        stubClaudeResponse("this is not json at all");

        assertThatThrownBy(() -> claudeService.suggestTask("anything"))
                .isInstanceOf(ClaudeParseException.class)
                .hasMessageContaining("Failed to parse");
    }

    @Test
    void suggestTask_noTextContentBlock_throwsClaudeParseException() {
        when(message.content()).thenReturn(List.of());

        assertThatThrownBy(() -> claudeService.suggestTask("anything"))
                .isInstanceOf(ClaudeParseException.class)
                .hasMessageContaining("no text content");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void stubClaudeResponse(String json) {
        TextBlock textBlock = TextBlock.builder().text(json).citations(List.of()).build();
        ContentBlock contentBlock = ContentBlock.ofText(textBlock);
        when(message.content()).thenReturn(List.of(contentBlock));
    }
}