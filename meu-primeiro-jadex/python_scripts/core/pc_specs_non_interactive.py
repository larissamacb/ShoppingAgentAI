import json
import os

SPECS_FILE = 'my_pc_specs.json'

def get_pc_specs_no_prompt():
    """Lê as especificações do arquivo na pasta atual."""
    if not os.path.exists(SPECS_FILE):
        return None
    with open(SPECS_FILE, 'r', encoding='utf-8') as f:
        return json.load(f)

def save_pc_specs(specs):
    """Salva as especificações no arquivo na pasta atual."""
    with open(SPECS_FILE, 'w', encoding='utf-8') as f:
        json.dump(specs, f, indent=4)