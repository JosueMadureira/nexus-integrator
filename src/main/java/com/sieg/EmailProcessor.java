package com.sieg;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import jakarta.mail.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class EmailProcessor {
    private static final Logger logger = LoggerFactory.getLogger(EmailProcessor.class);
    private final Config config;
    private final SiegService siegService;
    private LogListener listener;
    private int processedNfe = 0;
    private int processedNfce = 0;

    public EmailProcessor(Config config, SiegService siegService) {
        this.config = config;
        this.siegService = siegService;
        this.processedNfe = config.getNfeCount();
        this.processedNfce = config.getNfceCount();
    }

    public void setListener(LogListener listener) {
        this.listener = listener;
    }

    private void logToGui(String msg, String type) {
        if (listener != null)
            listener.onLog(msg, type);
        logger.info("[{}] {}", type, msg);
    }

    public void processEmails() {
        logToGui("Verificando e-mails...", "busca");
        if (listener != null)
            listener.onStatusUpdate("● Sincronizando...", false);

        Properties props = new Properties();
        props.put("mail.store.protocol", "imap");
        props.put("mail.imap.host", config.getHost());
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.port", "993");

        Session session = Session.getInstance(props);

        try (Store store = session.getStore("imap")) {
            store.connect(config.getHost(), config.getUser(), config.getPass());

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Folder destFolder = store.getFolder("INBOX.Notas Importadas");
            if (!destFolder.exists()) {
                destFolder.create(Folder.HOLDS_MESSAGES);
            }

            Message[] messages = inbox.getMessages();
            logger.info("Encontradas {} mensagens na Caixa de Entrada", messages.length);

            if (listener != null)
                listener.onStatusUpdate("● Monitoramento Ativo", true);

            int totalProcessado = 0;
            for (Message message : messages) {
                try {
                    boolean success = processMessage(message);
                    if (success) {
                        // Tentar mover com retry em caso de falha de conexão
                        boolean moved = false;
                        int retries = 3;

                        for (int attempt = 1; attempt <= retries && !moved; attempt++) {
                            try {
                                // Verificar se a conexão ainda está ativa
                                if (!store.isConnected()) {
                                    logToGui("Reconectando ao servidor...", "info");
                                    store.connect(config.getHost(), config.getUser(), config.getPass());
                                    inbox = store.getFolder("INBOX");
                                    inbox.open(Folder.READ_WRITE);
                                    destFolder = store.getFolder("INBOX.Notas Importadas");
                                    if (!destFolder.exists()) {
                                        destFolder.create(Folder.HOLDS_MESSAGES);
                                    }
                                }

                                // Copiar para destino
                                inbox.copyMessages(new Message[] { message }, destFolder);
                                // Marcar para exclusão
                                message.setFlag(Flags.Flag.DELETED, true);
                                logToGui("✅ E-mail movido para 'Notas Importadas'", "info");
                                moved = true;
                                totalProcessado++;
                            } catch (MessagingException e) {
                                if (attempt < retries) {
                                    logToGui("⚠ Tentativa " + attempt + " falhou, tentando novamente...", "aviso");
                                    Thread.sleep(1000); // Aguarda 1 segundo antes de tentar novamente
                                } else {
                                    logToGui("❌ Falha ao mover e-mail após " + retries + " tentativas: "
                                            + e.getMessage(), "erro");
                                    logger.error("Falha definitiva ao mover e-mail", e);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    logToGui("Erro processando mensagem: " + e.getMessage(), "erro");
                }
            }

            if (totalProcessado == 0) {
                logToGui("ℹ️ Nenhum documento novo encontrado.", "info");
            } else {
                logToGui("Total de " + totalProcessado + " documento(s) processado(s).", "sucesso");
            }

            inbox.close(true); // Expurga mensagens deletadas
            store.close();
        } catch (Exception e) {
            logToGui("Erro IMAP: " + e.getMessage(), "erro");
        }
    }

    private boolean processMessage(Message message) throws Exception {
        boolean anyXmlProcessed = false;
        Object content = message.getContent();

        // Debug content type
        // logToGui("Tipo de mensagem: " + message.getContentType(), "info");

        if (content instanceof Multipart) {
            anyXmlProcessed = processMultipart((Multipart) content);
        } else if (content instanceof String) {
            // Handle simple text/html messages
            String textContent = (String) content;
            if (processLinksInContent(textContent)) {
                anyXmlProcessed = true;
            }
        }

        return anyXmlProcessed;
    }

    private boolean processMultipart(Multipart multipart) throws Exception {
        boolean success = false;
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);

            if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) ||
                    (bodyPart.getFileName() != null && !bodyPart.getFileName().isEmpty())) {

                String fileName = bodyPart.getFileName();
                if (fileName == null)
                    continue;
                fileName = fileName.toLowerCase();

                try (InputStream is = bodyPart.getInputStream()) {
                    byte[] data = is.readAllBytes();

                    if (fileName.endsWith(".xml")) {
                        if (processXml(data, fileName))
                            success = true;
                    } else if (fileName.endsWith(".zip")) {
                        if (processZip(data))
                            success = true;
                    } else if (fileName.endsWith(".rar")) {
                        if (processRar(data))
                            success = true;
                    }
                }
            } else if (bodyPart.getContent() instanceof Multipart) {
                // Recursive multipart (e.g. apple mail sometimes does this)
                if (processMultipart((Multipart) bodyPart.getContent()))
                    success = true;
            } else if (bodyPart.isMimeType("text/html") || bodyPart.isMimeType("text/plain")) {
                // Check for links in body if no attachment processed yet (or always scan)
                String content = "";
                try {
                    content = bodyPart.getContent().toString();
                } catch (Exception e) {
                    // streaming issue sometimes
                    content = new String(bodyPart.getInputStream().readAllBytes());
                }

                if (processLinksInContent(content))
                    success = true;
            }
        }
        return success;
    }

    private boolean processLinksInContent(String content) {
        // Use Jsoup to parse HTML
        org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(content);
        org.jsoup.select.Elements links = doc.select("a[href]");

        boolean anyLinkSuccess = false;

        if (links.size() > 0) {
            logToGui("Encontrados " + links.size() + " links (Jsoup).", "info");
        } else {
            logToGui("Jsoup não encontrou links. Tentando Regex...", "info");
        }

        // 1. Attempt Jsoup Links
        for (org.jsoup.nodes.Element link : links) {
            if (processSingleLink(link.attr("href"), link.text()))
                anyLinkSuccess = true;
        }

        // 2. Fallback: Regex on raw content (catches plain text URLs or malformed HTML)
        if (!anyLinkSuccess) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile("https?://[^\\s\"<>]+");
            java.util.regex.Matcher m = p.matcher(content);
            while (m.find()) {
                String url = m.group();
                // Avoid re-processing if Jsoup already caught it (simple check might be needed,
                // or just re-try is harmless if check is fast)
                if (processSingleLink(url, "Regex Match"))
                    anyLinkSuccess = true;
            }
        }

        return anyLinkSuccess;
    }

    private boolean processSingleLink(String url, String text) {
        // Clean URL (handle HTML entities like &amp;)
        url = cleanUrl(url);
        String lowerUrl = url.toLowerCase();
        String lowerText = text.toLowerCase();

        boolean isFiscalFlow = lowerUrl.contains("rdsv1.net") || lowerUrl.contains("fiscalflow")
                || lowerUrl.contains("linx");
        // Robust zip check (handles query params)
        boolean isZip = lowerUrl.endsWith(".zip") || lowerUrl.contains(".zip?") || lowerUrl.contains(".zip&");
        boolean isDownloadText = lowerText.contains("download") || lowerText.contains("baixar")
                || lowerText.contains("acessado aqui") || lowerText.contains("clique aqui")
                || lowerText.contains("xml");

        // Log specific candidates for debugging
        if (lowerUrl.contains("fiscalflow") || lowerUrl.contains("linx")) {
            logToGui("Candidato FiscalFlow encontrado: "
                    + (url.length() > 50 ? "..." + url.substring(url.length() - 50) : url), "info");
        }

        if (isFiscalFlow || isZip || (isDownloadText && !lowerUrl.contains("unsubscribe")
                && !lowerUrl.contains("facebook") && !lowerUrl.contains("twitter"))) {
            try {
                logToGui("Baixando: " + url, "busca");
                byte[] data = downloadFile(url);
                if (data != null && data.length > 0) {
                    try {
                        if (processZip(data)) {
                            logToGui("ZIP processado via Link!", "sucesso");
                            return true;
                        } else if (processXml(data, "download_link.xml")) {
                            logToGui("XML processado via Link!", "sucesso");
                            return true;
                        } else {
                            logToGui("Arquivo baixado, mas não é ZIP/XML válido.", "erro");
                        }
                    } catch (Exception e) {
                        logToGui("Falha ao processar arquivo baixado: " + e.getMessage(), "erro");
                    }
                }
            } catch (Exception e) {
                logToGui("Erro download: " + e.getMessage(), "erro");
            }
        }
        return false;
    }

    private String cleanUrl(String url) {
        // Basic entity decoding for common email cases
        if (url == null)
            return null;
        return url.replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("%20", " "); // rare in http urls but possible
    }

    private byte[] downloadFile(String url) throws Exception {
        java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                .build();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("User-Agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .GET()
                .build();
        java.net.http.HttpResponse<byte[]> response = client.send(request,
                java.net.http.HttpResponse.BodyHandlers.ofByteArray());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            logToGui("HTTP Error " + response.statusCode() + " para: " + url, "erro");
        }
        return null;
    }

    private boolean processXml(byte[] data, String filename) {

        // Basic validation
        String content = new String(data, StandardCharsets.UTF_8).trim();
        if (content.startsWith("<?xml") || content.startsWith("<nfe") || content.startsWith("<NFe")) {
            boolean sent = siegService.sendXml(data, filename);
            if (sent) {
                logToGui("XML Enviado: " + filename, "sucesso");
                // Simple logic to guess type needed for dashboard (simulated based on python
                // script)
                // Or we could parse XML properly, but for speed let's just increment generally
                // The python script checked for <mod>55</mod>
                if (new String(data).contains("<mod>55</mod>")) {
                    processedNfe++;
                    config.setNfeCount(processedNfe);
                } else {
                    processedNfce++;
                    config.setNfceCount(processedNfce);
                }
                if (listener != null)
                    listener.onStatsUpdate(processedNfe, processedNfce);
            } else {
                logToGui("Falha ao enviar XML: " + filename, "erro");
            }
            return sent;
        }
        return false;
    }

    private boolean processZip(byte[] data) {
        boolean anySuccess = false;
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(data))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory())
                    continue;

                String name = entry.getName().toLowerCase();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
                byte[] entryData = baos.toByteArray();

                if (name.endsWith(".xml")) {
                    if (processXml(entryData, entry.getName()))
                        anySuccess = true;
                } else if (name.endsWith(".zip")) {
                    if (processZip(entryData))
                        anySuccess = true;
                }
                // Nested RAR inside ZIP is possible
                else if (name.endsWith(".rar")) {
                    if (processRar(entryData))
                        anySuccess = true;
                }
            }
        } catch (IOException e) {
            logToGui("Erro processando ZIP: " + e.getMessage(), "erro");
        }
        return anySuccess;
    }

    private boolean processRar(byte[] data) {
        boolean anySuccess = false;
        try (Archive archive = new Archive(new ByteArrayInputStream(data))) {
            FileHeader fileHeader;
            while ((fileHeader = archive.nextFileHeader()) != null) {
                if (fileHeader.isDirectory())
                    continue;
                String name = fileHeader.getFileName().toLowerCase();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                archive.extractFile(fileHeader, baos);
                byte[] entryData = baos.toByteArray();

                if (name.endsWith(".xml")) {
                    if (processXml(entryData, fileHeader.getFileName()))
                        anySuccess = true;
                } else if (name.endsWith(".zip")) {
                    if (processZip(entryData))
                        anySuccess = true;
                } else if (name.endsWith(".rar")) {
                    // Recursive RAR might be tricky with Junrar stream, but let's try if needed.
                    // For now, assume 1 level of RAR or RAR inside ZIP.
                    if (processRar(entryData))
                        anySuccess = true;
                }
            }
        } catch (Exception e) {
            logToGui("Erro processando RAR: " + e.getMessage(), "erro");
        }
        return anySuccess;
    }
}
