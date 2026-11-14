package com.vaelor.jarvis;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextToSpeech tts;
    private boolean ttsReady = false, voiceEnabled = true;
    private EditText input;
    private TextView status;
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
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24, 28, 24, 20); root.setBackgroundColor(0xFFF4F7FA);
        TextView title = new TextView(this); title.setText("VAELOR JARVIS"); title.setTextSize(25); title.setTextColor(0xFF10243A); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView intro = new TextView(this); intro.setText("হ্যালো, Captain VAELOR। আমি VAELOR JARVIS।\nবাংলায় লিখুন অথবা মাইক্রোফোন চাপুন।"); intro.setTextSize(17); intro.setTextColor(0xFF263746); intro.setPadding(0, 24, 0, 18); root.addView(intro);
        status = new TextView(this); status.setText("Speech ও TTS service প্রস্তুত হচ্ছে…"); status.setTextColor(0xFF526477); root.addView(status);
        ScrollView scroll = new ScrollView(this); root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView help = new TextView(this); help.setText("পরীক্ষা: ‘বাংলায় কথা বলো’ লিখে Speak চাপুন। মাইক্রোফোনে কথা বললে শনাক্ত লেখা এখানে আসবে।"); help.setTextSize(16); help.setTextColor(0xFF10243A); scroll.addView(help);
        input = new EditText(this); input.setHint("এখানে বাংলা লিখুন…"); input.setTextSize(17); root.addView(input, new LinearLayout.LayoutParams(-1, -2));
        Button mic = new Button(this); mic.setText("🎙 কথা বলুন / Speech Recognition"); root.addView(mic); mic.setOnClickListener(v -> startSpeech());
        Button speak = new Button(this); speak.setText("🔊 লেখা পড়ে শোনাও / Speak"); root.addView(speak); speak.setOnClickListener(v -> speakText());
        Button toggle = new Button(this); toggle.setText("Voice Output: ON"); root.addView(toggle); toggle.setOnClickListener(v -> { voiceEnabled = !voiceEnabled; status.setText(voiceEnabled ? "Voice Output চালু" : "Voice Output বন্ধ"); toggle.setText(voiceEnabled ? "Voice Output: ON" : "Voice Output: OFF"); if (!voiceEnabled && tts != null) tts.stop(); });
        setContentView(root);
    }

    private void startSpeech() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) { status.setText("এই ফোনে Speech Recognition service পাওয়া যায়নি।"); return; }
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) { requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 41); return; }
        launchRecognizer();
    }
    private void launchRecognizer() {
        try {
            if (recognizer != null) { recognizer.destroy(); recognizer = null; }
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new android.speech.RecognitionListener() {
                public void onReadyForSpeech(Bundle p) { status.setText("শুনছি… এখন বাংলায় বলুন।"); }
                public void onBeginningOfSpeech() {}
                public void onRmsChanged(float r) {}
                public void onBufferReceived(byte[] b) {}
                public void onEndOfSpeech() { status.setText("কথা শনাক্ত করা হচ্ছে…"); }
                public void onError(int e) { status.setText("Speech error: " + e + " — Speech Service/ইন্টারনেট/ভাষা সেটিংস পরীক্ষা করুন।"); }
                public void onResults(Bundle b) { ArrayList<String> a = b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION); if (a != null && !a.isEmpty()) { input.setText(a.get(0)); status.setText("Speech Recognition ফল এসেছে; পাঠানোর আগে দেখে নিন।"); } }
                public void onPartialResults(Bundle b) {}
                public void onEvent(int t, Bundle p) {}
            });
            Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
            recognizer.startListening(i);
        } catch (Exception ex) { status.setText("Speech Recognition চালু করা যায়নি: " + ex.getMessage()); }
    }
    private void speakText() {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) { Toast.makeText(this, "আগে কিছু বাংলা লিখুন।", Toast.LENGTH_SHORT).show(); return; }
        if (!voiceEnabled) { status.setText("Voice Output বন্ধ আছে।"); return; }
        if (!ttsReady) { status.setText("বাংলা TTS voice প্রস্তুত নয়। ফোনের Text-to-Speech settings পরীক্ষা করুন।"); return; }
        tts.setSpeechRate(0.92f);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "vaelor-utterance");
    }
    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == 41 && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) launchRecognizer();
        else if (requestCode == 41) status.setText("মাইক্রোফোন permission দেওয়া হয়নি।");
    }
    @Override protected void onPause() { super.onPause(); if (recognizer != null) recognizer.cancel(); }
    @Override protected void onDestroy() {
        if (recognizer != null) { recognizer.destroy(); recognizer = null; }
        if (tts != null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}