package com.unieuro.agents;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.OnStart;
import jadex.micro.annotation.Agent;

@Agent // Anotação básica para um agente micro/simples
public class DummyAgent {

    @Agent
    protected IInternalAccess agent;

    @OnStart
    public void body() {
        System.out.println("--- Agente Dummy Iniciado com Sucesso! ---");
        System.out.println("ID do Agente: " + agent.getId());
        // O agente não faz mais nada e termina.
    }
}