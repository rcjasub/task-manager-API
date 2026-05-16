package com.example.taskmanager.dto;

import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;

import java.time.LocalDate;

/**
 * Transient response object for POST /tasks/suggest.
 * Never persisted — it is only returned to the caller.
 */
public class TaskSuggestion {

    private String title;
    private String description;
    private LocalDate dueDate;
    private Priority priority;
    private Status status;

    public TaskSuggestion() {}

    public TaskSuggestion(String title, String description, LocalDate dueDate,
                          Priority priority, Status status) {
        this.title       = title;
        this.description = description;
        this.dueDate     = dueDate;
        this.priority    = priority;
        this.status      = status;
    }

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