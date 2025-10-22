package com.unieuro.services;

import java.util.List;
import java.util.Map;
import jadex.commons.future.IFuture;

/**
 * Interface de serviço para o agente que lida com a IA (Gemini).
 */
public interface IAIService {
    
    /**
     * Dado uma descrição do usuário, retorna uma lista de tags de busca.
     * Corresponde a (Request: GerarTags) -> (Inform: TagsGeradas)
     */
    IFuture<List<String>> getTags(String userDescription);
    
    /**
     * Dada a descrição e a lista de jogos processados, gera a recomendação final.
     * Corresponde a (Request: GerarVereditoFinal) -> (Inform: VereditoGerado)
     */
    IFuture<String> getFinalRecommendation(String userDescription, List<Map<String, Object>> allGamesData);
}