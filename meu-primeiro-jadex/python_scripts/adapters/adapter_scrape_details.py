import sys
import json
import os

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from core import steam_scraper
from core import metacritic_scraper

if __name__ == "__main__":
    if len(sys.argv) > 1:
        game_url = sys.argv[1]
        
        # Passo 1: Scrape Steam
        steam_data = steam_scraper.scrape_steam_page_details(game_url)
        if not steam_data:
            print(json.dumps({"erro": f"Falha ao coletar dados da Steam para {game_url}"}))
            sys.exit()

        # Passo 2: Scrape Metacritic
        game_name = steam_data.get('name')
        meta_data = metacritic_scraper.scrape_metacritic(game_name)

        # Passo 3: Juntar os resultados
        final_data = {**steam_data, **meta_data}
        print(json.dumps(final_data, ensure_ascii=False))
    else:
        print(json.dumps({"erro": "Nenhuma URL de jogo fornecida."}))