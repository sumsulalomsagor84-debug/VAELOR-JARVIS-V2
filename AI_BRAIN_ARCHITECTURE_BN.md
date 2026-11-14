# J.A.R.V.I.S. — Hybrid AI Brain (চূড়ান্ত বাস্তবায়ন পরিকল্পনা)

## সিদ্ধান্ত
- AI Brain হবে **Cloud-first Hybrid**: ফোনে বড় model download/install করতে হবে না; এতে TECNO ফোনের storage বাঁচবে।
- Voice ও Chat একই conversation history ও AI Gateway ব্যবহার করবে।
- Wake Word / background listening পরে।
- Gemini ও Claude এই build-এ নেই। Gemini API-এর বর্তমান অতিরিক্ত শর্ত consumer app-এর উপযোগিতা নিয়ে সীমাবদ্ধতা দেয়; Claude API-তে নির্ভরযোগ্য দৈনিক ফ্রি quota নেই।
- Free-only provider order: **Groq → Cloudflare Workers AI → OpenRouter Free**।

## Failover আচরণ
1. Android app সাম্প্রতিক chat messages HTTPS-এ Cloudflare Worker-এ পাঠাবে।
2. Worker প্রথমে Groq free quota ব্যবহার করবে।
3. Groq unavailable / rate-limited হলে Cloudflare Workers AI চেষ্টা করবে।
4. সেটিও unavailable / daily free allocation শেষ হলে OpenRouter-এর `openrouter/free` চেষ্টা করবে।
5. সবগুলো ব্যর্থ হলে ব্যবহারকারীকে অপেক্ষা করতে বলবে; paid model বা billing fallback হবে না।
6. Provider rate limit তাদের নিজস্ব server-এ প্রয়োগ হয়; সব quota প্রতিদিন রিসেট হবে—এমন নিশ্চয়তা নেই। Provider-specific reset সময় অনুসরণ করতে হবে।

## Provider ও quota নোট
- Groq: free tier-এর RPM/RPD/TPM/TPD মডেল ও account অনুযায়ী বদলায়; Dashboard-এর Limits page authoritative।
- Cloudflare Workers AI: Workers Free plan-এ বর্তমানে 10,000 Neurons/day free allocation; সীমা পার হলে Free plan-এ inference বন্ধ হয়। Paid Workers plan-এ upgrade করবে না।
- OpenRouter: free variants বর্তমানে সাধারণত 50 requests/day ও 20 requests/minute; model/provider availability পরিবর্তন হতে পারে।
- Free tier সীমিত, তাই “অসীম প্রশ্ন” নয়। এক provider quota শেষ হলে পরেরটি চেষ্টা করা হবে।

## নিরাপত্তা ও ডেটা
- Provider API keys শুধু Cloudflare Worker Secrets-এ থাকবে; Android APK, GitHub source বা ChatGPT chat-এ নয়।
- Worker `JARVIS_GATEWAY_TOKEN` ছাড়া chat request গ্রহণ করে না। Android app-এ শুধু Worker URL ও gateway token সেট করতে হবে।
- Gateway token app-এর private preferences-এ থাকে; এটি provider key নয়, তবে APK/device থেকে extraction অসম্ভব—এমন দাবি করা যাবে না।
- Worker conversation database-এ history সংরক্ষণ করে না; Android app সাম্প্রতিক turn request-এ পাঠায়।
- Sensitive phone actions-এর আগে explicit confirmation লাগবে; AI response নিজে থেকে phone action execute করতে পারবে না।

## বর্তমান কোড
- `backend/worker.js`: Groq → Cloudflare → OpenRouter free-only failover.
- `backend/wrangler.toml`: Workers AI binding configuration.
- `backend/README_BN.md`: Cloudflare deployment ও secrets setup guide.
- `AiApiClient.java`: Android থেকে HTTPS Worker request.
- `MainActivity.java`: Voice ও Chat message একই AI history-তে পাঠায়; settings dialog দিয়ে Worker URL/token নেওয়া হয়।

## পরবর্তী কাজ
1. Cloudflare Worker deploy ও AI binding enable.
2. Worker Secrets-এ gateway token, Groq key, OpenRouter key যোগ (দুই API key optional; provider skip হবে)।
3. Android app-এ Worker URL ও gateway token সেট।
4. Health check এবং বাস্তব failover test—প্রতিটি provider আলাদাভাবে unavailable করে।
5. GitHub Actions build pass; তারপর TECNO-তে APK install এবং বাংলা response/voice test.

## Status
- [x] Backend failover source যোগ।
- [x] Android AI gateway client ও Voice/Chat wiring যোগ।
- [x] Android INTERNET permission যোগ।
- [ ] Cloudflare deployment ও secrets setup.
- [ ] Live API/failover verification.
- [ ] APK build এবং device test pending.
