import google.generativeai as genai
from dotenv import load_dotenv
import json
import sys
import os

MODEL_NAME = 'gemini-2.5-flash'

def get_tags_from_ia(description, tag_map):
    """Usa a IA para escolher as melhores tags de uma lista pré-definida."""
    try:
        load_dotenv()
        genai.configure(api_key=os.environ.get("GOOGLE_API_KEY"))
        model = genai.GenerativeModel(MODEL_NAME)
        
        tag_names = list(tag_map.keys())
        prompt = f"""
        Você é um classificador especialista em tags da loja Steam. Sua tarefa é analisar a descrição de um usuário e escolher, da lista de tags oficiais fornecida abaixo, as 4 tags que melhor correspondem ao pedido.
        Lista de Tags Oficiais Disponíveis: {tag_names}
        Analise a seguinte descrição do usuário: "{description}"
        Sua resposta deve ser APENAS uma lista JSON de strings, contendo os nomes exatos das tags como aparecem na lista.
        """
        response = model.generate_content(prompt)
        cleaned_response = response.text.strip().replace('```json', '').replace('```', '')
        tag_list = json.loads(cleaned_response)
        validated_tags = [tag for tag in tag_list if tag in tag_map]
        return validated_tags
    except Exception as e:
        sys.stderr.write(f"Erro na IA (gerar tags): {e}\n") # Usa sys.stderr
        return []

def analyze_requirements_with_ia(user_pc, min_req_text, rec_req_text):
    """Usa a IA com regras de MÁXIMO RIGOR para analisar os requisitos de hardware."""
    try:
        load_dotenv()
        genai.configure(api_key=os.environ.get("GOOGLE_API_KEY"))
        model = genai.GenerativeModel(MODEL_NAME)

        prompt = f"""
        Aja como um analista de hardware extremamente lógico e literal. Sua única tarefa é comparar as especificações do PC de um usuário com os requisitos de um jogo e dar um veredito preciso, seguindo as regras abaixo sem desvio.

        **REGRAS OBRIGATÓRIAS:**
        1.  **CPU:** Ignore a família (i5 vs i7). Foque SOMENTE na geração e no modelo. Um i5-10400F é de 10ª geração. Um i7-4770K é de 4ª geração. 10 é maior que 4, portanto, o i5-10400F é superior. Se a geração do usuário for igual ou maior que a recomendada, ele atende.
        2.  **GPU:** Compare as placas. Uma AMD RX 6500 XT é da série 6000. Ela é superior a uma Nvidia GTX 1060 (série 1000) e a uma AMD RX 480 (série 400). Se a GPU do usuário for de uma série mais nova e de performance comparável ou superior à recomendada, ele atende.
        3.  **RAM:** Compare os números diretamente.
        4.  **VEREDITO FINAL:**
            - Se o PC do usuário atende ou supera **TODOS** os 3 campos (CPU, GPU, RAM) dos requisitos **RECOMENDADOS**, a resposta é "recomendados".
            - Se o PC atende a **TODOS** os requisitos **MÍNIMOS**, mas falha em pelo menos um dos recomendados, o veredito é "mínimos".
            - Se o PC falha em atender a **QUALQUER UM** dos requisitos **MÍNIMOS**, a resposta é "não roda bem".

        **DADOS PARA ANÁLISE:**

        **PC do Usuário:**
        - CPU: {user_pc.get('cpu')}
        - GPU: {user_pc.get('gpu')}
        - RAM: {user_pc.get('ram')} GB

        **Requisitos Mínimos do Jogo:**
        "{min_req_text}"

        **Requisitos Recomendados do Jogo:**
        "{rec_req_text}"

        Sua resposta deve ser APENAS UMA das três opções abaixo, sem saudações, explicações ou texto adicional:
        1. "✅ Sim, atende aos recomendados."
        2. "⚠️ Sim, atende aos mínimos."
        3. "❌ Não, provavelmente não roda bem."
        """
        response = model.generate_content(prompt)
        return response.text.strip()
    except Exception as e:
        sys.stderr.write(f"Erro na IA (análise de requisitos): {e}\n") # Usa sys.stderr
        return "Não foi possível analisar os requisitos."


def summarize_reviews_with_ia(positive_reviews, mixed_reviews, negative_reviews):
    """Usa a IA para resumir os principais pontos dos comentários por sentimento."""
    try:
        load_dotenv()
        genai.configure(api_key=os.environ.get("GOOGLE_API_KEY"))
        model = genai.GenerativeModel(MODEL_NAME)
        
        # Consolida os dados em uma estrutura clara para o prompt
        reviews_data = {
            "POSITIVAS": "\n- " + "\n- ".join(positive_reviews),
            "MISTAS": "\n- " + "\n- ".join(mixed_reviews),
            "NEGATIVAS": "\n- " + "\n- ".join(negative_reviews),
        }
        
        prompt = f"""
        Você é um analista de sentimento de jogos. Sua tarefa é ler as reviews de usuários 
        (Positivas, Mistas e Negativas) e gerar três parágrafos de resumo concisos, um para cada sentimento.
        Considere e deixe claro a quantidade de comentários coletados em cada seção.
        
        Use as seguintes informações:
        --- REVIEWS POSITIVAS ---
        {reviews_data['POSITIVAS']}
        
        --- REVIEWS MISTAS ---
        {reviews_data['MISTAS']}
        
        --- REVIEWS NEGATIVAS ---
        {reviews_data['NEGATIVAS']}
        
        Sua resposta deve ser APENAS um objeto JSON no formato:
        {{
            "resumo_positivo": "Um resumo conciso sobre o que os usuários mais elogiaram.",
            "resumo_misto": "Um resumo sobre os pontos onde o jogo é divisivo ou neutro.",
            "resumo_negativo": "Um resumo conciso sobre as principais críticas e falhas mencionadas."
        }}
        """
        response = model.generate_content(prompt)
        cleaned_response = response.text.strip().replace('```json', '').replace('```', '')
        
        return json.loads(cleaned_response)
    
    except Exception as e:
        sys.stderr.write(f"Erro na IA (resumo de reviews): {e}\n") # Usa sys.stderr
        return {
            "resumo_positivo": "Não foi possível gerar o resumo positivo.",
            "resumo_misto": "Não foi possível gerar o resumo misto.",
            "resumo_negativo": "Não foi possível gerar o resumo negativo."
        }
    

def generate_final_recommendations_with_ia(user_query, games_data):
    """Gera um parágrafo de resumo recomendando os melhores jogos da lista encontrada, usando Metascore e resumos de reviews."""
    try:
        load_dotenv()
        genai.configure(api_key=os.environ.get("GOOGLE_API_KEY"))
        model = genai.GenerativeModel(MODEL_NAME)
        
        # O games_data virá com os novos campos de resumo (Metascore, Summary Positive, etc.) anexados.
        games_json = json.dumps(games_data, indent=2, ensure_ascii=False)
        
        prompt = f"""
        Aja como um assistente de jogos. O usuário pesquisou por: "{user_query}".
        
        Estes são os dados dos jogos encontrados (incluindo Metascore e resumos de reviews): {games_json}
        
        **INSTRUÇÕES OBRIGATÓRIAS:**
        1. **Foco:** Recomende 1 ou 2 jogos da lista que sejam compatíveis com o PC do usuário (onde "PC Roda?" não é '❌'). Se todos falharem, justifique a falha.
        2. **Justificativa:** Justifique a escolha baseando-se em:
           - **Notas:** Mencione o 'Metascore' e o 'User Score'.
           - **Preço** (Se é caro ou barato).
           - **Experiência do Usuário:** Use os campos 'Summary Positive', 'Summary Mixed', e 'Summary Negative' para descrever a experiência de usuário.
        3. **Formato:** Escreva um parágrafo de "Recomendação da IA". Comece DIRETAMENTE com a recomendação (ex: "Baseado na sua busca...").
        """
        response = model.generate_content(prompt)
        return response.text.strip()
    except Exception as e:
        sys.stderr.write(f"Erro na IA (resumo final): {e}\n") # Usa sys.stderr
        return "Não foi possível gerar o resumo."