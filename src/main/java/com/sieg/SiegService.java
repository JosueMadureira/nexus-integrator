package com.sieg;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class SiegService {
    private static final Logger logger = LoggerFactory.getLogger(SiegService.class);
    private final Config config;
    private String currentToken;
    private LogListener listener;

    public SiegService(Config config, LogListener listener) {
        this.config = config;
        this.listener = listener;
        logToGui("SiegService (HttpURLConnection) inicializado", "info");
    }

    private void logToGui(String msg, String type) {
        if (listener != null)
            listener.onLog(msg, type);
        if ("erro".equals(type))
            logger.error(msg);
        else
            logger.info(msg);
    }

    private synchronized String getToken() {
        if (currentToken != null)
            return currentToken;

        HttpURLConnection conn = null;
        try {
            logToGui("Solicitando novo token JWT...", "info");

            URL url = new URL("https://api.sieg.com/api/v1/create-jwt");
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("X-Client-Id", config.getClientid());
            conn.setRequestProperty("X-Secret-Key", config.getSecret());
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            // Force Content-Length: 0 (required by Sieg API for empty POST)
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(0);

            int statusCode = conn.getResponseCode();

            if (statusCode == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                currentToken = response.toString().replace("\"", "").trim();
                logToGui("Token adquirido com sucesso.", "sucesso");
                return currentToken;
            } else {
                logToGui("Falha ao obter token. Status: " + statusCode, "erro");
                // Try to read error body
                try {
                    BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();
                    logger.error("Error response: " + errorResponse.toString());
                } catch (Exception e) {
                    // Ignore error reading error
                }
                return null;
            }
        } catch (Exception e) {
            logToGui("Erro ao obter token: " + e.getMessage(), "erro");
            logger.error("Exception details", e);
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public boolean sendXml(byte[] xmlContent, String filename) {
        logToGui("Testando envio API para: " + filename, "info");
        String token = getToken();
        if (token == null)
            return false;

        HttpURLConnection conn = null;
        try {
            String base64Xml = Base64.getEncoder().encodeToString(xmlContent);
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(Map.of("Xml", base64Xml));

            URL url = new URL("https://api.sieg.com/api/v1/send-xml");
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("X-API-Key", config.getApikey());
            conn.setRequestProperty("X-Client-Id", config.getClientid());
            conn.setRequestProperty("X-Secret-Key", config.getSecret());
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);

            // Write JSON body
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int statusCode = conn.getResponseCode();

            if (statusCode == 200 || statusCode == 409) {
                logToGui("XML enviado: " + filename, "sucesso");
                return true;
            } else if (statusCode == 401) {
                logToGui("Token expirado (401). Renovando...", "aviso");
                this.currentToken = null;
                token = getToken();
                if (token != null) {
                    // Retry once with new token
                    conn.disconnect();
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                    conn.setRequestProperty("X-API-Key", config.getApikey());
                    conn.setRequestProperty("X-Client-Id", config.getClientid());
                    conn.setRequestProperty("X-Secret-Key", config.getSecret());
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(30000);
                    conn.setReadTimeout(30000);
                    conn.setDoOutput(true);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                        os.write(input, 0, input.length);
                    }

                    statusCode = conn.getResponseCode();
                    if (statusCode == 200 || statusCode == 409) {
                        logToGui("XML enviado (retry): " + filename, "sucesso");
                        return true;
                    }
                }
            }

            // Read error response if available
            String errorBody = "";
            try {
                BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                errorBody = errorResponse.toString();
            } catch (Exception e) {
                // Ignore
            }

            logToGui("Falha Envio (" + statusCode + "): " + errorBody, "erro");
            return false;
        } catch (Exception e) {
            logToGui("Exceção envio XML: " + e.getMessage(), "erro");
            logger.error("Exception details", e);
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
