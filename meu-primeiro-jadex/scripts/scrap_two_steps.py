# Importando todas as bibliotecas necessárias
import random
import time
import json
import os
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

# --- CONFIGURAÇÕES E ESTRATÉGIAS ---
MAX_TENTATIVAS = 3
user_agents = [
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36',
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36'
]

chrome_options = Options()
chrome_options.add_argument("--headless=new") 
chrome_options.add_argument("--window-size=1920,1080")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")
chrome_options.add_argument(f'user-agent={random.choice(user_agents)}')

# --- INÍCIO DO SCRIPT ---
print("Iniciando o navegador...")
driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=chrome_options)
wait = WebDriverWait(driver, 20)

links_para_visitar = []
dados_finais_produtos = []

# --- ETAPA 1 (sem alterações) ---
for tentativa in range(MAX_TENTATIVAS):
    try:
        # ... (a lógica da Etapa 1 continua a mesma) ...
        termo_de_busca = "notebook"
        url = f"https://www.amazon.com.br/s?k={termo_de_busca}"
        print(f"\n--- ETAPA 1, Tentativa {tentativa + 1} de {MAX_TENTATIVAS} ---")
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
        print(f"Coleta de links finalizada. {len(links_para_visitar)} links válidos foram guardados.")
        break
    except Exception as e:
        print(f"❌ Falha na tentativa {tentativa + 1}. Causa: {type(e).__name__}")
        if tentativa == MAX_TENTATIVAS - 1:
            print("❌ Todas as tentativas da Etapa 1 falharam.")
            driver.save_screenshot("erro_etapa_1.png")

# --- ETAPA 2: ATUALIZADA COM AS DESCRIÇÕES ---
if links_para_visitar:
    print(f"\n--- INICIANDO ETAPA 2: Visitando {len(links_para_visitar)} páginas de produto ---")
    
    for i, produto_info in enumerate(links_para_visitar):
        url_produto = produto_info['link']
        titulo_produto = produto_info['titulo']
        
        print(f"\nProcessando produto {i + 1} de {len(links_para_visitar)}: {titulo_produto[:50]}...")
        
        try:
            driver.get(url_produto)
            wait.until(EC.presence_of_element_located((By.ID, 'productTitle')))

            # Coleta dos dados que já tínhamos...
            preco = "Preço não encontrado"
            try:
                preco = driver.find_element(By.CSS_SELECTOR, '#corePrice_feature_div .a-offscreen').get_attribute("textContent")
            except NoSuchElementException: pass
            
            avaliacao = "Avaliação não encontrada"
            try:
                avaliacao = driver.find_element(By.CSS_SELECTOR, "span#acrPopover span.a-icon-alt").get_attribute("innerHTML").strip()
            except NoSuchElementException: pass

            total_avaliacoes = "0"
            try:
                total_avaliacoes = driver.find_element(By.CSS_SELECTOR, 'span[data-hook="total-review-count"]').text.split(' ')[0]
            except NoSuchElementException: pass

            imagem = "Imagem não encontrada"
            try:
                imagem = driver.find_element(By.CSS_SELECTOR, "div#imgTagWrapperId img").get_attribute("src")
            except NoSuchElementException: pass

            # ✨ NOVO: Coletando a Descrição Curta (Bullets) ✨
            descricao_curta = "Descrição curta não encontrada"
            try:
                # Usando o seletor de ID que você encontrou
                descricao_element = driver.find_element(By.CSS_SELECTOR, "div#feature-bullets")
                descricao_curta = descricao_element.text
            except NoSuchElementException:
                pass

            # ✨ NOVO: Coletando a Descrição Longa ✨
            descricao_longa = "Descrição longa não encontrada"
            try:
                # Usando o seletor de ID da segunda caixa que você encontrou
                descricao_element = driver.find_element(By.CSS_SELECTOR, "div#productDescription")
                descricao_longa = descricao_element.text
            except NoSuchElementException:
                pass
            
            # Monta o dicionário com todos os dados
            dados_completos = {
                "id": i + 1, "titulo": titulo_produto, "preco_final": preco.strip() if preco else "Não encontrado",
                "avaliacao_estrelas": avaliacao, "total_avaliacoes": total_avaliacoes,
                "descricao_curta": descricao_curta.strip(), # Adicionamos .strip() para limpar espaços em branco
                "descricao_longa": descricao_longa.strip(),
                "imagem_url": imagem, "link": url_produto
            }
            dados_finais_produtos.append(dados_completos)
            print(f"✔️ Dados coletados com sucesso para o produto #{i+1}.")

            pausa = random.uniform(2, 5)
            time.sleep(pausa)
        
        except Exception as e:
            print(f"❌ Falha ao processar a página do produto: {url_produto}")
            continue

# --- SALVANDO OS DADOS EM JSON (sem alterações) ---
if dados_finais_produtos:
    print(f"\nSalvando {len(dados_finais_produtos)} produtos em 'produtos_finais.json'...")
    with open('produtos_finais.json', 'w', encoding='utf-8') as f:
        json.dump(dados_finais_produtos, f, ensure_ascii=False, indent=4)
    print("✔️ Arquivo 'produtos_finais.json' salvo com sucesso!")

print("\nFechando o navegador.")
driver.quit()
print("Script finalizado com sucesso!")