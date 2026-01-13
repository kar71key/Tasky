package com.tasky;

import com.tasky.service.DataService;
import com.tasky.service.TaskService;
import com.tasky.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            DataService dataService = new DataService();
            TaskService taskService = new TaskService(dataService);
            MainFrame mainFrame = new MainFrame(taskService);
            mainFrame.setVisible(true);
        });
    }
}
