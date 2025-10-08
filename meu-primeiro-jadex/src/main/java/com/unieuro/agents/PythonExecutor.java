package com.unieuro.agents;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Classe utilitária para executar scripts Python a partir de código Java.
 * Esta versão assume que os scripts estão em 'python_scripts/adapters/'.
 */
public class PythonExecutor {

    /**
     * Classe interna para armazenar o resultado da execução do script Python.
     */
    public static class PythonResult {
        private final boolean success;
        private final String output;

        /**
         * Construtor da classe PythonResult.
         * @param success Indica se a execução foi bem-sucedida (código de saída 0).
         * @param output A saída (stdout) ou mensagem de erro (stderr/detalhes).
         */
        public PythonResult(boolean success, String output) {
            this.success = success;
            this.output = output;
        }

        /**
         * Verifica se a execução foi bem-sucedida.
         * @return true se o código de saída for 0, false caso contrário.
         */
        public boolean isSuccess() {
            return success;
        }

        /**
         * Obtém a saída do script.
         * @return A saída padrão (em caso de sucesso) ou a mensagem de erro formatada.
         */
        public String getOutput() {
            return output;
        }
    }

    /**
     * Executa um script Python em um processo separado.
     *
     * O primeiro argumento no 'command' deve ser o nome do arquivo Python (ex: "meu_script.py"),
     * e os argumentos seguintes são passados como parâmetros para o script.
     *
     * @param command O nome do script Python e seus argumentos.
     * @return Um objeto PythonResult com o resultado da execução.
     */
    public static PythonResult execute(String... command) {
        List<String> commandList = new ArrayList<>();
        commandList.add("python3"); // Supondo que 'python3' está disponível no PATH
        String scriptPath = "python_scripts/adapters/" + command[0];
        commandList.add(scriptPath);

        // Adiciona os argumentos restantes do script
        for (int i = 1; i < command.length; i++) {
            commandList.add(command[i]);
        }

        ProcessBuilder pb = new ProcessBuilder(commandList);

        try {
            Process process = pb.start();

            // Lendo a saída padrão (o que esperamos que seja o JSON)
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }

            // Lendo a saída de erro (avisos ou erros do Python)
            StringBuilder errorOutput = new StringBuilder();
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

            System.out.println("--- DEBUG PYTHON-JAVA --- Recebido do Python (Saída Padrão): [" + output.toString().trim() + "]");
            if(errorOutput.length() > 0) {
                System.out.println("--- DEBUG PYTHON-JAVA --- Recebido do Python (Saída de Erro): [" + errorOutput.toString().trim() + "]");
            }

            // Verifica o código de saída
            if (process.exitValue() == 0) {
                // Sucesso
                return new PythonResult(true, output.toString());
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