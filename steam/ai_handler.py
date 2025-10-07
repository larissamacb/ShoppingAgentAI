import google.generativeai as genai
import json

API_KEY = 'SUA CHAVE API AQUI'
MODEL_NAME = 'gemini-2.5-flash'

def get_tags_from_ia(description, tag_map):
    """Usa a IA para escolher as melhores tags de uma lista pré-definida."""
    try:
        genai.configure(api_key=API_KEY)
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
        print(f"Erro na IA (gerar tags): {e}")
        return []

def analyze_requirements_with_ia(user_pc, min_req_text, rec_req_text):
    """Usa a IA com regras de MÁXIMO RIGOR para analisar os requisitos de hardware."""
    try:
        genai.configure(api_key=API_KEY)
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
        print(f"Erro na IA (análise de requisitos): {e}")
        return "Não foi possível analisar os requisitos."

def generate_final_recommendations_with_ia(user_query, games_data):
    """Gera um parágrafo de resumo recomendando os melhores jogos da lista encontrada."""
    try:
        genai.configure(api_key=API_KEY)
        model = genai.GenerativeModel(MODEL_NAME)
        
        prompt = f"""
        Aja como um assistente de jogos. O usuário pesquisou por: "{user_query}".
        Estes são os dados dos jogos encontrados: {json.dumps(games_data, indent=2, ensure_ascii=False)}
        Com base nisso, escreva um parágrafo de "Recomendação da IA". Comece DIRETAMENTE com a recomendação (ex: "Baseado na sua busca..."), sem usar "Olá!" ou qualquer outra saudação. Seja conciso. Recomende 1 ou 2 jogos da lista e justifique com base nas notas, preço e se o PC do usuário roda o jogo.
        """
        response = model.generate_content(prompt)
        return response.text.strip()
    except Exception as e:
        print(f"Erro na IA (resumo final): {e}")
        return "Não foi possível gerar o resumo."
