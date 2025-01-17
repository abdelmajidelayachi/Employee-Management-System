package io.hahnsoftware.emp.service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DashboardPanel extends JPanel {
    private static final Color MAIN_COLOR = new Color(57, 145, 169); // #3991a9
    private static final Color SECONDARY_COLOR = new Color(245, 245, 245);
    private static final Color HOVER_COLOR = new Color(41, 128, 152);
    
    private JTable dataTable;
    private JLabel statusLabel;
    private Timer refreshTimer;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Initialize components
        createHeader();
        createSidebar();
        createMainContent();
        createStatusBar();
        setupRefreshTimer();
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(MAIN_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Add title
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Add header buttons
        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        headerButtons.setOpaque(false);
        
        String[] buttonLabels = {"Refresh", "Settings", "Profile"};
        for (String label : buttonLabels) {
            JButton button = createStyledButton(label);
            headerButtons.add(button);
        }
        
        headerPanel.add(headerButtons, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createSidebar() {
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(SECONDARY_COLOR);
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        String[] menuItems = {"Overview", "Analytics", "Reports", "Users", "Settings"};
        for (String item : menuItems) {
            JButton menuButton = createMenuButton(item);
            sidebarPanel.add(menuButton);
            sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        add(sidebarPanel, BorderLayout.WEST);
    }

    private void createMainContent() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create cards panel for quick stats
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        cardsPanel.setOpaque(false);
        
        String[][] cardData = {
            {"Total Users", "1,234", "↑ 12%"},
            {"Active Sessions", "456", "↑ 8%"},
            {"System Status", "Healthy", "100% Uptime"}
        };
        
        for (String[] data : cardData) {
            cardsPanel.add(createStatsCard(data[0], data[1], data[2]));
        }
        
        mainPanel.add(cardsPanel, BorderLayout.NORTH);

        // Create table
        createDataTable();
        JScrollPane scrollPane = new JScrollPane(dataTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(SECONDARY_COLOR));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void createDataTable() {
        String[] columns = {"ID", "Name", "Status", "Last Active", "Actions"};
        Object[][] data = {
            {"001", "John Doe", "Online", "Just now", "View"},
            {"002", "Jane Smith", "Offline", "5m ago", "View"},
            {"003", "Bob Johnson", "Online", "2m ago", "View"},
            {"004", "Alice Brown", "Away", "15m ago", "View"}
        };

        DefaultTableModel model = new DefaultTableModel(data, columns) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only make Actions column editable
            }
        };

        dataTable = new JTable(model);
        dataTable.setRowHeight(35);
        dataTable.getTableHeader().setBackground(MAIN_COLOR);
        dataTable.getTableHeader().setForeground(Color.WHITE);
        dataTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        dataTable.setSelectionBackground(new Color(232, 240, 254));
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(MAIN_COLOR);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(HOVER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(MAIN_COLOR);
            }
        });
        
        return button;
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setBackground(SECONDARY_COLOR);
        button.setForeground(Color.DARK_GRAY);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(230, 230, 230));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(SECONDARY_COLOR);
            }
        });

        return button;
    }

    private JPanel createStatsCard(String title, String value, String trend) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECONDARY_COLOR),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.GRAY);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(MAIN_COLOR);

        JLabel trendLabel = new JLabel(trend);
        trendLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        trendLabel.setForeground(new Color(39, 174, 96));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(trendLabel, BorderLayout.SOUTH);

        return card;
    }

    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(SECONDARY_COLOR);
        statusPanel.setPreferredSize(new Dimension(0, 25));
        statusPanel.setBorder(new EmptyBorder(5, 15, 5, 15));

        statusLabel = new JLabel("System Status: Online | Last Updated: Just now");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusPanel.add(statusLabel, BorderLayout.WEST);

        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupRefreshTimer() {
        refreshTimer = new Timer(30000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Simulate data refresh
                statusLabel.setText("System Status: Online | Last Updated: Just now");
            }
        });
        refreshTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dashboard Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new DashboardPanel());
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}