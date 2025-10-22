package com.unieuro.agents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import com.unieuro.services.IAIService;
import com.unieuro.services.IScraperService;

import jadex.bridge.IInternalAccess;
import jadex.bridge.service.ServiceScope;
import jadex.bridge.service.annotation.OnStart;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.RequiredService;
import jadex.micro.annotation.RequiredServices;

@Agent // Anotação Micro Agent
@RequiredServices({ // Continua a precisar dos serviços
        @RequiredService(name="ai_service", type=IAIService.class, scope=ServiceScope.PLATFORM),
        @RequiredService(name="scraper_service", type=IScraperService.class, scope=ServiceScope.PLATFORM)
})
public class CoordinatorAgent {

    @Agent
    protected IInternalAccess agent;

    // Variáveis para guardar o estado
    protected String descricao_usuario;
    protected int num_jogos;
    protected List<String> tags_busca = new ArrayList<>();
    protected List<String> urls_encontradas = new ArrayList<>();
    protected List<Map<String, Object>> jogos_processados = new ArrayList<>();
    protected String recomendacao_final = "Falha no processo.";

    // Injeção dos serviços (automática pelo Jadex)
    protected IFuture<IAIService> aiServiceFuture;
    protected IFuture<IScraperService> scraperServiceFuture;

    @OnStart
    public void runAgentLogic() {
        System.out.println("--- Bem-vindo ao Recomendador de Jogos da Steam! ---");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Descreva o tipo de jogo que voce quer jogar (ou digite 'sair'): ");
        String userDescriptionInput = scanner.nextLine().trim();

        if (userDescriptionInput.equalsIgnoreCase("sair")) {
            System.out.println("Saindo do recomendador.");
            agent.killComponent();
            return;
        }

        int numGamesInput = 5;
        System.out.print("Quantos jogos voce quer pesquisar? (Padrao: 5, pressione Enter para usar o padrao): ");
        String numGamesStr = scanner.nextLine().trim();
        if (!numGamesStr.isEmpty()) {
            try {
                numGamesInput = Integer.parseInt(numGamesStr);
            } catch (NumberFormatException e) {
                System.out.println("Entrada invalida. Usando o padrao: 5.");
                numGamesInput = 5;
            }
        }
        // scanner.close(); // Não fechar System.in

        // Guarda o input nas variáveis
        this.descricao_usuario = userDescriptionInput;
        this.num_jogos = numGamesInput;

        System.out.println("\nCoordinatorAgent (Micro): Iniciando processo com a descrição: '" + descricao_usuario + "' para " + num_jogos + " jogos.");

        // *** CORREÇÃO AQUI: REMOVIDAS as duas linhas getComponentFeature ***
        // A injeção dos campos aiServiceFuture e scraperServiceFuture é automática.

        // Verifica se os futures foram injetados (devem ter sido pelo Jadex)
        if (aiServiceFuture == null || scraperServiceFuture == null) {
            System.err.println("Coordinator (Micro): Erro Crítico - Serviços não foram injetados.");
            recomendacao_final = "Falha interna na injeção dos serviços.";
            agent.killComponent(); // Aborta se não conseguir os serviços
            return;
        }

        // *** LÓGICA DE ORQUESTRAÇÃO ***
        try {
            // Obtém os proxies dos serviços, esperando se necessário
            IAIService ai = aiServiceFuture.get();
            IScraperService scraper = scraperServiceFuture.get();

            // --- PASSO 1: Gerar Tags ---
            System.out.println("Coordinator (Micro): Solicitando tags...");
            tags_busca = ai.getTags(descricao_usuario).get();
            System.out.println("Coordinator (Micro): Tags recebidas: " + tags_busca);

            // --- PASSO 2: Buscar URLs ---
            System.out.println("Coordinator (Micro): Solicitando URLs...");
            urls_encontradas = scraper.getGameUrls(tags_busca, num_jogos).get();
            System.out.println("Coordinator (Micro): URLs recebidas: " + urls_encontradas.size());

            // --- PASSO 3: Loop para Analisar Cada Jogo ---
            System.out.println("\n--- Coordinator (Micro): Coletando Dados dos Jogos ---");
            jogos_processados.clear();
            for (String url : urls_encontradas) {
                try {
                    System.out.println("Coordinator (Micro): Solicitando detalhes para: " + url);
                    Map<String, Object> gameDetailsBruto = scraper.getGameDetails(url).get();
                    String gameName = (String) gameDetailsBruto.getOrDefault("name", "Nome Desconhecido");
                    System.out.println("Coordinator (Micro): Detalhes recebidos para: " + gameName);

                    // Consolidação (igual)
                    Map<String, Object> consolidatedData = new HashMap<>();
                    consolidatedData.put("Nome", gameDetailsBruto.get("name"));
                    consolidatedData.put("Preço", gameDetailsBruto.get("price"));
                    consolidatedData.put("Metascore", gameDetailsBruto.getOrDefault("metascore", "N/A"));
                    consolidatedData.put("User Score", gameDetailsBruto.getOrDefault("user_score", "N/A"));
                    consolidatedData.put("positive_reviews", gameDetailsBruto.getOrDefault("reviews_positive", new ArrayList<>()));
                    consolidatedData.put("mixed_reviews", gameDetailsBruto.getOrDefault("reviews_mixed", new ArrayList<>()));
                    consolidatedData.put("negative_reviews", gameDetailsBruto.getOrDefault("reviews_negative", new ArrayList<>()));
                    consolidatedData.put("Tags", gameDetailsBruto.get("tags_steam"));
                    jogos_processados.add(consolidatedData);

                } catch (Exception e) {
                    System.err.println("Coordinator (Micro): Falha ao processar URL " + url + ". Pulando. Erro: " + e.getMessage());
                }
            }

            // --- PASSO 4: Gerar Veredito Final ---
            if (jogos_processados.isEmpty()) {
                System.out.println("Coordinator (Micro): Nenhum jogo processado.");
                recomendacao_final = "Não foi possível encontrar/processar jogos.";
            } else {
                System.out.println("\nCoordinator (Micro): Solicitando recomendação final...");
                recomendacao_final = ai.getFinalRecommendation(descricao_usuario, jogos_processados).get();
            }

            // --- PASSO 5: Apresentar Resultado ---
            System.out.println("\n========================= RECOMENDAÇÃO PRONTA! =========================");
            System.out.println(recomendacao_final);
            System.out.println("\n--- Processo Finalizado ---");

        } catch (Exception e) {
            System.err.println("Coordinator (Micro): Ocorreu um erro no fluxo principal! Erro: " + e.getMessage());
            e.printStackTrace();
            recomendacao_final = "Ocorreu um erro durante o processamento.";
            System.out.println("\n========================= FALHA NO PROCESSO =========================");
            System.out.println(recomendacao_final); // Mostra a mensagem de erro
        } finally {
            // Opcional: Desligar o agente após a execução
            // agent.killComponent();
        }
    }
}