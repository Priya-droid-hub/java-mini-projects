
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class Main extends JFrame {

    // ---------- COLORS ----------
    private static final Color BACKGROUND = new Color(245, 247, 251);
    private static final Color SIDEBAR = new Color(24, 32, 50);
    private static final Color SIDEBAR_SELECTED = new Color(58, 72, 103);
    private static final Color BLUE = new Color(76, 110, 245);
    private static final Color ADD_BUTTON_BLUE = new Color(35, 75, 210);
    private static final Color ADD_BUTTON_HOVER = new Color(24, 55, 170);
    private static final Color GREEN = new Color(39, 174, 96);
    private static final Color TEXT = new Color(42, 48, 60);
    private static final Color MUTED = new Color(130, 139, 153);
    private static final Color CARD = Color.WHITE;

    // ---------- TASK DATA ----------
    private final List<Task> tasks = new ArrayList<>();

    private final Path storageFile = Paths.get(
            System.getProperty("user.home"),
            ".javatodolist",
            "tasks.txt"
    );

    // ---------- UI COMPONENTS ----------
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JPanel taskListPanel;
    private JPanel statsPanel;

    private JTextField taskInput;
    private JLabel totalValue;
    private JLabel pendingValue;
    private JLabel completedValue;
    private JLabel progressLabel;
    private JProgressBar progressBar;

    private String currentFilter = "All Tasks";

    // ---------- CONSTRUCTOR ----------
    public Main() {
        setTitle("TaskFlow - To-Do List");
        setSize(1050, 700);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BACKGROUND);

        loadTasks();
        createUI();
        refreshTasks();
    }

    // ---------- TASK MODEL ----------
    private static class Task {
        String title;
        boolean completed;

        Task(String title, boolean completed) {
            this.title = title;
            this.completed = completed;
        }
    }

    // ---------- MAIN UI ----------
    private void createUI() {
        sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(225, 0));
        sidebarPanel.setBackground(SIDEBAR);

        createSidebar();

        contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(28, 30, 25, 30));

        createWorkspace();

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    // ---------- SIDEBAR ----------
    private void createSidebar() {
        sidebarPanel.removeAll();
        sidebarPanel.setLayout(new BorderLayout());
        sidebarPanel.setBackground(SIDEBAR);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBorder(new EmptyBorder(30, 22, 20, 22));

        JLabel logo = new JLabel("  TaskFlow");
        logo.setFont(new Font("SansSerif", Font.BOLD, 25));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("  YOUR PERSONAL WORKSPACE");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 9));
        subtitle.setForeground(new Color(190, 199, 218));
        subtitle.setBorder(new EmptyBorder(8, 0, 32, 0));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(logo);
        top.add(subtitle);

        JLabel menuTitle = new JLabel("  MENU");
        menuTitle.setFont(new Font("SansSerif", Font.BOLD, 10));
        menuTitle.setForeground(new Color(170, 183, 207));
        menuTitle.setBorder(new EmptyBorder(0, 0, 12, 0));
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        top.add(menuTitle);
        top.add(createNavButton("▦   All Tasks", "All Tasks"));
        top.add(Box.createVerticalStrut(8));
        top.add(createNavButton("◷   Pending", "Pending"));
        top.add(Box.createVerticalStrut(8));
        top.add(createNavButton("✓   Completed", "Completed"));

        sidebarPanel.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(15, 22, 25, 22));

        JLabel footer = new JLabel("Stay focused. Get things done.");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(new Color(190, 199, 218));

        bottom.add(footer);
        sidebarPanel.add(bottom, BorderLayout.SOUTH);

        sidebarPanel.revalidate();
        sidebarPanel.repaint();
    }

    // ---------- NAVIGATION BUTTON ----------
    private JButton createNavButton(String label, String filter) {
        JButton button = new JButton(label);

        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);

        button.setBackground(
                currentFilter.equals(filter)
                        ? SIDEBAR_SELECTED
                        : SIDEBAR
        );

        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(new EmptyBorder(13, 12, 13, 10));
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(SIDEBAR_SELECTED);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(
                        currentFilter.equals(filter)
                                ? SIDEBAR_SELECTED
                                : SIDEBAR
                );
            }
        });

        button.addActionListener(e -> {
            currentFilter = filter;
            createSidebar();
            refreshTasks();
        });

        return button;
    }

    // ---------- WORKSPACE ----------
    private void createWorkspace() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("My Tasks");
        title.setFont(new Font("SansSerif", Font.BOLD, 29));
        title.setForeground(TEXT);

        JLabel description = new JLabel(
                "Organize your day, one task at a time."
        );
        description.setFont(new Font("SansSerif", Font.PLAIN, 13));
        description.setForeground(MUTED);
        description.setBorder(new EmptyBorder(6, 0, 0, 0));

        heading.add(title);
        heading.add(description);
        header.add(heading, BorderLayout.WEST);

        JLabel dateLabel = new JLabel(
                java.time.LocalDate.now().format(
                        java.time.format.DateTimeFormatter.ofPattern(
                                "EEE, dd MMM yyyy"
                        )
                )
        );
        dateLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dateLabel.setForeground(MUTED);
        header.add(dateLabel, BorderLayout.EAST);

        contentPanel.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // ---------- STATISTICS CARDS ----------
        statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsPanel.add(createStatCard("TOTAL TASKS", "0", "#", BLUE));
        statsPanel.add(createStatCard(
                "PENDING", "0", "◷", new Color(230, 155, 48)
        ));
        statsPanel.add(createStatCard("COMPLETED", "0", "✓", GREEN));

        center.add(statsPanel);
        center.add(Box.createVerticalStrut(20));

        // ---------- ADD TASK INPUT ----------
        JPanel addPanel = new JPanel(new BorderLayout(10, 0));
        addPanel.setBackground(CARD);
        addPanel.setBorder(new EmptyBorder(16, 17, 16, 17));
        addPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));
        addPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        taskInput = new JTextField();
        taskInput.setFont(new Font("SansSerif", Font.PLAIN, 14));
        taskInput.setForeground(TEXT);
        taskInput.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 232, 240)),
                new EmptyBorder(10, 12, 10, 12)
        ));
        taskInput.setToolTipText("Enter a task and press Add Task");
        taskInput.addActionListener(e -> addTask());

        // ---------- HIGH-CONTRAST ADD TASK BUTTON ----------
        JButton addButton = new JButton("+  Add Task");
        addButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addButton.setForeground(Color.WHITE);
        addButton.setBackground(ADD_BUTTON_BLUE);
        addButton.setFocusPainted(false);
        addButton.setOpaque(true);
        addButton.setContentAreaFilled(true);
        addButton.setBorderPainted(false);
        addButton.setBorder(new EmptyBorder(12, 18, 12, 18));
        addButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        addButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                addButton.setBackground(ADD_BUTTON_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                addButton.setBackground(ADD_BUTTON_BLUE);
            }
        });

        addButton.addActionListener(e -> addTask());

        addPanel.add(taskInput, BorderLayout.CENTER);
        addPanel.add(addButton, BorderLayout.EAST);

        center.add(addPanel);
        center.add(Box.createVerticalStrut(20));

        // ---------- PROGRESS ----------
        JPanel progressPanel = new JPanel(new BorderLayout(0, 10));
        progressPanel.setOpaque(false);
        progressPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        progressPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressLabel = new JLabel("Your progress");
        progressLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        progressLabel.setForeground(TEXT);

        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressBar.setString("0%");
        progressBar.setForeground(GREEN);
        progressBar.setBackground(new Color(225, 230, 238));
        progressBar.setBorderPainted(false);
        progressBar.setPreferredSize(new Dimension(100, 12));

        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        center.add(progressPanel);
        center.add(Box.createVerticalStrut(20));

        // ---------- TASK LIST ----------
        JLabel listHeading = new JLabel("Your task list");
        listHeading.setFont(new Font("SansSerif", Font.BOLD, 17));
        listHeading.setForeground(TEXT);
        listHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(listHeading);
        center.add(Box.createVerticalStrut(10));

        taskListPanel = new JPanel();
        taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
        taskListPanel.setBackground(BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(taskListPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);

        center.add(scrollPane);

        contentPanel.add(center, BorderLayout.CENTER);
    }

    // ---------- STATISTICS CARD ----------
    private JPanel createStatCard(
            String title,
            String value,
            String symbol,
            Color accent
    ) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(16, 17, 16, 17));

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        titleLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 27));
        valueLabel.setForeground(TEXT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(8));
        textPanel.add(valueLabel);

        JLabel iconLabel = new JLabel(symbol, SwingConstants.CENTER);
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        iconLabel.setForeground(accent);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(new Color(
                accent.getRed(),
                accent.getGreen(),
                accent.getBlue(),
                25
        ));
        iconLabel.setPreferredSize(new Dimension(43, 43));

        card.add(textPanel, BorderLayout.CENTER);
        card.add(iconLabel, BorderLayout.EAST);

        if (title.equals("TOTAL TASKS")) {
            totalValue = valueLabel;
        } else if (title.equals("PENDING")) {
            pendingValue = valueLabel;
        } else if (title.equals("COMPLETED")) {
            completedValue = valueLabel;
        }

        return card;
    }

    // ---------- ADD TASK ----------
    private void addTask() {
        String title = taskInput.getText().trim();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a task first.",
                    "Empty task",
                    JOptionPane.WARNING_MESSAGE
            );
            taskInput.requestFocus();
            return;
        }

        tasks.add(new Task(title, false));
        taskInput.setText("");

        saveTasks();
        refreshTasks();
        taskInput.requestFocus();
    }

    // ---------- REFRESH TASKS ----------
    private void refreshTasks() {
        if (taskListPanel == null) {
            return;
        }

        taskListPanel.removeAll();

        int total = tasks.size();
        int completed = 0;

        for (Task task : tasks) {
            if (task.completed) {
                completed++;
            }
        }

        int pending = total - completed;

        totalValue.setText(String.valueOf(total));
        pendingValue.setText(String.valueOf(pending));
        completedValue.setText(String.valueOf(completed));

        int percentage = total == 0
                ? 0
                : (int) Math.round(completed * 100.0 / total);

        progressBar.setValue(percentage);
        progressBar.setString(percentage + "%");
        progressLabel.setText(
                completed + " of " + total + " tasks completed"
        );

        int visibleTasks = 0;

        for (Task task : tasks) {
            if (currentFilter.equals("Pending") && task.completed) {
                continue;
            }

            if (currentFilter.equals("Completed") && !task.completed) {
                continue;
            }

            taskListPanel.add(createTaskCard(task));
            taskListPanel.add(Box.createVerticalStrut(9));
            visibleTasks++;
        }

        if (visibleTasks == 0) {
            JLabel emptyLabel = new JLabel(
                    tasks.isEmpty()
                            ? "No tasks yet. Add your first task above!"
                            : "No tasks in this category."
            );

            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            emptyLabel.setForeground(MUTED);
            emptyLabel.setBorder(new EmptyBorder(25, 5, 25, 5));
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            taskListPanel.add(emptyLabel);
        }

        taskListPanel.revalidate();
        taskListPanel.repaint();
    }

    // ---------- TASK CARD ----------
    private JPanel createTaskCard(Task task) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(CARD);
        card.setBorder(new EmptyBorder(13, 15, 13, 12));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox checkBox = new JCheckBox();
        checkBox.setSelected(task.completed);
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        checkBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel taskLabel = new JLabel(task.title);
        taskLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        taskLabel.setForeground(task.completed ? MUTED : TEXT);

        if (task.completed) {
            taskLabel.setText(
                    "<html><strike>" + escapeHtml(task.title)
                            + "</strike></html>"
            );
        }

        checkBox.addActionListener(e -> {
            task.completed = checkBox.isSelected();
            saveTasks();
            refreshTasks();
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.setFont(new Font("SansSerif", Font.PLAIN, 11));
        deleteButton.setForeground(new Color(220, 75, 75));
        deleteButton.setBackground(new Color(255, 242, 242));
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(new EmptyBorder(7, 10, 7, 10));
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        deleteButton.addActionListener(e -> {
            int answer = JOptionPane.showConfirmDialog(
                    this,
                    "Delete this task?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION
            );

            if (answer == JOptionPane.YES_OPTION) {
                tasks.remove(task);
                saveTasks();
                refreshTasks();
            }
        });

        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);
        left.add(checkBox, BorderLayout.WEST);
        left.add(taskLabel, BorderLayout.CENTER);

        card.add(left, BorderLayout.CENTER);
        card.add(deleteButton, BorderLayout.EAST);

        return card;
    }

    // ---------- ESCAPE HTML ----------
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    // ---------- SAVE TASKS ----------
    private void saveTasks() {
        try {
            Files.createDirectories(storageFile.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(
                    storageFile,
                    StandardCharsets.UTF_8
            )) {
                for (Task task : tasks) {
                    String encoded = Base64.getEncoder().encodeToString(
                            task.title.getBytes(StandardCharsets.UTF_8)
                    );

                    writer.write(
                            (task.completed ? "1" : "0") + "\t" + encoded
                    );
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not save tasks:\n" + e.getMessage(),
                    "Save error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ---------- LOAD TASKS ----------
    private void loadTasks() {
        if (!Files.exists(storageFile)) {
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(
                storageFile,
                StandardCharsets.UTF_8
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t", 2);

                if (parts.length != 2) {
                    continue;
                }

                try {
                    boolean completed = parts[0].equals("1");

                    String title = new String(
                            Base64.getDecoder().decode(parts[1]),
                            StandardCharsets.UTF_8
                    );

                    tasks.add(new Task(title, completed));

                } catch (IllegalArgumentException ignored) {
                    // Skip malformed entries.
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Could not load tasks:\n" + e.getMessage(),
                    "Load error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ---------- MAIN ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception ignored) {
                // Keep the default look and feel.
            }

            Main app = new Main();
            app.setVisible(true);
        });
    }
}