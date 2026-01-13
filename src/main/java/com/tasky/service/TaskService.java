package com.tasky.service;

import com.tasky.model.AppData;
import com.tasky.model.Task;
import com.tasky.model.TaskList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TaskService {
    private final DataService dataService;
    private AppData appData;

    public TaskService(DataService dataService) {
        this.dataService = dataService;
        this.appData = dataService.load();
    }

    public List<Task> getTasksByList(String listId) {
        return appData.getTasks().stream()
                .filter(task -> listId.equals(task.getListId()))
                .sorted(Comparator
                        .comparing(Task::isCompleted)
                        .thenComparing((Task t) -> t.getPriority().getLevel()).reversed()
                        .thenComparing(Task::getCreatedAt))
                .collect(Collectors.toList());
    }

    public Task getTaskById(String taskId) {
        return appData.getTasks().stream()
                .filter(task -> taskId.equals(task.getId()))
                .findFirst()
                .orElse(null);
    }

    public Task createTask(Task task) {
        if (task.getListId() == null || task.getListId().isEmpty()) {
            task.setListId(TaskList.INBOX_ID);
        }
        appData.getTasks().add(task);
        save();
        return task;
    }

    public void updateTask(Task task) {
        for (int i = 0; i < appData.getTasks().size(); i++) {
            if (appData.getTasks().get(i).getId().equals(task.getId())) {
                appData.getTasks().set(i, task);
                break;
            }
        }
        save();
    }

    public void deleteTask(String taskId) {
        appData.getTasks().removeIf(task -> taskId.equals(task.getId()));
        save();
    }

    public void toggleTaskCompletion(String taskId) {
        Task task = getTaskById(taskId);
        if (task != null) {
            task.setCompleted(!task.isCompleted());
            save();
        }
    }

    public List<TaskList> getAllLists() {
        return appData.getLists().stream()
                .sorted((a, b) -> {
                    if (a.isDefault())
                        return -1;
                    if (b.isDefault())
                        return 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .collect(Collectors.toList());
    }

    public TaskList getListById(String listId) {
        return appData.getLists().stream()
                .filter(list -> listId.equals(list.getId()))
                .findFirst()
                .orElse(null);
    }

    public TaskList createList(TaskList list) {
        appData.getLists().add(list);
        save();
        return list;
    }

    public void updateList(TaskList list) {
        for (int i = 0; i < appData.getLists().size(); i++) {
            if (appData.getLists().get(i).getId().equals(list.getId())) {
                appData.getLists().set(i, list);
                break;
            }
        }
        save();
    }

    public boolean deleteList(String listId) {
        TaskList list = getListById(listId);
        if (list == null || list.isDefault()) {
            return false;
        }

        appData.getTasks().stream()
                .filter(task -> listId.equals(task.getListId()))
                .forEach(task -> task.setListId(TaskList.INBOX_ID));

        appData.getLists().removeIf(l -> listId.equals(l.getId()));
        save();
        return true;
    }

    public int getTaskCount(String listId) {
        return (int) appData.getTasks().stream()
                .filter(task -> listId.equals(task.getListId()))
                .count();
    }

    public int getIncompleteTaskCount(String listId) {
        return (int) appData.getTasks().stream()
                .filter(task -> listId.equals(task.getListId()) && !task.isCompleted())
                .count();
    }

    public void save() {
        dataService.save(appData);
    }

    public void reload() {
        this.appData = dataService.load();
    }
}
