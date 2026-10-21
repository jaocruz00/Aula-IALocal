from pathlib import Path
import math
import time
import tkinter as tk
from tkinter import filedialog

import httpx
import ollama


# Confira o nome instalado com: ollama list
MODELO = "qwen2.5vl:3b"

ENDERECO_OLLAMA = "http://localhost:11434"

# Tempo de espera depois da análise, em segundos.
TEMPO_PARA_DESCARREGAR = 120


def selecionar_imagem():
    """Abre uma janela para selecionar a imagem."""

    janela = tk.Tk()
    janela.withdraw()
    janela.attributes("-topmost", True)

    try:
        return filedialog.askopenfilename(
            parent=janela,
            title="Contador de objetos — selecione uma imagem",
            filetypes=[
                ("Imagens", "*.jpg *.jpeg *.png *.bmp *.webp"),
                ("Todos os arquivos", "*.*"),
            ],
        )
    finally:
        janela.destroy()


def aguardar_e_descarregar(cliente):
    """Mostra o contador e solicita a descarga do modelo."""

    print(
        f"\nO modelo será descarregado em "
        f"{TEMPO_PARA_DESCARREGAR} segundos."
    )
    print("Pressione Ctrl+C para descarregar agora.\n")

    # Usa um relógio que não é afetado por alterações na hora do Windows.
    prazo = time.monotonic() + TEMPO_PARA_DESCARREGAR

    try:
        while True:
            restante = max(
                0,
                math.ceil(prazo - time.monotonic()),
            )

            minutos, segundos = divmod(restante, 60)

            # \r atualiza o contador na mesma linha.
            print(
                f"\rTempo para descarregar: "
                f"{minutos:02d}:{segundos:02d}   ",
                end="",
                flush=True,
            )

            if restante == 0:
                break

            time.sleep(0.2)

    except KeyboardInterrupt:
        print("\nDescarregamento antecipado solicitado.")

    print("\nDescarregando o modelo...", flush=True)

    # Uma requisição vazia com keep_alive=0 descarrega o modelo.
    cliente.generate(
        model=MODELO,
        prompt="",
        keep_alive=0,
        stream=False,
    )

    print("Modelo descarregado da memória.")


def analisar_imagem(cliente, caminho_imagem):
    """Envia a imagem ao Ollama e imprime a resposta aos poucos."""

    # Confere a conexão e se o modelo está disponível.
    cliente.show(MODELO)

    print(f"\nImagem selecionada: {caminho_imagem}")
    print(f"Modelo utilizado: {MODELO}")
    print(
        "\nCarregando o modelo e analisando a imagem..."
        "\nO primeiro processamento pode demorar.",
        flush=True,
    )

    resposta = cliente.chat(
        model=MODELO,
        messages=[
            {
                "role": "user",
                "content": (
                    "Analise esta imagem e conte os objetos visíveis. "
                    "Informe a quantidade de cada categoria e, ao final, "
                    "o total geral dos objetos contados. "
                    "Não conte o mesmo objeto em duas categorias. "
                    "Não inclua o fundo ou a superfície de apoio. "
                    "Não invente objetos escondidos. "
                    "Se houver dúvida na identificação ou contagem, "
                    "informe essa limitação. "
                    "Responda em português, de forma curta e organizada."
                ),
                "images": [caminho_imagem],
            }
        ],
        options={
            # Aumenta o contexto para acomodar imagem e texto.
            "num_ctx": 8192,

            # Limita o tamanho da resposta.
            "num_predict": 512,

            # Reduz a variação das respostas.
            "temperature": 0,
        },
        stream=True,

        # Descarga automática após 2 minutos de inatividade.
        # Continua valendo se o programa for fechado após a resposta.
        keep_alive=f"{TEMPO_PARA_DESCARREGAR}s",
    )

    print("\nResultado:\n", flush=True)

    for trecho in resposta:
        print(
            trecho.message.content or "",
            end="",
            flush=True,
        )

    print("\n\nAnálise concluída.")

    aguardar_e_descarregar(cliente)


def main():
    """Controla o programa e apresenta mensagens de erro."""

    try:
        caminho_imagem = selecionar_imagem()

        if not caminho_imagem:
            print("Nenhuma imagem selecionada. Encerrando.")
            return

        if not Path(caminho_imagem).is_file():
            print("Erro: o arquivo selecionado não foi encontrado.")
            return

        cliente = ollama.Client(
            host=ENDERECO_OLLAMA,
            timeout=600.0,
        )

        analisar_imagem(cliente, caminho_imagem)

        print("Programa encerrado.")

    except ollama.ResponseError as erro:
        print(f"\nErro retornado pelo Ollama: {erro.error}")

        if erro.status_code == 404:
            print(
                "\nConfira o nome do modelo com: ollama list"
                f"\nPara baixar o modelo configurado: "
                f"ollama pull {MODELO}"
            )

        elif "context" in erro.error.lower():
            print(
                "\nA imagem e o texto excederam o contexto disponível."
                "\nTente uma imagem com resolução menor ou aumente "
                "'num_ctx', se houver memória suficiente."
            )

    except (ConnectionError, httpx.ConnectError):
        print(
            "\nNão foi possível conectar ao Ollama."
            "\nAbra o aplicativo Ollama e execute novamente."
            "\nSe ele não estiver iniciado, execute: ollama serve"
        )

    except httpx.TimeoutException:
        print(
            "\nO tempo de espera foi excedido."
            "\nConfira se o Ollama está respondendo "
            "e se há memória livre."
        )

    except httpx.HTTPError as erro:
        print(f"\nFalha na comunicação com o Ollama: {erro}")

    except (OSError, ValueError) as erro:
        print(f"\nErro ao carregar ou enviar a imagem: {erro}")

    except tk.TclError as erro:
        print(f"\nErro ao abrir a janela de seleção: {erro}")

    except KeyboardInterrupt:
        print(
            "\nExecução interrompida."
            "\nPara descarregar o modelo manualmente, execute:"
            f"\nollama stop {MODELO}"
        )


if __name__ == "__main__":
    main()