package com.example.taskmanager.dto;

import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Request body for POST /tasks.
 * Decouples the API contract from the Task entity — id and JPA internals
 * are never exposed or bindable from the outside.
 */
public class CreateTaskRequest {

    @NotBlank(message = "title must not be blank")
    private String title;

    private String description;   // optional

    private LocalDate dueDate;    // optional, no temporal constraint

    @NotNull(message = "priority must not be null")
    private Priority priority;

    @NotNull(message = "status must not be null")
    private Status status;

    // -------------------------------------------------------------------------
    // Getters & Setters
    // -------------------------------------------------------------------------

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}