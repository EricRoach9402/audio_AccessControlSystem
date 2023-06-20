package com.example.user.audio_soundnet.faceID_Register;

import android.content.Intent;
import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.user.audio_soundnet.MainActivity;
import com.example.user.audio_soundnet.MediaPacket.MainMusic;
import com.example.user.audio_soundnet.R;
import com.example.user.audio_soundnet.ROOT.ROOT;
import com.example.user.audio_soundnet.Register;
import com.example.user.audio_soundnet.UserLogin;
import com.example.user.audio_soundnet.WebSocketPackage.WebSocket;
import com.royrodriguez.transitionbutton.TransitionButton;

import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class Login extends AppCompatActivity {

    private com.example.user.audio_soundnet.WebSocketPackage.WebSocket WebSocket;
    private com.example.user.audio_soundnet.WebSocketPackage.WebSocketConnection WebSocketConnection;
    private MainActivity mac;

    ImageView Dolphin, DolphinMaster, Soundent;
    EditText username, password;
    Button button, google, facebook, frame1, frame2;
    URI uri = URI.create(WebSocketConnection.ws);

    public static String UID,SOUNDID,doorID;
    String WebReceive;
    boolean isSuccessful = false;
    private TransitionButton transitionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Dolphin = (ImageView) findViewById(R.id.Dolphin);
        DolphinMaster = (ImageView) findViewById(R.id.DolphinMaster);
        Soundent = (ImageView) findViewById(R.id.Soundent);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        button = (Button) findViewById(R.id.button);
        google = (Button) findViewById(R.id.google);
        facebook = (Button) findViewById(R.id.facebook);
        frame1 = (Button) findViewById(R.id.frame1);
        frame2 = (Button) findViewById(R.id.frame2);

        initSocketClient();

        Opening();

        frame1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });
        frame2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, UserLogin.class);
                startActivity(intent);
            }
        });


        /*transitionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start the loading animation when the user tap the button
                //transitionButton.startAnimation();
                String entername = username.getText().toString();
                String enterpassword = password.getText().toString();
                sendPost(entername,enterpassword);

                // Do your networking task or background work here.
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // Choose a stop animation if your call was succesful or not
                        if (isSuccessful) {
                            transitionButton.stopAnimation(TransitionButton.StopAnimationStyle.EXPAND, new TransitionButton.OnAnimationStopEndListener() {
                                @Override
                                public void onAnimationStopEnd() {
                                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                    startActivity(intent);
                                }
                            });
                        } else {
                            transitionButton.stopAnimation(TransitionButton.StopAnimationStyle.SHAKE, null);
                        }
                    }
                }, 2000);
            }
        });*/
        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, MainActivity.class);
                startActivity(intent);
            }
        });
        facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPOST();
                //Intent intent = new Intent(Login.this, ROOT.class);
                //startActivity(intent);
            }
        });

    }
    //**數據傳送*/
    public void sendPOST() {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES) // read timeout
                .build();
        /**設置傳送所需夾帶的內容*/
        FormBody formBody = new FormBody.Builder()
                .add("StudenNumber", "2")
                .add("password", "4")
                .add("OpenDoor", "N")
                .add("MusicNumber","1")
                .build();
        final Request request = new Request.Builder()
                .url("http://192.168.50.194/sound_networking/PHP/AudioLogin.php") //2022/0712後IP更改
                .post(formBody)
                .build();
        /**設置回傳*/
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                /**如果傳送過程有發生錯誤*/
                //text.setText(e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                /**取得回傳*/
                String reponseString = response.body().string(),state;//response.body().string()僅可呼叫一次
                String[] cmds = reponseString.split(":");
                Log.e("Dolphinweb", "收到的錯誤消息：" + cmds[1]);
                if (cmds[1].equals("成功}")){
                    Looper.prepare();
                    Toast.makeText(Login.this,reponseString,Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    startActivity(intent);
                    Looper.loop();
                }
                else{
                    Looper.prepare();
                    Toast.makeText(Login.this,reponseString,Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }

                Log.v("Response",reponseString);
            }
        });
    }
    /**
     * 初始化websocket连接
     */
    private void initSocketClient() {
        URI uri = URI.create(WebSocketConnection.ws);
        WebSocket = new WebSocket(uri) {
            @Override
            public void onMessage(String message) {
                Log.e("Dolphinweb", "收到的消息：" + message);

                if (message.indexOf("\"webview\"") != -1){
                    WebReceive = message;

                    int startUID = WebReceive.indexOf("\"uid\"");
                    int endUID = WebReceive.indexOf(",",startUID);
                    UID = WebReceive.substring(startUID+7,endUID-1);

                    int startSOUNDID = WebReceive.indexOf("\"soundId\"");
                    int endSOUNDID = WebReceive.indexOf(",",startSOUNDID);
                    SOUNDID = WebReceive.substring(startSOUNDID+11,endSOUNDID-1);

                    int startDoorID = WebReceive.indexOf("\"door\"");
                    int endDoorID = WebReceive.indexOf("}",startDoorID);
                    doorID = WebReceive.substring(startDoorID+7,endDoorID);

                    Log.e("Dolphinweb", "UID：" + UID);
                    Log.e("Dolphinweb", "SOUNDID：" + SOUNDID);
                    Log.e("Dolphinweb", "Door：" + doorID);
                    closeConnect();
                    Intent intent = new Intent(Login.this,MainActivity.class);
                    startActivity(intent);
                }


                /*int j = message.indexOf(":");
                int i = message.indexOf(",");
                WebReceive = message.substring(j+1,i);
                Log.d("Dolphinweb","結果:" + WebReceive);

                if (WebReceive.equals("\"door\"")){
                    //Log.d("Dolphinweb","成功");
                    //
                    //WebSocket.send("6js7");
                    Intent intent = new Intent(Login.this,MainActivity.class);
                    startActivity(intent);
                    closeConnect();
                }*/
            }

            @Override
            public void onOpen(ServerHandshake handshakedata) {
                super.onOpen(handshakedata);
                Log.e("Dolphinweb", "websocket连接成功");
            }
        };
        connect();
    }

    /**
     * 连接websocket
     */
    private void connect() {
        new Thread() {
            @Override
            public void run() {
                try {
                    //connectBlocking多出一个等待操作，会先连接再发送，否则未连接发送会报错
                    WebSocket.connectBlocking();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * 断开连接
     */
    private void closeConnect() {
        try {
            if (null != WebSocket) {
                WebSocket.close();
                Log.e("Dolphinweb", "websocket斷開成功");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            WebSocket = null;
        }
    }



    private void Opening() {
        Animation am = new TranslateAnimation(500, 700, 600, 100);
        Animation bm = new TranslateAnimation(500, 350, 1500, 350);
        Animation cm = new TranslateAnimation(700, 550, 1600, 450);
        am.setDuration(2500);
        bm.setDuration(2500);
        cm.setDuration(2500);
        am.setRepeatCount(0);
        bm.setRepeatCount(0);
        cm.setRepeatCount(0);
        Dolphin.setAnimation(am);
        DolphinMaster.setAnimation(bm);
        Soundent.setAnimation(cm);
        am.startNow();
        bm.startNow();
        cm.startNow();
        am.setFillAfter(true);
        bm.setFillAfter(true);
        cm.setFillAfter(true);
        username.setVisibility(View.INVISIBLE);
        password.setVisibility(View.INVISIBLE);
        //button.setVisibility(View.INVISIBLE);
        google.setVisibility(View.INVISIBLE);
        facebook.setVisibility(View.INVISIBLE);
        frame1.setVisibility(View.INVISIBLE);
        frame2.setVisibility(View.INVISIBLE);
        //transitionButton.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                username.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                //button.setVisibility(View.VISIBLE);
                google.setVisibility(View.VISIBLE);
                facebook.setVisibility(View.VISIBLE);
                frame1.setVisibility(View.VISIBLE);
                frame2.setVisibility(View.VISIBLE);
                //transitionButton.setVisibility(View.VISIBLE);
            }
        }, 2700);
    }
}
//    private  void sendPost(String username,String password){
//        String json = "{\"username\":\"" + username + "\",\"password\":\""+password+"\"}";
//        /**建立連線*/
//        OkHttpClient client = new OkHttpClient().newBuilder()
//                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
//                .build();
//        /**設置傳送所需夾帶的內容*/
//        RequestBody body = RequestBody.create(
//                MediaType.parse("application/json"), json);
//        /**設置傳送需求*/
//        Request request = new Request.Builder()
//                .url("http://172.20.10.4:5000/applogin")
//                .post(body)
//                .build();
//        /**設置回傳*/
//        Call call = client.newCall(request);
//        call.enqueue(new Callback() {
//            @Override
//            public void onFailure(@NotNull Call call, @NotNull IOException e) {
//                final String errorMMessage = e.getMessage();
//                Login.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        isSuccessful = false;
//                        //tvRes.setText(errorMMessage);
//                    }
//                });
//            }
//
//            @Override
//            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
//                final String Message = response.body().string();
//                if (Message.equals("ok")) {
//                    Login.this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            isSuccessful = true;
//                        }
//                    });
//                }
//                else{
//                    isSuccessful = false;
//                }
//            }
//        });
//    }
