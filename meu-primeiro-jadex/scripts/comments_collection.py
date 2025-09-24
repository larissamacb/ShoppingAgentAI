# Arquivo: coletor_de_comentarios.py (versão final e mais inteligente)
import random
import time
import json
import os
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# --- CONFIGURAÇÕES E LOGIN (sem alterações) ---
# ... (todo o código de configuração e login com cookies continua o mesmo)
chrome_options = Options()
chrome_options.add_argument("--headless=new") 
chrome_options.add_argument("--window-size=1920,1080")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")
chrome_options.add_argument(f'user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36')

print("Iniciando o robô com login via cookies...")
driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=chrome_options)
wait = WebDriverWait(driver, 15)

try:
    # ... (A lógica de carregar cookies continua a mesma)
    print("Carregando cookies de sessão...")
    driver.get("https://www.amazon.com.br/")
    with open('meus_cookies.json', 'r') as f:
        cookies = json.load(f)
    for cookie in cookies:
        if 'sameSite' in cookie and cookie['sameSite'] not in ['Strict', 'Lax', 'None']:
            cookie['sameSite'] = 'None'
        driver.add_cookie(cookie)
    driver.refresh()
    time.sleep(3) 
    print("Sessão iniciada com sucesso (logado)!")
except Exception as e:
    print(f"❌ ERRO ao carregar os cookies: {type(e).__name__} - {e}")
    driver.quit()
    exit()

def coletar_varios_comentarios(driver, wait, url_filtro, max_comentarios=20):
    # (A função 'coletar_varios_comentarios' continua a mesma)
    lista_comentarios = []
    driver.get(url_filtro)
    while len(lista_comentarios) < max_comentarios:
        try:
            wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, 'div[id^="customer_review-"]')))
            elementos_comentario = driver.find_elements(By.CSS_SELECTOR, 'div[id^="customer_review-"]')
            primeiro_comentario_pagina_atual = elementos_comentario[0]
            for comentario_el in elementos_comentario:
                if len(lista_comentarios) < max_comentarios:
                    try:
                        texto = comentario_el.find_element(By.CSS_SELECTOR, 'span[data-hook="review-body"] span').text.strip()
                        if texto: lista_comentarios.append(texto)
                    except NoSuchElementException: continue
                else: break
            if len(lista_comentarios) >= max_comentarios: break
            next_page_button = driver.find_element(By.CSS_SELECTOR, 'li.a-last a')
            next_page_button.click()
            wait.until(EC.staleness_of(primeiro_comentario_pagina_atual))
        except (TimeoutException, NoSuchElementException):
            break
    return lista_comentarios[:max_comentarios]

# --- LÓGICA DE COLETA DE DADOS ---
try:
    with open('produtos_finais.json', 'r', encoding='utf-8') as f:
        dados_produtos = json.load(f)
except FileNotFoundError:
    driver.quit()
    exit()

MAX_TENTATIVAS_PRODUTO = 2 # Reduzido para 2, pois a principal causa de falha agora é tratada

for i, produto in enumerate(dados_produtos):
    print(f"\n--- Processando Produto {i + 1}/{len(dados_produtos)}: {produto['titulo'][:50]}... ---")
    
    # O loop de retentativas ainda é útil para falhas de rede gerais
    for tentativa in range(MAX_TENTATIVAS_PRODUTO):
        try:
            url_produto = produto['link']
            driver.get(url_produto)
            
            # ✨ LÓGICA ATUALIZADA: Verificamos o link ANTES de prosseguir ✨
            try:
                # Usamos uma espera mais curta aqui para a verificação
                wait_link = WebDriverWait(driver, 7)
                link_todos_comentarios_el = wait_link.until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, 'a[data-hook="see-all-reviews-link-foot"]'))
                )
                url_comentarios_base = link_todos_comentarios_el.get_attribute('href')
                print("Link de avaliações encontrado. Prosseguindo com a coleta...")
            except (TimeoutException, NoSuchElementException):
                # Se o link não for encontrado, este produto não tem avaliações.
                print("AVISO: Produto não possui avaliações. Marcando e pulando para o próximo.")
                produto['comentarios'] = {"positivos": [], "criticos": ["Produto sem avaliações"]}
                # Usamos 'break' para sair do loop de RETENTATIVAS, pois não adianta tentar de novo.
                break 
            
            # Se o código chegou até aqui, o link foi encontrado e podemos coletar.
            url_positivos = f"{url_comentarios_base.split('/ref=')[0]}/ref=cm_cr_arp_d_viewopt_sr?ie=UTF8&reviewerType=all_reviews&filterByStar=positive&pageNumber=1"
            comentarios_positivos = coletar_varios_comentarios(driver, wait, url_positivos, max_comentarios=20)
            
            time.sleep(random.uniform(2, 4))

            url_criticos = f"{url_comentarios_base.split('/ref=')[0]}/ref=cm_cr_arp_d_viewopt_sr?ie=UTF8&reviewerType=all_reviews&filterByStar=critical&pageNumber=1"
            comentarios_negativos = coletar_varios_comentarios(driver, wait, url_criticos, max_comentarios=20)
                
            produto['comentarios'] = {"positivos": comentarios_positivos, "criticos": comentarios_negativos}
            print(f"Coletados {len(comentarios_positivos)} positivos e {len(comentarios_negativos)} críticos.")
            
            # Se tudo deu certo, saímos do loop de retentativas.
            break

        except Exception as e:
            print(f"AVISO: Falha na tentativa {tentativa + 1} de {MAX_TENTATIVAS_PRODUTO}. Erro: {type(e).__name__}")
            if tentativa == MAX_TENTATIVAS_PRODUTO - 1:
                print("❌ Todas as tentativas falharam para este produto.")
                produto['comentarios'] = {"positivos": ["Erro ao processar"], "criticos": ["Erro ao processar"]}

    time.sleep(random.uniform(3, 6))

# --- SALVANDO O ARQUIVO FINAL ---
print("\nSalvando dados enriquecidos em 'produtos_com_comentarios.json'...")
with open('produtos_com_comentarios.json', 'w', encoding='utf-8') as f:
    json.dump(dados_produtos, f, ensure_ascii=False, indent=4)
print("✔️ Arquivo salvo com sucesso!")

print("\nFechando o navegador.")
driver.quit()
print("Processo finalizado!")