package br.edu.aula;
import java.text.Normalizer;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class BuscaService {
 public record Fonte(long documentoId,String titulo,int numero,String trecho,double pontuacao) {}
 private final DocumentoRepository documentos;
 private static final Set<String> IGNORAR=Set.of(
  "a","o","as","os","de","da","do","das","dos","e","em","na","no","nas","nos",
  "um","uma","para","por","com","que","qual","quais","como","quando","onde",
  "preciso","precisa","posso","devo","meu","minha","eu","se","ao","sobre","tem","ter","ser","sao");
 public BuscaService(DocumentoRepository documentos) { this.documentos=documentos; }
 // Base pequena: recalcula sobre os documentos atuais, sem índice separado obsoleto.
 public List<Fonte> buscar(String pergunta) {
  Set<String> termos=termos(pergunta);
  if(termos.isEmpty()) return List.of();
  var candidatos=new ArrayList<Fonte>();
  for(var d:documentos.listar()) {
   var partes=dividir(d.conteudo());
   for(int i=0;i<partes.size();i++) {
    var palavras=termos(d.titulo()+" "+partes.get(i));
    long encontrados=termos.stream().filter(palavras::contains).count();
    double cobertura=(double)encontrados/termos.size();
    if(cobertura>=0.5) candidatos.add(new Fonte(d.id(),d.titulo(),i+1,partes.get(i),cobertura));
   }
  }
  return candidatos.stream().sorted(Comparator.comparingDouble(Fonte::pontuacao).reversed()
   .thenComparingLong(Fonte::documentoId).thenComparingInt(Fonte::numero)).limit(3).toList();
 }
 static Set<String> termos(String texto) {
  String normal=Normalizer.normalize(texto,Normalizer.Form.NFD)
   .replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);
  var resultado=new HashSet<String>();
  for(String t:normal.split("[^a-z0-9]+"))
   if(t.length()>1 && !IGNORAR.contains(t)) resultado.add(t);
  return resultado;
 }
 static List<String> dividir(String texto) {
  var saida=new ArrayList<String>();
  for(String paragrafo:texto.split("\\R\\s*\\R")) {
   String resto=paragrafo.trim();
   while(!resto.isEmpty()) {
    int fim=Math.min(700,resto.length());
    if(fim<resto.length()) {
     int espaco=resto.lastIndexOf(' ',fim);
     if(espaco>0) fim=espaco;
    }
    saida.add(resto.substring(0,fim).trim());resto=resto.substring(fim).trim();
   }
  }
  return saida;
 }
}
