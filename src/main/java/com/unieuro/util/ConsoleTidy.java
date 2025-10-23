package com.unieuro.util;

import java.io.PrintStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * ConsoleTidy
 *
 * Limpa ruídos do Jadex/Gradle e padroniza mensagens dos agentes.
 * Modo ASCII opcional (-Dvicia.ascii=true) remove acentos na saída.
 */
public final class ConsoleTidy {

    private ConsoleTidy() {}

    /** Ativa modo ASCII (remove acentos) se -Dvicia.ascii=true */
    private static final boolean ASCII_MODE =
            Boolean.parseBoolean(System.getProperty("vicia.ascii", "false"));

    /** Ruídos para ocultar por completo */
    private static final Pattern[] NOISE_LINES = new Pattern[] {
        Pattern.compile("^Available network .* secret key:.*$"),
        Pattern.compile("^Platform .* access key:.*$"),
        Pattern.compile("^Started websocket server: .*"),
        Pattern.compile("^Jadex shell \\(type 'h' for help\\)$"),
        Pattern.compile("^[a-z0-9_-]+>\\s*$", Pattern.CASE_INSENSITIVE),
        Pattern.compile("^Jadex Version .*"),
        Pattern.compile("^.* platform startup time: .*ms\\.$"),
        Pattern.compile("^<[-=<>\\s]+>.*$"),
        Pattern.compile("^\\s*$"),
    };

    private static String toAscii(String s) {
        String norm = Normalizer.normalize(s, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{M}+", "")
                   .replace("™", "TM")
                   .replace("®", "R");
    }

    private static String fixMojibake(String s) {
        s = s.replace("Ã§", "ç").replace("Ã£", "ã")
             .replace("Ã©", "é").replace("Ãª", "ê")
             .replace("Ã³", "ó").replace("Ã´", "ô")
             .replace("Ãº", "ú").replace("Ã¢", "â")
             .replace("Ã", "Ã").replace("Ã‰", "É")
             .replace("ￃﾣ", "ã").replace("ￃﾧ", "ç")
             .replace("ￃﾪ", "ê").replace("ￃﾳ", "ó");
        return s;
    }

    /** Extrai nome de jogo legível da URL */
    private static String gameNameFromUrl(String url) {
        String name = url.replaceAll(".*/app/\\d+/([^/?]+).*", "$1");
        if (name == null || name.equals(url)) {
            name = url.replaceAll(".*/([^/?#]+).*", "$1");
        }
        name = name.replace('_', ' ').replace('-', ' ').trim();
        return name.isEmpty() ? "Jogo desconhecido" : name;
    }

    private static String rewrite(String line) {
        line = fixMojibake(line);

        // ===== Prefixos dos agentes =====
        line = line.replaceFirst("^CoordinatorAgent \\(Micro\\):\\s*", "[Coordinator] ");
        line = line.replaceFirst("^Coordinator \\(Micro\\):\\s*",      "[Coordinator] ");
        line = line.replaceFirst("^ScraperAgent \\(Micro\\):\\s*",     "[Scraper] ");
        line = line.replaceFirst("^AIHandlerAgent \\(Micro\\):\\s*",   "[AI Handler] ");

        // ===== Prompts =====
        line = line.replaceFirst("^--- Bem-vindo ao Recomendador de Jogos da Steam! ---$",
                "[Coordinator] --- Bem-vindo ao Recomendador de Jogos da Steam! ---");
        line = line.replaceFirst("^Descreva o tipo de jogo que voce quer jogar.*$",
                "[Coordinator] Descreva o tipo de jogo que você quer jogar (ou digite 'sair'): ");
        line = line.replaceFirst("^Quantos jogos voce quer pesquisar.*$",
                "[Coordinator] Quantos jogos você quer pesquisar? (Padrão: 5, Enter = padrão): ");

        // ===== Limpeza e condensação =====
        if (line.contains("[Coordinator] Serviços localizados")) return "";
        if (line.matches("^\\[Coordinator\\] Obtendo .*IRequiredServicesFeature.*$")) return "";
        if (line.matches("^\\[Coordinator\\] Iniciando busca pelo servi.*AI.*$")) return "";
        if (line.matches("^\\[Coordinator\\] Servi.*AI encontrado.*$")) return "";

        // ===== Fluxo =====
        line = line.replaceFirst("^--- Coordinator \\(Micro\\): Coletando Dados dos Jogos ---$",
                "[Coordinator] Coletando dados dos jogos…");
        line = line.replaceFirst("^\\[Coordinator\\] Solicitando tags.*$", "[Coordinator] Solicitando tags ao AI Handler…");
        line = line.replaceFirst("^\\[Coordinator\\] Solicitando URLs.*$", "[Coordinator] Solicitando URLs ao Scraper…");

        // ===== Compacta detalhes em 1 linha =====
        if (line.matches("^\\[Coordinator\\] Solicitando detalhes: .*")) {
            String url = line.replaceFirst("^\\[Coordinator\\] Solicitando detalhes: ", "").trim();
            String game = gameNameFromUrl(url);
            line = "[Scraper] Coletando detalhes de " + game + "…";
        }
        if (line.startsWith("[Scraper] Recebido pedido para buscar detalhes de")) return "";
        if (line.startsWith("[Scraper] Detalhes obtidos")) return "";
        if (line.startsWith("[Coordinator] Detalhes recebidos")) return "";

        // ===== Scraper e AI Handler =====
        line = line.replaceFirst("^\\[Scraper\\] Recebido pedido para buscar URLs com tags:.*$",
                "[Scraper] Buscando URLs na Steam/Metacritic…");
        line = line.replaceFirst("^\\[AI Handler\\] Recebido pedido para gerar tags.*$",
                "[AI Handler] Gerando tags…");
        line = line.replaceFirst("^\\[AI Handler\\] Recebido pedido para gerar recomenda.*final.*$",
                "[AI Handler] Gerando recomendação final…");

        // ===== Banner final =====
        if (line.matches("^=+.*RECOMENDA.*PRONTA!.*=+$")) {
            line = "========================= RECOMENDAÇÃO PRONTA! =========================";
        }

        // ===== Espaçamento visual =====
        if (line.startsWith("[Coordinator]") || line.startsWith("[Scraper]") || line.startsWith("[AI Handler]")) {
            line = "\n" + line;
        }

        if (ASCII_MODE) line = toAscii(line);
        return line;
    }

    private static boolean isNoise(String line) {
        for (Pattern p : NOISE_LINES)
            if (p.matcher(line).matches()) return true;
        return false;
    }

    public static void install() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        System.setOut(new PrintStream(new FilteringStream(originalOut), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FilteringStream(originalErr), true, StandardCharsets.UTF_8));
    }

    private static final class FilteringStream extends OutputStream {
        private final PrintStream delegate;
        private final StringBuilder buf = new StringBuilder();

        FilteringStream(PrintStream delegate) { this.delegate = delegate; }

        @Override public void write(int b) {
            char c = (char) b;
            buf.append(c);
            if (c == '\n') flushLine();
        }

        @Override public void flush() {
            if (buf.length() > 0) flushLine();
            delegate.flush();
        }

        private void flushLine() {
            String raw = buf.toString();
            buf.setLength(0);
            String line = raw.endsWith("\n") ? raw.substring(0, raw.length() - 1) : raw;
            if (isNoise(line)) return;

            String rewritten = rewrite(line);
            if (rewritten == null || rewritten.isEmpty()) return;
            delegate.println(rewritten);
        }
    }
}
