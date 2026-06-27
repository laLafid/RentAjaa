package com.lafid.rentaja.utils;

import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImgbbHelper {

    // GANTI dengan API Key yang Anda dapatkan dari website ImgBB
    private static final String API_KEY = "e4ab0a33d68bde1a8c1c0c128c1ff975";
    private static final String IMGBB_URL = "https://api.imgbb.com/1/upload?key=" + API_KEY;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }

    public static void uploadBase64(String base64Image, UploadCallback callback) {
        executor.execute(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(IMGBB_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                // ImgBB menerima data dalam format x-www-form-urlencoded dengan key "image"
                String postData = "image=" + URLEncoder.encode(base64Image, StandardCharsets.UTF_8.name());
                byte[] postDataBytes = postData.getBytes(StandardCharsets.UTF_8);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(postDataBytes);
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    // Parsing JSON Response dari ImgBB
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    if (jsonResponse.getBoolean("success")) {
                        // Mengambil URL gambar langsung (direct link)
                        String directUrl = jsonResponse.getJSONObject("data").getString("url");
                        callback.onSuccess(directUrl);
                    } else {
                        callback.onFailure("Gagal upload berdasarkan respon ImgBB");
                    }
                } else {
                    callback.onFailure("HTTP Error: " + responseCode);
                }

            } catch (Exception e) {
                Log.e("ImgbbHelper", "Error upload", e);
                callback.onFailure(e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }
}