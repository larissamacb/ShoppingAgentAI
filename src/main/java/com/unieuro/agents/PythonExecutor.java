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

    public static PythonResult execute(String... command) {
        List<String> commandList = new ArrayList<>();
        commandList.add("python3");

        String scriptPath = "python_scripts/adapters/" + command[0];
        commandList.add(scriptPath);

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

            if (!process.waitFor(120, TimeUnit.SECONDS)) {
                process.destroy();
                return new PythonResult(false, "{\"erro\": \"Timeout: O script Python demorou mais de 120 segundos para responder.\"}");
            }

            if (process.exitValue() == 0) {
                return new PythonResult(true, finalOutput);
            } else {
                String cleanError = errorOutput.toString().replace("\"", "'").trim();
                return new PythonResult(false, "{\"erro\": \"O script Python retornou um erro.\", \"detalhes\": \"" + cleanError + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new PythonResult(false, "{\"erro\": \"Falha ao executar o processo Python: " + e.getMessage() + "\"}");
        }
    }
}