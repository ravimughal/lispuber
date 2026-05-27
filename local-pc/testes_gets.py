import pytest
import requests
from datetime import datetime, timedelta

BASE_URL = "https://lispuber.xyz/api"  # Baseado na especificação OpenAPI

# Variáveis globais para partilhar o estado entre testes sequenciais
token_motorista = ""
token_cliente = ""
token_gestor = ""
turno_id = None
pedido_id = None
viagem_id = None


# LOGIN (COM users já existentes)


def test_login_motorista():
    global token_motorista
    url = f"{BASE_URL}/auth/login"
    payload = {
        "email": "motorista@lisp.com",
        "password": "motorista123"
    }
    response = requests.post(url, json=payload)
    assert response.status_code == 200, f"Falha no login do motorista: {response.text}"
    
    dados = response.json()
    assert "token" in dados or "accessToken" in dados or dados != {}, "Resposta vazia"
    token_motorista = dados.get("token") or dados.get("accessToken") or list(dados.values())[0]


def test_login_cliente():
    global token_cliente
    url = f"{BASE_URL}/auth/login"
    payload = {
        "email": "cliente1234@gmail.com",
        "password": "cliente1234"
    }
    response = requests.post(url, json=payload)
    assert response.status_code == 200
    dados = response.json()
    token_cliente = dados.get("token") or dados.get("accessToken") or list(dados.values())[0]


# TÁXIS


def test_login_gestor():
    global token_gestor
    url = f"{BASE_URL}/auth/login"
    payload = {
        "email": "gestor@lisp.com",
        "password": "gestor123"
    }
    response = requests.post(url, json=payload)
    assert response.status_code == 200
    dados = response.json()
    token_gestor = dados.get("token") or dados.get("accessToken") or list(dados.values())[0]


def test_get_taxi(id: int):
    pass


def test_get_todos_taxis():
    pass


def test_criar_taxi(matricula:str, modelo:str, marca:str, tipoMotor:str, 
                            anoCompra:int, nivelConforto:str, kilometros:float):
    pass


def test_remover_taxi(id:int):
    pass


# MOTORISTAS


def test_get_motorista(id:int):
    pass


def test_get_todos_motoristas():
    pass


def test_criar_motorista(nif:str, email:str, senha:str, nome:str, genero: str, 
                                 dataNascimento:datetime, rua:str, ruaNumero:int, codigoPostal:str,
                                 numCarta:str, validadeCarta:datetime):
    pass


def test_remover_motorista(id:int):
    pass


# VIAGENS


def test_get_viagem(id:int):
    pass


def test_get_todas_viagens():
    pass


def test_criar_viagem(clienteId:int, turnoId:int, pedidoId:int, dataInicio:datetime, kilometros:float):
    pass


def test_get_viagem_byMotorista(motoristaLogadoId:int):
    pass


def test_get_viagem_byCliente(clienteLogadoId:int):
    pass


# TURNOS


def test_get_turnos():
    pass


def test_get_taxis_disponiveis(idTurno:int):
    pass


def test_get_turnos_byMotorista(motoristaId:int):
    pass


# REABASTECIMENTOS


def test_get_reabastecimento(id:int):
    pass


def test_get_todos_reabastecimentos():
    pass


def test_criar_reabastecimento(turnoId:int):
    pass


def test_finalizar_reabastecimento(id:int):
    pass


# PEDIDOS


def test_criar_pedido(clienteId:int, confortoId:int, numPassageiros:int, ruaOrigem:str, latitudeOrigem:float,
                      longitudeOrigem:float, ruaDestino:str, latitudeDestino:float, longitudeDestino:float):
    pass


def test_get_pedido(id:int):
    pass


def test_get_todos_pedidos():
    pass


def test_get_pedidos_pendentes():
    pass


def test_get_pedidos_byMotorista(motoristaLogadoId:int):
    pass


def test_get_pedidos_byCliente(motoristaClienteId:int):
    pass


# FATURAS


def test_get_faturas_motorsita(motoristaId:int):
    pass


# AVALIAÇỖES


def test_get_avaliacao_viagem(viagemId:int):
    pass


def test_get_avaliacoes_motorista(motoristaId:int):
    pass


def test_get_media_motorista(motoristaId:int):
    pass


def test_get_avaliacoes_cliente(clienteId:int):
    pass


# CONFORTO


def test_get_confortos():
    pass