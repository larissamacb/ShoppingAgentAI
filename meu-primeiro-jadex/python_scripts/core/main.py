import pc_specs
import ai_handler
import steam_scraper
import metacritic_scraper
import re
import json
import os

def load_tag_map():
    """Carrega o mapa de tags do arquivo JSON."""
    if not os.path.exists('steam_tags.json'):
        print("ERRO: Arquivo 'steam_tags.json' não encontrado.")
        return None
    with open('steam_tags.json', 'r', encoding='utf-8') as f:
        return json.load(f)

def clean_description(description):
    """Remove o 'Sobre este jogo' e espaços excessivos do início da descrição."""
    if description.lower().startswith("sobre este jogo"):
        description = description[len("sobre este jogo"):].strip()
    elif description.lower().startswith("sobre este conteúdo"):
        description = description[len("sobre este conteúdo"):].strip()
    return description.lstrip()

def main():
    print("--- Bem-vindo ao Recomendador de Jogos da Steam! ---")
    
    tag_map = load_tag_map()
    if not tag_map: return

    user_pc = pc_specs.get_pc_specs()
    
    while True:
        user_description = input("\nDescreva o tipo de jogo que você quer jogar (ou digite 'atualizar pc' ou 'sair'): ")

        if user_description.lower() == 'sair': break
        if user_description.lower() == 'atualizar pc':
            user_pc = pc_specs.update_pc_specs()
            continue

        num_games_to_search = 5
        num_input = input("Quantos jogos você quer pesquisar? (Padrão: 5, pressione Enter para usar o padrão): ")
        if num_input.isdigit() and int(num_input) > 0:
            num_games_to_search = int(num_input)

        print("\n🤖 Consultando a IA (modo expert) para gerar tags de busca...")
        tags_from_ai = ai_handler.get_tags_from_ia(user_description, tag_map)

        if not tags_from_ai:
            print("Não consegui gerar tags para essa descrição.")
            continue

        print(f"Tags oficiais selecionadas pela IA: {', '.join(tags_from_ai)}")
        
        tag_ids = [tag_map.get(tag) for tag in tags_from_ai if tag_map.get(tag)]
        
        if not tag_ids:
            print("Não foi possível encontrar IDs correspondentes para as tags geradas.")
            continue

        search_url = f"https://store.steampowered.com/search/?tags={','.join(tag_ids)}&ndl=1"
        game_urls = steam_scraper.get_game_urls_from_search(search_url, num_games_to_search)
        
        if not game_urls:
            print("Nenhum jogo encontrado com essa combinação de tags.")
            continue
            
        print("\n--- Coletando Dados dos Jogos Encontrados ---")
        all_games_data = []

        for url in game_urls:
            print(f"\n" + "="*50)
            game_name_from_url = url.split('/')[5].replace('_', ' ')
            print(f"🔎 Analisando {game_name_from_url}...")
            
            steam_data = steam_scraper.scrape_steam_page_details(url)
            if not steam_data: continue
            
            meta_data = metacritic_scraper.scrape_metacritic(steam_data['name'])
            
            print("🤖 IA está analisando os requisitos do seu PC...")
            req_check = ai_handler.analyze_requirements_with_ia(user_pc, steam_data['min_req'], steam_data['rec_req'])
            
            cleaned_desc = clean_description(steam_data.get('description', 'N/A'))

            consolidated_data = {
                "Nome": steam_data['name'],
                "Preço": steam_data['price'],
                "User Score": meta_data.get('user_score', 'N/A'),
                "Tags": steam_data.get('tags', []),
                "PC Roda?": req_check,
            }
            all_games_data.append(consolidated_data)
            
            print("\n" + "*"*20 + f" {steam_data['name']} " + "*"*20)
            print(f"| Desenvolvedor: {steam_data.get('developer', 'N/A')}")
            print(f"| Preço Atual na Steam: {consolidated_data['Preço']}")
            print("-" * 50)
            print(f"| Nota dos Usuários (Metacritic): {consolidated_data['User Score']}")
            print(f"| Tags na Steam: {', '.join(consolidated_data['Tags'])}")
            print(f"| Descrição: {cleaned_desc}")
            if meta_data.get('comments'):
                print("| Comentários de Usuários:")
                for comment in meta_data['comments']:
                    print(f"|  - \"{comment}\"")
            print("-" * 50)
            print(f"| SEU PC RODA? (Análise da IA): {consolidated_data['PC Roda?']}")
            print("*"* (42 + len(consolidated_data['Nome'])) + "\n")

        if all_games_data:
            print("\n" + "="*25 + " RESUMO FINAL " + "="*25)
            print("🤖 Gerando recomendação final com base nos resultados...")
            final_recommendation = ai_handler.generate_final_recommendations_with_ia(user_description, all_games_data)
            print("\nRecomendação da IA:")
            print(final_recommendation)


if __name__ == "__main__":
    main()
