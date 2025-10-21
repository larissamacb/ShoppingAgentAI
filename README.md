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

O processo é iniciado pelo usuário que descreve o tipo de jogo desejado. Em resposta, o sistema:

1.  Usa a IA para transformar a descrição em tags de busca (Steam).
2.  Busca jogos correspondentes na Steam.
3.  Coleta notas e reviews no Metacritic.
4.  Gera um resumo conciso dos sentimentos dos usuários (Positivo, Misto, Negativo).
5.  Emite uma recomendação final, justificando a escolha com base em Descrição do jogo, Metascore, User Score e Resumos de Review.

-----

## ✨ Funcionalidades

  - **Tradução de Intenção:** Utiliza a *IA (Gemini)* para converter a descrição textual do usuário em tags oficiais da Steam.
  - **Coleta de Dados Dupla:** Combina informações:
      - **Steam:** Nome, Preço, Descrição e Tags.
      - **Metacritic:** *Metascore* (Nota da Crítica) e *User Score* (Nota do Usuário) extraídos da página principal.
  - **Análise de Sentimento (IA):** Coleta até *10 reviews* por sentimento (Positivas, Mistas e Negativas) e utiliza a IA para criar um resumo conciso de cada sentimento.
  - **Recomendação Final Otimizada:** Gera um parágrafo de recomendação justificado, com base nas notas, descrição do jogo, preço e experiência resumida dos usuários.
  - **Compatibilidade Terminal:** Todo o output da IA é *transliterado* (sem acentos ou cedilha) para garantir a correta exibição em qualquer console.

-----

## ⚙️ Arquitetura do Sistema

| Agente / Componente | Função | Tecnologia |
| :--- | :--- | :--- |
| *OrchestratorAgent* | *Coordenador Central.* Gerencia o fluxo de 4 passos, a interação com o usuário e a consolidação final dos dados. | *Jadex (Java)* |
| *Adaptadores (Python)* | Executam tarefas específicas, fazendo um papel de comunicação entre os arquivos Python principais e o Java. | *Python Executor (Java)* |
| *ai_handler.py* | Contém a lógica de *Inteligência Artificial* (geração de tags e todos os resumos de reviews). | *Google Gemini API* |
| *steam_scraper.py* | Realiza o Web Scraping da *Steam* (URLs, detalhes, preço, requisitos). | *Requests / BeautifulSoup* |
| *metacritic_scraper.py* | Realiza o Web Scraping do *Metacritic* (Notas e Reviews). | *Requests / BeautifulSoup* |

### Fluxo de Trabalho

1.  **Entrada:** Usuário insere a descrição e N jogos desejados para a pesquisa (influenciam no tempo de execução e na variabilidade de recomendações).
2.  **Passo 1 (IA):** OrchestratorAgent → adapter_get_tags.py → Retorna Tags.
3.  **Passo 2 (Scraping):** OrchestratorAgent → adapter_get_game_urls.py → Retorna URLs de jogos.
4.  **Loop de Análise:** Para cada URL:
      - OrchestratorAgent → adapter_scrape_details.py (Chama Steam, Metacritic e IA para resumo) → Retorna Dados Consolidados (incluindo notas e os 3 resumos de reviews).
5.  **Passo 3 (Resumo Final):** OrchestratorAgent → adapter_final_recommendation.py (IA) → Retorna o texto justificando a recomendação.

-----

## 🛠️ Tecnologias Utilizadas

| Categoria | Tecnologia | Notas |
| :--- | :--- | :--- |
| *Plataforma Multiagente* | *Jadex Active Components (Micro)* | Orquestração do ciclo de vida e das tarefas em Java. |
| *Inteligência Artificial* | *Google Gemini API* | Geração de tags, análise de sentimento e geração de resumos finais. |
| *Comunicação* | *Google Gson* | Conversão de dados (JSON ↔ Java Objects) no PythonExecutor. |
| *Web Scraping* | *Requests* e *BeautifulSoup* | Coleta de dados estáticos da Steam e Metacritic. |
| *Variáveis de Ambiente* | *Python-dotenv* | Carregamento da chave de API de forma segura. |

-----

## 🚀 Como Executar

Para o desenvolvimento do projeto, o ambiente utilizado foi o Codespaces do GitHub. Recomendamos que o use também.

### Pré-requisitos


1.  **Java JDK (11+):** Necessário para a plataforma Jadex.
2.  **Python 3 e Pip:** Necessário para todos os adaptadores.
3.  **Maven:** Necessário para compilar e executar o projeto Java.
4.  **Chave de API do Google Gemini:** Configure-a no arquivo `.env` (veja a seção Instalação).

### Instalação

Siga os passos no terminal do Codespaces para preparar o ambiente:

1.  **Configurar Chave de API:**
    Encontre o arquivo `.env` na raiz do projeto e adicione sua chave.

    ```
    GOOGLE_API_KEY="SEU TOKEN AQUI"
    ```

2.  **Instalar Dependências do Sistema (Maven e Python):**

    ```bash
    apt update
    apt install maven python3 python3-pip
    ```

3.  **Instalar Dependências Python (Bibliotecas):**

    ```bash
    pip install -r requirements.txt
    ```

### Uso

Execute o projeto através do Maven. O comando fará a compilação e iniciará a plataforma de agentes Jadex:

1.  **Navegue para o Diretório Principal:**

    ```bash
    cd meu-primeiro-jadex
    ```

2.  **Compilar e Instalar o Agente Java:**

    ```bash
    mvn clean install
    ```

3.  **Executar a Aplicação:**
   Depois de executar os comandos anteriores, execute apenas este quando quiser uma recomendação de jogo.

    ```bash
    mvn exec:java
    ```

O sistema será iniciado e o `OrchestratorAgent` pedirá a **descrição do jogo** e o **número de jogos** (para ele pesquisar e decidir entre eles) diretamente no console do terminal.
Use Ctrl + C quando quiser parar a execução.

-----

## 🗺️ Roadmap

  - [ ] Implementar funcionalidade de análise de compatibilidade para comparar os requisitos de hardware do jogo com os recursos do dispositivo do usuário.
  - [ ] Implementar recomendação de período de compra com base no histórico de preços do jogo na Steam.

-----
