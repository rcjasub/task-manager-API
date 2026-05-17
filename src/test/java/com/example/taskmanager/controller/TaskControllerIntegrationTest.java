package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskSuggestion;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.service.ClaudeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full-context integration tests using H2 and MockMvc.
 * ClaudeService is replaced with a @MockBean so no real API calls are made.
 */
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired TaskRepository taskRepository;

    // Replaces the real ClaudeService bean for the entire test context
    @MockBean ClaudeService claudeService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void clearDatabase() {
        taskRepository.deleteAll();
    }

    // ── POST /tasks ───────────────────────────────────────────────────────────

    @Test
    void createTask_validRequest_returns201WithBody() throws Exception {
        String body = toJson(Map.of(
                "title",    "Integration test task",
                "priority", "HIGH",
                "status",   "TODO"
        ));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Integration test task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTask_blankTitle_returns400WithMessage() throws Exception {
        String body = toJson(Map.of(
                "title",    "",
                "priority", "MEDIUM",
                "status",   "TODO"
        ));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("title")));
    }

    @Test
    void createTask_missingPriority_returns400() throws Exception {
        String body = toJson(Map.of(
                "title",  "Missing priority",
                "status", "TODO"
        ));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("priority")));
    }

    // ── GET /tasks ────────────────────────────────────────────────────────────

    @Test
    void getAllTasks_returnsListSortedByDueDate() throws Exception {
        // Insert in reverse order — later due date first
        persistTask("Task due July",  LocalDate.of(2026, 7, 1));
        persistTask("Task due June",  LocalDate.of(2026, 6, 1));
        persistTask("Task no date",   null);

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                // dueDate ascending: June → July → null
                .andExpect(jsonPath("$[0].title").value("Task due June"))
                .andExpect(jsonPath("$[1].title").value("Task due July"))
                .andExpect(jsonPath("$[2].title").value("Task no date"));
    }

    @Test
    void getAllTasks_emptyDatabase_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /tasks/{id} ───────────────────────────────────────────────────────

    @Test
    void getTaskById_existingId_returns200() throws Exception {
        Task saved = persistTask("Fetch me", LocalDate.of(2026, 6, 15));

        mockMvc.perform(get("/tasks/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.title").value("Fetch me"));
    }

    @Test
    void getTaskById_missingId_returns404() throws Exception {
        mockMvc.perform(get("/tasks/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("99999")));
    }

    // ── PUT /tasks/{id} ───────────────────────────────────────────────────────

    @Test
    void updateTask_existingId_returns200WithUpdatedFields() throws Exception {
        Task saved = persistTask("Original title", LocalDate.of(2026, 6, 1));

        String body = toJson(Map.of(
                "title",    "Updated title",
                "priority", "LOW",
                "status",   "IN_PROGRESS"
        ));

        mockMvc.perform(put("/tasks/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.priority").value("LOW"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTask_missingId_returns404() throws Exception {
        String body = toJson(Map.of(
                "title",    "Doesn't matter",
                "priority", "LOW",
                "status",   "TODO"
        ));

        mockMvc.perform(put("/tasks/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTask_blankTitle_returns400() throws Exception {
        Task saved = persistTask("Has a title", null);

        String body = toJson(Map.of(
                "title",    "",
                "priority", "MEDIUM",
                "status",   "TODO"
        ));

        mockMvc.perform(put("/tasks/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("title")));
    }

    // ── DELETE /tasks/{id} ────────────────────────────────────────────────────

    @Test
    void deleteTask_existingId_returns204AndRemovesTask() throws Exception {
        Task saved = persistTask("Delete me", null);

        mockMvc.perform(delete("/tasks/" + saved.getId()))
                .andExpect(status().isNoContent());

        // Confirm it's actually gone
        mockMvc.perform(get("/tasks/" + saved.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTask_missingId_returns404() throws Exception {
        mockMvc.perform(delete("/tasks/99999"))
                .andExpect(status().isNotFound());
    }

    // ── POST /tasks/suggest ───────────────────────────────────────────────────

    @Test
    void suggestTask_validInput_returns200WithSuggestion() throws Exception {
        TaskSuggestion mockSuggestion = new TaskSuggestion(
                "Finish quarterly report",
                "Complete and submit the Q3 report.",
                LocalDate.of(2026, 5, 23),
                Priority.HIGH,
                Status.TODO
        );
        when(claudeService.suggestTask(anyString())).thenReturn(mockSuggestion);

        String body = toJson(Map.of("description", "finish the quarterly report before Friday"));

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Finish quarterly report"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.dueDate").value("2026-05-23"));
    }

    @Test
    void suggestTask_blankDescription_returns400() throws Exception {
        String body = toJson(Map.of("description", ""));

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("description")));
    }

    @Test
    void suggestTask_claudeParseFailure_returns502() throws Exception {
        when(claudeService.suggestTask(anyString()))
                .thenThrow(new com.example.taskmanager.exception.ClaudeParseException(
                        "Claude returned an unrecognised value 'URGENT' for Priority"));

        String body = toJson(Map.of("description", "something urgent"));

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value(containsString("URGENT")));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Task persistTask(String title, LocalDate dueDate) {
        Task task = new Task(title, null, dueDate, Priority.MEDIUM, Status.TODO);
        return taskRepository.save(task);
    }

    private String toJson(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}