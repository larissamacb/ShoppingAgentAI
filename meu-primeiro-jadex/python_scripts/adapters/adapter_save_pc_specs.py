import sys
import json
import os

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from core import pc_specs_non_interactive as pc_specs

if __name__ == "__main__":
    if len(sys.argv) > 1:
        specs_json_string = sys.argv[1]
        try:
            specs_data = json.loads(specs_json_string)
            pc_specs.save_pc_specs(specs_data)
            print(json.dumps({"status": "sucesso", "dados_salvos": specs_data}))
        except json.JSONDecodeError:
            print(json.dumps({"erro": "String JSON inválida fornecida."}))
    else:
        print(json.dumps({"erro": "Nenhum dado de especificação fornecido."}))