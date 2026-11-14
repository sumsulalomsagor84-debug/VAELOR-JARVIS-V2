# J.A.R.V.I.S. — Free AI Failover Gateway

এই Backend J.A.R.V.I.S.-এর জন্য একাধিক **ফ্রি-টিয়ার** AI provider ধারাবাহিকভাবে ব্যবহার করে।

## Provider order

1. Groq — `openai/gpt-oss-120b` (শুধু Groq-এর free account quota)
2. Cloudflare Workers AI — `@cf/openai/gpt-oss-20b` (Workers AI free daily allocation)
3. OpenRouter — `openrouter/free` (শুধু free model router)

একটি provider quota শেষ করলে বা সাময়িকভাবে unavailable হলে পরের provider-এ চেষ্টা করে। সবগুলো ব্যর্থ হলে 503 দেয়; **কোনো paid model বা paid fallback নির্বাচন করে না**।

> Free quota, model availability ও rate limit provider-রা যেকোনো সময় বদলাতে পারে। Free tier মানে সীমাহীন ব্যবহার নয়। Cloudflare-এর Workers AI Free allocation বর্তমানে 10,000 Neurons/day; Workers Free plan-এ সীমা পার হলে inference বন্ধ হয়। Paid Workers plan চালু করা যাবে না। OpenRouter free variants বর্তমানে 50 requests/day ও 20 requests/minute সীমায় থাকতে পারে। Groq-এর সঠিক সীমা account-এর Limits page-এ দেখতে হবে।

## Security

- Provider API keys কেবল Cloudflare Worker Secrets-এ থাকবে; Android APK বা GitHub source-এ নয়।
- `JARVIS_GATEWAY_TOKEN` আবশ্যক। Worker এটি ছাড়া chat request গ্রহণ করে না।
- Android app-এ gateway token দিতে হবে; এটিকে provider API key-এর সঙ্গে গুলিয়ে ফেলবে না।
- Chat history Worker-এ সংরক্ষণ হয় না; app প্রতিটি অনুরোধে সাম্প্রতিক messages পাঠায়।
- Personal-use gateway token APK থেকে বের করা সম্ভব হতে পারে; তাই এটি একক-ব্যবহারকারীর basic barrier, পূর্ণ authentication নয়। Token ফাঁস হলে Cloudflare dashboard থেকে secret rotate করতে হবে।

## Deploy (Cloudflare Dashboard / Wrangler)

1. Cloudflare account-এ Workers & Pages খুলে Worker তৈরি করো।
2. এই ফোল্ডারের Worker source deploy করো; `wrangler.toml`-এ Workers AI binding আগে থেকেই আছে।
3. Worker → Settings → Variables and Secrets → Add secret:
   - `JARVIS_GATEWAY_TOKEN` — নিজে তৈরি করা লম্বা random token
   - `GROQ_API_KEY` — Groq key (optional; না থাকলে skip হবে)
   - `OPENROUTER_API_KEY` — OpenRouter key (optional; না থাকলে skip হবে)
4. Workers AI binding-এ `AI` নামটি সক্রিয় আছে নিশ্চিত করো।
5. Cloudflare **Workers Free** plan-এই থাকো; Paid plan বা billing upgrade করবে না।
6. Deploy শেষে `https://<worker-name>.<subdomain>.workers.dev/health` খুলে provider flags দেখো।

## API

POST `/v1/chat`

Headers:
- `Authorization: Bearer <JARVIS_GATEWAY_TOKEN>`
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

Successful response contains `answer`, `provider`, `model`, and `fallback_used`.

## Important

এই commit শুধু Backend source প্রস্তুত করে। Worker deploy, secrets configure, Android app-এর endpoint/token settings, live provider tests এবং APK build/device test এখনো বাকি। কোনো API key GitHub-এ commit করবে না, ChatGPT-তে পাঠাবে না।
