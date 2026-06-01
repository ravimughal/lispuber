import time
import requests
from collections import Counter
from concurrent.futures import ThreadPoolExecutor, as_completed

URL = "https://lispuber.xyz/api"
TOTAL_REQUESTS = 100
CONCURRENCY = 50


app_servers = []
response_times = []
status_codes = []



def testar_login():
    """Executa uma requisição de login e retorna o status code, o tempo de resposta e o servidor (se disponível)."""
    url = f"{URL}/auth/login"
    payload = {
        "email": "cliente4@lisp.com",
        "password": "cliente123"
    }

    start_time = time.time()
    try:
        response = requests.post(url, json=payload, timeout=10)
        latency = (time.time() - start_time) * 1000
        status = response.status_code

        backend = response.headers.get("X-Backend-Name", "VM-Desconhecida")

        return status, latency, backend

    except requests.RequestException:
        latency = (time.time() - start_time) * 1000
        return "ERRO", latency, "Falha_Conexao"



print(f"A iniciar teste de carga: {TOTAL_REQUESTS} pedidos em grupos de {CONCURRENCY} em paralelo...")
print(f"Alvo: {URL}\n")

start_test = time.time()


# Execução em threads paralelas
with ThreadPoolExecutor(max_workers=CONCURRENCY) as executor:
    futures = [executor.submit(testar_login) for _ in range(TOTAL_REQUESTS)]

    for i, future in enumerate(as_completed(futures), 1):
        status, latency, backend = future.result()

        status_codes.append(status)
        response_times.append(latency)
        app_servers.append(backend)

        if i % 100 == 0:
            print(f"- {i}/{TOTAL_REQUESTS} pedidos concluídos...")

end_test = time.time()
total_time = end_test - start_test

successful_pings = [t for t, s in zip(response_times, status_codes) if s in [200, 201]]

print("\n" + "="*40)
print("RESULTADOS DO TESTE:")
print("="*40)

print(f"Tempo total do teste: {total_time:.2f} segundos")
sucessos = status_codes.count(200) + status_codes.count(201)
print(f"Pedidos com sucesso (200/201 OK): {sucessos} / {TOTAL_REQUESTS}")
print(f"Pedidos com erro (5xx/4xx/Timeout): {TOTAL_REQUESTS - sucessos}")

print("\nDistribuição de Carga pelas Instâncias (Apps)")
server_counts = Counter(app_servers)
for app, count in server_counts.items():
    percentage = (count / TOTAL_REQUESTS) * 100
    print(f"  {app}: {count} pedidos tratados ({percentage:.1f}%)")

print("\nMétricas de Latência (ms)")
if successful_pings:
    print(f"  Mínimo: {min(successful_pings):.2f} ms")
    print(f"  Médio:  {sum(successful_pings) / len(successful_pings):.2f} ms")
    print(f"  Máximo: {max(successful_pings):.2f} ms")
else:
    print("  Nenhuma requisição de sucesso para calcular a latência.")