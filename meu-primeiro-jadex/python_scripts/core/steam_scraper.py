import requests
from bs4 import BeautifulSoup
import sys # Import necessário para sys.stderr.write

def get_game_urls_from_search(search_url, num_to_search=5):
    """
    A partir de uma URL de busca da Steam já filtrada, extrai as URLs
    da quantidade de jogos especificada.
    """
    urls = []
    try:
        # Headers mais neutros para não forçar o idioma, mas aceita pt-BR para contexto geral.
        headers = {'User-Agent': 'Mozilla/5.0', 'Accept-Language': 'pt-BR,pt;q=0.9'} 
        # Mantém cookies essenciais (idade e região Brasil)
        cookies = {'birthtime': '568022401', 'wants_mature_content': '1', 'steamCountry': 'BR'}
        
        response = requests.get(search_url, headers=headers, cookies=cookies)
        response.raise_for_status()
        
        soup = BeautifulSoup(response.text, 'html.parser')
        
        # Usa a variável num_to_search em vez do número 5 fixo
        results = soup.select('a.search_result_row')[:num_to_search]
        for result in results:
            urls.append(result['href'])
            
        return urls
    except Exception as e:
        sys.stderr.write(f"Erro ao buscar resultados da Steam: {e}\n")
        return []

def scrape_steam_page_details(game_url):
    """
    Extrai informações da página de um jogo na Steam. Usa headers neutros para 
    obter o nome canônico (universal) do jogo, mas mantém o preço em BRL.
    """
    # Headers mais neutros, removendo a preferência de idioma para forçar o nome original/Inglês
    headers_neutral = {'User-Agent': 'Mozilla/5.0'} 
    try:
        # Mantemos o cookie 'steamCountry: BR' e 'birthtime' para evitar bloqueios e checagem de idade.
        cookies = {'birthtime': '568022401', 'wants_mature_content': '1', 'steamCountry': 'BR'}
        
        # O parâmetro cc=br na URL é o que garante o preço em Reais (BRL). É crucial mantê-lo.
        if '?' in game_url:
            url_br = f"{game_url}&cc=br"
        else:
            url_br = f"{game_url}?cc=br"

        response = requests.get(url_br, headers=headers_neutral, cookies=cookies)
        response.raise_for_status()
        soup = BeautifulSoup(response.text, 'html.parser')

        game_data = {}

        # 1. Nome do Jogo: Com headers neutros e o parâmetro cc=br, a Steam
        #    tende a retornar o nome original/Inglês, enquanto o preço é BRL.
        app_name_elem = soup.find('div', id='appHubAppName')
        game_data['name'] = app_name_elem.text.strip() if app_name_elem else "Nome não encontrado"
        
        # 2. Desenvolvedor
        dev_publisher_elements = soup.select('.dev_row a')
        game_data['developer'] = dev_publisher_elements[0].text.strip() if len(dev_publisher_elements) > 0 else "Desenvolvedor não encontrado"
        
        # 3. Preço: Deve vir em BRL devido ao cc=br
        price_area = soup.find('div', class_='game_purchase_price') or soup.find('div', class_='discount_final_price')
        game_data['price'] = price_area.text.strip() if price_area else "Grátis para jogar ou não disponível"
        
        # 4. Descrição (Pode vir em Inglês ou Português, dependendo do cache da Steam)
        description_div = soup.find('div', id='game_area_description')
        game_data['description'] = description_div.text.strip() if description_div else "Descrição não encontrada."
        
        # 5. Tags
        tags = soup.find_all('a', class_='app_tag')
        game_data['tags_steam'] = [tag.text.strip() for tag in tags[:5]]
        
        # 6. Requisitos de Sistema
        reqs_min = soup.find('div', class_='game_area_sys_reqs_leftCol')
        reqs_rec = soup.find('div', class_='game_area_sys_reqs_rightCol')
        game_data['min_req'] = reqs_min.get_text(separator='\n').strip() if reqs_min else "Não especificado"
        game_data['rec_req'] = reqs_rec.get_text(separator='\n').strip() if reqs_rec else "Não especificado"

        return game_data
    except (requests.exceptions.RequestException, AttributeError) as e:
        sys.stderr.write(f"Erro ao fazer scraping da página da Steam ({game_url}): {e}\n")
        return None