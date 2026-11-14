package com.vaelor.jarvis;

import android.Manifest;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.graphics.drawable.GradientDrawable;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private TextToSpeech tts;
    private boolean ttsReady = false, voiceEnabled = true, listening = false;
    private EditText input;
    private TextView status, response, voiceToggle;
    private SpeechRecognizer recognizer;
    private OrbView orb;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
        tts = new TextToSpeech(this, result -> {
            if (result == TextToSpeech.SUCCESS) {
                int lang = tts.setLanguage(new Locale("bn", "BD"));
                ttsReady = lang != TextToSpeech.LANG_MISSING_DATA && lang != TextToSpeech.LANG_NOT_SUPPORTED;
                if (ttsReady) setStatus("বাংলা কণ্ঠ প্রস্তুত");
                else setStatus("বাংলা TTS voice data নেই। ফোনের Text-to-Speech সেটিংসে Bengali voice ইনস্টল করো।");
            } else setStatus("ফোনের Text-to-Speech Engine চালু হয়নি।");
        });
    }

    private GradientDrawable rounded(int color, int radius, int strokeColor) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        if (strokeColor != 0) d.setStroke(2, strokeColor);
        return d;
    }

    private TextView text(String value, float size, int color) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(color);
        return t;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(12), dp(20), dp(14));
        root.setBackgroundColor(Color.BLACK);

        TextView brand = text("VAELOR  JARVIS", 15, 0xFFB9B9C4);
        brand.setLetterSpacing(0.12f);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand, new LinearLayout.LayoutParams(-1, dp(42)));

        FrameLayout center = new FrameLayout(this);
        root.addView(center, new LinearLayout.LayoutParams(-1, 0, 1));

        orb = new OrbView();
        FrameLayout.LayoutParams op = new FrameLayout.LayoutParams(dp(152), dp(152), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        op.topMargin = dp(26);
        center.addView(orb, op);
        orb.setOnClickListener(v -> startSpeech());

        LinearLayout messageArea = new LinearLayout(this);
        messageArea.setOrientation(LinearLayout.VERTICAL);
        messageArea.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams mp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        mp.topMargin = dp(150);
        mp.leftMargin = dp(10); mp.rightMargin = dp(10);
        center.addView(messageArea, mp);

        response = text("হ্যালো, Captain VAELOR।\nআমি VAELOR JARVIS।", 18, Color.WHITE);
        response.setGravity(Gravity.CENTER);
        response.setLineSpacing(dp(5), 1f);
        messageArea.addView(response);

        status = text("কথা বলতে মাঝের মাইক্রোফোনে চাপুন", 13, 0xFF9999A5);
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(18), 0, 0);
        messageArea.addView(status);

        LinearLayout composer = new LinearLayout(this);
        composer.setOrientation(LinearLayout.VERTICAL);
        composer.setPadding(dp(18), dp(12), dp(14), dp(10));
        composer.setBackground(rounded(0xFF171719, dp(28), 0xFF303035));
        root.addView(composer, new LinearLayout.LayoutParams(-1, -2));

        input = new EditText(this);
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setMaxLines(3);
        input.setTextSize(16);
        input.setTextColor(Color.WHITE);
        input.setHintTextColor(0xFFB6B6BE);
        input.setHint("Ask me anything...");
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setPadding(0, dp(3), 0, dp(8));
        composer.addView(input, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        composer.addView(controls, new LinearLayout.LayoutParams(-1, dp(42)));

        TextView plus = control("＋");
        controls.addView(plus, new LinearLayout.LayoutParams(dp(42), -1));
        plus.setOnClickListener(v -> Toast.makeText(this, "অতিরিক্ত টুল পরবর্তী সংস্করণে যোগ হবে।", Toast.LENGTH_SHORT).show());

        TextView globe = control("◎");
        controls.addView(globe, new LinearLayout.LayoutParams(dp(42), -1));
        globe.setOnClickListener(v -> {
            String q = input.getText().toString().trim();
            if (q.isEmpty()) { setStatus("সার্চ করার বিষয়টি লিখুন।"); return; }
            searchWeb(q);
        });

        TextView tools = control("⚙");
        controls.addView(tools, new LinearLayout.LayoutParams(dp(42), -1));
        tools.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_SETTINGS)));

        View spacer = new View(this);
        controls.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));

        TextView mic = control("♩");
        controls.addView(mic, new LinearLayout.LayoutParams(dp(42), -1));
        mic.setOnClickListener(v -> startSpeech());

        voiceToggle = control("≋");
        voiceToggle.setTextColor(Color.BLACK);
        voiceToggle.setTextSize(22);
        voiceToggle.setBackground(rounded(Color.WHITE, dp(30), 0));
        controls.addView(voiceToggle, new LinearLayout.LayoutParams(dp(46), dp(42)));
        voiceToggle.setOnClickListener(v -> {
            voiceEnabled = !voiceEnabled;
            voiceToggle.setText(voiceEnabled ? "≋" : "×");
            setStatus(voiceEnabled ? "Voice output চালু" : "Voice output বন্ধ");
            if (!voiceEnabled && tts != null) tts.stop();
        });

        setContentView(root);
    }

    private TextView control(String s) {
        TextView t = text(s, 23, Color.WHITE);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private int dp(int n) { return dp((float)n); }\n    private int dp(float n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }
    private void setStatus(String s) { if (status != null) status.setText(s); }

    private class OrbView extends View {
        private final Paint p = new Paint(3);
        OrbView() { super(MainActivity.this); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float cx=getWidth()/2f, cy=getHeight()/2f, r=Math.min(cx,cy)-5;
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(4));
            p.setShader(new LinearGradient(cx-r,cy-r,cx+r,cy+r,
                new int[]{0xFF328BFF,0xFF44D5E8,0xFFB66CFF,0xFF328BFF},null,Shader.TileMode.CLAMP));
            c.drawCircle(cx,cy,r,p);
            p.setShader(null); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2.4f)); p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(Color.WHITE);
            float top=cy-dp(17), bottom=cy+dp(7);
            c.drawRoundRect(new RectF(cx-dp(6),top,cx+dp(6),bottom),dp(7),dp(7),p);
            c.drawArc(new RectF(cx-dp(13),cy-dp(3),cx+dp(13),cy+dp(19)),0,180,false,p);
            c.drawLine(cx,cy+dp(19),cx,cy+dp(27),p);
            c.drawLine(cx-dp(7),cy+dp(27),cx+dp(7),cy+dp(27),p);
        }
    }

    private void startSpeech() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            setStatus("এই ফোনে Speech Recognition service নেই। Google app/voice service পরীক্ষা করুন।");
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
            if (recognizer != null) { recognizer.destroy(); recognizer = null; }
            recognizer = SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener() {
                public void onReadyForSpeech(Bundle p) { listening=true; setStatus("শুনছি… এখন বলুন"); if(orb!=null) orb.invalidate(); }
                public void onBeginningOfSpeech() { setStatus("আপনার কথা শুনছি…"); }
                public void onRmsChanged(float r) {}
                public void onBufferReceived(byte[] b) {}
                public void onEndOfSpeech() { listening=false; setStatus("কথা বুঝে উত্তর তৈরি করছি…"); }
                public void onError(int e) {
                    listening=false;
                    String msg;
                    switch(e) {
                        case SpeechRecognizer.ERROR_AUDIO: msg="মাইক্রোফোনে সমস্যা হয়েছে।"; break;
                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: msg="মাইক্রোফোন permission দিন।"; break;
                        case SpeechRecognizer.ERROR_NETWORK:
                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: msg="ইন্টারনেট বা Speech service সংযোগ পাওয়া যায়নি।"; break;
                        case SpeechRecognizer.ERROR_NO_MATCH: msg="কথা বুঝতে পারিনি। আরেকবার স্পষ্ট করে বলুন।"; break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: msg="কোনো কথা শোনা যায়নি। আবার মাইক্রোফোন চাপুন।"; break;
                        default: msg="ভয়েস শনাক্ত করা যায়নি (error "+e+")। আবার চেষ্টা করুন।";
                    }
                    setStatus(msg);
                    if (voiceEnabled) speak(msg);
                }
                public void onResults(Bundle b) {
                    listening=false;
                    ArrayList<String> a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if(a!=null && !a.isEmpty()) {
                        String heard=a.get(0);
                        input.setText(heard);
                        input.setSelection(input.length());
                        setStatus("শুনেছি: "+heard);
                        handleCommand(heard, true);
                    } else setStatus("কথা শনাক্ত হয়নি। আবার চেষ্টা করুন।");
                }
                public void onPartialResults(Bundle b) {}
                public void onEvent(int t, Bundle p) {}
            });
            Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,true);
            i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);
            recognizer.startListening(i);
        } catch(Exception ex) { setStatus("মাইক্রোফোন চালু হয়নি: "+ex.getMessage()); }
    }

    private void handleCommand() { handleCommand(input.getText().toString().trim(), false); }

    private void handleCommand(String command, boolean fromVoice) {
        String normalized=command.toLowerCase(new Locale("bn","BD"));
        if(normalized.isEmpty()) { setStatus("আগে কিছু বলুন বা লিখুন।"); return; }

        if(normalized.contains("ইউটিউব") || normalized.contains("youtube")) {
            reply("YouTube খুলছি।");
            openApp("com.google.android.youtube","https://www.youtube.com");
        } else if(normalized.contains("ক্রোম") || normalized.contains("chrome")) {
            reply("Chrome খুলছি।");
            openApp("com.android.chrome","https://www.google.com");
        } else if(normalized.contains("হোয়াটসঅ্যাপ") || normalized.contains("হোয়াটসঅ্যাপ") || normalized.contains("whatsapp")) {
            reply("WhatsApp খুলছি।");
            openApp("com.whatsapp",null);
        } else if(normalized.contains("গুগলে সার্চ") || normalized.contains("google search") || normalized.startsWith("search ")) {
            String q=command.replace("গুগলে সার্চ করো","").replace("গুগলে সার্চ কর","")
                .replaceAll("(?i)google search","").replaceFirst("(?i)^search\\s*","").trim();
            if(q.isEmpty()) reply("কী বিষয়ে সার্চ করব?"); else searchWeb(q);
        } else if(normalized.contains("সেটিংস") || normalized.contains("settings")) {
            reply("ফোনের Settings খুলছি।");
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        } else if(normalized.contains("সময়") || normalized.contains("কয়টা বাজে") || normalized.contains("কটা বাজে") || normalized.contains("time")) {
            String now=new SimpleDateFormat("hh:mm a",new Locale("bn","BD")).format(new Date());
            reply("এখন সময় "+now+"।");
        } else if(normalized.contains("তারিখ") || normalized.contains("date")) {
            String today=new SimpleDateFormat("EEEE, dd MMMM yyyy",new Locale("bn","BD")).format(new Date());
            reply("আজ "+today+"।");
        } else if(normalized.contains("পড়াশোনা") || normalized.contains("পড়াশোনা") || normalized.contains("study")) {
            reply("Study Mode প্রস্তুত। তোমার প্রশ্নটি লিখে দাও। তবে এই সংস্করণে পূর্ণ AI Tutor এখনো যুক্ত হয়নি।");
        } else if(normalized.contains("হ্যালো") || normalized.contains("hello") || normalized.contains("hi")) {
            reply("হ্যালো Captain VAELOR। আমি শুনছি।");
        } else if(fromVoice) {
            reply("তোমার কথা শুনেছি। তবে এই সংস্করণে এখনো AI Brain বা ইন্টারনেটভিত্তিক প্রশ্নের উত্তর দেওয়ার ব্যবস্থা যুক্ত হয়নি। আপাতত অ্যাপ খোলা, সার্চ, সময়, তারিখ ও Study Mode-এর সাধারণ কমান্ড চালাতে পারি।");
        } else {
            reply("এই প্রশ্নের উত্তর দেওয়ার AI Brain এখনো যুক্ত হয়নি। আপাতত YouTube, Chrome, WhatsApp, Google Search, সময় ও তারিখের কমান্ড কাজ করে।");
        }
    }

    private void openApp(String pkg,String fallback) {
        try {
            Intent launch=getPackageManager().getLaunchIntentForPackage(pkg);
            if(launch!=null) startActivity(launch);
            else if(fallback!=null) startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(fallback)));
            else reply("এই ফোনে WhatsApp ইনস্টল আছে কি না পরীক্ষা করো।");
        } catch(Exception e) { reply("অ্যাপটি খোলা যায়নি।"); }
    }

    private void searchWeb(String q) {
        reply("Google-এ খুঁজছি: "+q);
        try {
            Intent s=new Intent(Intent.ACTION_WEB_SEARCH);
            s.putExtra(SearchManager.QUERY,q);
            startActivity(s);
        } catch(Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="+Uri.encode(q))));
        }
    }

    private void reply(String message) {
        response.setText(message);
        setStatus("উত্তর প্রস্তুত");
        if(voiceEnabled) speak(message);
    }

    private void speak(String message) {
        if(tts!=null && ttsReady) {
            tts.setSpeechRate(0.92f);
            tts.speak(message,TextToSpeech.QUEUE_FLUSH,null,"vaelor-reply");
        } else setStatus("উত্তর লেখা হয়েছে, কিন্তু বাংলা TTS voice প্রস্তুত নয়। ফোনে Bengali voice data ইনস্টল করুন।");
    }

    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(code,permissions,results);
        if(code==41 && results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) launchRecognizer();
        else if(code==41) setStatus("মাইক্রোফোন permission দেওয়া হয়নি।");
    }

    @Override protected void onPause() {
        super.onPause();
        if(recognizer!=null) recognizer.cancel();
    }

    @Override protected void onDestroy() {
        if(recognizer!=null) { recognizer.destroy(); recognizer=null; }
        if(tts!=null) { tts.stop(); tts.shutdown(); }
        super.onDestroy();
    }
}