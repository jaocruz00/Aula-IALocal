# Validação do kit

Ambiente de verificação: Linux, OpenJDK 17.0.20, Maven 3.9.9.

## Concluído
- Solução completa compilada e empacotada como JAR executável.
- 7 testes JUnit de backend passaram: CRUD HTTP, atualização/exclusão refletidas
  na busca, validação, ausência de fontes sem chamada ao modelo, integração HTTP
  com servidor simulado, contingência, normalização/limite de trechos e falha de conexão.
  Alguns testes cobrem mais de um comportamento.
- Versão aluno compilada com 2 testes de busca aprovados.
- Sintaxe JavaScript verificada com node --check.
- JAR executado em modo trechos: recursos HTML/JS servidos, cadastro, consulta,
  edição, persistência após reiniciar o processo e exclusão verificados via HTTP.

## Pendente no seu laboratório
- Execução com Ollama e Qwen reais: qualidade, abstenção e tempo de resposta.
- Fluxo visual completo em navegador e operação dos botões.
- Scripts .bat em Windows e execução com JDK 21.
- Uso simultâneo de IDE, navegador e modelo nos PCs i5/16 GB.
- Validação de comportamento com documentos diferentes dos exemplos.

Não houve inferência real com Qwen neste ambiente. O teste de integração usa
um servidor HTTP local que simula o contrato da API, sem apresentar isso como IA.
A compilação usa alvo Java 17, compatível com execução em Java 21; Java 21 não
foi executado neste ambiente. O frontend foi inspecionado no código e na sintaxe,
sem teste visual automatizado concluído.
