# Leia o código nesta ordem

1. Documento.java: dados (id, título e conteúdo).
2. DocumentoRepository.java: CRUD com SQL parametrizado e JdbcTemplate.
3. ApiController.java: endpoints consumidos pelo navegador.
4. BuscaService.java: divisão, normalização, pontuação e seleção.
5. ChatService.java: busca → contexto → IA → resposta e fontes.
6. OllamaClient.java: integração HTTP e tratamento de indisponibilidade.
7. static/app.js: formulários, fetch e apresentação das fontes.

Sem JPA, Lombok, Docker, Node ou banco externo, para reduzir conceitos novos.
H2 é um banco SQL embutido. O schema é criado por schema.sql.
Arquitetura: interface HTML → controller → serviço/repositório → H2 ou Ollama.
É frontend estático + API REST, não MVC com templates no servidor.

## API

| Método | Caminho | Corpo / resultado |
|---|---|---|
| GET | /api/config | modo e modelo configurados; não é health check |
| GET | /api/documentos | lista completa |
| GET | /api/documentos/{id} | documento ou 404 |
| POST | /api/documentos | título e conteúdo; retorna 201 com id |
| PUT | /api/documentos/{id} | título e conteúdo atualizados |
| DELETE | /api/documentos/{id} | remove; retorna 204 |
| POST | /api/chat | {"pergunta":"Qual o prazo do relatório?"} |

## RAG sem embeddings

RAG = recuperar conteúdo e usá-lo na geração. Não exige necessariamente vetores.
Dividimos os documentos atuais no momento da pergunta em trechos de até 700 caracteres.
Procuramos palavras sem acentos e em minúsculas, removendo palavras frequentes.
Exigimos pelo menos 50% dos termos da pergunta presentes no título/trecho e selecionamos
até 3 trechos. Essa pontuação NÃO é confiança factual: é uma heurística didática.
Não entende sinônimos ou flexões. “vencimento” pode não encontrar “prazo”.

A base é varrida a cada consulta: edição/exclusão refletem nas próximas consultas,
sem índice persistido para sincronizar. Isso serve para poucos textos.
Uma pergunta já em andamento pode terminar usando o conteúdo anterior.
As fontes são os trechos realmente enviados ao modelo, fornecidos pelo backend.
Não comprovam automaticamente cada frase da resposta.

Nenhum trecho → mensagem determinística, sem chamar a IA.
Trecho insuficiente → o prompt pede abstenção, sem garantia. Confira as respostas.
O prompt orienta ignorar instruções embutidas nos documentos; não é proteção completa
contra prompt injection. O histórico do chat é só visual e some ao atualizar a página.

## Configuração

Edite src/main/resources/application.properties no desenvolvimento.
No JAR pronto, use argumentos:

    java -jar bin/assistente-academico.jar --app.modo=trechos --server.port=8081
    java -jar bin/assistente-academico.jar --ollama.modelo=qwen2.5:latest

Variáveis equivalentes: APP_MODO, OLLAMA_MODELO, OLLAMA_URL, SERVER_PORT.
O endereço local do Ollama é http://localhost:11434.
Usamos /api/generate, stream:false, temperatura 0.1, contexto 2048,
limite 220 tokens e timeout de 120 segundos. Respostas podem ser cortadas
pelo limite: ajuste num_predict e meça o impacto, se necessário.
Uma consulta por vez por instância. Cada PC deve rodar sua própria aplicação.

## Extensões depois da aula

Embeddings em português; PDF digital; histórico com limites; autenticação;
métricas de recuperação; testes com documentos reais autorizados.

## Referências técnicas consultadas em 16/10/2026

- https://ollama.com/library/qwen2.5
- https://github.com/ollama/ollama/blob/main/docs/api.md
- https://docs.spring.io/spring-boot/3.5/system-requirements.html

Spring Boot fixado em 3.5.7 para reprodutibilidade, sem afirmar que seja a versão
mais recente. Revise dependências antes de uso em produção.
