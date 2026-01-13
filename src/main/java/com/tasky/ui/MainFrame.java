package com.tasky.ui;

import com.tasky.model.TaskList;
import com.tasky.service.TaskService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final TaskService taskService;
    private SidebarPanel sidebarPanel;
    private TaskPanel taskPanel;
    private String currentListId;

    public MainFrame(TaskService taskService) {
        this.taskService = taskService;
        this.currentListId = TaskList.INBOX_ID;

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Tasky - Task Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);

        // Set up menu bar
        setJMenuBar(createMenuBar());

        // Main layout
        setLayout(new BorderLayout());

        // Create sidebar
        sidebarPanel = new SidebarPanel(taskService, this::onListSelected);
        add(sidebarPanel, BorderLayout.WEST);

        // Create task panel
        taskPanel = new TaskPanel(taskService, this::getCurrentListId);
        add(taskPanel, BorderLayout.CENTER);

        // Select inbox by default
        sidebarPanel.selectList(TaskList.INBOX_ID);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
        refreshItem.addActionListener(e -> refresh());
        fileMenu.add(refreshItem);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');

        JMenuItem aboutItem = new JMenuItem("About Tasky");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);

        menuBar.add(helpMenu);

        return menuBar;
    }

    private void onListSelected(String listId) {
        this.currentListId = listId;
        taskPanel.refreshTasks();

        TaskList list = taskService.getListById(listId);
        if (list != null) {
            setTitle("Tasky - " + list.getName());
        }
    }

    public String getCurrentListId() {
        return currentListId;
    }

    public void refresh() {
        taskService.reload();
        sidebarPanel.refreshLists();
        taskPanel.refreshTasks();
    }

    public void refreshSidebar() {
        sidebarPanel.refreshLists();
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                "Tasky - Task Manager\n\n" +
                        "A feature-rich offline task manager.\n" +
                        "Organize your tasks with custom lists,\n" +
                        "priorities, and due dates.\n\n" +
                        "Version 1.0",
                "About Tasky",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
