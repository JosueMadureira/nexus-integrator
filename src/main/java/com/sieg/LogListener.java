package com.sieg;

public interface LogListener {
    void onLog(String message, String type); // type: sucesso, erro, busca, info

    void onStatsUpdate(int nfe, int nfce);

    void onStatusUpdate(String status, boolean active);
}
