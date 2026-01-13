package com.tasky.model;

import java.util.UUID;

public class TaskList {
    public static final String INBOX_ID = "inbox";
    public static final String INBOX_NAME = "Inbox";

    private String id;
    private String name;
    private boolean isDefault;

    public TaskList() {
        this.id = UUID.randomUUID().toString();
        this.isDefault = false;
    }

    public TaskList(String name) {
        this();
        this.name = name;
    }

    public static TaskList createInbox() {
        TaskList inbox = new TaskList();
        inbox.setId(INBOX_ID);
        inbox.setName(INBOX_NAME);
        inbox.setDefault(true);
        return inbox;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return name;
    }
}
