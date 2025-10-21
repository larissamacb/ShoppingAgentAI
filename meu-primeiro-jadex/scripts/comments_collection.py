import random
import time
import json
import sys
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.by import By
from selenium.common.exceptions import TimeoutException, NoSuchElementException
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# tempo_inicial = time.time()

chrome_options = Options()
chrome_options.add_argument("--headless=new") 
chrome_options.add_argument("--window-size=1920,1080")
chrome_options.add_argument("--no-sandbox")
chrome_options.add_argument("--disable-dev-shm-usage")
chrome_options.add_argument(f'user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36')

driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=chrome_options)
wait = WebDriverWait(driver, 15)

try:
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
    print("Sessão iniciada com sucesso!\n")
except Exception as e:
    print(f"❌ Erro ao carregar os cookies: {type(e).__name__} - {e}")
    driver.quit()
    exit()

def coletar_varios_comentarios(driver, wait, url_filtro, max_comentarios=20):
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

# Coleta de Dados
try:
    with open('produtos_finais.json', 'r', encoding='utf-8') as f:
        dados_produtos = json.load(f)
except FileNotFoundError:
    driver.quit()
    exit()

MAX_TENTATIVAS_PRODUTO = 2
print("Coletando comentários...")

for i, produto in enumerate(dados_produtos):
    mensagem = f"\rProgresso: {i + 1}/{len(dados_produtos)}"
    sys.stdout.write(mensagem)
    sys.stdout.flush()

    for tentativa in range(MAX_TENTATIVAS_PRODUTO):
        try:
            url_produto = produto['link']
            driver.get(url_produto)
            try:
                wait_link = WebDriverWait(driver, 7)
                link_todos_comentarios_el = wait_link.until(EC.presence_of_element_located((By.CSS_SELECTOR, 'a[data-hook="see-all-reviews-link-foot"]')))
                url_comentarios_base = link_todos_comentarios_el.get_attribute('href')
            except (TimeoutException, NoSuchElementException):
                produto['comentarios'] = {"positivos": [], "criticos": ["Produto sem avaliações"]}
                break 
            
            url_positivos = f"{url_comentarios_base.split('/ref=')[0]}/ref=cm_cr_arp_d_viewopt_sr?ie=UTF8&reviewerType=all_reviews&filterByStar=positive&pageNumber=1"
            comentarios_positivos = coletar_varios_comentarios(driver, wait, url_positivos, max_comentarios=20)
            
            time.sleep(random.uniform(2, 4))

            url_criticos = f"{url_comentarios_base.split('/ref=')[0]}/ref=cm_cr_arp_d_viewopt_sr?ie=UTF8&reviewerType=all_reviews&filterByStar=critical&pageNumber=1"
            comentarios_negativos = coletar_varios_comentarios(driver, wait, url_criticos, max_comentarios=20)
                
            produto['comentarios'] = {"positivos": comentarios_positivos, "criticos": comentarios_negativos}
            
            break
        except Exception as e:
            if tentativa == MAX_TENTATIVAS_PRODUTO - 1:
                produto['comentarios'] = {"positivos": ["Erro ao processar"], "criticos": ["Erro ao processar"]}
    time.sleep(random.uniform(3, 6))

# Salvando o arquivo
with open('produtos_com_comentarios.json', 'w', encoding='utf-8') as f:
    json.dump(dados_produtos, f, ensure_ascii=False, indent=4)
print("\nArquivo 'produtos_com_comentarios.json' salvo com sucesso!")

driver.quit()

# tempo_final = time.time()
# duracao_total = tempo_final - tempo_inicial
# minutos = int(duracao_total // 60)
# segundos = int(duracao_total % 60)
# print(f"\n🕒 Tempo total de execução: {minutos} minutos e {segundos} segundos.")