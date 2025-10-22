package com.unieuro.main;

import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;
// REMOVIDO: import jadex.base.service.ISettingsService; // Não precisamos mais

import com.unieuro.agents.AIHandlerAgent;
import com.unieuro.agents.CoordinatorAgent;
import com.unieuro.agents.ScraperAgent;

public class Main {

    public static void main(String[] args) {
        // 1. Obtém a configuração mínima (sem GUI)
        IPlatformConfiguration config = PlatformConfigurationHandler.getDefaultNoGui();

        // REMOVIDO: Linhas que usavam ISettingsService
        // config.getService(ISettingsService.class).setProperty("kernel.shell.enabled", "false");

        // 2. Adiciona os nossos agentes (Micro Agents)
        config.addComponent(AIHandlerAgent.class);
        config.addComponent(ScraperAgent.class);
        config.addComponent(CoordinatorAgent.class);

        // 3. Inicia a plataforma
        try {
            Starter.createPlatform(config).get();
            // System.out.println("Plataforma Jadex iniciada..."); // Mensagem opcional
        } catch (Exception e) {
            System.err.println("Erro ao iniciar a plataforma Jadex:");
            e.printStackTrace();
        }
    }
}