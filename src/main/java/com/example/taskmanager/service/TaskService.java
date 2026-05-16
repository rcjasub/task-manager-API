package com.example.taskmanager.service;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    // Constructor injection — no @Autowired needed on single-constructor classes
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    // -------------------------------------------------------------------------
    // Read
    // -------------------------------------------------------------------------

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found with id: " + id));
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    public Task updateTask(Long id, Task incoming) {
        Task existing = getTaskById(id); // reuses the 404 logic above

        // @Valid on the controller ensures title is non-blank and enums are non-null
        // before this method is ever reached — no defensive guards needed here
        existing.setTitle(incoming.getTitle());
        existing.setDescription(incoming.getDescription());
        existing.setDueDate(incoming.getDueDate());
        existing.setPriority(incoming.getPriority());
        existing.setStatus(incoming.getStatus());

        return taskRepository.save(existing);
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    public void deleteTask(Long id) {
        // Verify existence first so we return 404 rather than silently succeeding
        getTaskById(id);
        taskRepository.deleteById(id);
    }
}