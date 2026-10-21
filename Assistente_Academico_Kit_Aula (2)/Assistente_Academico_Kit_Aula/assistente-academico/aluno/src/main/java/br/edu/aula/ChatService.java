package br.edu.aula;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
@Service
public class ChatService {
 public record Resposta(String resposta,List<BuscaService.Fonte> fontes,String modo) {}
 private final BuscaService busca;
 private final OllamaClient ollama;
 private final String modo;
 public ChatService(BuscaService busca,OllamaClient ollama,@Value("${app.modo}") String modo) {
  if(!modo.equals("ia") && !modo.equals("trechos")) throw new IllegalArgumentException("app.modo deve ser ia ou trechos.");
  this.busca=busca;this.ollama=ollama;this.modo=modo;
 }
 // Uma geração por vez nesta instância para não disputar a CPU.
 public synchronized Resposta perguntar(String pergunta) {
  if(pergunta==null || pergunta.isBlank() || pergunta.length()>500)
   throw new ResponseStatusException(BAD_REQUEST,"Escreva uma pergunta de até 500 caracteres.");
  var fontes=busca.buscar(pergunta);
  if(fontes.isEmpty()) return new Resposta("Não encontrei trechos suficientes na base. Tente palavras presentes no documento.",List.of(),modo);
  String contexto=montarContexto(fontes);
  if(modo.equals("trechos"))
   return new Resposta("Modo de contingência — sem geração por IA. Confira os trechos recuperados abaixo.",fontes,modo);
  return new Resposta(ollama.gerar(contexto,pergunta),fontes,modo);
 }
 // ETAPA 1: preparar o contexto que será enviado ao modelo.
 String montarContexto(List<BuscaService.Fonte> fontes) {
  // TODO ETAPA 1: use StringBuilder para juntar título, id, número e trecho.
  // Consulte o método equivalente na pasta completo, se precisar recuperar.
  throw new ResponseStatusException(NOT_IMPLEMENTED,"Etapa 1: implemente montarContexto em ChatService.java.");
 }
}
