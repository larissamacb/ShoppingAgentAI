package com.unieuro.agents;

import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unieuro.services.IAIService;

import jadex.bridge.IInternalAccess;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

@Agent 
@ProvidedServices(@ProvidedService(type = IAIService.class)) 
public class AIHandlerAgent implements IAIService {

    @Agent
    protected IInternalAccess agent;

    private final Gson gson = new Gson();

    @AgentCreated
    public void init() {
        System.out.println("AIHandlerAgent (Micro) iniciado.");
    }


    @Override
    public IFuture<List<String>> getTags(String userDescription) {
        System.out.println("AIHandlerAgent (Micro): Recebido pedido para gerar tags para: " + userDescription);
        Future<List<String>> future = new Future<>(); 

        try {
            PythonExecutor.PythonResult tagsResult = PythonExecutor.execute("adapter_get_tags.py", userDescription);

            if (!tagsResult.isSuccess()) {
                System.err.println("AIHandlerAgent (Micro): Falha ao obter tags da IA: " + tagsResult.getOutput());
                throw new RuntimeException("Falha ao chamar IA para tags: " + tagsResult.getOutput());
            }

            String tagsJsonOutput = tagsResult.getOutput().trim();
            Map<String, List<String>> tagsMap = gson.fromJson(tagsJsonOutput, new TypeToken<Map<String, List<String>>>(){}.getType());
            List<String> tags = tagsMap.get("tags");

            System.out.println("AIHandlerAgent (Micro): Tags geradas com sucesso.");
            future.setResult(tags); 
        } catch (Exception e) {
            future.setException(e);
        }
        return future; 
    }

    @Override
    public IFuture<String> getFinalRecommendation(String userDescription, List<Map<String, Object>> allGamesData) {
        System.out.println("AIHandlerAgent (Micro): Recebido pedido para gerar recomendação final.");
        Future<String> future = new Future<>();

        try {
            String allGamesJson = gson.toJson(allGamesData);
            PythonExecutor.PythonResult finalRecResult = PythonExecutor.execute("adapter_final_recommendation.py", userDescription, allGamesJson);

            if (!finalRecResult.isSuccess()) {
                System.err.println("AIHandlerAgent (Micro): Falha ao gerar recomendação final: " + finalRecResult.getOutput());
                throw new RuntimeException("Falha ao chamar IA para recomendação: " + finalRecResult.getOutput());
            }

            String finalRecJsonOutput = finalRecResult.getOutput().trim();
            Map<String, String> finalRecMap = gson.fromJson(finalRecJsonOutput, new TypeToken<Map<String, String>>(){}.getType());
            String recomendacao = finalRecMap.getOrDefault("resumo", "Nao foi possivel gerar a recomendacao.");

            System.out.println("AIHandlerAgent (Micro): Recomendação final gerada.");
            future.setResult(recomendacao);
        } catch (Exception e) {
            future.setException(e);
        }
        return future;
    }
}