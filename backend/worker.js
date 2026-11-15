const cors = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
  "Access-Control-Allow-Headers": "Authorization, Content-Type",
  "Content-Type": "application/json; charset=utf-8"
};
const json = (data, status=200) => new Response(JSON.stringify(data), {status, headers:cors});
function authorized(request, secret) {
  const got = (request.headers.get("Authorization") || "").replace(/^Bearer\\s+/i, "");
  if (!secret || got.length !== secret.length) return false;
  let diff = 0;
  for (let i=0;i<got.length;i++) diff |= got.charCodeAt(i) ^ secret.charCodeAt(i);
  return diff === 0;
}
function extractText(data) { return data?.choices?.[0]?.message?.content || data?.response || data?.result?.response || ""; }
async function providerFetch(url, key, model, messages, extraHeaders={}) {
  const response = await fetch(url,{method:"POST",headers:{"Authorization":"Bearer "+key,"Content-Type":"application/json",...extraHeaders},body:JSON.stringify({model,messages,temperature:0.4,max_tokens:1200})});
  const body=await response.json().catch(()=>({}));
  if(!response.ok) throw new Error("HTTP "+response.status+": "+(body?.error?.message||body?.message||"provider unavailable"));
  const answer=extractText(body).trim(); if(!answer) throw new Error("Provider returned an empty answer"); return answer;
}
export default {
 async fetch(request,env) {
  const url=new URL(request.url);
  if(request.method==="OPTIONS") return new Response(null,{status:204,headers:cors});
  if(url.pathname==="/health"&&request.method==="GET") return json({ok:true,service:"JARVIS AI Gateway",version:"1.0.0",providers:["Groq","Cloudflare Workers AI","OpenRouter"]});
  if(url.pathname!=="/v1/chat"||request.method!=="POST") return json({ok:false,error:"Not found"},404);
  if(!authorized(request,env.GATEWAY_TOKEN)) return json({ok:false,error:"Unauthorized"},401);
  let payload; try{payload=await request.json();}catch{return json({ok:false,error:"Invalid JSON"},400);}
  const messages=payload?.messages;
  if(!Array.isArray(messages)||messages.length<1||messages.length>24||messages.some(m=>!m||!["system","user","assistant"].includes(m.role)||typeof m.content!=="string"||m.content.length>16000)) return json({ok:false,error:"Invalid messages payload"},400);
  const failures=[];
  if(env.GROQ_API_KEY){try{const answer=await providerFetch("https://api.groq.com/openai/v1/chat/completions",env.GROQ_API_KEY,env.GROQ_MODEL||"openai/gpt-oss-120b",messages);return json({ok:true,answer,provider:"Groq"});}catch(e){failures.push("Groq: "+String(e.message||e));}}else failures.push("Groq: API key not configured");
  if(env.AI){try{const result=await env.AI.run(env.CF_AI_MODEL||"@cf/meta/llama-3.3-70b-instruct-fp8-fast",{messages,max_tokens:1200,temperature:0.4});const answer=extractText(result).trim();if(!answer)throw new Error("Empty answer");return json({ok:true,answer,provider:"Cloudflare Workers AI"});}catch(e){failures.push("Cloudflare AI: "+String(e.message||e));}}else failures.push("Cloudflare AI: binding unavailable");
  if(env.OPENROUTER_API_KEY){try{const answer=await providerFetch("https://openrouter.ai/api/v1/chat/completions",env.OPENROUTER_API_KEY,env.OPENROUTER_MODEL||"openrouter/free",messages,{"HTTP-Referer":"https://github.com/sumsulalomsagor84-debug/VAELOR-JARVIS-V2","X-Title":"VAELOR JARVIS"});return json({ok:true,answer,provider:"OpenRouter Free"});}catch(e){failures.push("OpenRouter: "+String(e.message||e));}}else failures.push("OpenRouter: API key not configured");
  return json({ok:false,error:"সব সংযুক্ত Free AI provider unavailable বা quota শেষ। কোনো Paid API ব্যবহার করা হয়নি।",details:failures},503);
 }
};