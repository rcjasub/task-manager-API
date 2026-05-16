package com.example.taskmanager.repository;

import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Filter by status — e.g. GET /tasks?status=TODO
    List<Task> findByStatus(Status status);

    // Filter by priority — e.g. GET /tasks?priority=HIGH
    List<Task> findByPriority(Priority priority);

    // All tasks due on or before a date — useful for "overdue" queries
    List<Task> findByDueDateLessThanEqual(LocalDate date);

    // Combine status + priority for filtered list views
    List<Task> findByStatusAndPriority(Status status, Priority priority);
}