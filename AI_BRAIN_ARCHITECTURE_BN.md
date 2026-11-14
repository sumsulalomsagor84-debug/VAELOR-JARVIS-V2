# J.A.R.V.I.S. — Hybrid AI Brain (বাংলা পরিকল্পনা)

## লক্ষ্য
Voice Mode ও Chat Mode একই AI Brain ব্যবহার করবে। Voice থেকে পাওয়া transcript এবং টাইপ করা message একই conversation pipeline-এ যাবে; উত্তর text UI-তে দেখাবে এবং Voice Output চালু থাকলে Android TTS পড়ে শোনাবে।

## Zero-cost ও safety সিদ্ধান্ত
- কোনো paid API, subscription, billing account বা গোপন API key নয়।
- Google Gemini API ব্যবহার করা হবে না: বর্তমান Gemini API Additional Terms-এ API ব্যবহারের জন্য ১৮+ বয়স এবং under-18 audience-এ ব্যবহার নিষিদ্ধ।
- প্রথম generative model হবে ফোনে চলা open-weight local model; internet ছাড়া local inference চলবে, যদি model ডাউনলোড করা থাকে।
- Online অংশে প্রথমে public web search intent/browser integration থাকবে; কোনো third-party cloud LLM API নয়।
- সব app-control tool allowlist-ভিত্তিক হবে। কল, মেসেজ, ফাইল delete বা অন্য sensitive action-এর আগে explicit confirmation লাগবে।

## সম্ভাব্য Local model/runtime
- Runtime: Google LiteRT-LM Android.
- পরীক্ষার candidate: litert-community/Qwen3-0.6B, Apache-2.0.
- Model artifact: Qwen3-0.6B.litertlm (প্রায় 586 MB download; exact file size model card-এ প্রায় 614 MB দেখানো হয়েছে).
- Model APK-তে bundle করা হবে না; user-এর অনুমতি নিয়ে in-app download অথবা manual file import হবে।
- Device RAM, free storage, CPU/GPU compatibility না জেনে model download বা auto-load করা হবে না।
- LiteRT-LM Engine initialize background thread-এ হবে; conversation lifecycle একটিমাত্র worker thread দ্বারা পরিচালিত হবে।

## AI pipeline
1. Input normalizer: Bengali + common English text.
2. Local command router: known safe phone commands first.
3. Conversation manager: recent turns and bounded local history.
4. Local LLM: general questions, explanations, study help, planning and coding assistance.
5. Tool broker: only explicit allowlisted Android intents/actions.
6. Response presenter: same answer displayed in Chat and Voice transcript; optional Bengali TTS.
7. Web search: user-requested/current information opens browser/search; no claim of AI web browsing until implemented.

## Development stages
- Stage A: inspect device RAM/storage and verify supported Android ABI/runtime.
- Stage B: add LiteRT-LM dependency and a model manager (download progress, pause/cancel, checksum/size validation, delete model).
- Stage C: add local chat engine, streaming replies, Bengali system instruction and bounded conversation history.
- Stage D: connect both Voice and Chat UI to the same brain; add local command/tool routing.
- Stage E: test cold start, latency, memory pressure, thermal/battery use and Bengali quality on the user's TECNO phone.
- Stage F: optional free cloud provider only if its terms allow this app and the user; never assume a free tier or age eligibility.

## Known limitations
- 0.6B is a small model, not equivalent to a large cloud assistant. Bengali reasoning, factual accuracy and code generation will be limited and require testing.
- On-device LLM may need substantial RAM; published measurements for this model show around 1.5 GB peak process memory on a Pixel 8a under one test setup. This is not a guarantee for TECNO devices.
- Model download uses mobile data unless Wi-Fi is selected; the app must ask before downloading and show the approximate size.
- No Wake Word or background microphone listening in this milestone.
- No unrestricted phone control, no automatic messages/calls, no silent access to private data.

## Status
Architecture documented. No LLM has been integrated or tested in the APK yet. Next required device detail: RAM size and available storage on the target TECNO phone.
