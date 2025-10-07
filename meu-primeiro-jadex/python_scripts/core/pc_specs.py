import json
import os

SPECS_FILE = 'my_pc_specs.json'

def get_pc_specs():
    """Carrega as especificações do PC. Se não existirem, pergunta ao usuário."""
    if not os.path.exists(SPECS_FILE):
        print("--- Configuração Inicial do seu PC ---")
        print("Precisamos saber as especificações do seu PC para comparar com os jogos.")
        specs = {
            'cpu': input("Qual é o seu processador (CPU)? (ex: Intel Core i5-9400F): "),
            'gpu': input("Qual é a sua placa de vídeo (GPU)? (ex: NVIDIA GeForce GTX 1660): "),
            'ram': int(input("Quanta memória RAM você tem (em GB)? (ex: 16): "))
        }
        save_pc_specs(specs)
        return specs
    else:
        with open(SPECS_FILE, 'r', encoding='utf-8') as f:
            return json.load(f)

def save_pc_specs(specs):
    """Salva as especificações em um arquivo JSON."""
    with open(SPECS_FILE, 'w', encoding='utf-8') as f:
        json.dump(specs, f, indent=4)
    print(f"Especificações do PC salvas em '{SPECS_FILE}'!")

def update_pc_specs():
    """Permite ao usuário atualizar suas especificações."""
    print("--- Atualização das Especificações do PC ---")
    current_specs = get_pc_specs()
    print(f"Configuração atual: {current_specs}")

    new_specs = {
        'cpu': input(f"Novo processador (CPU) ou pressione Enter para manter '{current_specs['cpu']}': ") or current_specs['cpu'],
        'gpu': input(f"Nova placa de vídeo (GPU) ou pressione Enter para manter '{current_specs['gpu']}': ") or current_specs['gpu'],
        'ram': input(f"Nova RAM (GB) ou pressione Enter para manter '{current_specs['ram']}': ") or current_specs['ram']
    }
    new_specs['ram'] = int(new_specs['ram'])
    save_pc_specs(new_specs)
    return new_specs
