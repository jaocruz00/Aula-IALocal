package br.edu.aula;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
@Repository
public class DocumentoRepository {
 private final JdbcTemplate banco;
 public DocumentoRepository(JdbcTemplate banco) { this.banco=banco; }
 public List<Documento> listar() {
  return banco.query("SELECT * FROM documentos ORDER BY id",
   (r,n)->new Documento(r.getLong("id"),r.getString("titulo"),r.getString("conteudo")));
 }
 public Documento obter(long id) {
  return banco.query("SELECT * FROM documentos WHERE id=?",
   (r,n)->new Documento(r.getLong("id"),r.getString("titulo"),r.getString("conteudo")),id)
   .stream().findFirst().orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Documento não encontrado."));
 }
 public Documento criar(Documento d) {
  validar(d);
  var chave=new GeneratedKeyHolder();
  banco.update(c->{
   var s=c.prepareStatement("INSERT INTO documentos(titulo,conteudo) VALUES (?,?)",new String[]{"ID"});
   s.setString(1,d.titulo().trim());s.setString(2,d.conteudo().trim());return s;
  },chave);
  return obter(chave.getKey().longValue());
 }
 public Documento atualizar(long id,Documento d) {
  validar(d);
  if(banco.update("UPDATE documentos SET titulo=?,conteudo=? WHERE id=?",
    d.titulo().trim(),d.conteudo().trim(),id)==0)
   throw new ResponseStatusException(NOT_FOUND,"Documento não encontrado.");
  return obter(id);
 }
 public void excluir(long id) {
  if(banco.update("DELETE FROM documentos WHERE id=?",id)==0)
   throw new ResponseStatusException(NOT_FOUND,"Documento não encontrado.");
 }
 private void validar(Documento d) {
  if(d==null || d.titulo()==null || d.titulo().isBlank() || d.titulo().length()>120
   || d.conteudo()==null || d.conteudo().isBlank() || d.conteudo().length()>30000)
   throw new ResponseStatusException(BAD_REQUEST,"Informe título (até 120 caracteres) e texto (até 30.000 caracteres).");
 }
}
