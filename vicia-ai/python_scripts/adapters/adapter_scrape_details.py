import sys
import json
import os
import re

# Adiciona o caminho para módulos na pasta superior (core e ai_handler)
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from core import steam_scraper
from core import metacritic_scraper


if __name__ == "__main__":
    if len(sys.argv) > 1:
        game_url = sys.argv[1]
        
        # Tentamos extrair o Steam ID da URL
        match = re.search(r'/app/(\d+)/', game_url)
        steam_id = match.group(1) if match else "N/A"
        
        # Passo 1: Scrape Steam
        steam_data = steam_scraper.scrape_steam_page_details(game_url)
        
        if not steam_data:
            sys.stderr.write(f"Falha crítica ao coletar dados da Steam para {game_url}\n")
            print(json.dumps({"erro": f"Falha ao coletar dados da Steam para {game_url}"}))
            sys.exit()

        # Passo 2: Scrape Metacritic
        game_name = steam_data.get('name')
        meta_data = metacritic_scraper.scrape_metacritic(game_name, steam_id=steam_id) 
        
        # Passo 3: Coletar Reviews
        positive = meta_data.get('reviews_positive', [])
        mixed = meta_data.get('reviews_mixed', [])
        negative = meta_data.get('reviews_negative', [])
        
        # Passo 4: Juntar os resultados
        final_data = {
            **steam_data, 
            "user_score": meta_data.get('user_score', 'N/A'),
            "reviews_positive": positive,
            "reviews_mixed": mixed,
            "reviews_negative": negative
        }
        
        # Apenas o JSON final no stdout
        print(json.dumps(final_data, ensure_ascii=False))
        
    else:
        sys.stderr.write("Nenhuma URL de jogo fornecida.\n")
        print(json.dumps({"erro": "Nenhuma URL de jogo fornecida."}))