import json
import os
import google.generativeai as genai
from dotenv import load_dotenv

def recommender_agent_gemini():
    """
    Agente que utiliza a API do Gemini para analisar uma lista de produtos
    e recomendar os melhores com base na necessidade do usuário.
    """

    # Usa a API key
    try:
        load_dotenv()
        api_key = os.environ.get("GEMINI_API_KEY")
        if not api_key:
            print("❌ ERRO: Chave de API do Gemini não encontrada no arquivo .env")
            return
        genai.configure(api_key=api_key)
        print("API do Gemini configurada com sucesso.")
    except Exception as e:
        print(f"❌ Erro ao configurar a API: {e}")
        return

    # Carrega os dados dos produtos do arquivo JSON
    try:
        with open('produtos_finais.json', 'r', encoding='utf-8') as f:
            dados_produtos = json.load(f)
    except FileNotFoundError:
        print("❌ ERRO: Arquivo 'produtos_finais.json' não encontrado. Execute o scraper primeiro.")
        return

    # Simulação da entrada do usuário
    necessidade_usuario = "Estou procurando um notebook que seja leve, fácil de carregar para a faculdade e bom para estudar e fazer trabalhos. Não preciso para jogos pesados."
    print(f"\nNecessidade do usuário: '{necessidade_usuario}'")

    # Prompt
    prompt = f"""
    Você é um assistente de compras especialista em tecnologia. Sua tarefa é analisar uma lista de produtos (cujo tema foi escolhido pelo usuário) em formato JSON e recomendar os 3 melhores para um usuário com uma necessidade específica.

    Necessidade do Usuário: "{necessidade_usuario}"

    Lista de Produtos (JSON):
    {json.dumps(dados_produtos, indent=2, ensure_ascii=False)}

    Instruções:
    1. Analise cada produto na lista, considerando o título, preço, avaliação em estrelas, número de avaliações e a descrição curta e longa.
    2. Compare as características dos produtos com a necessidade do usuário.
    3. Retorne sua resposta EXCLUSIVAMENTE em formato JSON, nada mais. O JSON deve ser uma lista contendo 3 objetos.
    4. Cada objeto deve ter as seguintes chaves: "id", "titulo", "preco" e "justificativa".
    5. A "justificativa" deve ser um texto curto explicando por que aquele produto é uma boa recomendação para o usuário.
    """

    # Resposta da IA
    try:
        model = genai.GenerativeModel('models/gemini-2.0-flash')
        response = model.generate_content(prompt)
        
        # Limpa a resposta da IA para extrair apenas o JSON
        json_response_text = response.text.strip().replace("```json", "").replace("```", "")
        
        # Converte a string JSON da resposta em um objeto Python
        recomendacoes = json.loads(json_response_text)
        
        print("\n=======================================================")
        print("🏆 TOP 3 RECOMENDAÇÕES GERADAS PELA IA 🏆")
        print("=======================================================")

        for i, rec in enumerate(recomendacoes):
            print(f"\n----- Posição #{i + 1} -----")
            print(f"Produto: {rec['titulo']}")
            print(f"Preço: {rec['preco']}")
            print(f"ID do Produto: {rec['id']}")
            print("\nJustificativa da IA:")
            print(rec['justificativa'])

    except Exception as e:
        print(f"\n❌ ERRO durante a comunicação com a API do Gemini ou processamento da resposta.")
        print(f"Tipo de Erro: {type(e).__name__}")
        print(f"Detalhes: {e}")


if __name__ == "__main__":
    recommender_agent_gemini()