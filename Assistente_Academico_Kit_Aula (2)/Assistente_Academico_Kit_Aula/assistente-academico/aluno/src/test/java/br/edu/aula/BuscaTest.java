package br.edu.aula;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BuscaTest {
 @Test void ignoraAcentosEMaiusculas() {
  assertEquals(BuscaService.termos("relatorio"),BuscaService.termos("RELATÓRIO"));
 }
 @Test void limitaTamanhoDosTrechos() {
  var partes=BuscaService.dividir("palavra ".repeat(300));
  assertTrue(partes.size()>1);
  assertTrue(partes.stream().allMatch(p->p.length()<=700));
 }
}
