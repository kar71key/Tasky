package com.tasky.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String description;
    private Priority priority;
    private LocalDate dueDate;
    private String listId;
    private boolean completed;
    private LocalDateTime createdAt;

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.priority = Priority.MEDIUM;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    public Task(String title, String listId) {
        this();
        this.title = title;
        this.listId = listId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isOverdue() {
        if (dueDate == null || completed) {
            return false;
        }
        return LocalDate.now().isAfter(dueDate);
    }

    @Override
    public String toString() {
        return title;
    }
}
