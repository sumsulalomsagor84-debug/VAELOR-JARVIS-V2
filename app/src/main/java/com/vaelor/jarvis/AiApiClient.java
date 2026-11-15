package com.vaelor.jarvis;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class AiApiClient {
    private static final String PREFS = "jarvis_ai_gateway";
    private static final String KEY_URL = "worker_url";
    private static final String KEY_TOKEN = "gateway_token";

    public interface Callback {
        void onSuccess(String answer, String provider);
        void onFailure(String message);
    }

    private AiApiClient() {}

    public static void saveConfig(Context context, String workerUrl, String token) {
        prefs(context).edit().putString(KEY_URL, workerUrl.trim().replaceAll("/+$", ""))
                .putString(KEY_TOKEN, token.trim()).apply();
    }

    public static String getWorkerUrl(Context context) {
        return prefs(context).getString(KEY_URL, "https://vaelor-jarvis-gateway.workers.dev");
    }

    public static String getGatewayToken(Context context) {
        return prefs(context).getString(KEY_TOKEN, "");
    }

    public static boolean isConfigured(Context context) {
        String url = getWorkerUrl(context);
        return url.startsWith("https://") && !getGatewayToken(context).isEmpty();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void chat(Context context, JSONArray messages, Callback callback) {
        String baseUrl = getWorkerUrl(context);
        String token = getGatewayToken(context);
        if (!baseUrl.startsWith("https://") || token.isEmpty()) {
            callback.onFailure("AI Gateway সেটআপ হয়নি। ⚙ বোতামে Worker URL ও Gateway Token যোগ করুন।");
            return;
        }

        Context appContext = context.getApplicationContext();
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(baseUrl + "/v1/chat");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(90000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + token);

                JSONObject body = new JSONObject();
                body.put("messages", messages);
                byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
                connection.setFixedLengthStreamingMode(bytes.length);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }

                int code = connection.getResponseCode();
                InputStream stream = code >= 200 && code < 300
                        ? connection.getInputStream() : connection.getErrorStream();
                String responseText = readAll(stream);
                JSONObject response = new JSONObject(responseText);

                if (code >= 200 && code < 300 && response.optBoolean("ok", false)) {
                    String answer = response.optString("answer", "").trim();
                    String provider = response.optString("provider", "AI");
                    if (answer.isEmpty()) throw new Exception("AI কোনো উত্তর পাঠায়নি।");
                    postSuccess(appContext, callback, answer, provider);
                } else {
                    String error = response.optString("error", "AI Gateway error " + code);
                    postFailure(appContext, callback, error);
                }
            } catch (Exception error) {
                String message = error.getMessage();
                if (message == null || message.trim().isEmpty()) message = "ইন্টারনেট বা AI Gateway সংযোগ পাওয়া যায়নি।";
                postFailure(appContext, callback, message);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }, "Jarvis-AI-Request").start();
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) return "{}";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }

    private static void postSuccess(Context context, Callback callback, String answer, String provider) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onSuccess(answer, provider));
    }

    private static void postFailure(Context context, Callback callback, String message) {
        new Handler(Looper.getMainLooper()).post(() -> callback.onFailure(message));
    }
}
