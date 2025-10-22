package com.unieuro.agents;

import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unieuro.services.IScraperService; 

import jadex.bridge.IInternalAccess;
import jadex.commons.future.Future;
import jadex.commons.future.IFuture;
import jadex.micro.annotation.Agent;
import jadex.micro.annotation.AgentCreated;
import jadex.micro.annotation.Implementation;
import jadex.micro.annotation.ProvidedService;
import jadex.micro.annotation.ProvidedServices;

@Agent 
@ProvidedServices(@ProvidedService(type = IScraperService.class))
public class ScraperAgent implements IScraperService {

    @Agent
    protected IInternalAccess agent;

    private final Gson gson = new Gson();

    @AgentCreated
    public void init() {
        System.out.println("ScraperAgent (Micro) iniciado.");
    }

    @Override
    public IFuture<List<String>> getGameUrls(List<String> tags, int numGames) {
        System.out.println("ScraperAgent (Micro): Recebido pedido para buscar URLs com tags: " + tags);
        Future<List<String>> future = new Future<>();

        try {
            String tagsJsonForArg = gson.toJson(tags);
            String numGamesStr = String.valueOf(numGames);

            PythonExecutor.PythonResult urlsResult = PythonExecutor.execute("adapter_get_game_urls.py", tagsJsonForArg, numGamesStr);

            if(!urlsResult.isSuccess() || urlsResult.getOutput().contains("erro")) {
                System.err.println("ScraperAgent (Micro): Falha ao obter URLs dos jogos: " + urlsResult.getOutput());
                throw new RuntimeException("Falha ao buscar URLs: " + urlsResult.getOutput());
            }

            String urlsJsonOutput = urlsResult.getOutput().trim();
            Map<String, List<String>> urlsMap = gson.fromJson(urlsJsonOutput, new TypeToken<Map<String, List<String>>>(){}.getType());
            List<String> gameUrls = urlsMap.get("urls");

            System.out.println("ScraperAgent (Micro): Encontradas " + gameUrls.size() + " URLs.");
            future.setResult(gameUrls);
        } catch (Exception e) {
            future.setException(e);
        }
        return future;
    }

    @Override
    public IFuture<Map<String, Object>> getGameDetails(String gameUrl) {
        System.out.println("ScraperAgent (Micro): Recebido pedido para buscar detalhes de: " + gameUrl);
        Future<Map<String, Object>> future = new Future<>();

        try {
            PythonExecutor.PythonResult detailsResult = PythonExecutor.execute("adapter_scrape_details.py", gameUrl);

            if(!detailsResult.isSuccess()) {
                System.err.println("ScraperAgent (Micro): Falha ao obter detalhes para " + gameUrl);
                throw new RuntimeException("Falha ao buscar detalhes: " + detailsResult.getOutput());
            }

            String detailsJsonOutput = detailsResult.getOutput().trim();
            Map<String, Object> gameDetails = gson.fromJson(detailsJsonOutput, new TypeToken<Map<String, Object>>(){}.getType());

            String gameName = (String) gameDetails.get("name");
            System.out.println("ScraperAgent (Micro): Detalhes de '" + gameName + "' obtidos.");
            future.setResult(gameDetails);
        } catch (Exception e) {
            future.setException(e);
        }
        return future;
    }
}