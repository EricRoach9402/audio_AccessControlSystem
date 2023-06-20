package com.example.user.audio_soundnet.WebSocketPackage;

import android.content.Context;
import android.widget.Toast;

public class WebSocketConnection {

    //public static final String ws = "ws://192.168.50.64:3000";
    public static final String ws = "wss://192.168.50.172:3000/api/register";
    //public static final String ws = "wss://soundnet-server.herokuapp.com";
    public static void showtoast(Context ctx, String msg){
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }
}
