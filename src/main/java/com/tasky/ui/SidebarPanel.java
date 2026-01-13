package com.tasky.ui;

import com.tasky.model.TaskList;
import com.tasky.service.TaskService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class SidebarPanel extends JPanel {
    private final TaskService taskService;
    private final Consumer<String> onListSelected;
    private final DefaultListModel<TaskList> listModel;
    private final JList<TaskList> listView;
    private String selectedListId;

    public SidebarPanel(TaskService taskService, Consumer<String> onListSelected) {
        this.taskService = taskService;
        this.onListSelected = onListSelected;
        this.listModel = new DefaultListModel<>();
        this.listView = new JList<>(listModel);

        initializeUI();
        refreshLists();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(220, 0));
        setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));
        setBackground(new Color(245, 245, 250));

        // Header
        JLabel headerLabel = new JLabel("My Lists");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setBorder(new EmptyBorder(15, 15, 10, 15));
        add(headerLabel, BorderLayout.NORTH);

        // List view
        listView.setCellRenderer(new ListCellRenderer());
        listView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listView.setBackground(new Color(245, 245, 250));
        listView.setBorder(new EmptyBorder(5, 10, 5, 10));

        listView.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                TaskList selected = listView.getSelectedValue();
                if (selected != null) {
                    selectedListId = selected.getId();
                    onListSelected.accept(selected.getId());
                }
            }
        });

        // Context menu
        JPopupMenu contextMenu = createContextMenu();
        listView.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handlePopup(e);
            }

            private void handlePopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = listView.locationToIndex(e.getPoint());
                    if (index >= 0) {
                        listView.setSelectedIndex(index);
                        contextMenu.show(listView, e.getX(), e.getY());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(listView);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(245, 245, 250));
        add(scrollPane, BorderLayout.CENTER);

        // Add button
        JButton addButton = new JButton("+ Add List");
        addButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        addButton.setFocusPainted(false);
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addButton.setBorder(new EmptyBorder(12, 15, 12, 15));
        addButton.setBackground(new Color(245, 245, 250));
        addButton.setHorizontalAlignment(SwingConstants.LEFT);
        addButton.addActionListener(e -> showAddListDialog());
        add(addButton, BorderLayout.SOUTH);
    }

    private JPopupMenu createContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Rename");
        editItem.addActionListener(e -> {
            TaskList selected = listView.getSelectedValue();
            if (selected != null && !selected.isDefault()) {
                showEditListDialog(selected);
            } else if (selected != null && selected.isDefault()) {
                JOptionPane.showMessageDialog(this,
                        "Cannot rename the Inbox list.",
                        "Cannot Rename",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        menu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> {
            TaskList selected = listView.getSelectedValue();
            if (selected != null && !selected.isDefault()) {
                deleteList(selected);
            } else if (selected != null && selected.isDefault()) {
                JOptionPane.showMessageDialog(this,
                        "Cannot delete the Inbox list.",
                        "Cannot Delete",
                        JOptionPane.WARNING_MESSAGE);
            }
        });
        menu.add(deleteItem);

        return menu;
    }

    private void showAddListDialog() {
        ListEditorDialog dialog = new ListEditorDialog(
                SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            TaskList newList = new TaskList(dialog.getListName());
            taskService.createList(newList);
            refreshLists();
            selectList(newList.getId());
        }
    }

    private void showEditListDialog(TaskList list) {
        ListEditorDialog dialog = new ListEditorDialog(
                SwingUtilities.getWindowAncestor(this), list);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            list.setName(dialog.getListName());
            taskService.updateList(list);
            refreshLists();
        }
    }

    private void deleteList(TaskList list) {
        int taskCount = taskService.getTaskCount(list.getId());
        String message = "Are you sure you want to delete \"" + list.getName() + "\"?";
        if (taskCount > 0) {
            message += "\n\n" + taskCount + " task(s) will be moved to Inbox.";
        }

        int result = JOptionPane.showConfirmDialog(this,
                message,
                "Delete List",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            taskService.deleteList(list.getId());
            refreshLists();
            selectList(TaskList.INBOX_ID);
        }
    }

    public void refreshLists() {
        listModel.clear();
        List<TaskList> lists = taskService.getAllLists();
        for (TaskList list : lists) {
            listModel.addElement(list);
        }

        // Restore selection
        if (selectedListId != null) {
            for (int i = 0; i < listModel.size(); i++) {
                if (listModel.get(i).getId().equals(selectedListId)) {
                    listView.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    public void selectList(String listId) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getId().equals(listId)) {
                listView.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Custom cell renderer for task lists.
     */
    private class ListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {

            TaskList taskList = (TaskList) value;
            int count = taskService.getIncompleteTaskCount(taskList.getId());

            String text = taskList.getName();
            if (count > 0) {
                text += " (" + count + ")";
            }

            JLabel label = (JLabel) super.getListCellRendererComponent(
                    list, text, index, isSelected, cellHasFocus);

            label.setBorder(new EmptyBorder(8, 10, 8, 10));
            label.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            if (isSelected) {
                label.setBackground(new Color(70, 130, 220));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(new Color(245, 245, 250));
                label.setForeground(Color.DARK_GRAY);
            }

            label.setOpaque(true);
            return label;
        }
    }
}
