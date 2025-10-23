# 🎮 Vicia AI

Um sistema de recomendação de jogos que utiliza a plataforma multiagente Jadex para orquestrar o web scraping e a Inteligência Artificial (Gemini) para analisar dados e gerar recomendações personalizadas.

Este projeto foca em coletar dados de múltiplas fontes (Steam e Metacritic) para fornecer uma recomendação baseada em estilo do jogo, preço, notas e sentimento dos usuários.

-----

## 📝 Índice

- [📖 Sobre o Projeto](#-sobre-o-projeto)
 - [✨ Funcionalidades](#-funcionalidades)
 - [⚙️ Arquitetura do Sistema](#%EF%B8%8F-arquitetura-do-sistema)
 - [🛠️ Tecnologias Utilizadas](#%EF%B8%8F-tecnologias-utilizadas)
 - [🚀 Como Executar](#-como-executar)
     - [Pré-requisitos](#pré-requisitos)
     - [Instalação](#instalação)
     - [Uso](#uso)
 - [🗺️ Roadmap](#roadmap)

-----

## 📖 Sobre o Projeto

O objetivo do *Vicia AI* é simplificar a escolha de jogos, automatizando a coleta de dados e aplicando lógica avançada de *Inteligência Artificial* para gerar um veredito final.

O processo é iniciado pelo usuário que descreve o tipo de jogo desejado. Em resposta, o sistema (orquestrado pelos agentes Jadex):

1.  Usa a IA (via `AIHandlerAgent`) para transformar a descrição em tags de busca.
2.  Busca URLs de jogos correspondentes na Steam (via `ScraperAgent`).
3.  Coleta detalhes (preço, notas, reviews brutos) de cada jogo encontrado (via `ScraperAgent`).
4.  Usa a IA (via `AIHandlerAgent`) para analisar todos os dados coletados e gerar uma recomendação final justificada.

-----

## ✨ Funcionalidades

  - **Tradução de Intenção:** Utiliza a *IA (Gemini)* para converter a descrição textual do usuário em tags oficiais da Steam.
  - **Coleta de Dados Dupla:** Combina informações:
      - **Steam:** Nome, Preço, Descrição e Tags.
      - **Metacritic:** *Metascore* (Nota da Crítica) e *User Score* (Nota do Usuário) extraídos da página principal.
  - **Análise de Sentimento (IA):** Coleta até *10 reviews* por sentimento (Positivas, Mistas e Negativas) e utiliza a IA para criar um resumo conciso de cada sentimento.
  - **Recomendação Final Otimizada:** Gera um parágrafo de recomendação justificado, com base nas notas, descrição do jogo, preço e experiência resumida dos usuários.

-----

## ⚙️ Arquitetura do Sistema

| Agente / Componente | Função | Tecnologia Principal |
| :------------------ | :----- | :------------------- |
| **`CoordinatorAgent`** | **Orquestrador Central.** Gerencia o fluxo, interage com o usuário e coordena os outros agentes via chamadas de serviço. | Jadex Micro (Java) |
| **`AIHandlerAgent`** | **Especialista IA.** Fornece serviços para gerar tags e a recomendação final, delegando para scripts Python. | Jadex Micro (Java), PythonExecutor |
| **`ScraperAgent`** | **Especialista Web Scraping.** Fornece serviços para buscar URLs e detalhes de jogos, delegando para scripts Python. | Jadex Micro (Java), PythonExecutor |
| `PythonExecutor.java` | Classe utilitária Java para executar scripts Python e capturar seus resultados (JSON). | Java ProcessBuilder |
| Scripts Python (`adapter_*.py`, `core/*.py`) | Contêm a lógica real de interação com a API Gemini e Web Scraping (Requests/BeautifulSoup). | Python, Google Gemini API, Requests, BeautifulSoup |
| Interfaces (`IAIService`, `IScraperService`) | Definem os contratos (métodos) para a comunicação entre os agentes via serviços Jadex. | Java |
| `Main.java` | Ponto de entrada. Configura e inicia a plataforma Jadex programaticamente, adicionando os agentes. | Java, Jadex Platform API |
| `build.gradle` | Ferramenta de build. Gerencia dependências (Jadex Standard, Gson) online e define tarefas de compilação/execução. | Gradle |

### Fluxo de Trabalho

1.  **Entrada:** Usuário insere a descrição e N jogos desejados para a pesquisa (influenciam no tempo de execução e na variabilidade de recomendações).
2.  **Geração de Tags:** `CoordinatorAgent` → `AIHandlerAgent.getTags()` → `PythonExecutor` → `adapter_get_tags.py` (IA) → Retorna Tags.
3.  **Busca de URLs:** `CoordinatorAgent` → `ScraperAgent.getGameUrls()` → `PythonExecutor` → `adapter_get_game_urls.py` (Web) → Retorna URLs.
4.  **Loop de Análise:** Para cada URL:
    * `CoordinatorAgent` → `ScraperAgent.getGameDetails()` → `PythonExecutor` → `adapter_scrape_details.py` (Web) → Retorna Dados Brutos (incluindo reviews).
5.  **Recomendação Final:** `CoordinatorAgent` → `AIHandlerAgent.getFinalRecommendation()` (com todos os dados brutos) → `PythonExecutor` → `adapter_final_recommendation.py` (IA) → Retorna Texto Final.
6.  **Saída:** `CoordinatorAgent` exibe a recomendação no console.

-----

## 🛠️ Tecnologias Utilizadas

| Categoria | Tecnologia | Notas |
| :---------------------- | :---------------------------------- | :------------------------------------------------------------------- |
| *Plataforma Multiagente* | *Jadex Active Components (Micro)* | Orquestração via Micro Agents, configuração programática. |
| *Build System* | *Gradle (com Wrapper)* | Gerencia dependências (online) e ciclo de vida do build/execução. |
| *Inteligência Artificial* | *Google Gemini API* | Usada nos scripts Python para geração de tags e recomendação. |
| *Comunicação Java-Python* | *PythonExecutor (Java ProcessBuilder)* | Executa scripts e troca dados via JSON (stdout). |
| *Processamento JSON (Java)* | *Google Gson* | Parse do JSON retornado pelos scripts Python. |
| *Web Scraping (Python)* | *Requests* e *BeautifulSoup* | Usadas nos scripts Python para coletar dados web. |
| *Variáveis de Ambiente* | *Python-dotenv* | Usada nos scripts Python para carregar a chave de API. |

-----

## 🚀 Como Executar

Para o desenvolvimento do projeto, o ambiente utilizado foi o Codespaces do GitHub (ambiente Linux). Recomendamos que o use também.

### Pré-requisitos

1.  **Java JDK (11+):** Necessário para executar o Jadex e o Gradle.
2.  **Python 3 e Pip:** Necessário para os scripts de IA e scraping.
3.  **Conexão à Internet:** Para o Gradle baixar dependências e para os scripts acessarem APIs/Web.
4.  **Chave de API do Google Gemini:** Configure-a no arquivo `.env`.

### Preparação

1.  **Configurar Chave de API:**
    * Crie ou edite o arquivo `.env` na raiz do projeto (`vicia-ai/.env`).
    * Adicione sua chave:
        ```
        GOOGLE_API_KEY="SEU_TOKEN_AQUI"
        ```

### Uso

Após a configuração inicial, siga estes passos para executar a aplicação:

1.  **Compilar o Projeto (Build):**
        ```
        ./gradlew clean build
        ```

2.  **Executar a Aplicação:**
        ```
        ./gradlew -q --no-daemon --console=plain run -Djadex.shell=false
        ```

    **Explicação das Flags:**
    * `-q` (quiet): Reduz a quantidade de logs do Gradle.
    * `--no-daemon`: Executa o Gradle sem usar um processo em background.
    * `--console=plain`: Formata a saída do console de forma mais simples, sem cores ou formatação especial (ajuda a evitar output "bagunçado").

O sistema será iniciado e o `CoordinatorAgent` pedirá a **descrição do jogo** e o **número de jogos** (para ele pesquisar e decidir entre eles) diretamente no console do terminal.
Use Ctrl + C quando quiser parar a execução.

-----

## 🗺️ Roadmap

  - [ ] Implementar funcionalidade de análise de compatibilidade para comparar os requisitos de hardware do jogo com os recursos do dispositivo do usuário.
  - [ ] Implementar recomendação de período de compra com base no histórico de preços do jogo na Steam.

-----
