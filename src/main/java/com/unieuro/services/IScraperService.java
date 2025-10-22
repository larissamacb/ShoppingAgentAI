package com.unieuro.services;

import java.util.List;
import java.util.Map;
import jadex.commons.future.IFuture;

/**
 * Interface de serviço para o agente que faz web scraping (Steam/Metacritic).
 */
public interface IScraperService {

    /**
     * Dadas as tags e um número, busca as URLs dos jogos na Steam.
     * Corresponde a (Request: BuscarURLs) -> (Inform: URLsEncontradas)
     */
    IFuture<List<String>> getGameUrls(List<String> tags, int numGames);

    /**
     * Dada uma URL de jogo, busca todos os detalhes (preço, score, reviews).
     * Corresponde a (Request: BuscarDetalhesSteam) e (Request: BuscarNotasMetacritic)
     */
    IFuture<Map<String, Object>> getGameDetails(String gameUrl);
}