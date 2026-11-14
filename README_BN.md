# J.A.R.V.I.S. V2 — Android Voice + Chat + Free AI Gateway

বাংলা-প্রধান native Android assistant. বর্তমান source version: **0.8.0**।

## অন্তর্ভুক্ত
- Android SpeechRecognizer দিয়ে bn-BD speech input-এর চেষ্টা।
- Android TextToSpeech দিয়ে বাংলা উত্তর পড়ে শোনানোর চেষ্টা।
- Voice Output On/Off, Voice ও Chat Mode, transcript toggle.
- Local commands: YouTube, Chrome, WhatsApp খোলা; Google Search; সময়/তারিখ।
- অজানা সাধারণ প্রশ্ন HTTPS AI Gateway-এ পাঠানোর Android client.
- একই app session-এ Voice ও Chat-এর recent conversation context.
- Gateway settings dialog: Worker URL ও Gateway Token.
- GitHub Actions দিয়ে Debug APK build ও artifact upload.

## AI Gateway
Backend source: `backend/`
Provider order: **Groq → Cloudflare Workers AI → OpenRouter Free**.
একটি provider rate-limited / unavailable হলে পরেরটি চেষ্টা হয়। সবগুলো unavailable হলে কোনো paid provider চালু না করে error দেখায়।

- Provider keys শুধু Cloudflare Worker Secrets-এ থাকবে।
- APK-তে Groq/OpenRouter provider keys রাখা হয় না।
- Cloudflare Workers AI Free allocation বর্তমানে 10,000 Neurons/day; OpenRouter free variants সাধারণত 50 requests/day; Groq quota model/account-specific. এগুলো পরিবর্তনশীল ও সীমাবদ্ধ—unlimited নয়।
- Cloudflare account Workers Free plan-এ রাখবে; Paid plan বা billing upgrade করবে না।
- Gemini/Claude এই build-এ নেই।

Setup guide: [backend/README_BN.md](backend/README_BN.md)

## এখনো বাকি
Cloudflare Worker deploy, Worker Secrets, Android app-এ Worker URL/token সেট, live API/failover পরীক্ষা, সফল APK build এবং TECNO ফোনে install/device test. Source code যোগ হওয়া মানেই live AI চালু বা device test সম্পন্ন নয়।

## APK
GitHub → Actions → Android APK Build → সর্বশেষ সফল run → Artifacts থেকে `VAELOR-JARVIS-debug-apk` ZIP ডাউনলোড করে extract করুন; ভেতরের `app-debug.apk` install করুন।
