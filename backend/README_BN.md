# J.A.R.V.I.S. — Cloud AI Failover Gateway

## Cloudflare deployment status
- <badge>Worker deployed</badge> Worker name: `vaelor-jarvis-gateway`
- URL: `https://vaelor-jarvis-gateway.workers.dev`
- Health endpoint: `GET /health` (deployment exists; live endpoint check still pending)
- Chat endpoint: `POST /v1/chat`
- Workers AI binding: `AI`
- Required Worker secret: `GATEWAY_TOKEN` (configured; never commit its value)
- Worker source: `backend/worker.js`

## Automatic provider order
1. **Groq** — `openai/gpt-oss-120b`; skipped until `GROQ_API_KEY` is added as a Cloudflare Worker secret.
2. **Cloudflare Workers AI** — `@cf/meta/llama-3.3-70b-instruct-fp8-fast`; AI binding is deployed.
3. **OpenRouter Free** — `openrouter/free`; skipped until `OPENROUTER_API_KEY` is added as a Cloudflare Worker secret.

A provider error, rate limit, exhausted quota, or empty response triggers the next provider. If every configured provider fails, the gateway returns HTTP 503. The Worker does not intentionally select paid models.

## Free-use and billing safety
- Cloudflare Workers AI has a free daily allocation, currently documented as 10,000 Neurons/day. Limits and pricing may change.
- This Worker does not enforce an independent hard spending cap yet. Do not enable a paid Workers plan or paid usage. Before enabling AI inference, verify the Cloudflare account remains on the Free plan and check its billing settings.
- Groq and OpenRouter keys are not configured yet. Do not paste provider keys into ChatGPT, the Android app, or GitHub; add them only under Cloudflare Worker → Settings → Variables and Secrets.
- No AI inference request has been sent during deployment, so no model usage was consumed by the deployment step.

## Android configuration
In J.A.R.V.I.S. open AI Gateway settings:
- Worker URL: `https://vaelor-jarvis-gateway.workers.dev`
- Gateway Token: use the private token supplied in the current ChatGPT conversation.

The Gateway Token is not a provider API key. It is a credential for this personal Worker endpoint. Because APK contents and app preferences can potentially be extracted from a rooted/compromised device, treat it as a basic access barrier, not perfect authentication. Rotate the Worker secret if exposed.

## API format
POST `/v1/chat`

Headers:
- `Authorization: Bearer <GATEWAY_TOKEN>`
- `Content-Type: application/json`

Body:
```json
{
  "messages": [
    {"role":"system","content":"You are J.A.R.V.I.S., a Bengali-first assistant."},
    {"role":"user","content":"বাংলাদেশের রাজধানী কী?"}
  ]
}
```

Successful response contains `ok`, `answer`, and `provider`.

## Remaining verification
- Add Groq and OpenRouter free-tier secrets (optional).
- Verify the /health endpoint from a browser.
- Configure the Android app with the Worker URL and Gateway Token.
- Run a controlled AI request after confirming billing is Free-only.
- Build APK and test on the TECNO device.
