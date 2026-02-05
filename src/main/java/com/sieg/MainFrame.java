package com.sieg;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainFrame extends JFrame implements LogListener {
    private static final String APP_DATA = System.getenv("APPDATA");
    private static final String APP_DIR = "NexusIntegrator";
    private static final String CONFIG_FILE = "config_sieg.json";

    private final DashboardPanel dashboard;
    private final ConfigPanel configPanel;
    private ScheduledExecutorService scheduler;
    private EmailProcessor emailProcessor;
    private Config currentConfig;

    public MainFrame() {
        setTitle("Nexus Integrator");
        setSize(650, 680);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Custom close handler
        setLocationRelativeTo(null);

        // Carregar ícone para taskbar e system tray
        try {
            java.net.URL iconURL = getClass().getResource("/icon.png");
            if (iconURL != null) {
                Image icon = Toolkit.getDefaultToolkit().getImage(iconURL);
                setIconImage(icon);
            }
        } catch (Exception e) {
            System.err.println("Não foi possível carregar o ícone: " + e.getMessage());
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        dashboard = new DashboardPanel();
        configPanel = new ConfigPanel();

        tabbedPane.addTab("Painel de Controle", dashboard);
        tabbedPane.addTab("Configurações", configPanel);

        add(tabbedPane);

        // Logic
        configPanel.setSaveAction(e -> saveConfig());
        loadConfig();

        // System Tray
        setupTray();

        // Start if config exists
        if (currentConfig != null && currentConfig.getClientid() != null && !currentConfig.getClientid().isEmpty()) {
            startService();
        }

        // Window Listener for Minimize to Tray
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (SystemTray.isSupported()) {
                    setVisible(false); // Hide window
                } else {
                    System.exit(0); // Exit if no tray
                }
            }
        });
    }

    private void loadConfig() {
        try {
            File configFile = Paths.get(APP_DATA, APP_DIR, CONFIG_FILE).toFile();
            if (configFile.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                currentConfig = mapper.readValue(configFile, Config.class);
                configPanel.loadConfig(currentConfig);
            }
        } catch (Exception e) {
            dashboard.appendLog("Erro ao carregar config: " + e.getMessage(), "erro");
        }
    }

    private void saveConfig() {
        try {
            Config newConfig = configPanel.getConfig();
            File configDir = Paths.get(APP_DATA, APP_DIR).toFile();
            if (!configDir.exists())
                configDir.mkdirs();

            File configFile = Paths.get(APP_DATA, APP_DIR, CONFIG_FILE).toFile();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(configFile, newConfig);

            JOptionPane.showMessageDialog(this, "Configurações Salvas! O serviço será reiniciado.");
            currentConfig = newConfig;
            startService();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveConfigSilent() {
        try {
            File configDir = Paths.get(APP_DATA, APP_DIR).toFile();
            if (!configDir.exists())
                configDir.mkdirs();

            File configFile = Paths.get(APP_DATA, APP_DIR, CONFIG_FILE).toFile();
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(configFile, currentConfig);
        } catch (Exception e) {
            System.err.println("Erro ao salvar config silenciosamente: " + e.getMessage());
        }
    }

    private void startService() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }

        dashboard.updateStatus("● Iniciando...", false);
        // Atualizar dashboard com contadores salvos
        if (currentConfig != null) {
            dashboard.updateStats(currentConfig.getNfeCount(), currentConfig.getNfceCount());
        }

        SiegService siegService = new SiegService(currentConfig, this);
        emailProcessor = new EmailProcessor(currentConfig, siegService);
        emailProcessor.setListener(this);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                emailProcessor.processEmails();
            } catch (Exception e) {
                onLog("Erro fatal no loop: " + e.getMessage(), "erro");
            }
        }, 0, 600, TimeUnit.SECONDS); // 10 min
    }

    private void setupTray() {
        if (!SystemTray.isSupported())
            return;
        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Carregar ícone do system tray
            Image image;
            try {
                java.net.URL iconURL = getClass().getResource("/icon.png");
                if (iconURL != null) {
                    image = Toolkit.getDefaultToolkit().getImage(iconURL);
                } else {
                    // Fallback
                    image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                }
            } catch (Exception ex) {
                image = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            }

            PopupMenu popup = new PopupMenu();
            MenuItem openItem = new MenuItem("Abrir");
            openItem.addActionListener(e -> {
                setVisible(true);
                setExtendedState(JFrame.NORMAL);
            });
            MenuItem exitItem = new MenuItem("Sair");
            exitItem.addActionListener(e -> {
                System.exit(0);
            });

            popup.add(openItem);
            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, "Nexus Integrator", popup);
            trayIcon.setImageAutoSize(true);
            trayIcon.addActionListener(e -> {
                setVisible(true);
                setExtendedState(JFrame.NORMAL);
            });

            tray.add(trayIcon);
        } catch (Exception e) {
            System.err.println("Tray error: " + e.getMessage());
        }
    }

    @Override
    public void onLog(String message, String type) {
        SwingUtilities.invokeLater(() -> dashboard.appendLog(message, type));
    }

    @Override
    public void onStatsUpdate(int nfe, int nfce) {
        SwingUtilities.invokeLater(() -> dashboard.updateStats(nfe, nfce));
        saveConfigSilent();
    }

    @Override
    public void onStatusUpdate(String status, boolean active) {
        SwingUtilities.invokeLater(() -> dashboard.updateStatus(status, active));
    }
}
