# 🛒 Shopping AgentAI

Um sistema multiagente em desenvolvimento com Jadex e Python para buscar, analisar e recomendar produtos com base em scraping de dados de múltiplos e-commerces. **Ainda está em construção, então nada do que está escrito aqui é definitivo ou está completo.**

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

Este projeto foi criado para solucionar o desafio de escolher o melhor produto em meio a tantas opções online. Através de um sistema multiagente, ele automatiza o processo de coleta de dados, análise de avaliações e geração de recomendações inteligentes.

O usuário simplesmente fornece o nome de um produto (ex: "fone de ouvido sem fio") e o sistema ativa uma série de agentes autônomos que trabalham em conjunto para entregar um ranking com o Top 5 de recomendações, com justificativas claras para cada escolha.

-----

## ✨ Funcionalidades

  - **Scraping Paralelo:** Três agentes coletores de dados buscam informações simultaneamente em diferentes lojas.
  - **Coleta de Dados Abrangente:** Para cada produto, são coletados:
      - Título
      - Preço
      - Avaliação (estrelas)
      - Quantidade de Reviews
      - Link da Imagem
      - Link do Produto
      - Descrição
      - Comentários Positivos e Negativos
  - **Análise e Sumarização de Comentários:** Um agente dedicado processa os comentários coletados e extrai os pontos mais relevantes, criando um resumo conciso.
  - **Recomendação Inteligente:** Um agente final analisa todos os dados (preço, avaliação, popularidade e resumo dos comentários) para gerar um Top 5 de recomendações.
  - **Justificativas Claras:** Cada item recomendado vem acompanhado de uma justificativa baseada nos dados analisados.

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

  - **Bibliotecas Python:**
      - `Selenium` : Para o web scraping.

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

...

-----
