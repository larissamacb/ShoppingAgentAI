import requests
from bs4 import BeautifulSoup
import re

def scrape_metacritic(game_name):
    """
    Busca apenas o User Score no Metacritic, de forma mais robusta.
    """
    search_name = re.sub(r'[^a-zA-Z0-9\s-]', '', game_name).replace(' ', '-').lower()
    url = f"https://www.metacritic.com/game/{search_name}/"
    headers = {'User-Agent': 'Mozilla/5.0'}
    
    data = {"user_score": "N/A", "reviews_count": "N/A", "comments": []}
    
    try:
        response = requests.get(url, headers=headers)
        if response.status_code != 200:
            print(f"AVISO: Página do Metacritic para '{game_name}' não encontrada (404).")
            return data
             
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Foca em encontrar apenas a seção de User Score
        user_score_elem = soup.find('div', class_=re.compile(r'c-productScoreInfo_scoreNumber'))
        if user_score_elem:
            data['user_score'] = user_score_elem.text.strip()

        # Comentários
        comment_elems = soup.select('.c-reviewsSection_userReview .c-siteReview_quote')
        for comment in comment_elems[:2]:
             data['comments'].append(comment.text.strip())

        return data
        
    except Exception as e:
        print(f"Erro ao fazer scraping do Metacritic para '{game_name}': {e}")
        return data
