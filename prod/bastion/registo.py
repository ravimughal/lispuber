import requests

URL = "https://lispuber.xyz/api"


def registo():
    url = f"{URL}/auth/register/client"

    payload = {
    "nif": "987632944",
    "email": "cliente4@lisp.com",
    "senha": "cliente123",
    "nome": "Cliente Ori",
    "genero": "MASCULINO",
    "dataNascimento": "2000-05-28",
    }
    response = requests.post(url, json=payload)

    assert (
        response.status_code == 201 or response.status_code == 200
    ), f"Falha no registo do motorista: {response.text}"

    dados = response.json()

    assert dados != {}, "Resposta vazia"



registo()