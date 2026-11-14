# TECNO ফোনে পরীক্ষা

## 1. ইনস্টল
1. GitHub Actions-এর সর্বশেষ সফল Build থেকে VAELOR-JARVIS-debug-apk ZIP ডাউনলোড করুন।
2. ZIP Extract করে app-debug.apk খুলুন।
3. Android চাইলে শুধু এই ইনস্টল প্রক্রিয়ার জন্য “Allow from this source” অনুমতি দিন।

## 2. Speech ও TTS
1. অ্যাপ খুলে মাইক্রোফোন বোতাম চাপুন এবং Microphone permission দিন।
2. বলুন: “আজকের আবহাওয়া কেমন”। লেখা ঠিক এসেছে কি না দেখুন।
3. ইনপুটে লিখুন: “হ্যালো Captain VAELOR, আমি J.A.R.V.I.S.।”
4. Speak চাপুন। বাংলা কণ্ঠ শোনা গেলে TTS কাজ করছে।
5. Voice Output Off করে Speak চাপুন; শব্দ হওয়া উচিত নয়।

## 3. Local Command Router
“ইউটিউব খোলো”, “ক্রোম খোলো”, “হোয়াটসঅ্যাপ খোলো”, “গুগলে সার্চ করো বাংলাদেশের ইতিহাস”, “এখন কয়টা বাজে”, “আজকের তারিখ বলো”, “পড়াশোনায় সাহায্য করো”—একটি করে লিখে বা বলে “কমান্ড চালান” চাপুন।
প্রথমে অ্যাপের নাম ও কমান্ড নিশ্চিত করুন। WhatsApp কেবল খোলে; কল বা মেসেজ পাঠায় না।

## রিপোর্ট
Device model, Android version, Speech result, TTS result, প্রতিটি command result ও error লিখে রাখুন। Remote build সফল হলেই device test সম্পন্ন হয় না।

## 0.7.0 — Voice/Chat Mode manual test
- [ ] Launcher label shows J.A.R.V.I.S.
- [ ] VOICE and CHAT buttons switch modes.
- [ ] Tapping the message field opens Chat Mode and keyboard.
- [ ] Typed message appears once; local command response appears once.
- [ ] Microphone captures Bengali speech.
- [ ] “চ্যাট মোডে যাও” switches to Chat Mode.
- [ ] “ভয়েস মোডে যাও” returns to Voice Mode.
- [ ] “স্ক্রিনে লেখা দেখাও” displays transcript while voice orb remains visible.
- [ ] “লেখা লুকাও” hides transcript.
- [ ] Voice output toggle mutes/unmutes speech.
- [ ] Unknown general questions clearly report that the AI Brain is not connected yet.
