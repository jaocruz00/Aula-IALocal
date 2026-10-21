package br.edu.aula;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
@Service
public class OllamaClient {
 private final ObjectMapper json;
 private final String url,modelo;
 private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
 public OllamaClient(ObjectMapper json,@Value("${ollama.url}") String url,@Value("${ollama.modelo}") String modelo) {
  this.json=json;this.url=url;this.modelo=modelo;
 }
 // ETAPA 2: enviar o contexto e a pergunta ao Ollama.
 public String gerar(String contexto,String pergunta) {
  // TODO ETAPA 2: monte JSON, faça POST /api/generate e leia response.
  // Use os campos json, http, url e modelo já preparados nesta classe.
  throw new ResponseStatusException(NOT_IMPLEMENTED,"Etapa 2: implemente gerar em OllamaClient.java.");
 }
}
