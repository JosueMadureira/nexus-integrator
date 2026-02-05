package com.sieg;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public class App {
    public static void main(String[] args) {
        try {
            System.out.println("Iniciando Nexus GUI...");
            // Setup Theme
            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());

                // Modern UI Props
                UIManager.put("Button.arc", 12);
                UIManager.put("Component.arc", 12);
                UIManager.put("TextComponent.arc", 12);
                UIManager.put("ProgressBar.arc", 12);
                UIManager.put("TabbedPane.showTabSeparators", true);
                UIManager.put("TabbedPane.selectedBackground", new java.awt.Color(75, 110, 175));

                // Accent Color (Blue/Purpleish)
                // FlatLaf automatically handles accent color for focus etc if we set it right,
                // but explicit component styling is often better.
            } catch (Exception e) {
                System.err.println("Falha ao carregar tema FlatLaf: " + e.getMessage());
                // Fallback to system
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }

            // Start UI
            SwingUtilities.invokeLater(() -> {
                try {
                    MainFrame frame = new MainFrame();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Erro na GUI: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERRO FATAL: " + e.getMessage());
            System.out.println("Pressione ENTER para sair...");
            new java.util.Scanner(System.in).nextLine();
        }
    }
}
