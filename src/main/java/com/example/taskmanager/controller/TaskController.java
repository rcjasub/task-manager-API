package com.example.taskmanager.controller;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.SuggestRequest;
import com.example.taskmanager.dto.TaskSuggestion;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ClaudeService;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ClaudeService claudeService;

    public TaskController(TaskService taskService, ClaudeService claudeService) {
        this.taskService   = taskService;
        this.claudeService = claudeService;
    }

    // -------------------------------------------------------------------------
    // POST /tasks → 201 Created
    // -------------------------------------------------------------------------

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task created = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // -------------------------------------------------------------------------
    // POST /tasks/suggest → 200 OK  (not persisted)
    // -------------------------------------------------------------------------

    @PostMapping("/suggest")
    public ResponseEntity<TaskSuggestion> suggestTask(@Valid @RequestBody SuggestRequest request) {
        TaskSuggestion suggestion = claudeService.suggestTask(request.getDescription());
        return ResponseEntity.ok(suggestion);
    }

    // -------------------------------------------------------------------------
    // GET /tasks → 200 OK
    // -------------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // -------------------------------------------------------------------------
    // GET /tasks/{id} → 200 OK | 404 Not Found
    // -------------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(task);
    }

    // -------------------------------------------------------------------------
    // PATCH /tasks/{id} → 200 OK | 404 Not Found
    // -------------------------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id,
                                          @Valid @RequestBody UpdateTaskRequest request) {
        Task updated = taskService.updateTask(id, request);
        return ResponseEntity.ok(updated);
    }

    // -------------------------------------------------------------------------
    // DELETE /tasks/{id} → 204 No Content | 404 Not Found
    // -------------------------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}