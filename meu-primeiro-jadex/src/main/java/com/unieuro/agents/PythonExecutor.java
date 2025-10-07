package com.unieuro.agents;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

        String scriptPath = "python_scripts/" + command[0];
        commandList.add(scriptPath);

        for (int i = 1; i < command.length; i++) {
            commandList.add(command[i]);
        }

        ProcessBuilder pb = new ProcessBuilder(commandList);
        // A linha 'redirectErrorStream' NÃO está aqui.

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

            if (!process.waitFor(120, TimeUnit.SECONDS)) {
                process.destroy();
                return new PythonResult(false, "{\"erro\": \"Timeout\"}");
            }

            System.out.println("--- DEBUG PYTHON-JAVA --- Recebido do Python (Saída Padrão): [" + output.toString().trim() + "]");
            if(errorOutput.length() > 0) {
                System.out.println("--- DEBUG PYTHON-JAVA --- Recebido do Python (Saída de Erro): [" + errorOutput.toString().trim() + "]");
            }

            if (process.exitValue() == 0) {
                return new PythonResult(true, output.toString());
            } else {
                return new PythonResult(false, "{\"erro\": \"O script Python retornou um erro.\", \"detalhes\": \"" + errorOutput.toString().replace("\"", "'") + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new PythonResult(false, "{\"erro\": \"Falha ao executar o processo Python: " + e.getMessage() + "\"}");
        }
    }
}