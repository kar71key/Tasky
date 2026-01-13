package com.tasky.model;

import java.util.ArrayList;
import java.util.List;

public class AppData {
    private List<Task> tasks;
    private List<TaskList> lists;

    public AppData() {
        this.tasks = new ArrayList<>();
        this.lists = new ArrayList<>();
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public List<TaskList> getLists() {
        return lists;
    }

    public void setLists(List<TaskList> lists) {
        this.lists = lists;
    }
}
