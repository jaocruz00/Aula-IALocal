package br.edu.aula;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:teste;DB_CLOSE_DELAY=-1","app.modo=ia"})
@AutoConfigureMockMvc
class FluxoTest {
 @Autowired MockMvc mvc;
 @Autowired DocumentoRepository docs;
 @Autowired BuscaService busca;
 @Autowired ChatService chat;
 @Autowired ObjectMapper json;
 static HttpServer servidor;
 static AtomicReference<String> recebido=new AtomicReference<>("");
 static {
  try {
   servidor=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
   servidor.createContext("/api/generate",e->{
    recebido.set(new String(e.getRequestBody().readAllBytes(),StandardCharsets.UTF_8));
    byte[] corpo="{\"response\":\"Resposta do servidor de teste, não de uma IA real.\"}".getBytes(StandardCharsets.UTF_8);
    e.getResponseHeaders().add("Content-Type","application/json");
    e.sendResponseHeaders(200,corpo.length);e.getResponseBody().write(corpo);e.close();
   });servidor.start();
  }catch(Exception e){throw new RuntimeException(e);}
 }
 @DynamicPropertySource static void config(DynamicPropertyRegistry r){
  r.add("ollama.url",()->"http://127.0.0.1:"+servidor.getAddress().getPort());
 }
 @BeforeEach void limpar(){for(var d:docs.listar())docs.excluir(d.id());recebido.set("");}
 @AfterAll static void parar(){servidor.stop(0);}
 @Test void crudHttpCompleto() throws Exception {
  String body=mvc.perform(post("/api/documentos").contentType("application/json")
   .content("{\"titulo\":\"Relatório\",\"conteudo\":\"Prazo do relatório: 15 dias.\"}"))
   .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
  long id=json.readTree(body).get("id").asLong();
  mvc.perform(get("/api/documentos/"+id)).andExpect(status().isOk()).andExpect(jsonPath("$.titulo").value("Relatório"));
  mvc.perform(put("/api/documentos/"+id).contentType("application/json")
   .content("{\"titulo\":\"Relatório\",\"conteudo\":\"Prazo do relatório: 30 dias.\"}")).andExpect(status().isOk());
  assertTrue(busca.buscar("prazo relatório").get(0).trecho().contains("30 dias"));
  assertFalse(busca.buscar("prazo relatório").get(0).trecho().contains("15 dias"));
  mvc.perform(delete("/api/documentos/"+id)).andExpect(status().isNoContent());
  assertTrue(busca.buscar("prazo relatório").isEmpty());
  mvc.perform(get("/api/documentos/"+id)).andExpect(status().isNotFound());
 }
 @Test void validacaoHttp() throws Exception {
  mvc.perform(post("/api/documentos").contentType("application/json")
   .content("{\"titulo\":\" \",\"conteudo\":\"texto\"}")).andExpect(status().isBadRequest());
  mvc.perform(post("/api/chat").contentType("application/json")
   .content("{\"pergunta\":\"\"}")).andExpect(status().isBadRequest());
 }
 @Test void semRespostaNaoChamaModelo(){
  docs.criar(new Documento(null,"Estágio","O estágio exige matrícula ativa."));
  var r=chat.perguntar("astronomia galáxias");
  assertTrue(r.fontes().isEmpty());assertEquals("",recebido.get());
 }
 @Test void contextoEnviadoEFontesReais(){
  var d=docs.criar(new Documento(null,"Relatório","O prazo do relatório é de 15 dias."));
  var r=chat.perguntar("prazo relatório");
  assertEquals(d.id().longValue(),r.fontes().get(0).documentoId());
  assertTrue(recebido.get().contains("15 dias"));
  assertTrue(recebido.get().contains("\"stream\":false"));
  assertTrue(r.resposta().contains("servidor de teste"));
 }
 @Test void contingenciaNaoChamaModelo(){
  docs.criar(new Documento(null,"Relatório","O prazo do relatório é de 15 dias."));
  var r=new ChatService(busca,null,"trechos").perguntar("prazo relatório");
  assertEquals("trechos",r.modo());assertFalse(r.fontes().isEmpty());assertEquals("",recebido.get());
 }
 @Test void normalizacaoELimiteDeTrechos(){
  docs.criar(new Documento(null,"Relatório","relatório prazo ".repeat(200)));
  var r=busca.buscar("RELATORIO PRAZO");
  assertEquals(3,r.size());assertTrue(r.stream().allMatch(f->f.trecho().length()<=700));
 }
 @Test void falhaDeConexaoViraErroAmigavel(){
  var cliente=new OllamaClient(json,"http://127.0.0.1:1","teste");
  var e=assertThrows(org.springframework.web.server.ResponseStatusException.class,
   ()->cliente.gerar("contexto","pergunta"));
  assertEquals(503,e.getStatusCode().value());
 }
}
