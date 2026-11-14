const MAX_MESSAGES = 24;
const MAX_TOTAL_CHARS = 16000;
const MAX_OUTPUT_TOKENS = 1200;

function reply(data, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "content-type": "application/json; charset=utf-8",
      "cache-control": "no-store",
      "access-control-allow-origin": "*",
      "access-control-allow-methods": "GET, POST, OPTIONS",
      "access-control-allow-headers": "authorization, content-type"
    }
  });
}

function safeEqual(a, b) {
  if (typeof a !== "string" || typeof b !== "string" || a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}

function validMessages(messages) {
  if (!Array.isArray(messages) || messages.length < 1 || messages.length > MAX_MESSAGES) return false;
  let total = 0;
  for (const item of messages) {
    if (!item || !["system", "user", "assistant"].includes(item.role) || typeof item.content !== "string") return false;
    total += item.content.length;
    if (total > MAX_TOTAL_CHARS) return false;
  }
  return messages.some(item => item.role === "user");
}

function extractText(data) {
  return data?.choices?.[0]?.message?.content
    ?? data?.response
    ?? data?.result?.response
    ?? null;
}

async function groq(messages, env) {
  if (!env.GROQ_API_KEY) return null;
  const response = await fetch("https://api.groq.com/openai/v1/chat/completions", {
    method: "POST",
    headers: {
      "authorization": `Bearer ${env.GROQ_API_KEY}`,
      "content-type": "application/json"
    },
    body: JSON.stringify({
      model: "openai/gpt-oss-120b",
      messages,
      temperature: 0.4,
      max_tokens: MAX_OUTPUT_TOKENS
    }),
    signal: AbortSignal.timeout(25000)
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(`Groq HTTP ${response.status}`);
  const text = extractText(data);
  if (typeof text !== "string" || !text.trim()) throw new Error("Groq returned an empty answer");
  return { provider: "Groq", model: data.model || "openai/gpt-oss-120b", answer: text.trim() };
}

async function cloudflare(messages, env) {
  if (!env.AI) return null;
  const data = await env.AI.run("@cf/openai/gpt-oss-20b", {
    messages,
    max_tokens: MAX_OUTPUT_TOKENS,
    temperature: 0.4
  });
  const text = extractText(data);
  if (typeof text !== "string" || !text.trim()) throw new Error("Cloudflare AI returned an empty answer");
  return { provider: "Cloudflare Workers AI", model: "@cf/openai/gpt-oss-20b", answer: text.trim() };
}

async function openRouter(messages, env) {
  if (!env.OPENROUTER_API_KEY) return null;
  const response = await fetch("https://openrouter.ai/api/v1/chat/completions", {
    method: "POST",
    headers: {
      "authorization": `Bearer ${env.OPENROUTER_API_KEY}`,
      "content-type": "application/json",
      "http-referer": "https://github.com/sumsulalomsagor84-debug/VAELOR-JARVIS-V2",
      "x-title": "VAELOR J.A.R.V.I.S."
    },
    body: JSON.stringify({
      model: "openrouter/free",
      messages,
      temperature: 0.4,
      max_tokens: MAX_OUTPUT_TOKENS
    }),
    signal: AbortSignal.timeout(25000)
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) throw new Error(`OpenRouter HTTP ${response.status}`);
  const text = extractText(data);
  if (typeof text !== "string" || !text.trim()) throw new Error("OpenRouter returned an empty answer");
  return { provider: "OpenRouter Free", model: data.model || "openrouter/free", answer: text.trim() };
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return reply({ ok: true });
    const url = new URL(request.url);

    if (request.method === "GET" && url.pathname === "/health") {
      return reply({
        ok: true,
        service: "VAELOR J.A.R.V.I.S. AI Gateway",
        providers: {
          groq: Boolean(env.GROQ_API_KEY),
          cloudflare_workers_ai: Boolean(env.AI),
          openrouter_free: Boolean(env.OPENROUTER_API_KEY)
        },
        billing_policy: "free-only; no paid model routing"
      });
    }

    if (request.method !== "POST" || url.pathname !== "/v1/chat") {
      return reply({ error: "Not found" }, 404);
    }

    if (!env.JARVIS_GATEWAY_TOKEN) {
      return reply({ error: "Gateway is not configured yet." }, 503);
    }
    const auth = request.headers.get("authorization") || "";
    const supplied = auth.startsWith("Bearer ") ? auth.slice(7) : "";
    if (!safeEqual(supplied, env.JARVIS_GATEWAY_TOKEN)) {
      return reply({ error: "Unauthorized" }, 401);
    }

    let body;
    try {
      body = await request.json();
    } catch {
      return reply({ error: "Invalid JSON" }, 400);
    }
    if (!validMessages(body?.messages)) {
      return reply({ error: "messages must contain 1-24 valid chat messages (maximum 16,000 characters)." }, 400);
    }

    const attempts = [
      ["Groq", () => groq(body.messages, env)],
      ["Cloudflare Workers AI", () => cloudflare(body.messages, env)],
      ["OpenRouter Free", () => openRouter(body.messages, env)]
    ];
    const failures = [];

    for (const [name, run] of attempts) {
      try {
        const result = await run();
        if (!result) {
          failures.push(`${name}: not configured`);
          continue;
        }
        return reply({ ok: true, ...result, fallback_used: failures.length > 0 });
      } catch (error) {
        failures.push(`${name}: ${String(error?.message || error).slice(0, 160)}`);
      }
    }

    return reply({
      ok: false,
      error: "All configured free AI providers are unavailable or out of quota. Please try after their quota resets.",
      providers: failures
    }, 503);
  }
};
