// Sem frameworks. textContent impede que os documentos sejam interpretados como HTML.
const $ = id => document.getElementById(id);
async function api(caminho, metodo='GET', corpo) {
 const r=await fetch('/api'+caminho,{method:metodo,
  headers:corpo ? {'Content-Type':'application/json'} : {},
  body:corpo ? JSON.stringify(corpo) : undefined});
 if(!r.ok){const e=await r.json().catch(()=>({}));throw new Error(e.message || 'Falha HTTP '+r.status);}
 return r.status===204 ? null : r.json();
}
function status(id,texto,erro=false){$(id).textContent=texto;$(id).className=erro?'erro':'';}
function limpar(){$('docForm').reset();$('docId').value='';$('salvar').textContent='Salvar documento';}
function elemento(tag,texto,classe){const e=document.createElement(tag);e.textContent=texto;if(classe)e.className=classe;return e;}
async function listar(){
 const docs=await api('/documentos');$('lista').replaceChildren();
 if(!docs.length)$('lista').append(elemento('p','Nenhum documento cadastrado.','muted'));
 for(const d of docs){
  const item=elemento('div','','doc');item.append(elemento('strong',d.titulo));
  item.append(elemento('p',d.conteudo.length+' caracteres • ID '+d.id,'muted'));
  const a=elemento('div','','actions');
  const editar=elemento('button','Visualizar / editar','secondary');editar.type='button';
  editar.onclick=()=>{$('docId').value=d.id;$('titulo').value=d.titulo;$('conteudo').value=d.conteudo;$('arquivo').value='';$('salvar').textContent='Atualizar documento';$('titulo').focus();};
  const excluir=elemento('button','Excluir','danger');excluir.type='button';
  excluir.onclick=async()=>{
   if(!confirm('Excluir "'+d.titulo+'"? Seus trechos deixarão de participar das novas consultas.'))return;
   try{await api('/documentos/'+d.id,'DELETE');if($('docId').value==d.id)limpar();await listar();status('docStatus','Documento excluído. Respostas antigas no chat são apenas histórico.');}
   catch(e){status('docStatus',e.message,true);}
  };a.append(editar,excluir);item.append(a);$('lista').append(item);
 }
}
$('arquivo').onchange=async()=>{
 const f=$('arquivo').files[0];if(!f)return;
 try{
  if(!f.name.toLowerCase().endsWith('.txt') || f.size>120000)throw new Error('Escolha um TXT UTF-8 de até 120 KB.');
  const texto=await f.text();
  if(texto.length>30000)throw new Error('O texto ultrapassa 30.000 caracteres.');
  $('conteudo').value=texto;if(!$('titulo').value)$('titulo').value=f.name.replace(/\.txt$/i,'');
  status('docStatus','Arquivo carregado no formulário. Clique em salvar para cadastrar.');
 }catch(e){status('docStatus',e.message,true);}
};
$('novo').onclick=limpar;
$('docForm').onsubmit=async e=>{
 e.preventDefault();$('salvar').disabled=true;
 try{const id=$('docId').value;await api('/documentos'+(id?'/'+id:''),id?'PUT':'POST',
  {titulo:$('titulo').value,conteudo:$('conteudo').value});
  limpar();await listar();status('docStatus','Salvo. As próximas buscas já usam este conteúdo.');
 }catch(e){status('docStatus',e.message,true);}finally{$('salvar').disabled=false;}
};
document.querySelectorAll('.exemplo').forEach(b=>b.onclick=()=>{$('pergunta').value=b.textContent;$('pergunta').focus();});
$('chatForm').onsubmit=async e=>{
 e.preventDefault();const pergunta=$('pergunta').value.trim();if(!pergunta)return;
 $('enviar').disabled=true;status('chatStatus','Buscando e preparando a resposta… A IA pode levar até 120 segundos.');
 $('conversa').append(elemento('div',pergunta,'mensagem usuario'));
 try{
  const r=await api('/chat','POST',{pergunta});
  const bloco=elemento('div',r.resposta,'mensagem');
  bloco.append(elemento('p',r.modo==='ia'?'Modo IA • confira os trechos':'Contingência • sem IA','muted'));
  for(const f of r.fontes){
   const det=document.createElement('details');
   det.append(elemento('summary',f.titulo+' • trecho '+f.numero+' • ID '+f.documentoId));
   det.append(elemento('pre',f.trecho));bloco.append(det);
  }
  $('conversa').append(bloco);status('chatStatus','Consulta concluída.');$('pergunta').value='';
 }catch(e){$('conversa').append(elemento('div',e.message,'mensagem erro'));status('chatStatus','A consulta falhou; você pode tentar novamente.',true);}
 finally{$('enviar').disabled=false;$('conversa').scrollTop=$('conversa').scrollHeight;}
};
(async()=>{try{const c=await api('/config');$('config').textContent=c.modo==='ia'?'IA LOCAL • '+c.modelo:'CONTINGÊNCIA • CONSULTA DE TRECHOS SEM IA';await listar();}
catch(e){status('docStatus',e.message,true);}})();
