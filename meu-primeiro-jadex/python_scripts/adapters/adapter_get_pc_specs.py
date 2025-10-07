import sys
import json
import os

sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from core import pc_specs_non_interactive as pc_specs

if __name__ == "__main__":
    specs = pc_specs.get_pc_specs_no_prompt()
    if specs:
        print(json.dumps(specs))
    else:
        print(json.dumps({"erro": "Arquivo de especificações não encontrado."}))