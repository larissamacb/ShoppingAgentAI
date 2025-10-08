import requests
from bs4 import BeautifulSoup
import re
import json
import sys 

# Definimos as constantes do filtro
POSITIVE_FILTER = "Positive%20Reviews"
NEGATIVE_FILTER = "Negative%20Reviews"
MIXED_FILTER = "Mixed%20Reviews"
LIMIT_REVIEWS = 10 # Limite de 10 reviews por categoria

def get_reviews_from_url_with_fallback(game_name, base_url_user, sentiment_class):
    """
    Tenta extrair até LIMIT_REVIEWS da página de User Reviews e, se necessário, 
    preenche a diferença com reviews da página de Critic Reviews.
    """
    current_reviews = []
    
    # --- 1. TENTATIVA INICIAL: USER REVIEWS ---
    # A URL base já é a página de reviews, então apenas adicionamos o filtro.
    url_user = f"{base_url_user}&filter={sentiment_class}"
    current_reviews.extend(_scrape_single_page(url_user, sentiment_class, LIMIT_REVIEWS))
    
    remaining_needed = LIMIT_REVIEWS - len(current_reviews)
    
    if remaining_needed > 0:
        # --- 2. FALLBACK: CRITIC REVIEWS ---
        
        search_name = re.sub(r'[^a-zA-Z0-9\s-]', '', game_name).replace(' ', '-').lower()
        base_url_critic = f"https://www.metacritic.com/game/{search_name}/critic-reviews/"
        
        url_critic = f"{base_url_critic}?filter={sentiment_class}"
        
        # Coleta apenas o número que falta
        fallback_reviews = _scrape_single_page(url_critic, sentiment_class, remaining_needed)
        current_reviews.extend(fallback_reviews)
        
    return current_reviews

def _scrape_single_page(url, sentiment_class, limit):
    """
    Função auxiliar para fazer a requisição e extrair reviews de uma URL específica.
    A tag de sentimento é simplificada (ex: [POSITIVE]).
    """
    comments = []
    headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36'}
    
    try:
        response = requests.get(url, headers=headers, timeout=15)
        if response.status_code != 200:
            sys.stderr.write(f"AVISO: Falha ao carregar reviews ({sentiment_class}). Status: {response.status_code}\n")
            return comments

        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Seleciona as quotes, que são o corpo da review
        comment_quote_elems = soup.select('div.c-siteReview_quote')
        
        for quote_div in comment_quote_elems[:limit]:
            comment_span = quote_div.find('span')
            if comment_span:
                # CORREÇÃO: Tag simplificada (ex: [POSITIVE])
                tag = sentiment_class.upper() 
                comments.append(f"[{tag}]: {comment_span.text.strip()}")
                
    except requests.exceptions.Timeout:
        sys.stderr.write(f"ERRO: Timeout (15s) ao buscar reviews para {sentiment_class}.\n")
    except Exception as e:
        sys.stderr.write(f"ERRO: Falha na extração de reviews {sentiment_class}: {e}\n")
            
    return comments

def scrape_metacritic(game_name, steam_id): 
    """
    Busca Metascore, User Score (na página principal) e coleta reviews (com fallback de críticos).
    """
    # 1. Limpa e formata o nome do jogo para o "slug" da URL
    search_name = re.sub(r'[^a-zA-Z0-9\s-]', '', game_name).replace(' ', '-').lower()
    
    # URL da Página Principal do Jogo (Onde estão os dois scores)
    main_url = f"https://www.metacritic.com/game/{search_name}/"
    
    # URL base para User Reviews (Usada apenas para o fallback de reviews)
    base_url_user = f"https://www.metacritic.com/game/{search_name}/user-reviews/?platform=pc"
    
    data = {
        "steam_id": steam_id,
        "metascore": "N/A",  # NOVO CAMPO
        "user_score": "N/A", 
        "reviews_positive": [],
        "reviews_mixed": [],
        "reviews_negative": []
    }
    
    # 2. Primeira requisição para a página principal (pegar scores)
    headers = {'User-Agent': 'Mozilla/5.0'}
    try:
        response = requests.get(main_url, headers=headers, timeout=10)
        if response.status_code != 200:
            sys.stderr.write(f"AVISO: Página principal do Metacritic para '{game_name}' não encontrada. Status: {response.status_code}\n")
            # Retorna dados vazios se a página principal falhar
            return data
                 
        soup = BeautifulSoup(response.text, 'html.parser')

        # Extração do METASCORE
        # Busca o div que NÃO tem 'user' na classe
        metascore_div = soup.find('div', class_=re.compile(r'c-siteReviewScore_medium(?!(.*user))'))
        if metascore_div:
            score_span = metascore_div.find('span')
            if score_span:
                data['metascore'] = score_span.text.strip()

        # Extração do USER SCORE
        # Busca o div que contém 'user' na classe (sua identificação anterior)
        user_score_div = soup.find('div', class_=re.compile(r'c-siteReviewScore_user'))
        if user_score_div:
            score_span = user_score_div.find('span')
            if score_span:
                data['user_score'] = score_span.text.strip()
        
    except Exception as e:
        sys.stderr.write(f"ERRO: Falha na requisição inicial Metacritic (scores): {e}\n")
        return data

    # 3. Coletar Reviews com Fallback (usa a URL de user reviews para o scraping)
    
    data['reviews_positive'] = get_reviews_from_url_with_fallback(game_name, base_url_user, POSITIVE_FILTER)
    data['reviews_mixed'] = get_reviews_from_url_with_fallback(game_name, base_url_user, MIXED_FILTER)
    data['reviews_negative'] = get_reviews_from_url_with_fallback(game_name, base_url_user, NEGATIVE_FILTER)

    # Retorna o dicionário com todos os campos esperados
    return data