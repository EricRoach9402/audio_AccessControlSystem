package com.example.user.audio_soundnet.WebSocketPackage;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class WebSocket extends WebSocketClient {
    public WebSocket(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Log.d("Dolphinweb:","onOpen");
    }

    @Override
    public void onMessage(String message) {
        Log.d("Dolphinweb:","onMessage" + message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d("Dolphinweb:","onClose");
    }

    @Override
    public void onError(Exception ex) {
        Log.d("Dolphinweb:","onError");
    }
}
