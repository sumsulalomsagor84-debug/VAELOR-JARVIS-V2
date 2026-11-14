package com.vaelor.jarvis;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextToSpeech tts;
    private boolean ttsReady = false, voiceEnabled = true;
    private EditText input;
    private TextView status, response;
    private SpeechRecognizer recognizer;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        buildUi();
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int lang = tts.setLanguage(new Locale("bn", "BD"));
                ttsReady = lang != TextToSpeech.LANG_MISSING_DATA && lang != TextToSpeech.LANG_NOT_SUPPORTED;
                status.setText(ttsReady ? "বাংলা কণ্ঠ প্রস্তুত" : "বাংলা voice data পাওয়া যায়নি; ফোনের TTS settings পরীক্ষা করুন।");
            } else status.setText("ফোনের Text-to-Speech Engine চালু হয়নি।");
        });
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 28, 24, 20);
        root.setBackgroundColor(0xFFF4F7FA);

        TextView title = new TextView(this);
        title.setText("VAELOR JARVIS");
        title.setTextSize(25);
        title.setTextColor(0xFF10243A);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView intro = new TextView(this);
        intro.setText("হ্যালো, Captain VAELOR। আমি VAELOR JARVIS।\nবাংলায় লিখুন অথবা মাইক্রোফোন চাপুন।");
        intro.setTextSize(17);
        intro.setTextColor(0xFF263746);
        intro.setPadding(0, 24, 0, 18);
        root.addView(intro);

        status = new TextView(this);
        status.setText("Speech ও TTS service প্রস্তুত হচ্ছে…");
        status.setTextColor(0xFF526477);
        root.addView(status);

        response = new TextView(this);
        response.setText("স্থানীয় কমান্ড: YouTube/Chrome/WhatsApp খোলা, Google Search, সময়-তারিখ ও Study Mode।");
        response.setTextSize(16);
        response.setTextColor(0xFF10243A);
        response.setPadding(0, 12, 0, 12);
        root.addView(response);

        ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView help = new TextView(this);
        help.setText("ব্যবহার:\n• “ইউটিউব খোলো”\n• “ক্রোম খোলো”\n• “হোয়াটসঅ্যাপ খোলো”\n• “গুগলে সার্চ করো বাংলাদেশের ইতিহাস”\n• “এখন কয়টা বাজে”\n• “পড়াশোনায় সাহায্য করো”\n\nমাইক্রোফোনে বলা কথার পর ‘কমান্ড চালান’ চাপুন। অনুমতি ছাড়া কোনো কল বা মেসেজ পাঠানো হয় না।");
        help.setTextSize(16);
        help.setTextColor(0xFF10243A);
        scroll.addView(help);

        input = new EditText(this);
        input.setHint("এখানে বাংলা লিখুন…");
        input.setTextSize(17);
        root.addView(input, new LinearLayout.LayoutParams(-1, -2));

        Button mic = new Button(this);
        mic.setText("🎙 কথা বলুন / Speech Recognition");
        root.addView(mic);
        mic.setOnClickListener(v -> startSpeech());

        Button run = new Button(this);
        run.setText("▶ কমান্ড চালান");
        root.addView(run);
        run.setOnClickListener(v -> handleCommand());

        Button speak = new Button(this);
        speak.setText("🔊 লেখা পড়ে শোনাও / Speak");
        root.addView(speak);
        speak.setOnClickListener(v -> speakText());

        Button toggle = new Button(this);
        toggle.setText("Voice Output: ON");
        root.addView(toggle);
        toggle.setOnClickListener(v -> {
            voiceEnabled = !voiceEnabled;
            status.setText(voiceEnabled ? "Voice Output চালু" : "Voice Output বন্ধ");
            toggle.setText(voiceEnabled ? "Voice Output: ON" : "Voice Output: OFF");
            if (!voiceEnabled && tts != null) tts.stop();
        });
        setContentView(root);
    }

    private void startSpeech() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.setText("এই ফোনে Speech Recognition service পাওয়া যায়নি।");
            return;
        }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 41);
            return;
        }
        launchRecognizer();
    }

    private void launchRecognizer() {
        try {
            if (recognizer != null) {
                recognizer.destroy();
                recognizer = null;
            }
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new android.speech.RecognitionListener() {
                public void onReadyForSpeech(Bundle p) { status.setText("শুনছি… এখন বাংলায় বলুন।"); }
                public void onBeginningOfSpeech() {}
                public void onRmsChanged(float r) {}
                public void onBufferReceived(byte[] b) {}
                public void onEndOfSpeech() { status.setText("কথা শনাক্ত করা হচ্ছে…"); }
                public void onError(int e) { status.setText("Speech error: " + e + " — Speech Service/ইন্টারনেট/ভাষা সেটিংস পরীক্ষা করুন।"); }
                public void onResults(Bundle b) {
                    ArrayList<String> a = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (a != null && !a.isEmpty()) {
                        input.setText(a.get(0));
                        status.setText("কথা শনাক্ত হয়েছে। যাচাই করে ‘কমান্ড চালান’ চাপুন।");
                    }
                }
                public void onPartialResults(Bundle b) {}
                public void onEvent(int t, Bundle p) {}
            });
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            recognizer.startListening(i);
        } catch (Exception ex) {
            status.setText("Speech Recognition চালু করা যায়নি: " + ex.getMessage());
        }
    }

    private void handleCommand() {
        String command = input.getText().toString().trim();
        String normalized = command.toLowerCase(new Locale("bn", "BD"));
        if (normalized.isEmpty()) {
            Toast.makeText(this, "আগে একটি কমান্ড লিখুন বা বলুন।", Toast.LENGTH_SHORT).show();
            return;
        }

        if (normalized.contains("ইউটিউব") || normalized.contains("youtube")) {
            openApp("com.google.android.youtube", "https://www.youtube.com", "YouTube খোলার চেষ্টা করছি।");
        } else if (normalized.contains("ক্রোম") || normalized.contains("chrome")) {
            openApp("com.android.chrome", "https://www.google.com", "Chrome খোলার চেষ্টা করছি।");
        } else if (normalized.contains("হোয়াটসঅ্যাপ") || normalized.contains("হোয়াটসঅ্যাপ") || normalized.contains("whatsapp")) {
            openApp("com.whatsapp", null, "WhatsApp খোলার চেষ্টা করছি।");
        } else if (normalized.contains("গুগলে সার্চ") || normalized.contains("google search") || normalized.startsWith("search ")) {
            String query = command.replace("গুগলে সার্চ করো", "")
                    .replace("গুগলে সার্চ কর", "")
                    .replaceAll("(?i)google search", "")
                    .replaceFirst("(?i)^search\\s*", "").trim();
            if (query.isEmpty()) {
                reply("কী বিষয়ে সার্চ করব? বিষয়টি লিখে আবার কমান্ড চালান।");
            } else {
                searchWeb(query);
            }
        } else if (normalized.contains("সেটিংস") || normalized.contains("settings")) {
            try {
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                reply("ফোনের Settings খুলছি।");
            } catch (Exception e) {
                reply("ফোনের Settings খোলা যায়নি।");
            }
        } else if (normalized.contains("সময়") || normalized.contains("কয়টা বাজে") || normalized.contains("কটা বাজে") || normalized.contains("time")) {
            String now = new SimpleDateFormat("hh:mm a", new Locale("bn", "BD")).format(new Date());
            reply("এখন সময় " + now + "।");
        } else if (normalized.contains("তারিখ") || normalized.contains("date")) {
            String today = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("bn", "BD")).format(new Date());
            reply("আজ " + today + "।");
        } else if (normalized.contains("পড়াশোনা") || normalized.contains("পড়াশোনা") || normalized.contains("study")) {
            reply("Study Mode চালু। তোমার বিষয়, অধ্যায়ের নাম অথবা প্রশ্ন লিখো। আমি এই সংস্করণে প্রাথমিক অফলাইন সহায়তা দিতে পারি; পূর্ণ AI Tutor এখনো যুক্ত হয়নি।");
        } else if (normalized.contains("হ্যালো") || normalized.contains("hello") || normalized.contains("hi")) {
            reply("হ্যালো Captain VAELOR। আমি প্রস্তুত। কী করতে হবে বলো।");
        } else {
            reply("এই কমান্ডটি এখনো যোগ করা হয়নি। আপাতত YouTube, Chrome, WhatsApp, Google Search, সময়, তারিখ ও Study Mode ব্যবহার করো।");
        }
    }

    private void openApp(String packageName, String fallbackUrl, String message) {
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage(packageName);
            if (launch != null) {
                startActivity(launch);
                reply(message);
            } else if (fallbackUrl != null) {
                openUrl(fallbackUrl, message);
            } else {
                reply("এই ফোনে অ্যাপটি ইনস্টল করা আছে কি না পরীক্ষা করো।");
            }
        } catch (Exception e) {
            reply("অ্যাপটি খোলা যায়নি। ফোনে অ্যাপটি ইনস্টল আছে কি না দেখো।");
        }
    }

    private void searchWeb(String query) {
        try {
            Intent search = new Intent(Intent.ACTION_WEB_SEARCH);
            search.putExtra(SearchManager.QUERY, query);
            startActivity(search);
            reply("Google Search-এর জন্য খুঁজছি: " + query);
        } catch (Exception e) {
            openUrl("https://www.google.com/search?q=" + Uri.encode(query), "Google Search খুলছি।");
        }
    }

    private void openUrl(String url, String message) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            reply(message);
        } catch (Exception e) {
            reply("লিংকটি খোলা যায়নি।");
        }
    }

    private void reply(String message) {
        response.setText(message);
        status.setText("কমান্ড সম্পন্ন/উত্তর প্রস্তুত।");
        if (voiceEnabled && ttsReady && tts != null) {
            tts.setSpeechRate(0.92f);
            tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, "vaelor-reply");
        }
    }

    private void speakText() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "আগে কিছু বাংলা লিখুন।", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!voiceEnabled) {
            status.setText("Voice Output বন্ধ আছে।");
            return;
        }
        if (!ttsReady) {
            status.setText("বাংলা TTS voice প্রস্তুত নয়। ফোনের Text-to-Speech settings পরীক্ষা করুন।");
            return;
        }
        tts.setSpeechRate(0.92f);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vaelor-utterance");
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 41 && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) launchRecognizer();
        else if (requestCode == 41) status.setText("মাইক্রোফোন permission দেওয়া হয়নি।");
    }

    @Override protected void onPause() {
        super.onPause();
        if (recognizer != null) recognizer.cancel();
    }

    @Override protected void onDestroy() {
        if (recognizer != null) {
            recognizer.destroy();
            recognizer = null;
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}