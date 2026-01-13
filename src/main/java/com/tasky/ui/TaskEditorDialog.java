package com.tasky.ui;

import com.tasky.model.Priority;
import com.tasky.model.Task;
import com.tasky.model.TaskList;
import com.tasky.service.TaskService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Dialog for creating and editing tasks.
 */
public class TaskEditorDialog extends JDialog {
    private final TaskService taskService;
    private final Task existingTask;
    private final String defaultListId;

    private JTextField titleField;
    private JTextArea descriptionArea;
    private JComboBox<Priority> priorityCombo;
    private JSpinner dueDateSpinner;
    private JCheckBox hasDueDateCheck;
    private JComboBox<TaskList> listCombo;

    private boolean confirmed = false;
    private Task resultTask;

    public TaskEditorDialog(Window owner, TaskService taskService, Task existingTask, String defaultListId) {
        super(owner, existingTask == null ? "Add Task" : "Edit Task", ModalityType.APPLICATION_MODAL);
        this.taskService = taskService;
        this.existingTask = existingTask;
        this.defaultListId = defaultListId;

        initializeUI();
        populateFields();
    }

    private void initializeUI() {
        setSize(450, 400);
        setLocationRelativeTo(getOwner());
        setResizable(false);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Title:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        titleField = new JTextField(25);
        titleField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(titleField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        descriptionArea = new JTextArea(4, 25);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        formPanel.add(descScroll, gbc);

        // Priority
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(new JLabel("Priority:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        priorityCombo = new JComboBox<>(Priority.values());
        priorityCombo.setSelectedItem(Priority.MEDIUM);
        priorityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        formPanel.add(priorityCombo, gbc);

        // Due Date
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Due Date:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        JPanel dueDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        hasDueDateCheck = new JCheckBox();
        hasDueDateCheck.addActionListener(e -> dueDateSpinner.setEnabled(hasDueDateCheck.isSelected()));
        dueDatePanel.add(hasDueDateCheck);

        SpinnerDateModel dateModel = new SpinnerDateModel();
        dueDateSpinner = new JSpinner(dateModel);
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dueDateSpinner, "MMM d, yyyy");
        dueDateSpinner.setEditor(dateEditor);
        dueDateSpinner.setEnabled(false);
        dueDateSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dueDatePanel.add(dueDateSpinner);

        formPanel.add(dueDatePanel, gbc);

        // List
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        formPanel.add(new JLabel("List:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        listCombo = new JComboBox<>();
        listCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        List<TaskList> lists = taskService.getAllLists();
        for (TaskList list : lists) {
            listCombo.addItem(list);
        }
        formPanel.add(listCombo, gbc);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.addActionListener(e -> saveTask());
        buttonPanel.add(saveButton);

        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(contentPanel);

        // Enter key to save
        getRootPane().setDefaultButton(saveButton);
    }

    private void populateFields() {
        if (existingTask != null) {
            titleField.setText(existingTask.getTitle());
            descriptionArea.setText(existingTask.getDescription());
            priorityCombo.setSelectedItem(existingTask.getPriority());

            if (existingTask.getDueDate() != null) {
                hasDueDateCheck.setSelected(true);
                dueDateSpinner.setEnabled(true);
                Date date = Date.from(existingTask.getDueDate()
                        .atStartOfDay(ZoneId.systemDefault()).toInstant());
                dueDateSpinner.setValue(date);
            }

            // Select current list
            for (int i = 0; i < listCombo.getItemCount(); i++) {
                if (listCombo.getItemAt(i).getId().equals(existingTask.getListId())) {
                    listCombo.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            // Select default list for new task
            for (int i = 0; i < listCombo.getItemCount(); i++) {
                if (listCombo.getItemAt(i).getId().equals(defaultListId)) {
                    listCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveTask() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a title for the task.",
                    "Missing Title",
                    JOptionPane.WARNING_MESSAGE);
            titleField.requestFocus();
            return;
        }

        if (existingTask != null) {
            resultTask = existingTask;
        } else {
            resultTask = new Task();
        }

        resultTask.setTitle(title);
        resultTask.setDescription(descriptionArea.getText().trim());
        resultTask.setPriority((Priority) priorityCombo.getSelectedItem());

        if (hasDueDateCheck.isSelected()) {
            Date date = (Date) dueDateSpinner.getValue();
            LocalDate localDate = date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            resultTask.setDueDate(localDate);
        } else {
            resultTask.setDueDate(null);
        }

        TaskList selectedList = (TaskList) listCombo.getSelectedItem();
        if (selectedList != null) {
            resultTask.setListId(selectedList.getId());
        }

        confirmed = true;
        dispose();
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public Task getTask() {
        return resultTask;
    }
}
