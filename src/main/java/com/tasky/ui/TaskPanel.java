package com.tasky.ui;

import com.tasky.model.Priority;
import com.tasky.model.Task;
import com.tasky.model.TaskList;
import com.tasky.service.TaskService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class TaskPanel extends JPanel {
    private final TaskService taskService;
    private final Supplier<String> currentListIdSupplier;
    private final TaskTableModel tableModel;
    private final JTable taskTable;
    private List<Task> tasks;

    public TaskPanel(TaskService taskService, Supplier<String> currentListIdSupplier) {
        this.taskService = taskService;
        this.currentListIdSupplier = currentListIdSupplier;
        this.tasks = new ArrayList<>();
        this.tableModel = new TaskTableModel();
        this.taskTable = new JTable(tableModel);

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Toolbar
        JPanel toolbar = createToolbar();
        add(toolbar, BorderLayout.NORTH);

        // Task table
        setupTable();
        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JButton addButton = new JButton("+ Add Task");
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addButton.setFocusPainted(false);
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddTaskDialog());
        toolbar.add(addButton);

        toolbar.add(Box.createHorizontalStrut(20));

        JLabel sortLabel = new JLabel("Sort by:");
        sortLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toolbar.add(sortLabel);

        String[] sortOptions = { "Priority", "Due Date", "Created" };
        JComboBox<String> sortCombo = new JComboBox<>(sortOptions);
        sortCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toolbar.add(sortCombo);

        return toolbar;
    }

    private void setupTable() {
        taskTable.setRowHeight(40);
        taskTable.setShowGrid(false);
        taskTable.setIntercellSpacing(new Dimension(0, 0));
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        taskTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        taskTable.getTableHeader().setBackground(new Color(248, 248, 252));
        taskTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        // Column widths
        taskTable.getColumnModel().getColumn(0).setPreferredWidth(50); // Done
        taskTable.getColumnModel().getColumn(0).setMaxWidth(50);
        taskTable.getColumnModel().getColumn(1).setPreferredWidth(300); // Title
        taskTable.getColumnModel().getColumn(2).setPreferredWidth(80); // Priority
        taskTable.getColumnModel().getColumn(2).setMaxWidth(100);
        taskTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Due Date
        taskTable.getColumnModel().getColumn(3).setMaxWidth(120);

        // Custom renderers
        taskTable.getColumnModel().getColumn(0).setCellRenderer(new CheckboxRenderer());
        taskTable.getColumnModel().getColumn(1).setCellRenderer(new TitleRenderer());
        taskTable.getColumnModel().getColumn(2).setCellRenderer(new PriorityRenderer());
        taskTable.getColumnModel().getColumn(3).setCellRenderer(new DueDateRenderer());

        // Double-click to edit
        taskTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = taskTable.rowAtPoint(e.getPoint());
                int col = taskTable.columnAtPoint(e.getPoint());

                if (row >= 0 && row < tasks.size()) {
                    if (col == 0) {
                        // Toggle completion
                        Task task = tasks.get(row);
                        taskService.toggleTaskCompletion(task.getId());
                        refreshTasks();
                    } else if (e.getClickCount() == 2) {
                        // Edit task
                        showEditTaskDialog(tasks.get(row));
                    }
                }
            }
        });

        // Delete key
        taskTable.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    int row = taskTable.getSelectedRow();
                    if (row >= 0 && row < tasks.size()) {
                        deleteTask(tasks.get(row));
                    }
                }
            }
        });

        // Context menu
        JPopupMenu contextMenu = createContextMenu();
        taskTable.setComponentPopupMenu(contextMenu);
    }

    private JPopupMenu createContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row >= 0 && row < tasks.size()) {
                showEditTaskDialog(tasks.get(row));
            }
        });
        menu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            int row = taskTable.getSelectedRow();
            if (row >= 0 && row < tasks.size()) {
                deleteTask(tasks.get(row));
            }
        });
        menu.add(deleteItem);

        return menu;
    }

    private void showAddTaskDialog() {
        String currentListId = currentListIdSupplier.get();
        TaskEditorDialog dialog = new TaskEditorDialog(
                SwingUtilities.getWindowAncestor(this), taskService, null, currentListId);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Task task = dialog.getTask();
            taskService.createTask(task);
            refreshTasks();
            // Refresh sidebar to update counts
            Container parent = getParent();
            if (parent instanceof JFrame) {
                ((MainFrame) SwingUtilities.getWindowAncestor(this)).refreshSidebar();
            }
        }
    }

    private void showEditTaskDialog(Task task) {
        TaskEditorDialog dialog = new TaskEditorDialog(
                SwingUtilities.getWindowAncestor(this), taskService, task, task.getListId());
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Task updatedTask = dialog.getTask();
            taskService.updateTask(updatedTask);
            refreshTasks();
            // Refresh sidebar in case list changed
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof MainFrame) {
                ((MainFrame) window).refreshSidebar();
            }
        }
    }

    private void deleteTask(Task task) {
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete \"" + task.getTitle() + "\"?",
                "Delete Task",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            taskService.deleteTask(task.getId());
            refreshTasks();
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window instanceof MainFrame) {
                ((MainFrame) window).refreshSidebar();
            }
        }
    }

    public void refreshTasks() {
        String listId = currentListIdSupplier.get();
        this.tasks = taskService.getTasksByList(listId);
        tableModel.fireTableDataChanged();
    }

    /**
     * Table model for tasks.
     */
    private class TaskTableModel extends AbstractTableModel {
        private final String[] columns = { "Done", "Title", "Priority", "Due Date" };

        @Override
        public int getRowCount() {
            return tasks.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Task task = tasks.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return task.isCompleted();
                case 1:
                    return task;
                case 2:
                    return task.getPriority();
                case 3:
                    return task.getDueDate();
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return Boolean.class;
                case 1:
                    return Task.class;
                case 2:
                    return Priority.class;
                case 3:
                    return LocalDate.class;
                default:
                    return Object.class;
            }
        }
    }

    private class CheckboxRenderer implements TableCellRenderer {
        private final JCheckBox checkBox = new JCheckBox();

        public CheckboxRenderer() {
            checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            checkBox.setSelected((Boolean) value);
            checkBox.setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
            return checkBox;
        }
    }

    /**
     * Title renderer with strikethrough for completed tasks.
     */
    private class TitleRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Task task = (Task) value;

            String text = task.getTitle();
            if (task.isCompleted()) {
                text = "<html><strike>" + text + "</strike></html>";
            }

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, text, isSelected, hasFocus, row, column);

            label.setBorder(new EmptyBorder(0, 10, 0, 0));

            if (task.isCompleted()) {
                label.setForeground(Color.GRAY);
            } else if (task.isOverdue()) {
                label.setForeground(new Color(200, 50, 50));
            } else {
                label.setForeground(isSelected ? table.getSelectionForeground() : Color.DARK_GRAY);
            }

            return label;
        }
    }

    private class PriorityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Priority priority = (Priority) value;

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, priority.getDisplayName(), isSelected, hasFocus, row, column);

            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (!isSelected) {
                switch (priority) {
                    case HIGH:
                        label.setForeground(new Color(200, 50, 50));
                        break;
                    case MEDIUM:
                        label.setForeground(new Color(200, 150, 50));
                        break;
                    case LOW:
                        label.setForeground(new Color(50, 150, 50));
                        break;
                }
            }

            return label;
        }
    }

    private class DueDateRenderer extends DefaultTableCellRenderer {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            LocalDate dueDate = (LocalDate) value;

            String text = dueDate != null ? formatter.format(dueDate) : "";

            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, text, isSelected, hasFocus, row, column);

            label.setHorizontalAlignment(SwingConstants.CENTER);

            if (dueDate != null && !isSelected) {
                Task task = tasks.get(row);
                if (!task.isCompleted() && LocalDate.now().isAfter(dueDate)) {
                    label.setForeground(new Color(200, 50, 50));
                    label.setFont(label.getFont().deriveFont(Font.BOLD));
                }
            }

            return label;
        }
    }
}
