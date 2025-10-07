package com.unieuro.agents;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import jadex.base.IPlatformConfiguration;
import jadex.base.PlatformConfigurationHandler;
import jadex.base.Starter;
import jadex.bridge.IInternalAccess;
import jadex.bridge.service.annotation.OnStart;
import jadex.micro.annotation.Agent;

@Agent
public class RecommenderAgent {
    
    @OnStart
    void run(IInternalAccess agent) {
        System.out.println("--- INICIANDO ORQUESTRAÇÃO COMPLETA ---");
        
        Gson gson = new Gson();
        String userDescription = "um jogo de fantasia com dragões e mundo aberto";
        int numGames = 2; // Analisando 2 jogos para ser mais rápido

        // --- PASSO 1: Obter Specs do PC ---
        System.out.println("Passo 1: Obtendo especificações do PC...");
        PythonExecutor.PythonResult pcSpecsResult = PythonExecutor.execute("adapters/adapter_get_pc_specs.py");
        
        if (!pcSpecsResult.isSuccess() || pcSpecsResult.getOutput().contains("erro")) {
            System.err.println("Falha crítica ao obter specs do PC: " + pcSpecsResult.getOutput());
            return;
        }
        String pcSpecsJson = pcSpecsResult.getOutput();
        System.out.println("PC Specs obtidas: " + pcSpecsJson);

        // --- PASSO 2: Gerando tags com a IA ---
        System.out.println("\nPasso 2: Gerando tags com a IA...");
        PythonExecutor.PythonResult tagsResult = PythonExecutor.execute("adapters/adapter_get_tags.py", userDescription);

        if (!tagsResult.isSuccess()) {
            System.err.println("Falha ao obter tags da IA: " + tagsResult.getOutput());
            return;
        }
        Map<String, List<String>> tagsMap = gson.fromJson(tagsResult.getOutput(), new TypeToken<Map<String, List<String>>>(){}.getType());
        List<String> tags = tagsMap.get("tags");
        System.out.println("Tags recebidas da IA: " + tags);

        // --- PASSO 3: Buscando URLs dos Jogos ---
        System.out.println("\nPasso 3: Buscando URLs dos jogos na Steam...");
        String tagsJsonForArg = gson.toJson(tags);
        PythonExecutor.PythonResult urlsResult = PythonExecutor.execute("adapters/adapter_get_game_urls.py", tagsJsonForArg, String.valueOf(numGames));

        if(!urlsResult.isSuccess() || urlsResult.getOutput().contains("erro")) {
            System.err.println("Falha ao obter URLs dos jogos: " + urlsResult.getOutput());
            return;
        }
        Map<String, List<String>> urlsMap = gson.fromJson(urlsResult.getOutput(), new TypeToken<Map<String, List<String>>>(){}.getType());
        List<String> gameUrls = urlsMap.get("urls");
        System.out.println(gameUrls.size() + " URLs de jogos encontradas.");

        // --- PASSO 4: Loop para Analisar Cada Jogo ---
        System.out.println("\nPasso 4: Analisando cada jogo individualmente...");
        List<Map<String, Object>> allGamesData = new ArrayList<>();

        for(String url : gameUrls) {
            System.out.println("\n------------------------------------------");
            System.out.println("Analisando URL: " + url);

            PythonExecutor.PythonResult detailsResult = PythonExecutor.execute("adapters/adapter_scrape_details.py", url);
            if(!detailsResult.isSuccess()) {
                System.err.println("Falha ao obter detalhes para " + url + ". Pulando.");
                continue;
            }
            Map<String, Object> gameDetails = gson.fromJson(detailsResult.getOutput(), new TypeToken<Map<String, Object>>(){}.getType());
            System.out.println("Detalhes coletados para: " + gameDetails.get("name"));
            
            String minReq = (String) gameDetails.getOrDefault("min_req", "N/A");
            String recReq = (String) gameDetails.getOrDefault("rec_req", "N/A");
            PythonExecutor.PythonResult reqsResult = PythonExecutor.execute("adapters/adapter_check_reqs.py", pcSpecsJson, minReq, recReq);
            
            Map<String, String> reqsMap = gson.fromJson(reqsResult.getOutput(), new TypeToken<Map<String, String>>(){}.getType());
            String pcRoda = reqsMap.get("resultado");
            System.out.println("Análise de compatibilidade: " + pcRoda);
            
            Map<String, Object> consolidatedData = new HashMap<>();
            consolidatedData.put("Nome", gameDetails.get("name"));
            consolidatedData.put("Preço", gameDetails.get("price"));
            consolidatedData.put("User Score", gameDetails.get("user_score"));
            consolidatedData.put("PC Roda?", pcRoda);
            allGamesData.add(consolidatedData);
        }

        // --- PASSO 5: GERAR RECOMENDAÇÃO FINAL ---
        System.out.println("\n------------------------------------------");
        System.out.println("\nPasso 5: Gerando recomendação final com a IA...");
        if(!allGamesData.isEmpty()) {
            String allGamesJson = gson.toJson(allGamesData);
            PythonExecutor.PythonResult finalRecResult = PythonExecutor.execute("adapters/adapter_final_recommendation.py", userDescription, allGamesJson);

            if(finalRecResult.isSuccess()) {
                Map<String, String> finalRecMap = gson.fromJson(finalRecResult.getOutput(), new TypeToken<Map<String, String>>(){}.getType());
                String recomendacao = finalRecMap.get("resumo");
                
                System.out.println("\n========== RECOMENDAÇÃO DA IA ==========");
                System.out.println(recomendacao);
                System.out.println("==========================================");
            } else {
                System.err.println("Falha ao gerar recomendação final: " + finalRecResult.getOutput());
            }
        }

        System.out.println("\n--- ORQUESTRAÇÃO COMPLETA FINALIZADA ---");
    }

    public static void main(String[] args) {
        String specsFilePath = "my_pc_specs.json";
        File specsFile = new File(specsFilePath);

        if (!specsFile.exists()) {
            System.out.println("--- Bem-vindo! Arquivo de especificações do PC não encontrado. ---");
            System.out.println("Vamos configurar seu PC pela primeira vez.");
            
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("Qual é o seu processador (CPU)? (ex: Intel Core i5-9400F): ");
                String cpu = scanner.nextLine();
                System.out.print("Qual é a sua placa de vídeo (GPU)? (ex: NVIDIA GeForce GTX 1660): ");
                String gpu = scanner.nextLine();
                System.out.print("Quanta memória RAM você tem (em GB)? (ex: 16): ");
                int ram = scanner.nextInt();

                Map<String, Object> specs = new HashMap<>();
                specs.put("cpu", cpu);
                specs.put("gpu", gpu);
                specs.put("ram", ram);
                String specsJson = new Gson().toJson(specs);

                System.out.println("\nSalvando suas especificações...");
                PythonExecutor.execute("adapters/adapter_save_pc_specs.py", specsJson);
                System.out.println("Especificações salvas com sucesso!");
            }
        }

        System.out.println("\nIniciando plataforma de agentes...");
        IPlatformConfiguration config = PlatformConfigurationHandler.getMinimal();
        config.addComponent(RecommenderAgent.class);
        Starter.createPlatform(config).get();
    }
}