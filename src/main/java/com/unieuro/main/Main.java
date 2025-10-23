package com.unieuro.main;

import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;

import com.unieuro.agents.AIHandlerAgent;
import com.unieuro.agents.CoordinatorAgent;
import com.unieuro.agents.ScraperAgent;
import com.unieuro.util.ConsoleTidy;

public class Main {

    public static void main(String[] args) {
        ConsoleTidy.install();

        IPlatformConfiguration config = PlatformConfigurationHandler.getDefaultNoGui();

        config.addComponent(AIHandlerAgent.class);
        config.addComponent(ScraperAgent.class);
        config.addComponent(CoordinatorAgent.class);

        try {
            Starter.createPlatform(config).get();
        } catch (Exception e) {
            System.err.println("[Main] Erro ao iniciar a plataforma Jadex: " + e);
        }
    }
}
