import sys
import json
import os

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from core import steam_scraper

def load_tag_map():
    path = os.path.join(os.path.dirname(__file__), '..', 'core', 'steam_tags.json')
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)

if __name__ == "__main__":
    if len(sys.argv) > 2:
        tags_from_ai_json = sys.argv[1]
        num_games = int(sys.argv[2])
        tag_map = load_tag_map()
        
        tags_from_ai = json.loads(tags_from_ai_json)
        tag_ids = [str(tag_map.get(tag)) for tag in tags_from_ai if tag_map.get(tag)]
        
        if not tag_ids:
            print(json.dumps({"erro": "Não foi possível encontrar IDs para as tags."}))
        else:
            search_url = f"https://store.steampowered.com/search/?tags={','.join(tag_ids)}&ndl=1"
            urls = steam_scraper.get_game_urls_from_search(search_url, num_games)
            print(json.dumps({"urls": urls}))
    else:
        print(json.dumps({"erro": "Argumentos insuficientes."}))