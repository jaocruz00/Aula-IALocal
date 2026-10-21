# Assistente Acadêmico Local — kit de aula

CRUD de documentos + recuperação por palavras-chave + Qwen 2.5 via Ollama.
Preparado para o minicurso de João Vitor Cruz • 2 horas • dados fictícios.

## Comece pela demonstração pronta

1. Extraia este ZIP (não execute arquivos de dentro do ZIP).
2. Tenha Java 17 ou 21 instalado. Para editar em aula, use JDK 21 e Maven 3.6.3+.
3. Para testar sem IA, abra executar-trechos.bat no Windows ou execute bash executar-trechos.sh no Linux/macOS.
4. Acesse http://localhost:8080. Cadastre cada TXT da pasta exemplos pela tela e clique em salvar.
5. Para IA real, instale/abra Ollama e execute ollama pull qwen2.5:7b ANTES da aula.
   Pare a aplicação com Ctrl+C e abra executar-ia.bat ou execute bash executar-ia.sh.
6. Se a tag já instalada for qwen2.5:latest, em vez de baixar outra use:
   java -jar bin/assistente-academico.jar --ollama.modelo=qwen2.5:latest
7. Faça a pergunta “Qual o prazo do relatório?” e expanda a fonte.

O JAR pronto inclui as dependências Java, mas NÃO inclui Java, Ollama nem o modelo.
Com Java, Ollama e o modelo já instalados, a execução não precisa de internet.
O primeiro build Maven precisa de internet para baixar dependências.
O banco nasce na pasta dados do diretório de execução e permanece após reiniciar.
Os scripts prontos sempre trabalham na raiz do kit. As pastas completo e aluno possuem
bancos próprios quando executadas dentro de suas pastas.

## Conteúdo

- bin/assistente-academico.jar: versão completa compilada para recuperação rápida.
- completo/: código-fonte completo, comentado e testes.
- aluno/: mesmo CRUD e interface, com duas tarefas Java marcadas TODO.
- ROTEIRO-AULA.md: sequência de 120 minutos, exercícios e resultados esperados.
- GUIA-CODIGO.md: mapa das classes, API, configuração e limitações.
- GABARITO.md: recuperar cada etapa sem apagar o restante.
- VALIDACAO.md: testes executados e o que ainda precisa de teste no laboratório.
- exemplos/: três TXT com regras fictícias; não são regras da FATEC.

## Editar e executar

Abra aluno ou completo na IDE como projeto Maven.
Dentro da pasta escolhida, execute:

    mvn spring-boot:run

Ou compile e execute:

    mvn clean package
    java -jar target/assistente-academico-1.0.0.jar

O código usa recursos Java 17 e pode ser trabalhado com JDK 21.
Na versão aluno, as tarefas não implementadas retornam HTTP 501 com instrução.
O CRUD continua funcionando. O teste inicial do aluno verifica a busca.
Depois de finalizar, copie completo/src/test para aluno/src/test e rode mvn test.

## Se algo falhar

- Porta em uso: feche a execução anterior com Ctrl+C ou acrescente --server.port=8081.
- Modelo ausente: ollama list; instale a tag desejada e ajuste --ollama.modelo=...
- Ollama indisponível: abra o aplicativo/serviço. ollama serve só se ainda não estiver rodando.
- CPU lenta: contexto 2048, até 3 trechos e geração de até 220 tokens. Meça no laboratório.
  Pode testar qwen2.5:3b, após baixar, com --ollama.modelo=qwen2.5:3b.
- Sem Ollama: pare e execute o modo trechos. Ele NÃO simula uma resposta de IA.
- Banco bloqueado: não abra duas instâncias sobre a mesma pasta de dados.
- Acentos errados: salve como TXT UTF-8.
- Maven sem internet: use o JAR pronto; para desenvolver offline, prepare o cache Maven antes.
- Recuperação: pare o projeto aluno, execute o JAR pronto e importe os TXT.
  O banco do aluno não é apagado nem copiado automaticamente.

## Antes de levar à sala

Teste no mesmo i5/16 GB do laboratório, com IDE e navegador abertos.
Prepare Java, Maven, dependências e modelo ANTES da aula. Teste as perguntas
do roteiro e cronometre a resposta. Não há garantia de velocidade sem esse teste.
O sistema é didático, local, sem autenticação e sem controle por usuário.
Usa loopback (127.0.0.1); não o publique na internet como produto pronto.
