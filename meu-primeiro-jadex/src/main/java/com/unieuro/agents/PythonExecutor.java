package com.unieuro.agents;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Classe utilitária para executar scripts Python a partir de código Java.
 * Esta versão assume que os scripts estão em 'python_scripts/adapters/'
 * e filtra a saída para pegar apenas a última linha como resultado JSON.
 */
public class PythonExecutor {

    /**
     * Classe interna para armazenar o resultado da execução do script Python.
     */
    public static class PythonResult {
        private final boolean success;
        private final String output;

        public PythonResult(boolean success, String output) {
            this.success = success;
            this.output = output;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getOutput() {
            return output;
        }
    }

    /**
     * Executa um script Python em um processo separado.
     *
     * O primeiro argumento no 'command' deve ser o nome do arquivo Python (ex: "meu_script.py").
     *
     * @param command O nome do script Python e seus argumentos.
     * @return Um objeto PythonResult com o resultado da execução.
     */
    public static PythonResult execute(String... command) {
        List<String> commandList = new ArrayList<>();
        commandList.add("python3");

        // CORREÇÃO DE CAMINHO: Assumindo que o script está em 'python_scripts/adapters/'
        // O comando[0] deve ser apenas o nome do script (ex: "adapter_get_tags.py")
        String scriptPath = "python_scripts/adapters/" + command[0];
        commandList.add(scriptPath);

        // Adiciona os argumentos restantes do script
        for (int i = 1; i < command.length; i++) {
            commandList.add(command[i]);
        }

        ProcessBuilder pb = new ProcessBuilder(commandList);

        try {
            Process process = pb.start();
            String finalOutput = "";
            StringBuilder errorOutput = new StringBuilder();

            // Lendo a saída padrão (stdout)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                // Guarda a última linha não vazia como o resultado JSON, ignorando logs anteriores
                List<String> lines = reader.lines().collect(Collectors.toList());
                for (int i = lines.size() - 1; i >= 0; i--) {
                    String line = lines.get(i).trim();
                    if (!line.isEmpty()) {
                        finalOutput = line;
                        break;
                    }
                }
            }
            
            // Lendo a saída de erro (stderr)
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errorOutput.append(line).append(System.lineSeparator());
                }
            }

            // Espera pelo processo, com um timeout de 120 segundos
            if (!process.waitFor(120, TimeUnit.SECONDS)) {
                process.destroy();
                return new PythonResult(false, "{\"erro\": \"Timeout: O script Python demorou mais de 120 segundos para responder.\"}");
            }

            System.out.println("--- DEBUG PYTHON-JAVA --- Recebido do Python (Saída Padrão Limpa): [" + finalOutput + "]");
            if(errorOutput.length() > 0) {
                System.out.println("--- DEBUG PYTHON-JAVA --- Recebido do Python (Saída de Erro): [" + errorOutput.toString().trim() + "]");
            }

            if (process.exitValue() == 0) {
                // Sucesso
                return new PythonResult(true, finalOutput);
            } else {
                // Erro na execução do script
                String cleanError = errorOutput.toString().replace("\"", "'").trim();
                return new PythonResult(false, "{\"erro\": \"O script Python retornou um erro.\", \"detalhes\": \"" + cleanError + "\"}");
            }
        } catch (Exception e) {
            // Erro ao iniciar ou interagir com o processo
            e.printStackTrace();
            return new PythonResult(false, "{\"erro\": \"Falha ao executar o processo Python: " + e.getMessage() + "\"}");
        }
    }
}