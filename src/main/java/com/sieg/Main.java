package com.sieg;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String APP_DATA = System.getenv("APPDATA");
    private static final String APP_DIR = "NexusIntegrator";
    private static final String CONFIG_FILE = "config_sieg.json";

    public static void main(String[] args) {
        logger.info("Iniciando Sistema Nexus (Java)...");

        File configPath = Paths.get(APP_DATA, APP_DIR, CONFIG_FILE).toFile();
        if (!configPath.exists()) {
            logger.error("Arquivo de configuração não encontrado em: " + configPath.getAbsolutePath());
            logger.info("Por favor, crie o arquivo de configuração e reinicie.");
            logger.info("Pressione ENTER para sair...");
            new java.util.Scanner(System.in).nextLine();
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            Config config = mapper.readValue(configPath, Config.class);
            logger.info("Configuração carregada para o usuário: {}", config.getUser());

            SiegService siegService = new SiegService(config, null); // Null listener for console mode
            EmailProcessor emailProcessor = new EmailProcessor(config, siegService);

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

            // Rodar imediatamente e depois a cada 600 segundos (10 minutos)
            logger.info("Iniciando loop de monitoramento (Intervalo: 10 minutos)...");
            scheduler.scheduleWithFixedDelay(() -> {
                try {
                    logger.info("Iniciando ciclo de sincronização...");
                    emailProcessor.processEmails();
                    logger.info("Ciclo de sincronização finalizado.");
                } catch (Exception e) {
                    logger.error("Erro no ciclo de sincronização", e);
                }
            }, 0, 600, TimeUnit.SECONDS);

            // Adicionar hook de desligamento
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Desligando...");
                scheduler.shutdown();
            }));

        } catch (Exception e) {
            logger.error("Erro fatal ao iniciar aplicação", e);
            logger.info("Pressione ENTER para sair...");
            new java.util.Scanner(System.in).nextLine();
        }
    }
}
