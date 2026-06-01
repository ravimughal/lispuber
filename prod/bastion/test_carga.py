import time
import requests
from collections import Counter
from concurrent.futures import ThreadPoolExecutor, as_completed

URL = "https://lispuber.xyz/api/auth/debug"
TOTAL_REQUESTS = 100
CONCURRENCY = 50

app_servers = []
response_times = []
status_codes = []

def send_request():
    start_time = time.perf_counter()
    try:
        headers = {
            "Connection": "close"
        }
        response = requests.get(URL, timeout=5)
        latency = (time.perf_counter() - start_time) * 1000
        backend = response.headers.get("X-Backend-Name", "VM-Desconhecida")

        return response.status_code, latency, backend
    except requests.RequestException:
        latency = (time.perf_counter() - start_time) * 1000
        return "ERRO", latency, "Falha de Conexão"

print(f"A iniciar teste de carga: {TOTAL_REQUESTS} pedidos em grupos de {CONCURRENCY} em paralelo...")
print(f"Alvo: {URL}\n")

start_test = time.time()

# Execução em threads paralelas
with ThreadPoolExecutor(max_workers=CONCURRENCY) as executor:
    futures = [executor.submit(send_request) for _ in range(TOTAL_REQUESTS)]

    for i, future in enumerate(as_completed(futures), 1):
        status, latency, backend = future.result()

        status_codes.append(status)
        response_times.append(latency)
        app_servers.append(backend)

        if i % 100 == 0:
            print(f"-{i}/{TOTAL_REQUESTS} pedidos concluídos...")

end_test = time.time()

total_time = end_test - start_test
successful_pings = [t for t, s in zip(response_times, status_codes) if s != "ERRO"]


print("RESULTADOS DO TESTE:")

print(f"Tempo total do teste: {total_time:.2f} segundos")
print(f"Pedidos com sucesso (200 OK): {status_codes.count(200)} / {TOTAL_REQUESTS}")
print(f"Pedidos com erro (5xx/4xx/Timeout): {TOTAL_REQUESTS - status_codes.count(200)}")

print("\nDistribuição de Carga pelas Instâncias (Apps)")
server_counts = Counter(app_servers)
for app, count in server_counts.items():
    percentage = (count / TOTAL_REQUESTS) * 100
    print(f"{app}: {count} pedidos tratados ({percentage:.1f}%)")

print("\nMétricas de Latência (ms)")
if successful_pings:
    print(f"  Mínimo: {min(successful_pings):.2f} ms")
    print(f"  Médio:  {sum(successful_pings) / len(successful_pings):.2f} ms")
    print(f"  Maximum: {max(successful_pings):.2f} ms")