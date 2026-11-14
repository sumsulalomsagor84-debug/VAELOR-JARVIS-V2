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
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
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
    private boolean ttsReady=false, voiceEnabled=true, showTranscript=false, chatMode=false;
    private EditText input;
    private TextView status, transcript, modeVoice, modeChat, voiceToggle;
    private LinearLayout transcriptList, centerArea;
    private ScrollView transcriptScroll;
    private SpeechRecognizer recognizer;
    private OrbView orb;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        buildUi();
        tts=new TextToSpeech(this,result->{
            if(result==TextToSpeech.SUCCESS){
                int lang=tts.setLanguage(new Locale("bn","BD"));
                ttsReady=lang!=TextToSpeech.LANG_MISSING_DATA && lang!=TextToSpeech.LANG_NOT_SUPPORTED;
                setStatus(ttsReady?"বাংলা কণ্ঠ প্রস্তুত":"ফোনে Bengali Text-to-Speech voice data ইনস্টল করতে হবে।");
            } else setStatus("ফোনের Text-to-Speech Engine চালু হয়নি।");
        });
    }

    private int dp(float n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
    private GradientDrawable rounded(int color,int radius,int stroke){
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        if(stroke!=0)d.setStroke(dp(1),stroke); return d;
    }
    private TextView label(String value,float size,int color){
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color); return t;
    }
    private TextView control(String value){
        TextView t=label(value,16,Color.WHITE); t.setGravity(Gravity.CENTER);
        t.setPadding(dp(12),dp(8),dp(12),dp(8)); t.setBackground(rounded(0xFF202024,24,0xFF34343A)); return t;
    }

    private void buildUi(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18),dp(10),dp(18),dp(12)); root.setBackgroundColor(Color.BLACK);

        TextView brand=label("J.A.R.V.I.S.",18,0xFFE8EAF5);
        brand.setGravity(Gravity.CENTER); brand.setLetterSpacing(0.16f);
        root.addView(brand,new LinearLayout.LayoutParams(-1,dp(48)));

        LinearLayout modes=new LinearLayout(this); modes.setGravity(Gravity.CENTER); modes.setOrientation(LinearLayout.HORIZONTAL);
        modeVoice=control("VOICE"); modeChat=control("CHAT");
        LinearLayout.LayoutParams modeLp=new LinearLayout.LayoutParams(dp(112),dp(40)); modeLp.setMargins(dp(5),0,dp(5),0);
        modes.addView(modeVoice,modeLp); modes.addView(modeChat,modeLp);
        root.addView(modes,new LinearLayout.LayoutParams(-1,dp(48)));
        modeVoice.setOnClickListener(v->setMode(false));
        modeChat.setOnClickListener(v->setMode(true));

        FrameLayout stage=new FrameLayout(this);
        root.addView(stage,new LinearLayout.LayoutParams(-1,0,1));

        centerArea=new LinearLayout(this); centerArea.setOrientation(LinearLayout.VERTICAL); centerArea.setGravity(Gravity.CENTER);
        stage.addView(centerArea,new FrameLayout.LayoutParams(-1,-1));

        orb=new OrbView();
        LinearLayout.LayoutParams orbLp=new LinearLayout.LayoutParams(dp(154),dp(154)); orbLp.gravity=Gravity.CENTER;
        centerArea.addView(orb,orbLp); orb.setOnClickListener(v->startSpeech());

        status=label("কথা বলতে মাইক্রোফোনে চাপুন",13,0xFF9696A2);
        status.setGravity(Gravity.CENTER); status.setPadding(0,dp(20),0,dp(10));
        centerArea.addView(status,new LinearLayout.LayoutParams(-1,-2));

        transcriptScroll=new ScrollView(this);
        transcriptScroll.setFillViewport(false);
        transcriptList=new LinearLayout(this); transcriptList.setOrientation(LinearLayout.VERTICAL);
        transcriptList.setPadding(dp(4),dp(10),dp(4),dp(10));
        transcriptScroll.addView(transcriptList);
        FrameLayout.LayoutParams tlp=new FrameLayout.LayoutParams(-1,-1);
        stage.addView(transcriptScroll,tlp);
        transcriptScroll.setVisibility(View.GONE);

        LinearLayout composer=new LinearLayout(this); composer.setOrientation(LinearLayout.VERTICAL);
        composer.setPadding(dp(16),dp(10),dp(12),dp(10));
        composer.setBackground(rounded(0xFF171719,28,0xFF303035));
        root.addView(composer,new LinearLayout.LayoutParams(-1,-2));

        input=new EditText(this); input.setSingleLine(false); input.setMinLines(1); input.setMaxLines(3);
        input.setTextSize(16); input.setTextColor(Color.WHITE); input.setHintTextColor(0xFFB6B6BE);
        input.setHint("Ask me anything..."); input.setBackgroundColor(Color.TRANSPARENT);
        input.setPadding(0,dp(4),0,dp(8));
        composer.addView(input,new LinearLayout.LayoutParams(-1,-2));
        input.setOnFocusChangeListener((v,hasFocus)->{if(hasFocus && !chatMode)setMode(true);});

        LinearLayout actions=new LinearLayout(this); actions.setGravity(Gravity.CENTER_VERTICAL);
        composer.addView(actions,new LinearLayout.LayoutParams(-1,dp(44)));

        TextView plus=control("＋"); actions.addView(plus,new LinearLayout.LayoutParams(dp(42),-1));
        plus.setOnClickListener(v->Toast.makeText(this,"অতিরিক্ত টুল পরবর্তী সংস্করণে আসবে।",Toast.LENGTH_SHORT).show());

        TextView search=control("◎"); actions.addView(search,new LinearLayout.LayoutParams(dp(42),-1));
        search.setOnClickListener(v->{String q=input.getText().toString().trim();if(!q.isEmpty())searchWeb(q);else setStatus("সার্চের বিষয় লিখুন।");});

        TextView spacer=label("",1,Color.WHITE); actions.addView(spacer,new LinearLayout.LayoutParams(0,1,1));

        TextView mic=control("♩"); actions.addView(mic,new LinearLayout.LayoutParams(dp(42),-1));
        mic.setOnClickListener(v->{setMode(false);startSpeech();});

        voiceToggle=control("VOICE ON"); actions.addView(voiceToggle,new LinearLayout.LayoutParams(-2,-1));
        voiceToggle.setOnClickListener(v->{voiceEnabled=!voiceEnabled;voiceToggle.setText(voiceEnabled?"VOICE ON":"VOICE OFF");if(!voiceEnabled&&tts!=null)tts.stop();setStatus(voiceEnabled?"Voice response চালু":"Voice response বন্ধ");});

        TextView send=control("➤"); actions.addView(send,new LinearLayout.LayoutParams(dp(44),-1));
        send.setOnClickListener(v->sendTypedMessage());

        setContentView(root);
        setMode(false);
    }

    private void setMode(boolean chat){
        chatMode=chat;
        modeChat.setBackground(rounded(chat?0xFF34343B:0xFF202024,24,chat?0xFF8D75FF:0xFF34343A));
        modeVoice.setBackground(rounded(!chat?0xFF34343B:0xFF202024,24,!chat?0xFF8D75FF:0xFF34343A));
        if(chat){
            orb.setVisibility(View.GONE); status.setVisibility(View.GONE);
            transcriptScroll.setVisibility(View.VISIBLE);
            if(transcriptList.getChildCount()==0)addBubble("J.A.R.V.I.S.","হ্যালো Captain VAELOR। কীভাবে সাহায্য করতে পারি?",false);
        }else{
            transcriptScroll.setVisibility(showTranscript?View.VISIBLE:View.GONE);
            orb.setVisibility(View.VISIBLE); status.setVisibility(View.VISIBLE);
            ((FrameLayout)orb.getParent()).bringChildToFront(centerArea);
            if(showTranscript)transcriptScroll.bringToFront();
        }
    }

    private void addBubble(String who,String message,boolean user){
        LinearLayout bubble=new LinearLayout(this); bubble.setOrientation(LinearLayout.VERTICAL);
        bubble.setPadding(dp(14),dp(10),dp(14),dp(10));
        bubble.setBackground(rounded(user?0xFF283449:0xFF202024,18,0));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,-2);
        lp.setMargins(user?dp(42):dp(2),dp(6),user?dp(2):dp(42),dp(6));
        lp.gravity=user?Gravity.RIGHT:Gravity.LEFT;
        TextView name=label(who,11,0xFF9EA7BB); bubble.addView(name);
        TextView body=label(message,15,Color.WHITE); body.setPadding(0,dp(4),0,0); bubble.addView(body);
        transcriptList.addView(bubble,lp);
        transcriptScroll.post(()->transcriptScroll.fullScroll(View.FOCUS_DOWN));
    }

    private void sendTypedMessage(){
        String message=input.getText().toString().trim(); if(message.isEmpty())return;
        input.setText(""); ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(input.getWindowToken(),0);
        setMode(true); addBubble("তুমি",message,true); processMessage(message,false);
    }

    private class OrbView extends View{
        private final Paint p=new Paint(3);
        OrbView(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float cx=getWidth()/2f,cy=getHeight()/2f,r=Math.min(cx,cy)-5;
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(4));
            p.setShader(new LinearGradient(cx-r,cy-r,cx+r,cy+r,new int[]{0xFF328BFF,0xFF44D5E8,0xFFB66CFF,0xFF328BFF},null,Shader.TileMode.CLAMP));
            c.drawCircle(cx,cy,r,p);p.setShader(null);p.setStrokeWidth(dp(2.4f));p.setStrokeCap(Paint.Cap.ROUND);p.setColor(Color.WHITE);
            c.drawRoundRect(new RectF(cx-dp(6),cy-dp(17),cx+dp(6),cy+dp(7)),dp(7),dp(7),p);
            c.drawArc(new RectF(cx-dp(13),cy-dp(3),cx+dp(13),cy+dp(19)),0,180,false,p);
            c.drawLine(cx,cy+dp(19),cx,cy+dp(27),p);c.drawLine(cx-dp(7),cy+dp(27),cx+dp(7),cy+dp(27),p);
        }
    }

    private void startSpeech(){
        if(!SpeechRecognizer.isRecognitionAvailable(this)){setStatus("Speech Recognition service পাওয়া যায়নি। Google voice service পরীক্ষা করুন।");return;}
        if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},41);return;}
        try{
            if(recognizer!=null){recognizer.destroy();recognizer=null;}
            recognizer=SpeechRecognizer.createSpeechRecognizer(this);
            recognizer.setRecognitionListener(new RecognitionListener(){
                public void onReadyForSpeech(Bundle p){setStatus("শুনছি… এখন বলুন");}
                public void onBeginningOfSpeech(){setStatus("তোমার কথা শুনছি…");}
                public void onRmsChanged(float r){} public void onBufferReceived(byte[] b){}
                public void onEndOfSpeech(){setStatus("উত্তর তৈরি করছি…");}
                public void onError(int e){
                    String m=e==SpeechRecognizer.ERROR_NO_MATCH?"কথা বুঝতে পারিনি। আবার বলুন।":
                    e==SpeechRecognizer.ERROR_SPEECH_TIMEOUT?"কোনো কথা শোনা যায়নি।":
                    (e==SpeechRecognizer.ERROR_NETWORK||e==SpeechRecognizer.ERROR_NETWORK_TIMEOUT)?"ইন্টারনেট বা Speech Service সংযোগ পাওয়া যায়নি।":"ভয়েস শনাক্ত করা যায়নি (error "+e+")।";
                    setStatus(m);if(voiceEnabled)speak(m);
                }
                public void onResults(Bundle b){
                    ArrayList<String>a=b.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if(a!=null&&!a.isEmpty()){
                        String heard=a.get(0);input.setText(heard);processMessage(heard,true);
                    }else setStatus("কথা শনাক্ত হয়নি।");
                }
                public void onPartialResults(Bundle b){} public void onEvent(int t,Bundle p){}
            });
            Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-BD");i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false);
            recognizer.startListening(i);
        }catch(Exception e){setStatus("মাইক্রোফোন চালু হয়নি: "+e.getMessage());}
    }

    private void processMessage(String message,boolean fromVoice){
        String n=message.toLowerCase(new Locale("bn","BD")).trim();
        if(n.contains("চ্যাট মোডে যাও")||n.contains("chat mode")||n.contains("চ্যাট মুডে যাও")){
            setMode(true);respond("ঠিক আছে। Chat Mode চালু করেছি।",fromVoice);return;
        }
        if(n.contains("ভয়েস মোডে যাও")||n.contains("ভয়েস মোডে যাও")||n.contains("voice mode")){
            showTranscript=false;setMode(false);respond("Voice Mode-এ ফিরে এসেছি।",fromVoice);return;
        }
        if(n.contains("স্ক্রিনে লেখা দেখাও")||n.contains("লেখা দেখাও")||n.contains("show transcript")){
            showTranscript=true;setMode(false);transcriptScroll.setVisibility(View.VISIBLE);respond("এখন থেকে কথার লেখা স্ক্রিনে দেখাব।",fromVoice);return;
        }
        if(n.contains("লেখা লুকাও")||n.contains("লেখা বন্ধ করো")||n.contains("hide transcript")){
            showTranscript=false;if(!chatMode)transcriptScroll.setVisibility(View.GONE);respond("কথার লেখা লুকিয়ে দিয়েছি।",fromVoice);return;
        }
        String answer=localCommand(n,message);
        if(answer==null){
            answer="তোমার কথা বুঝেছি। কিন্তু পূর্ণ Hybrid AI Brain এখনো সংযুক্ত হয়নি। এই সংস্করণে স্থানীয় কমান্ডগুলো কাজ করে; পরের ধাপে নিরাপদ Free AI connection যোগ করব।";
        }
        respond(answer,fromVoice);
        if(fromVoice&&showTranscript){addBubble("তুমি",message,true);addBubble("J.A.R.V.I.S.",answer,false);}
        else if(!fromVoice)addBubble("J.A.R.V.I.S.",answer,false);
    }

    private String localCommand(String n,String original){
        if(n.contains("ইউটিউব")||n.contains("youtube")){openApp("com.google.android.youtube","https://www.youtube.com");return "YouTube খুলছি।";}
        if(n.contains("ক্রোম")||n.contains("chrome")){openApp("com.android.chrome","https://www.google.com");return "Chrome খুলছি।";}
        if(n.contains("হোয়াটসঅ্যাপ")||n.contains("হোয়াটসঅ্যাপ")||n.contains("whatsapp")){openApp("com.whatsapp",null);return "WhatsApp খুলছি।";}
        if(n.contains("গুগলে সার্চ")||n.contains("google search")||n.startsWith("search ")){
            String q=original.replace("গুগলে সার্চ করো","").replace("গুগলে সার্চ কর","").replaceAll("(?i)google search","").replaceFirst("(?i)^search\\s*","").trim();
            if(!q.isEmpty())searchWeb(q);return q.isEmpty()?"কী বিষয়ে সার্চ করব?":"Google-এ খুঁজছি: "+q;
        }
        if(n.contains("সেটিংস")||n.contains("settings")){startActivity(new Intent(Settings.ACTION_SETTINGS));return "ফোনের Settings খুলছি।";}
        if(n.contains("সময়")||n.contains("কয়টা বাজে")||n.contains("কটা বাজে")||n.contains("time"))return "এখন সময় "+new SimpleDateFormat("hh:mm a",new Locale("bn","BD")).format(new Date())+"।";
        if(n.contains("তারিখ")||n.contains("date"))return "আজ "+new SimpleDateFormat("EEEE, dd MMMM yyyy",new Locale("bn","BD")).format(new Date())+"।";
        if(n.contains("হ্যালো")||n.contains("hello")||n.equals("hi"))return "হ্যালো Captain VAELOR। আমি শুনছি।";
        if(n.contains("পড়াশোনা")||n.contains("পড়াশোনা")||n.contains("study"))return "Study Mode খুলতে পারি, তবে পূর্ণ AI Tutor এখনো সংযুক্ত হয়নি।";
        return null;
    }

    private void respond(String answer,boolean spoken){
        setStatus(answer);
        if(voiceEnabled)speak(answer);
    }
    private void openApp(String pkg,String fallback){
        try{Intent i=getPackageManager().getLaunchIntentForPackage(pkg);if(i!=null)startActivity(i);else if(fallback!=null)startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(fallback)));else Toast.makeText(this,"অ্যাপটি ইনস্টল করা নেই।",Toast.LENGTH_SHORT).show();}
        catch(Exception e){setStatus("অ্যাপটি খোলা যায়নি।");}
    }
    private void searchWeb(String q){
        try{Intent i=new Intent(Intent.ACTION_WEB_SEARCH);i.putExtra(SearchManager.QUERY,q);startActivity(i);}
        catch(Exception e){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/search?q="+Uri.encode(q))));}
    }
    private void speak(String s){
        if(tts!=null&&ttsReady){tts.setSpeechRate(0.92f);tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"jarvis-reply");}
        else setStatus("উত্তর লেখা হয়েছে। বাংলা TTS voice data ফোনে প্রস্তুত নেই।");
    }
    private void setStatus(String s){if(status!=null)status.setText(s);}
    @Override public void onRequestPermissionsResult(int c,String[] p,int[] r){
        super.onRequestPermissionsResult(c,p,r);if(c==41&&r.length>0&&r[0]==PackageManager.PERMISSION_GRANTED)startSpeech();else if(c==41)setStatus("মাইক্রোফোন permission দেওয়া হয়নি।");
    }
    @Override protected void onPause(){super.onPause();if(recognizer!=null)recognizer.cancel();}
    @Override protected void onDestroy(){if(recognizer!=null){recognizer.destroy();recognizer=null;}if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
}