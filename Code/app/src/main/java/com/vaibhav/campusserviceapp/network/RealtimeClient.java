package com.vaibhav.campusserviceapp.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.vaibhav.campusserviceapp.models.Message;
import com.vaibhav.campusserviceapp.utils.Constants;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class RealtimeClient {
    private static final String TAG = "RealtimeClient";
    private WebSocket webSocket;
    private final OkHttpClient client;
    private final Gson gson = new Gson();
    private MessageListener listener;
    private String currentChatId;

    public interface MessageListener {
        void onNewMessage(Message message);
    }

    public RealtimeClient(OkHttpClient client) {
        this.client = client;
    }

    public void connect(String chatId, MessageListener listener) {
        this.listener = listener;
        this.currentChatId = chatId;

        Request request = new Request.Builder()
                .url(Constants.REALTIME_URL)
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket ws, Response response) {
                Log.d(TAG, "WebSocket connected");
                // Send join message
                JsonObject joinMsg = new JsonObject();
                joinMsg.addProperty("topic", "realtime:public:messages:room_id=eq." + chatId);
                joinMsg.addProperty("event", "phx_join");

                JsonObject payload = new JsonObject();
                JsonObject config = new JsonObject();
                config.addProperty("event", "INSERT");
                config.addProperty("schema", "public");
                config.addProperty("table", "messages");
                config.addProperty("filter", "room_id=eq." + chatId);

                JsonArray postgresChanges = new JsonArray();
                postgresChanges.add(config);
                payload.add("config", new JsonObject());
                payload.add("postgres_changes", postgresChanges);

                joinMsg.add("payload", payload);
                joinMsg.addProperty("ref", "1");

                ws.send(joinMsg.toString());

                // Start heartbeat
                startHeartbeat(ws);
            }

            @Override
            public void onMessage(WebSocket ws, String text) {
                try {
                    JsonObject msg = gson.fromJson(text, JsonObject.class);
                    String event = msg.has("event") ? msg.get("event").getAsString() : "";

                    if ("postgres_changes".equals(event) || "INSERT".equals(event)) {
                        JsonObject payload2 = msg.getAsJsonObject("payload");
                        if (payload2 != null && payload2.has("record")) {
                            Message message = gson.fromJson(payload2.getAsJsonObject("record"), Message.class);
                            if (listener != null) {
                                listener.onNewMessage(message);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(WebSocket ws, Throwable t, Response response) {
                Log.e(TAG, "WebSocket failure: " + t.getMessage());
            }

            @Override
            public void onClosed(WebSocket ws, int code, String reason) {
                Log.d(TAG, "WebSocket closed: " + reason);
            }
        });
    }

    private void startHeartbeat(WebSocket ws) {
        new Thread(() -> {
            while (webSocket != null) {
                try {
                    Thread.sleep(30000);
                    JsonObject heartbeat = new JsonObject();
                    heartbeat.addProperty("topic", "phoenix");
                    heartbeat.addProperty("event", "heartbeat");
                    heartbeat.add("payload", new JsonObject());
                    heartbeat.addProperty("ref", String.valueOf(System.currentTimeMillis()));
                    ws.send(heartbeat.toString());
                } catch (InterruptedException e) {
                    break;
                }
            }
        }).start();
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close(1000, "Leaving");
            webSocket = null;
        }
        listener = null;
    }
}
