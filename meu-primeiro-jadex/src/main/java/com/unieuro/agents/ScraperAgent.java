// Arquivo: src/com/unieuro/agents/ScraperAgent.java
package com.unieuro.agents;

import jadex.bridge.service.annotation.OnStart;
import jadex.micro.annotation.Agent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Agent
public class ScraperAgent {

    private static final int MAX_TENTATIVAS = 3;
    private static final String SCRIPT_PRODUTOS = "scrap_two_steps.py";
    private static final String SCRIPT_COMENTARIOS = "comments_collection.py";
    private static final String ARQUIVO_PRODUTOS_JSON = "produtos_finais.json";

    @OnStart
    public void onStart() {
        System.out.println("ScraperAgent: Olá! Iniciando o processo de web scraping.");
        boolean sucessoFinalProdutos = false;

        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            System.out.println("\n--- Tentando executar " + SCRIPT_PRODUTOS + " (Tentativa " + tentativa + "/" + MAX_TENTATIVAS + ") ---");
            try {
                boolean sucessoExecucao = executarScriptPython(SCRIPT_PRODUTOS);
                
                if (sucessoExecucao && verificarArquivoJSON(ARQUIVO_PRODUTOS_JSON)) {
                    System.out.println("✔️ Sucesso! Script executado e arquivo JSON gerado corretamente.");
                    sucessoFinalProdutos = true;
                    break;
                } else {
                    System.out.println("AVISO: A execução do script falhou ou o arquivo JSON está vazio/inválido.");
                }

            } catch (Exception e) {
                System.out.println("ERRO: Uma exceção ocorreu durante a execução do script na tentativa " + tentativa);
                e.printStackTrace();
            }

            if(tentativa < MAX_TENTATIVAS) {
                try {
                    System.out.println("Aguardando 30 segundos antes da próxima tentativa...");
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (sucessoFinalProdutos) {
            System.out.println("\n--- Partindo para a coleta de comentários ---");
            try {
                boolean sucessoScrapComentarios = executarScriptPython(SCRIPT_COMENTARIOS);
                if (sucessoScrapComentarios) {
                    System.out.println("ScraperAgent: Todos os scripts foram executados com sucesso!");
                } else {
                    System.out.println("ScraperAgent: ERRO! O script de coleta de comentários falhou.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("\n❌ ERRO FATAL: O script " + SCRIPT_PRODUTOS + " falhou após " + MAX_TENTATIVAS + " tentativas. Abortando a missão.");
        }

        System.out.println("\nScraperAgent: Missão concluída. Encerrando.");
    }

    private boolean verificarArquivoJSON(String nomeArquivo) {
        File file = new File("meu-primeiro-jadex/scripts/" + nomeArquivo);
        if (file.exists() && file.length() > 10) { 
            return true;
        }
        System.out.println("Verificação falhou: Arquivo '" + nomeArquivo + "' não encontrado ou vazio.");
        return false;
    }

    private boolean executarScriptPython(String scriptName) throws Exception {
        System.out.println("--- Executando o script: " + scriptName + " ---");
        
        // ✨ MUDANÇA CRÍTICA: Apontando para o diretório correto do projeto
        File scriptDir = new File("meu-primeiro-jadex/scripts");
        
        ProcessBuilder pb = new ProcessBuilder("/usr/bin/python3", scriptName);
        
        pb.directory(scriptDir);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[Python]: " + line);
        }
        
        int exitCode = process.waitFor();
        
        System.out.println("--- Script " + scriptName + " finalizado com código de saída: " + exitCode + " ---");
        
        return exitCode == 0;
    }
}