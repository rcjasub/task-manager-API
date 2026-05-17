package com.example.taskmanager.service;

import com.example.taskmanager.dto.CreateTaskRequest;
import com.example.taskmanager.dto.UpdateTaskRequest;
import com.example.taskmanager.model.Priority;
import com.example.taskmanager.model.Status;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task("Write tests", "Ensure full coverage", LocalDate.of(2026, 6, 1),
                Priority.HIGH, Status.TODO);
        // Simulate a persisted entity with an id
        setId(sampleTask, 1L);
    }

    // ── createTask ────────────────────────────────────────────────────────────

    @Test
    void createTask_savesAndReturnsTask() {
        CreateTaskRequest request = makeCreateRequest("Write tests", Priority.HIGH, Status.TODO);
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        Task result = taskService.createTask(request);

        assertThat(result.getTitle()).isEqualTo("Write tests");
        assertThat(result.getPriority()).isEqualTo(Priority.HIGH);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // ── getAllTasks ───────────────────────────────────────────────────────────

    @Test
    void getAllTasks_returnsAllTasksSorted() {
        Task second = new Task("Deploy app", null, LocalDate.of(2026, 7, 1),
                Priority.MEDIUM, Status.TODO);
        when(taskRepository.findAll(any(Sort.class))).thenReturn(List.of(sampleTask, second));

        List<Task> result = taskService.getAllTasks();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("Write tests");
        verify(taskRepository).findAll(TaskRepository.SORT_BY_DUE_DATE);
    }

    // ── getTaskById ───────────────────────────────────────────────────────────

    @Test
    void getTaskById_existingId_returnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Task result = taskService.getTaskById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Write tests");
    }

    @Test
    void getTaskById_missingId_throwsNoSuchElementException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(99L))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");
    }

    // ── updateTask ────────────────────────────────────────────────────────────

    @Test
    void updateTask_existingId_appliesAllFields() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateTaskRequest request = makeUpdateRequest("Updated title", Priority.LOW, Status.DONE);
        Task result = taskService.updateTask(1L, request);

        assertThat(result.getTitle()).isEqualTo("Updated title");
        assertThat(result.getPriority()).isEqualTo(Priority.LOW);
        assertThat(result.getStatus()).isEqualTo(Status.DONE);
        verify(taskRepository).save(sampleTask);
    }

    @Test
    void updateTask_missingId_throwsNoSuchElementException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(99L, makeUpdateRequest("x", Priority.LOW, Status.TODO)))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("99");

        verify(taskRepository, never()).save(any());
    }

    // ── deleteTask ────────────────────────────────────────────────────────────

    @Test
    void deleteTask_existingId_deletesTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        taskService.deleteTask(1L);

        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteTask_missingId_throwsAndNeverDeletes() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(99L))
                .isInstanceOf(NoSuchElementException.class);

        verify(taskRepository, never()).deleteById(any());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private CreateTaskRequest makeCreateRequest(String title, Priority priority, Status status) {
        CreateTaskRequest r = new CreateTaskRequest();
        r.setTitle(title);
        r.setDescription("Some description");
        r.setDueDate(LocalDate.of(2026, 6, 1));
        r.setPriority(priority);
        r.setStatus(status);
        return r;
    }

    private UpdateTaskRequest makeUpdateRequest(String title, Priority priority, Status status) {
        UpdateTaskRequest r = new UpdateTaskRequest();
        r.setTitle(title);
        r.setDescription("Updated description");
        r.setDueDate(LocalDate.of(2026, 8, 1));
        r.setPriority(priority);
        r.setStatus(status);
        return r;
    }

    /** Reflectively sets the auto-generated id so we can simulate a persisted entity. */
    private void setId(Task task, Long id) {
        try {
            var field = Task.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(task, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}