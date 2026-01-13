package com.tasky.ui;

import com.tasky.model.TaskList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ListEditorDialog extends JDialog {
    private final TaskList existingList;

    private JTextField nameField;
    private boolean confirmed = false;
    private String resultName;

    public ListEditorDialog(Window owner, TaskList existingList) {
        super(owner, existingList == null ? "Add List" : "Rename List", ModalityType.APPLICATION_MODAL);
        this.existingList = existingList;

        initializeUI();
    }

    private void initializeUI() {
        setSize(350, 150);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel formPanel = new JPanel(new BorderLayout(10, 0));

        JLabel label = new JLabel("List Name:");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(label, BorderLayout.WEST);

        nameField = new JTextField(20);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (existingList != null) {
            nameField.setText(existingList.getName());
            nameField.selectAll();
        }
        formPanel.add(nameField, BorderLayout.CENTER);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.addActionListener(e -> saveList());
        buttonPanel.add(saveButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);

        // Enter key to save
        getRootPane().setDefaultButton(saveButton);
    }

    private void saveList() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a name for the list.",
                    "Missing Name",
                    JOptionPane.WARNING_MESSAGE);
            nameField.requestFocus();
            return;
        }

        resultName = name;
        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public String getListName() {
        return resultName;
    }
}
