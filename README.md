# 🛒 Shopping AgentAI

Um sistema de recomendação de jogos que utiliza a plataforma multiagente Jadex para orquestrar o web scraping e a Inteligência Artificial (Gemini) para analisar dados e gerar recomendações personalizadas.

Este projeto foca em coletar dados de múltiplas fontes (Steam e Metacritic) para fornecer uma recomendação baseada em estilo do jogo, preço, notas e sentimento dos usuários.

[](https://shields.io)

-----

## 📝 Índice

  - [Sobre o Projeto](https://www.google.com/search?q=%23-sobre-o-projeto) (🛠️)
  - [✨ Funcionalidades](https://www.google.com/search?q=%23-funcionalidades) (🛠️)
  - [⚙️ Arquitetura do Sistema](https://www.google.com/search?q=%23%EF%B8%8F-arquitetura-do-sistema) (🛠️)
  - [🛠️ Tecnologias Utilizadas](https://www.google.com/search?q=%23%EF%B8%8F-tecnologias-utilizadas) (🛠️)
  - [🚀 Como Executar](https://www.google.com/search?q=%23-como-executar) (🛠️)
      - [Pré-requisitos](https://www.google.com/search?q=%23pr%C3%A9-requisitos)
      - [Instalação](https://www.google.com/search?q=%23instala%C3%A7%C3%A3o)
      - [Uso](https://www.google.com/search?q=%23uso)
  - [🗺️ Roadmap](https://www.google.com/search?q=%23%EF%B8%8F-roadmap) (🛠️)

-----

## 📖 Sobre o Projeto

O objetivo do *GameRecommenderAI* é simplificar a escolha de jogos, automatizando a coleta de dados e aplicando lógica avançada de *Inteligência Artificial* para gerar um veredito final.

O processo é iniciado pelo usuário que descreve o tipo de jogo desejado. Em resposta, o sistema:

1.  Usa a *IA* para transformar a descrição em *tags de busca* (Steam).
2.  Busca jogos correspondentes na *Steam*.
3.  Coleta *notas e reviews* no *Metacritic*.
4.  Gera um *resumo conciso* dos sentimentos dos usuários (Positivo, Misto, Negativo).
5.  Emite uma recomendação final, justificando a escolha com base em *Descrição do jogo*, *Metascore*, *User Score* e *Resumos de Review*.

-----

## ✨ Funcionalidades

  - *Tradução de Intenção:* Utiliza a *IA (Gemini)* para converter a descrição textual do usuário em tags oficiais da Steam.
  - *Coleta de Dados Dupla:* Combina informações:
      - *Steam:* Nome, Preço, Descrição e Tags.
      - *Metacritic:* *Metascore* (Nota da Crítica) e *User Score* (Nota do Usuário) extraídos da página principal.
  - *Análise de Sentimento (IA):* Coleta até *10 reviews* por sentimento (Positivas, Mistas e Negativas) e utiliza a IA para criar um *resumo conciso* de cada sentimento.
  - *Recomendação Final Otimizada:* Gera um parágrafo de recomendação justificado, com base nas notas, descrição do jogo, preço e experiência resumida dos usuários.
  - *Compatibilidade Terminal:* Todo o output da IA é *transliterado* (sem acentos ou cedilha) para garantir a correta exibição em qualquer console.

-----

## ⚙️ Arquitetura do Sistema

O sistema é orquestrado pela plataforma de agentes **Jadex** e executa ações de lógica de negócio através de scripts **Python**. O fluxo de trabalho é o seguinte:

1.  **Entrada do Usuário:** O processo inicia quando o usuário fornece o nome do produto desejado.
2.  **Ativação dos Agentes Scrapers:** O agente principal (ou o próprio Jadex) ativa três `Agentes Scrapers`. Cada um é responsável por um site:
      - `AmazonScraperAgent`: Realiza a busca na **Amazon**.
      - `[Site2]ScraperAgent`: *(A ser implementado)* - Responsável por buscar no **[Nome do Site 2]**.
      - `[Site3]ScraperAgent`: *(A ser implementado)* - Responsável por buscar no **[Nome do Site 3]**.
3.  **Coleta de Dados:** Os agentes executam seus scripts Python de scraping em paralelo e coletam as informações listadas na seção de funcionalidades.
4.  **Agente Sumarizador:** Após a coleta, os dados são enviados para o `SummarizerAgent`. Este agente processa o texto para criar um resumo dos prós e contras de cada produto.
5.  **Agente Recomendador:** O `RecommenderAgent` recebe os dados estruturados de todos os produtos e os resumos dos comentários. Ele aplica uma lógica de decisão para comparar os produtos com base em critérios como custo-benefício, popularidade e feedback geral dos usuários.
6.  **Saída para o Usuário:** O agente gera o ranking Top 5 e o apresenta ao usuário, finalizando o ciclo.

-----

## 🛠️ Tecnologias Utilizadas

| Categoria | Tecnologia | Notas |
| :--- | :--- | :--- |
| *Plataforma Multiagente* | *Jadex Active Components (Micro)* | Orquestração do ciclo de vida e das tarefas em Java. |
| *Inteligência Artificial| **Google Gemini API* | Geração de tags, análise de sentimento e geração de resumos finais. |
| *Comunicação* | *Google Gson* | Conversão de dados (JSON ↔ Java Objects) no PythonExecutor. |
| *Web Scraping* | *Requests* e *BeautifulSoup* | Coleta de dados estáticos da Steam e Metacritic. |
| *Variáveis de Ambiente| **Python-dotenv* | Carregamento da chave de API de forma segura. |

-----

## 🚀 Como Executar

...

### Pré-requisitos

...

### Instalação

...

### Uso

...

-----

## 🗺️ Roadmap

  - [ ] Implementar funcionalidade de análise de compatibilidade para comparar os requisitos de hardware do jogo com os recursos do dispositivo do usuário.
  - [ ] Implementar recomendação de período de compra com base no histórico de preços do jogo na Steam.

-----
