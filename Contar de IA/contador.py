from collections import Counter

import tkinter as tk
from tkinter import filedialog

import cv2
from ultralytics import YOLO

# Abre a janela para escolher uma imagem.
janela = tk.Tk()
janela.withdraw()
janela.attributes("-topmost", True)

caminho_imagem = filedialog.askopenfilename(
    parent=janela,
    title="Contador de objetos — selecione uma imagem",
    filetypes=[
        ("Imagens", "*.jpg *.jpeg *.png *.bmp *.webp"),
        ("Todos os arquivos", "*.*"),
    ],
)

janela.destroy()

# Encerra normalmente se o usuário cancelar.
if not caminho_imagem:
    print("Nenhuma imagem selecionada. Encerrando.")
    raise SystemExit(0)

print(f"Imagem selecionada: {caminho_imagem}")

modelo = YOLO("yolo11n.pt")

# Categorias que queremos contar.
categorias = {
    "apple": "maca",
    "bottle": "garrafa",
    "orange": "laranja",
    "banana": "banana",
}

classes = [
    codigo
    for codigo, nome in modelo.names.items()
    if nome in categorias
]

resultado = modelo.predict(
    source=caminho_imagem,
    device="cpu",
    classes=classes,
    conf=0.40,
    imgsz=640,
    verbose=False,
)[0]

imagem = resultado.orig_img.copy()
contagem = Counter()

# Ajusta os desenhos ao tamanho da foto.
espessura = max(2, round(max(imagem.shape[:2]) / 600))
fonte = max(0.6, max(imagem.shape[:2]) / 1800)

for numero, objeto in enumerate(resultado.boxes, start=1):
    classe = int(objeto.cls.item())
    nome = categorias[modelo.names[classe]]
    confianca = objeto.conf.item()
    contagem[nome] += 1

    x1, y1, x2, y2 = map(int, objeto.xyxy[0].tolist())

    cv2.rectangle(
        imagem, (x1, y1), (x2, y2),
        (0, 180, 0), espessura
    )

    legenda = f"#{numero} {nome} {confianca:.0%}"
    cv2.putText(
        imagem, legenda,
        (x1, max(int(35 * fonte), y1 - 10)),
        cv2.FONT_HERSHEY_SIMPLEX,
        fonte, (0, 100, 0), espessura
    )

total = sum(contagem.values())

print(f"\nTOTAL DETECTADO: {total}")
for nome, quantidade in contagem.items():
    print(f"  {nome}: {quantidade}")

# Adiciona uma faixa com o total.
margem = int(65 * fonte)
imagem = cv2.copyMakeBorder(
    imagem, margem, 0, 0, 0,
    cv2.BORDER_CONSTANT, value=(255, 255, 255)
)

cv2.putText(
    imagem, f"TOTAL DETECTADO: {total}",
    (20, int(45 * fonte)),
    cv2.FONT_HERSHEY_SIMPLEX,
    fonte, (0, 0, 0), espessura
)

if not cv2.imwrite("resultado.jpg", imagem):
    raise RuntimeError("Nao foi possivel salvar resultado.jpg")

print("\nImagem marcada salva em resultado.jpg")

# Exibe uma previa ajustada ao tamanho da tela.
altura, largura = imagem.shape[:2]
escala = min(1100 / largura, 750 / altura, 1.0)
previa = cv2.resize(
    imagem, (int(largura * escala), int(altura * escala))
)

cv2.imshow("Contagem de objetos - pressione uma tecla para fechar", previa)
cv2.waitKey(0)
cv2.destroyAllWindows()