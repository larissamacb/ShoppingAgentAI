import random
import time
import json
import sys
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.common.exceptions import NoSuchElementException
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

MAX_TENTATIVAS = 3
user_agents = [
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36'
]

chrome_options = Options()
chrome_options.add_argument("--headless=new") # o navegador não abrirá uma janela visível
chrome_options.add_argument("--window-size=1920,1080") # define tamanho fixo
chrome_options.add_argument("--no-sandbox") # reduz segurança para evitar problemas em alguns ambientes
chrome_options.add_argument("--disable-dev-shm-usage") # evita problemas de memória em ambientes limitados
chrome_options.add_argument(f'user-agent={random.choice(user_agents)}') # define um user-agent aleatório para evitar bloqueios simples

driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=chrome_options)
wait = WebDriverWait(driver, 20)

links_para_visitar = []
dados_finais_produtos = []

# --- ETAPA 1 | Busca de títulos e links de produto ---
for tentativa in range(MAX_TENTATIVAS): # tenta acessar a página até 3 vezes (em caso de erros de carregamento)
    try:
        termo_de_busca = "notebook"
        url = f"https://www.amazon.com.br/s?k={termo_de_busca}"
        print(f"Tentativa {tentativa + 1} de {MAX_TENTATIVAS} de acessar o site")
        driver.get(url)
        container_principal = wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, '#search')))
        produtos_na_busca = container_principal.find_elements(By.XPATH, ".//div[@data-component-type='s-search-result']")
        for produto in produtos_na_busca:
            try:
                link_element = produto.find_element(By.CSS_SELECTOR, 'a.a-link-normal.a-text-normal')
                titulo = link_element.text
                link = link_element.get_attribute('href')
                if titulo and link and "slredirect" not in link:
                    links_para_visitar.append({"titulo": titulo, "link": link})
            except NoSuchElementException:
                continue
        print(f"{len(links_para_visitar)} produtos encontrados.")
        break
    except Exception as e:
        if tentativa == MAX_TENTATIVAS - 1:
            print("❌ Todas as tentativas falharam.")

# --- ETAPA 2 | Visita os links guardados e reúne o resto das informações ---
if links_para_visitar:
    print(f"\nVisitando {len(links_para_visitar)} páginas de produto...")
    
    for i, produto_info in enumerate(links_para_visitar):
        url_produto = produto_info['link']

        mensagem = f"\rProgresso: {i + 1}/{len(links_para_visitar)}" # exibe o progresso na mesma linha
        sys.stdout.write(mensagem)
        sys.stdout.flush()
        
        try:
            driver.get(url_produto)
            wait.until(EC.presence_of_element_located((By.ID, 'productTitle')))

            preco = "Não encontrado"
            try:
                preco = driver.find_element(By.CSS_SELECTOR, '#corePrice_feature_div .a-offscreen').get_attribute("textContent")
            except NoSuchElementException: pass
            
            avaliacao = "Não encontrada"
            try:
                avaliacao = driver.find_element(By.CSS_SELECTOR, "span#acrPopover span.a-icon-alt").get_attribute("innerHTML").strip()
            except NoSuchElementException: pass

            total_avaliacoes = "0"
            try:
                total_avaliacoes = driver.find_element(By.CSS_SELECTOR, 'span[data-hook="total-review-count"]').text.split(' ')[0]
            except NoSuchElementException: pass

            imagem = "Não encontrada"
            try:
                imagem = driver.find_element(By.CSS_SELECTOR, "div#imgTagWrapperId img").get_attribute("src")
            except NoSuchElementException: pass

            descricao_curta = "Não encontrada"
            try:
                descricao_element = driver.find_element(By.CSS_SELECTOR, "div#feature-bullets")
                descricao_curta = descricao_element.text.replace('\n', ' ')[17:]
            except NoSuchElementException:
                pass

            descricao_longa = "Não encontrada"
            try:
                descricao_element = driver.find_element(By.CSS_SELECTOR, "div#productDescription")
                descricao_longa = descricao_element.text.replace('\n', ' ')
            except NoSuchElementException:
                pass
            
            dados_completos = {
                "id": i + 1, 
                "titulo": produto_info['titulo'], 
                "preco_final": preco.strip() if preco else "Não encontrado",
                "avaliacao_estrelas": avaliacao, "total_avaliacoes": total_avaliacoes,
                "descricao_curta": descricao_curta.strip(),
                "descricao_longa": descricao_longa.strip(),
                "imagem_url": imagem, "link": url_produto
            }
            dados_finais_produtos.append(dados_completos)

            pausa = random.uniform(2, 5)
            time.sleep(pausa)
        
        except Exception as e:
            print(f"❌ Falha ao processar a página do produto: {url_produto}")
            continue

# --- Salvando os dados em JSON ---
if dados_finais_produtos:
    with open('produtos_finais.json', 'w', encoding='utf-8') as f:
        json.dump(dados_finais_produtos, f, ensure_ascii=False, indent=4)
    print("\n\nArquivo 'produtos_finais.json' salvo com sucesso!")

driver.quit()