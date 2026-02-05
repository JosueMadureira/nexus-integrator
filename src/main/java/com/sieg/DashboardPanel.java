package com.sieg;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DashboardPanel extends JPanel {
    private JLabel lblStatus;
    private JLabel lblNfeCount;
    private JLabel lblNfceCount;
    private JTextArea consoleArea;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public DashboardPanel() {
        setLayout(new BorderLayout(20, 20)); // More spacing
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Top Status
        JPanel topPanel = new JPanel(new BorderLayout());
        lblStatus = new JLabel("● Aguardando Configuração", SwingConstants.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblStatus.setForeground(Color.GRAY);
        topPanel.add(lblStatus, BorderLayout.CENTER);

        // Cards Container
        JPanel cardsPanel = new JPanel(new GridLayout(1, 2, 40, 0)); // Big gap between cards
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        lblNfeCount = createModernCard(cardsPanel, "NF-e Enviadas", new Color(0, 150, 136)); // Teal
        lblNfceCount = createModernCard(cardsPanel, "NFC-e Enviadas", new Color(255, 143, 0)); // Orange

        topPanel.add(cardsPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        // Console with rounded border via ScrollPane
        consoleArea = new JTextArea();
        consoleArea.setEditable(false);
        consoleArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        consoleArea.setBackground(new Color(30, 30, 30)); // Darker bg
        consoleArea.setForeground(new Color(200, 200, 200));

        JScrollPane scrollPane = new JScrollPane(consoleArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
                "Nexus Console (Tempo Real)"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JLabel createModernCard(JPanel parent, String title, Color accentColor) {
        JPanel card = new RoundedPanel(15, new Color(45, 48, 55)); // Slightly lighter than background
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(200, 120));

        // Strip/Indicator on left
        JPanel strip = new RoundedPanel(15, accentColor);
        strip.setPreferredSize(new Dimension(8, 100));
        // We mask the rounding on right of strip effectively by just placing it,
        // simplified for standard JPanel
        // Actually lets just use border for accent

        JLabel lblTitle = new JLabel(title.toUpperCase(), SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(180, 180, 180));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        JLabel lblValue = new JLabel("0", SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 48));
        lblValue.setForeground(accentColor);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);

        parent.add(card);
        return lblValue;
    }

    public void appendLog(String msg, String type) {
        String icon = "";
        switch (type) {
            case "sucesso":
                icon = "✅ ";
                break;
            case "erro":
                icon = "❌ ";
                break;
            case "busca":
                icon = "🔍 ";
                break;
            case "info":
                icon = "ℹ️ ";
                break;
            default:
                icon = "  ";
                break;
        }
        String time = timeFormat.format(new Date());
        consoleArea.append("[" + time + "] " + icon + msg + "\n");
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
    }

    public void updateStats(int nfe, int nfce) {
        lblNfeCount.setText(String.valueOf(nfe));
        lblNfceCount.setText(String.valueOf(nfce));
    }

    public void updateStatus(String status, boolean active) {
        lblStatus.setText(status);
        lblStatus.setForeground(active ? new Color(76, 175, 80) : Color.GRAY);
    }

    // Helper for Rounded JPanel
    private static class RoundedPanel extends JPanel {
        private int radius;
        private Color bgColor;

        public RoundedPanel(int radius, Color bgColor) {
            this.radius = radius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            // Subtle border
            g2.setColor(bgColor.brighter().brighter());
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
        }
    }
}
