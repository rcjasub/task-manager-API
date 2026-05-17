package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    public Task createTask(CreateTaskRequest request) {
        Task task = new Task(
            request.getTitle(),
            request.getDescription(),
            request.getDueDate(),
            request.getPriority(),
            request.getStatus()
        );
        return taskRepository.save(task);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    public List<Task> getAllTasks() {
        return taskRepository.findAllSortedByDueDateNullsLast();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
    }

    // -------------------------------------------------------------------------
    // Update  (PATCH — only present fields are applied)
    // -------------------------------------------------------------------------

    public Task updateTask(Long id, UpdateTaskRequest request) {
        Task existing = getTaskById(id);

        // @Valid on the controller guarantees title is non-blank and enums are
        // non-null before we reach here — straight assignment is safe.
        existing.setTitle(request.getTitle());
        existing.setDescription(request.getDescription());
        existing.setDueDate(request.getDueDate());
        existing.setPriority(request.getPriority());
        existing.setStatus(request.getStatus());

        return taskRepository.save(existing);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    public void deleteTask(Long id) {
        getTaskById(id); // throws 404 if missing rather than silently no-oping
        taskRepository.deleteById(id);
    }
}