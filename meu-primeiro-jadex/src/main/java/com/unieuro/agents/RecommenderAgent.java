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
        
        System.out.print("Descreva o tipo de jogo que voce quer jogar (ou digite 'sair'): ");
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
        
        // --- PASSO 1: Gerando tags com a IA ---
        System.out.println("\nIniciando busca...");
        PythonExecutor.PythonResult tagsResult = PythonExecutor.execute("adapter_get_tags.py", userDescription);

        if (!tagsResult.isSuccess()) {
            System.err.println("Falha ao obter tags da IA: " + tagsResult.getOutput());
            return;
        }
        
        String tagsJsonOutput = tagsResult.getOutput().trim();
        Map<String, List<String>> tagsMap = gson.fromJson(tagsJsonOutput, new TypeToken<Map<String, List<String>>>(){}.getType());
        List<String> tags = tagsMap.get("tags");
        
        // --- PASSO 2: Buscando URLs dos Jogos ---
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

        // --- PASSO 3: Loop para Analisar Cada Jogo ---
        System.out.println("\n--- Coletando Dados dos Jogos Encontrados ---");
        List<Map<String, Object>> allGamesData = new ArrayList<>();

        for(String url : gameUrls) {

            // 3a. Coleta de Detalhes
            PythonExecutor.PythonResult detailsResult = PythonExecutor.execute("adapter_scrape_details.py", url);
            
            if(!detailsResult.isSuccess()) {
                System.err.println("Falha ao obter detalhes para " + url + ". Pulando.");
                continue;
            }
            
            String detailsJsonOutput = detailsResult.getOutput().trim();
            Map<String, Object> gameDetails = gson.fromJson(detailsJsonOutput, new TypeToken<Map<String, Object>>(){}.getType());
            
            String gameName = (String) gameDetails.get("name");
        
            System.out.println("Pesquisando detalhes para: " + gameName);
            
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
            
            // Outros campos
            consolidatedData.put("Tags", gameDetails.get("tags_steam")); 
            allGamesData.add(consolidatedData);
        
        }
        
        // --- PASSO 4: GERAR RECOMENDAÇÃO FINAL ---
        System.out.println("\n========================= ENCONTREI O SEU JOGO IDEAL! =========================");
        System.out.println("Aguarde um momento, estou gerando a recomendação...");
        
        if(!allGamesData.isEmpty()) {
            String allGamesJson = gson.toJson(allGamesData);
            PythonExecutor.PythonResult finalRecResult = PythonExecutor.execute("adapter_final_recommendation.py", userDescription, allGamesJson);

            if(finalRecResult.isSuccess()) {
                String finalRecJsonOutput = finalRecResult.getOutput().trim();
                Map<String, String> finalRecMap = gson.fromJson(finalRecJsonOutput, new TypeToken<Map<String, String>>(){}.getType());
                String recomendacao = finalRecMap.getOrDefault("resumo", "Nao foi possivel gerar a recomendacao. Tente outra vez!");
                
                System.out.println("\nRecomendacao da IA:");
                System.out.println(recomendacao);
            } else {
                System.err.println("Falha ao gerar recomendacao final: " + finalRecResult.getOutput());
            }
        }

        System.out.println("\n--- Processo Finalizado ---");
    }

    public static void main(String[] args) {
        System.out.println("\nIniciando plataforma de agentes...");
        IPlatformConfiguration config = PlatformConfigurationHandler.getMinimal();
        config.addComponent(RecommenderAgent.class);
        Starter.createPlatform(config).get();
    }
}