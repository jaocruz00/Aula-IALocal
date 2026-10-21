# Receita da aula — 120 minutos

Objetivo: entender e completar um chatbot que consulta documentos locais com fontes.

## 0–15 min — mostrar o resultado

Professor: execute o JAR pronto. Importe e salve os 3 TXT.
Pergunte “Qual o prazo do relatório?”. Abra a fonte e explique que o backend
recuperou o trecho; o modelo não foi treinado novamente.

## 15–30 min — explorar o CRUD pronto

Alunos: abram aluno, executem e cadastrem os exemplos.
Leiam Documento, Repository e Controller. Localizem POST, GET, PUT e DELETE.
O chat ainda avisa que falta implementar a etapa 1; isso é esperado.

## 30–45 min — entender a recuperação

Leia BuscaService.buscar e dividir.
Compare “prazo relatório” com “astronomia galáxias”.
Explique 3 trechos e a diferença entre palavras-chave e busca semântica.
Execute mvn test no projeto aluno para testar a recuperação.

## 45–65 min — ETAPA 1: montar contexto

Em ChatService.java, método montarContexto, substitua o TODO por um StringBuilder.
Percorra as fontes e inclua id, título, número e texto. Retorne o texto final.
Reinicie em modo trechos:

    mvn spring-boot:run -Dspring-boot.run.arguments=--app.modo=trechos

Confira a recuperação sem depender do modelo.
Se travar, copie somente o método equivalente da pasta completo.

## 65–90 min — ETAPA 2: conectar a IA

Em OllamaClient.java, método gerar, siga a receita da versão completa.
Monte o Map com model, system, prompt, stream=false e options;
serialize com ObjectMapper; faça POST com HttpClient;
valide HTTP 200 e leia o campo response.
Escreva com a turma, discutindo os blocos. O tratamento de erro pode ser copiado.
Reinicie no modo normal e confirme que o modelo já está instalado.

## 90–110 min — testar o comportamento

1. “Quais documentos são necessários para iniciar o estágio?”
   Esperado: termo de compromisso, plano e comprovante; confira documento 02.
2. “Qual o prazo do relatório?” → 15 dias, conforme documento 03.
3. Edite 15 para 30, salve, faça NOVA pergunta → fonte deve mostrar 30.
4. Exclua documento 03 e repita → não deve recuperar o documento removido.
5. “Qual a distância entre Marte e Júpiter?” → sem fontes, sem chamada à IA.
6. “Qual o prazo da bolsa de estágio?” → pode recuperar algo parcialmente relacionado;
   observe se o modelo reconhece que falta informação. Um acerto não garante todos.
7. “Qual o vencimento da entrega?” → discuta a limitação de vocabulário da busca.
8. Reinicie a aplicação e confirme a persistência.
São exemplos de avaliação; a qualidade do Qwen ainda precisa ser medida no laboratório.

## 110–120 min — revisão

Onde estão os dados? Quem encontra os trechos? Quem escreve a resposta?
O que muda quando o documento é excluído?
Desafio opcional: adaptar os textos para suporte de TI.
Deixe PDFs, embeddings e autenticação para depois da prática principal.

## Plano B

Código do aluno falhou: copie apenas o método do gabarito.
Não compila: pare e execute o JAR pronto.
Ollama falhou: executar-trechos.bat; explique que é busca sem geração.
Não instale modelos grandes durante os 120 minutos.
