package com.unieuro.agents;

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
    
    private final Gson gson = new Gson();

    @OnStart
    void run(IInternalAccess agent) {
        
        // --- Interação com o Usuário para coletar INPUTS ---
        System.out.println("--- Bem-vindo ao Recomendador de Jogos da Steam! ---");
        Scanner scanner = new Scanner(System.in);
        
        // Remoção da referência a 'atualizar pc'
        System.out.print("Descreva o tipo de jogo que você quer jogar (ou digite 'sair'): ");
        String userDescription = scanner.nextLine().trim();
        
        if (userDescription.equalsIgnoreCase("sair")) {
            System.out.println("Saindo do recomendador.");
            return;
        }
        
        int numGames = 5; // Valor padrão
        System.out.print("Quantos jogos voce quer pesquisar? (Padrao: 5, pressione Enter para usar o padrao): ");
        String numGamesInput = scanner.nextLine().trim();
        if (!numGamesInput.isEmpty()) {
            try {
                numGames = Integer.parseInt(numGamesInput);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Usando o padrao: 5.");
                numGames = 5;
            }
        }
        
        // --- REMOVIDO PASSO 1: Obter Specs do PC ---
        // pcSpecsJson foi removido, assim como a execução do adapter_get_pc_specs.py

        // --- PASSO 1: Gerando tags com a IA --- (Anteriormente Passo 2)
        System.out.println("\nConsultando a IA para gerar tags de busca...");
        PythonExecutor.PythonResult tagsResult = PythonExecutor.execute("adapter_get_tags.py", userDescription);

        if (!tagsResult.isSuccess()) {
            System.err.println("Falha ao obter tags da IA: " + tagsResult.getOutput());
            return;
        }
        
        String tagsJsonOutput = tagsResult.getOutput().trim();
        Map<String, List<String>> tagsMap = gson.fromJson(tagsJsonOutput, new TypeToken<Map<String, List<String>>>(){}.getType());
        List<String> tags = tagsMap.get("tags");
        System.out.println("Tags oficiais selecionadas pela IA: " + String.join(", ", tags));
        
        // --- PASSO 2: Buscando URLs dos Jogos --- (Anteriormente Passo 3)
        System.out.println("\nPasso 2: Buscando URLs dos jogos na Steam...");
        String tagsJsonForArg = gson.toJson(tags);
        PythonExecutor.PythonResult urlsResult = PythonExecutor.execute("adapter_get_game_urls.py", tagsJsonForArg, String.valueOf(numGames));

        if(!urlsResult.isSuccess() || urlsResult.getOutput().contains("erro")) {
            System.err.println("Falha ao obter URLs dos jogos: " + urlsResult.getOutput());
            return;
        }
        
        String urlsJsonOutput = urlsResult.getOutput().trim();
        Map<String, List<String>> urlsMap = gson.fromJson(urlsJsonOutput, new TypeToken<Map<String, List<String>>>(){}.getType());
        List<String> gameUrls = urlsMap.get("urls");
        System.out.println("Encontradas " + gameUrls.size() + " URLs de jogos para analisar.");

        // --- PASSO 3: Loop para Analisar Cada Jogo --- (Anteriormente Passo 4)
        System.out.println("\n--- Coletando Dados dos Jogos Encontrados ---");
        List<Map<String, Object>> allGamesData = new ArrayList<>();

        for(String url : gameUrls) {
            System.out.println("==================================================");
            System.out.println("🔎 Analisando URL: " + url);

            // 3a. Coleta de Detalhes
            PythonExecutor.PythonResult detailsResult = PythonExecutor.execute("adapter_scrape_details.py", url);
            if(!detailsResult.isSuccess()) {
                System.err.println("Falha ao obter detalhes para " + url + ". Pulando.");
                continue;
            }
            
            String detailsJsonOutput = detailsResult.getOutput().trim();
            Map<String, Object> gameDetails = gson.fromJson(detailsJsonOutput, new TypeToken<Map<String, Object>>(){}.getType());
            System.out.println("Detalhes coletados para: " + gameDetails.get("name"));
            
            // --- REMOVIDO 3b. Análise de Requisitos com IA (adapter_check_reqs.py) ---
            
            // 3b. Consolidação dos Dados
            Map<String, Object> consolidatedData = new HashMap<>();
            consolidatedData.put("Nome", gameDetails.get("name"));
            consolidatedData.put("Preço", gameDetails.get("price"));
            
            // Campos de Score e Resumo
            consolidatedData.put("Metascore", gameDetails.getOrDefault("metascore", "N/A"));
            consolidatedData.put("User Score", gameDetails.getOrDefault("user_score", "N/A"));
            consolidatedData.put("Summary Positive", gameDetails.getOrDefault("summary_positive", "N/A"));
            consolidatedData.put("Summary Mixed", gameDetails.getOrDefault("summary_mixed", "N/A"));
            consolidatedData.put("Summary Negative", gameDetails.getOrDefault("summary_negative", "N/A"));
            
            // Campo 'PC Roda?' foi removido.
            consolidatedData.put("Tags", gameDetails.get("tags_steam")); 
            allGamesData.add(consolidatedData);
            
            System.out.println("******************** " + gameDetails.get("name") + " ********************");
        }

        // --- PASSO 4: GERAR RECOMENDAÇÃO FINAL --- (Anteriormente Passo 5)
        System.out.println("\n========================= RESUMO FINAL =========================");
        System.out.println("🤖 Gerando recomendacao final com base nos resultados...");
        
        if(!allGamesData.isEmpty()) {
            String allGamesJson = gson.toJson(allGamesData);
            PythonExecutor.PythonResult finalRecResult = PythonExecutor.execute("adapter_final_recommendation.py", userDescription, allGamesJson);

            if(finalRecResult.isSuccess()) {
                String finalRecJsonOutput = finalRecResult.getOutput().trim();
                Map<String, String> finalRecMap = gson.fromJson(finalRecJsonOutput, new TypeToken<Map<String, String>>(){}.getType());
                String recomendacao = finalRecMap.getOrDefault("resumo", "Nao foi possivel gerar o resumo.");
                
                System.out.println("\nRecomendacao da IA:");
                System.out.println(recomendacao);
            } else {
                System.err.println("Falha ao gerar recomendacaoo final: " + finalRecResult.getOutput());
            }
        }

        System.out.println("\n--- Processo Finalizado ---");
    }

    public static void main(String[] args) {
        
        // --- REMOVIDA LÓGICA DE CONFIGURAÇÃO INICIAL DO PC ---
        
        System.out.println("\nIniciando plataforma de agentes...");
        IPlatformConfiguration config = PlatformConfigurationHandler.getMinimal();
        config.addComponent(RecommenderAgent.class);
        Starter.createPlatform(config).get();
    }
}