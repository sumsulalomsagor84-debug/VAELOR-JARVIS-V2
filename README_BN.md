# VAELOR JARVIS V2 — Android Speech Foundation

বাংলা-প্রধান Android assistant-এর প্রাথমিক native Java project (0.4.0)।

## অন্তর্ভুক্ত
- Android SpeechRecognizer দিয়ে bn-BD speech input-এর চেষ্টা।
- Android TextToSpeech দিয়ে বাংলা লেখা পড়ে শোনানোর চেষ্টা।
- Voice Output On/Off।
- Microphone permission কেবল বোতাম চাপলে চাওয়া হয়।
- GitHub Actions দিয়ে Debug APK build এবং artifact upload।

## সীমাবদ্ধতা
বাংলা speech ও TTS ফোনের Speech Service এবং TTS engine-এ থাকা ভাষা-ডেটার ওপর নির্ভরশীল। Offline কাজ নিশ্চিত নয়। এটি Wake Word, background listening, AI backend বা unrestricted app control নয়। APK build ও TECNO-তে পরীক্ষা না হওয়া পর্যন্ত সেগুলো verified নয়।

## APK
Actions → Android APK Build → Run workflow। সফল হলে Artifacts থেকে VAELOR-JARVIS-debug-apk ডাউনলোড করে extract করে app-debug.apk ইনস্টল করুন।