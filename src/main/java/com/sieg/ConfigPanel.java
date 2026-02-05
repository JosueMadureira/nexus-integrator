package com.sieg;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ConfigPanel extends JPanel {
    private JTextField txtHost;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JPasswordField txtApiKey;
    private JTextField txtClientId;
    private JPasswordField txtSecret;
    private JButton btnSave;

    public ConfigPanel() {
        setLayout(new GridBagLayout());
        // More padding
        setBorder(BorderFactory.createEmptyBorder(30, 80, 30, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Bigger gaps
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        int row = 0;

        addSectionTitle("Configuração de E-mail (IMAP)", row++, gbc);
        txtHost = addField("Servidor:", row++, false, gbc);
        txtUser = addField("E-mail:", row++, false, gbc);
        txtPass = (JPasswordField) addField("Senha:", row++, true, gbc);

        addSectionTitle("Integração API (Destino)", row++, gbc);
        txtApiKey = (JPasswordField) addField("API Key:", row++, true, gbc);
        txtClientId = (JTextField) addField("Client ID:", row++, false, gbc);
        txtSecret = (JPasswordField) addField("Secret Key:", row++, true, gbc);

        // Separator
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        add(Box.createVerticalStrut(20), gbc);

        btnSave = new JButton("SALVAR ALTERAÇÕES");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBackground(new Color(75, 110, 175)); // Nice Blue
        btnSave.setPreferredSize(new Dimension(250, 50));

        // Let FlatLaf handle rounding but set color
        btnSave.putClientProperty("JButton.buttonType", "roundRect");

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE; // Don't stretch button
        gbc.anchor = GridBagConstraints.CENTER;
        add(btnSave, gbc);
    }

    private void addSectionTitle(String title, int row, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(new Color(100, 149, 237)); // Cornflower Blue
        lbl.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        add(lbl, gbc);
        gbc.gridwidth = 1; // Reset
    }

    private JTextField addField(String label, int row, boolean isPassword, GridBagConstraints gbc) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        add(lbl, gbc);

        JTextField field = isPassword ? new JPasswordField() : new JTextField();
        field.setPreferredSize(new Dimension(300, 35)); // Taller inputs

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        add(field, gbc);

        return field;
    }

    public void setSaveAction(ActionListener action) {
        btnSave.addActionListener(action);
    }

    public Config getConfig() {
        Config c = new Config();
        c.setHost(txtHost.getText());
        c.setUser(txtUser.getText());
        c.setPass(new String(txtPass.getPassword()));
        c.setApikey(new String(txtApiKey.getPassword()));
        c.setClientid(txtClientId.getText());
        c.setSecret(new String(txtSecret.getPassword()));
        return c;
    }

    public void loadConfig(Config c) {
        if (c == null)
            return;
        txtHost.setText(c.getHost());
        txtUser.setText(c.getUser());
        txtPass.setText(c.getPass());
        txtApiKey.setText(c.getApikey());
        txtClientId.setText(c.getClientid());
        txtSecret.setText(c.getSecret());
    }
}
