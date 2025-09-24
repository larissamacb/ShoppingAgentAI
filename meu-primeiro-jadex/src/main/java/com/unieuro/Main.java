// Arquivo: src/com/unieuro/Main.java
package com.unieuro;

import com.unieuro.agents.ScraperAgent;

import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;

public class Main {
    public static void main(String[] args) {
        IPlatformConfiguration config = PlatformConfigurationHandler.getMinimal();
        // ✨ MUDANÇA: Adiciona o ScraperAgent para ser iniciado
        config.addComponent(ScraperAgent.class); 
        Starter.createPlatform(config).get();
    }
}