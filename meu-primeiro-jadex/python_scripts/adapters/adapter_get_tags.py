import sys
import json
import os

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from core import ai_handler

def load_tag_map():
    path = os.path.join(os.path.dirname(__file__), '..', 'core', 'steam_tags.json')
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)

if __name__ == "__main__":
    if len(sys.argv) > 1:
        user_description = sys.argv[1]
        tag_map = load_tag_map()
        tags = ai_handler.get_tags_from_ia(user_description, tag_map)
        print(json.dumps({"tags": tags}))
    else:
        print(json.dumps({"erro": "Nenhuma descrição fornecida."}))