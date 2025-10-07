import sys
import json
import os

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from core import ai_handler

if __name__ == "__main__":
    if len(sys.argv) > 3:
        user_pc_json = sys.argv[1]
        min_req_text = sys.argv[2]
        rec_req_text = sys.argv[3]
        
        user_pc = json.loads(user_pc_json)
        
        result = ai_handler.analyze_requirements_with_ia(user_pc, min_req_text, rec_req_text)
        print(json.dumps({"resultado": result}))
    else:
        print(json.dumps({"erro": "Argumentos insuficientes."}))