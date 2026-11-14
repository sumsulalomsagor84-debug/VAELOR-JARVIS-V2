# J.A.R.V.I.S. 0.8.0 — পরীক্ষা তালিকা

## 1. Android UI / Voice
- [ ] Launcher label J.A.R.V.I.S. দেখায়।
- [ ] VOICE / CHAT mode switch কাজ করে।
- [ ] বাংলা microphone recognition কাজ করে (Speech Service-নির্ভর)।
- [ ] Bengali TTS voice data থাকলে উত্তর শোনা যায়।
- [ ] Voice output toggle কাজ করে।
- [ ] “চ্যাট মোডে যাও”, “ভয়েস মোডে যাও”, “স্ক্রিনে লেখা দেখাও”, “লেখা লুকাও” কাজ করে।

## 2. AI Gateway configuration
- [ ] Plus/⚙ বোতামে Gateway settings dialog খোলে।
- [ ] HTTPS Cloudflare Worker URL ও Gateway Token সংরক্ষণ হয়।
- [ ] Provider API keys Android app-এ দেওয়া হয় না।
- [ ] ভুল token হলে 401 দেখায়।
- [ ] Gateway deploy না থাকলে connection error পরিষ্কার দেখায়।

## 3. AI responses
- [ ] বাংলা সাধারণ প্রশ্নের উত্তর আসে।
- [ ] English প্রশ্নের উত্তর আসে।
- [ ] Study / explanation prompt AI-তে যায়।
- [ ] Voice ও Chat একই recent context ব্যবহার করে।
- [ ] AI answer transcript-এ সঠিকভাবে দেখা যায়।
- [ ] Voice output On থাকলে AI answer TTS পড়ে শোনায়।

## 4. Failover (প্রতিটি provider পরীক্ষা)
- [ ] Groq quota স্বাভাবিক থাকলে Groq response provider হিসেবে দেখায়।
- [ ] Groq secret বাদ/limit হলে Cloudflare Workers AI-তে fallback হয়।
- [ ] Cloudflare AI unavailable/quota exhausted হলে OpenRouter Free-তে fallback হয়।
- [ ] সব provider বন্ধ/limit হলে 503 ও wait message আসে।
- [ ] কোনো paid model বা billing fallback হয় না।

## 5. Device test
- [ ] TECNO ফোনে APK install হয়।
- [ ] Wi-Fi ও mobile data দিয়ে API test হয়।
- [ ] App background/foreground করলে crash হয় না।
- [ ] বাংলা voice recognition এবং TTS বাস্তবে পরীক্ষা হয়।

## গুরুত্বপূর্ণ
GitHub Actions build success শুধু compile/build প্রমাণ করে। Worker deploy, live provider fallback এবং TECNO device test আলাদা করে যাচাই করতে হবে।
