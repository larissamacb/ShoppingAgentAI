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
    
    private final Gson gson = new Gson();

    @OnStart
    void run(IInternalAccess agent) {
        
        // --- Interação com o Usuário para coletar INPUTS ---
        System.out.println("--- Bem-vindo ao Recomendador de Jogos da Steam! ---");
        Scanner scanner = new Scanner(System.in);
        
        System.out.print("Descreva o tipo de jogo que você quer jogar (ou digite 'atualizar pc' ou 'sair'): ");
        String userDescription = scanner.nextLine().trim();
        
        if (userDescription.equalsIgnoreCase("sair")) {
            System.out.println("Saindo do recomendador.");
            return;
        }
        
        int numGames = 5; // Valor padrão
        System.out.print("Quantos jogos você quer pesquisar? (Padrão: 5, pressione Enter para usar o padrão): ");
        String numGamesInput = scanner.nextLine().trim();
        if (!numGamesInput.isEmpty()) {
            try {
                numGames = Integer.parseInt(numGamesInput);
            } catch (NumberFormatException e) {
                System.out.println("Entrada inválida. Usando o padrão: 5.");
                numGames = 5;
            }
        }
        
        // --- PASSO 1: Obter Specs do PC ---
        System.out.println("Passo 1: Obtendo especificações do PC...");
        // CORREÇÃO: Removido o prefixo 'adapters/'
        PythonExecutor.PythonResult pcSpecsResult = PythonExecutor.execute("adapter_get_pc_specs.py");
        
        if (!pcSpecsResult.isSuccess() || pcSpecsResult.getOutput().contains("erro")) {
            System.err.println("Falha crítica ao obter specs do PC: " + pcSpecsResult.getOutput());
            return;
        }
        String pcSpecsJson = pcSpecsResult.getOutput().trim(); 
        System.out.println("PC Specs obtidas: " + pcSpecsJson);

        // --- PASSO 2: Gerando tags com a IA ---
        System.out.println("\n🤖 Consultando a IA (modo expert) para gerar tags de busca...");
        // CORREÇÃO: Removido o prefixo 'adapters/'
        PythonExecutor.PythonResult tagsResult = PythonExecutor.execute("adapter_get_tags.py", userDescription);

        if (!tagsResult.isSuccess()) {
            System.err.println("Falha ao obter tags da IA: " + tagsResult.getOutput());
            return;
        }
        
        String tagsJsonOutput = tagsResult.getOutput().trim();
        Map<String, List<String>> tagsMap = gson.fromJson(tagsJsonOutput, new TypeToken<Map<String, List<String>>>(){}.getType());
        List<String> tags = tagsMap.get("tags");
        System.out.println("Tags oficiais selecionadas pela IA: " + String.join(", ", tags));
        
        // --- PASSO 3: Buscando URLs dos Jogos ---
        System.out.println("\nPasso 3: Buscando URLs dos jogos na Steam...");
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

        // --- PASSO 4: Loop para Analisar Cada Jogo ---
        System.out.println("\n--- Coletando Dados dos Jogos Encontrados ---");
        List<Map<String, Object>> allGamesData = new ArrayList<>();

        for(String url : gameUrls) {
            System.out.println("==================================================");
            System.out.println("🔎 Analisando URL: " + url);

            // 4a. Coleta de Detalhes
            PythonExecutor.PythonResult detailsResult = PythonExecutor.execute("adapter_scrape_details.py", url);
            if(!detailsResult.isSuccess()) {
                System.err.println("Falha ao obter detalhes para " + url + ". Pulando.");
                continue;
            }
            String detailsJsonOutput = detailsResult.getOutput().trim();
            Map<String, Object> gameDetails = gson.fromJson(detailsJsonOutput, new TypeToken<Map<String, Object>>(){}.getType());
            System.out.println("Detalhes coletados para: " + gameDetails.get("name"));
            
            // 4b. Análise de Requisitos com IA
            System.out.println("🤖 IA está analisando os requisitos do seu PC...");
            String minReq = (String) gameDetails.getOrDefault("min_req", "N/A");
            String recReq = (String) gameDetails.getOrDefault("rec_req", "N/A");
            
            PythonExecutor.PythonResult reqsResult = PythonExecutor.execute("adapter_check_reqs.py", pcSpecsJson, minReq, recReq);
            
            String reqsJsonOutput = reqsResult.getOutput().trim();
            Map<String, String> reqsMap = gson.fromJson(reqsJsonOutput, new TypeToken<Map<String, String>>(){}.getType());
            String pcRoda = reqsMap.getOrDefault("resultado", "Não foi possível analisar os requisitos.");
            System.out.println("| SEU PC RODA? (Análise da IA): " + pcRoda);
            
            // 4c. Consolidação dos Dados
            Map<String, Object> consolidatedData = new HashMap<>();
            consolidatedData.put("Nome", gameDetails.get("name"));
            consolidatedData.put("Preço", gameDetails.get("price"));
            consolidatedData.put("User Score", gameDetails.get("user_score"));
            consolidatedData.put("PC Roda?", pcRoda);
            consolidatedData.put("Tags", gameDetails.get("tags_steam")); 
            allGamesData.add(consolidatedData);
            System.out.println("******************** " + gameDetails.get("name") + " ********************");
        }

        // --- PASSO 5: GERAR RECOMENDAÇÃO FINAL ---
        System.out.println("\n========================= RESUMO FINAL =========================");
        System.out.println("🤖 Gerando recomendação final com base nos resultados...");
        
        if(!allGamesData.isEmpty()) {
            String allGamesJson = gson.toJson(allGamesData);
            PythonExecutor.PythonResult finalRecResult = PythonExecutor.execute("adapter_final_recommendation.py", userDescription, allGamesJson);

            if(finalRecResult.isSuccess()) {
                String finalRecJsonOutput = finalRecResult.getOutput().trim();
                Map<String, String> finalRecMap = gson.fromJson(finalRecJsonOutput, new TypeToken<Map<String, String>>(){}.getType());
                String recomendacao = finalRecMap.getOrDefault("resumo", "Não foi possível gerar o resumo.");
                
                System.out.println("\nRecomendação da IA:");
                System.out.println(recomendacao);
            } else {
                System.err.println("Falha ao gerar recomendação final: " + finalRecResult.getOutput());
            }
        }

        System.out.println("\n--- Processo Finalizado ---");
    }

    public static void main(String[] args) {
        String specsFilePath = "python_scripts/my_pc_specs.json";
        File specsFile = new File(specsFilePath);

        if (!specsFile.exists()) {
            System.out.println("--- Bem-vindo! Arquivo de especificações do PC não encontrado. ---");
            System.out.println("Vamos configurar seu PC pela primeira vez.");
            
            Scanner scanner = new Scanner(System.in);
            try {
                System.out.print("Qual é o seu processador (CPU)? (ex: Intel Core i5-9400F): ");
                String cpu = scanner.nextLine();
                System.out.print("Qual é a sua placa de vídeo (GPU)? (ex: NVIDIA GeForce GTX 1660): ");
                String gpu = scanner.nextLine();
                System.out.print("Quanta memória RAM você tem (em GB)? (ex: 16): ");
                
                // Verifica se a entrada é um número antes de ler com nextInt()
                while (!scanner.hasNextInt()) {
                    System.out.print("Entrada inválida. Por favor, digite um número: ");
                    scanner.next(); // consome a entrada inválida
                }
                int ram = scanner.nextInt();
                scanner.nextLine(); // Consome a quebra de linha após nextInt

                Map<String, Object> specs = new HashMap<>();
                specs.put("cpu", cpu);
                specs.put("gpu", gpu);
                specs.put("ram", ram);
                String specsJson = new Gson().toJson(specs);

                System.out.println("\nSalvando suas especificações...");
                PythonExecutor.execute("adapter_save_pc_specs.py", specsJson);
                System.out.println("Especificações salvas com sucesso!");
            } catch (Exception e) {
                 System.err.println("Erro durante a configuração inicial do PC: " + e.getMessage());
            }
        }

        System.out.println("\nIniciando plataforma de agentes...");
        IPlatformConfiguration config = PlatformConfigurationHandler.getMinimal();
        config.addComponent(RecommenderAgent.class);
        Starter.createPlatform(config).get();
    }
}