import sys
import json
import os

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from core import ai_handler

if __name__ == "__main__":
    if len(sys.argv) > 2:
        user_query = sys.argv[1]
        games_data_json = sys.argv[2]
        
        games_data = json.loads(games_data_json)
        
        summary = ai_handler.generate_final_recommendations_with_ia(user_query, games_data)
        print(json.dumps({"resumo": summary}))
    else:
        print(json.dumps({"erro": "Argumentos insuficientes."}))