package br.edu.aula;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api")
public class ApiController {
 private final DocumentoRepository documentos;
 private final ChatService chat;
 private final String modo,modelo;
 public ApiController(DocumentoRepository documentos,ChatService chat,
  @Value("${app.modo}") String modo,@Value("${ollama.modelo}") String modelo) {
  this.documentos=documentos;this.chat=chat;this.modo=modo;this.modelo=modelo;
 }
 @GetMapping("/config") public Map<String,String> config() { return Map.of("modo",modo,"modelo",modelo); }
 @GetMapping("/documentos") public List<Documento> listar() { return documentos.listar(); }
 @GetMapping("/documentos/{id}") public Documento obter(@PathVariable long id) { return documentos.obter(id); }
 @PostMapping("/documentos") @ResponseStatus(HttpStatus.CREATED)
 public Documento criar(@RequestBody Documento d) { return documentos.criar(d); }
 @PutMapping("/documentos/{id}") public Documento atualizar(@PathVariable long id,@RequestBody Documento d) {
  return documentos.atualizar(id,d);
 }
 @DeleteMapping("/documentos/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
 public void excluir(@PathVariable long id) { documentos.excluir(id); }
 public record Pergunta(String pergunta) {}
 @PostMapping("/chat") public ChatService.Resposta perguntar(@RequestBody Pergunta p) {
  return chat.perguntar(p.pergunta());
 }
}
