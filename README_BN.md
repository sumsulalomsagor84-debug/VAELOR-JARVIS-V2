# J.A.R.V.I.S. V2 — Android Speech + Local Commands

বাংলা-প্রধান Android assistant-এর native Java foundation (0.5.0)।

## অন্তর্ভুক্ত
- Android SpeechRecognizer দিয়ে bn-BD speech input-এর চেষ্টা।
- Android TextToSpeech দিয়ে বাংলা লেখা ও কমান্ডের উত্তর পড়ে শোনানোর চেষ্টা।
- Voice Output On/Off।
- Microphone permission কেবল মাইক্রোফোন বোতাম চাপলে চাওয়া হয়।
- স্থানীয় কমান্ড: YouTube, Chrome, WhatsApp খোলা; Google Search; সময়/তারিখ; প্রাথমিক Study Mode।
- কথা শনাক্ত হওয়ার পর ব্যবহারকারীকে “কমান্ড চালান” চাপতে হয়; কোনো অ্যাপ কমান্ড নিজে থেকে চালু হয় না।
- GitHub Actions দিয়ে Debug APK build এবং artifact upload।

## সীমাবদ্ধতা ও নিরাপত্তা
বাংলা speech ও TTS ফোনের Speech Service এবং TTS engine-এ থাকা ভাষা-ডেটার ওপর নির্ভরশীল। Offline কাজ নিশ্চিত নয়। এটি Wake Word, background listening, পূর্ণ AI backend, Contacts access, WhatsApp call/message automation বা unrestricted app control নয়। Study Mode এখনো নির্দেশনা-ভিত্তিক স্থানীয় প্রাথমিক ধাপ; পূর্ণ AI Tutor নয়।
অ্যাপ নিজে থেকে কল বা মেসেজ পাঠায় না। ফোনে ইনস্টল ও বাস্তব পরীক্ষা না হওয়া পর্যন্ত device compatibility verified নয়।

## APK
GitHub → Actions → Android APK Build → সর্বশেষ সফল run → Artifacts থেকে VAELOR-JARVIS-debug-apk ZIP ডাউনলোড করে extract করুন; ভেতরের app-debug.apk ইনস্টল করুন।

## সংস্করণ 0.7.0 — Voice ও Chat Mode
- অ্যাপের Launcher নাম: J.A.R.V.I.S.
- Voice ও Chat Mode-এর মধ্যে পরিবর্তন করা যায়।
- একই স্ক্রিনে Voice Mode-এর সঙ্গে কথার Transcript দেখানোর ব্যবস্থা যোগ হয়েছে।
- ভয়েস কমান্ড: “চ্যাট মোডে যাও”, “ভয়েস মোডে যাও”, “স্ক্রিনে লেখা দেখাও”, “লেখা লুকাও”।
- টাইপ করা বার্তা Chat Mode-এ কথোপকথনের মতো দেখায়।
- AI Brain এখনো সংযুক্ত হয়নি; অজানা প্রশ্নে অ্যাপ স্পষ্টভাবে এই সীমাবদ্ধতা জানায়।
- Wake Word এখনো ইচ্ছাকৃতভাবে পরে রাখা হয়েছে।
