import google.generativeai as genai
from dotenv import load_dotenv
import json
import sys
import os
import re # Necessario para a transliteracao

# ***** SOLUÇÃO PARA O LOG VERBOSO DA IA (E0000 00:00...) *****
# Configura o gRPC para suprimir logs verbosos de nível WARNING e INFO, mantendo apenas ERROR.
os.environ['GRPC_VERBOSITY'] = 'ERROR' 
os.environ['GRPC_TRACE'] = 'off' 

MODEL_NAME = 'gemini-2.5-flash'

def transliterate_pt(text):
    """Substitui caracteres acentuados ou especiais (como ç e ã) por suas versoes sem acento."""
    if not isinstance(text, str):
        return text

    replacements = {
        'á': 'a', 'à': 'a', 'ã': 'a', 'â': 'a',
        'é': 'e', 'ê': 'e',
        'í': 'i',
        'ó': 'o', 'ô': 'o', 'õ': 'o',
        'ú': 'u',
        'ç': 'c',
        'Á': 'A', 'À': 'A', 'Ã': 'A', 'Â': 'A',
        'É': 'E', 'Ê': 'E',
        'Í': 'I',
        'Ó': 'O', 'Ô': 'O', 'Õ': 'O',
        'Ú': 'U',
        'Ç': 'C',
    }
    
    # Cria a expressao regular para buscar todos os caracteres na lista de substituicao
    pattern = '[' + re.escape(''.join(replacements.keys())) + ']'
    
    # Usa re.sub para substituir todas as ocorrencias
    return re.sub(pattern, lambda match: replacements.get(match.group(0), match.group(0)), text)


def get_tags_from_ia(description, tag_map):
    """Usa a IA para escolher as melhores tags de uma lista pre-definida."""
    # NOTE: Nao aplicamos transliteracao aqui, pois as tags devem ser os nomes oficiais da Steam.
    try:
        load_dotenv()
        genai.configure(api_key=os.environ.get("GOOGLE_API_KEY"))
        model = genai.GenerativeModel(MODEL_NAME)
        
        tag_names = list(tag_map.keys())
        prompt = f"""
        Voce e um classificador especialista em tags da loja Steam. Sua tarefa e analisar a descricao de um usuario e escolher, da lista de tags oficiais fornecida abaixo, as 4 tags que melhor correspondem ao pedido.
        Lista de Tags Oficiais Disponiveis: {tag_names}
        Analise a seguinte descricao do usuario: "{description}"
        Sua resposta deve ser APENAS uma lista JSON de strings, contendo os nomes exatos das tags como aparecem na lista.
        """
        response = model.generate_content(prompt)
        cleaned_response = response.text.strip().replace('```json', '').replace('```', '')
        tag_list = json.loads(cleaned_response)
        validated_tags = [tag for tag in tag_list if tag in tag_map]
        return validated_tags
    except Exception as e:
        sys.stderr.write(f"Erro na IA (gerar tags): {e}\n")
        return []

# A função summarize_reviews_with_ia() foi removida daqui.
    

def generate_final_recommendations_with_ia(user_query, games_data):
    """Gera um paragrafo de resumo recomendando os melhores jogos da lista encontrada, usando Metascore e as listas de reviews."""
    try:
        load_dotenv()
        genai.configure(api_key=os.environ.get("GOOGLE_API_KEY"))
        model = genai.GenerativeModel(MODEL_NAME)
        
        games_json = json.dumps(games_data, indent=2, ensure_ascii=False)
        
        prompt = f"""
        Aja como um assistente de jogos. O usuario pesquisou por: "{user_query}".
        
        Estes sao os dados dos jogos encontrados (incluindo Metascore e as listas completas de reviews de usuarios, ex: 'positive_reviews'): {games_json}
        
        **INSTRUCOES OBRIGATORIAS:**
        1. **Foco:** Recomende 1 ou 2 jogos da lista que sejam mais adequados a descricao do usuario.
        2. **Justificativa:** Justifique a escolha baseando-se em:
           - **Descricao do Jogo:** Relacione com a busca do usuario.
           - **Notas:** Mencione o 'Metascore' e o 'User Score'.
           - **Preco** (Se e caro ou barato).
           - **Experiencia do Usuario:** Use as listas de comentarios (ex: 'positive_reviews', 'mixed_reviews', 'negative_reviews') para analisar e descrever a experiencia real do usuario, incluindo as opiniões deles.
        3. **Formato:** Escreva um paragrafo de "Recomendacao da IA". Comece DIRETAMENTE com a recomendacao (ex: "Baseado na sua busca..."). **NAO inclua o titulo "Recomendacao da IA:".**
        4. **Linguagem:** Use portugues claro e simples, adequado para um usuario leigo
        """
        response = model.generate_content(prompt)
        
        # APLICANDO A TRANSLITERAÇÃO NO TEXTO FINAL
        return transliterate_pt(response.text.strip())
    except Exception as e:
        sys.stderr.write(f"Erro na IA (resumo final): {e}\n")
        return "Nao foi possivel gerar o resumo."