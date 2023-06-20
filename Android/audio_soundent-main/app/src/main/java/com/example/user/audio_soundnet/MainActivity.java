package com.example.user.audio_soundnet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

//import android.support.annotation.UiThread;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.audio_soundnet.OWLoadingAniment.OWLoading;
import com.example.user.audio_soundnet.faceID_Register.Login;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

//新增計時器
import java.util.Timer;
import java.util.TimerTask;
import android.widget.Toast;
import java.util.Random;
//import com.sackcentury.shinebuttonlib.ShineButton;


public class  MainActivity extends AppCompatActivity {

    public static double sample_rate;
    public static double duration;
    public static double symbol_size;
    public static double sample_period;
    public static String recovered_string;

    TextView text;
    Button bs;
    OWLoading owLoading;


    private Context context;
    private Receiver receiver;
    private com.example.user.audio_soundnet.WebSocketPackage.WebSocket WebSocket;
    private Login mLogin;

    private static String TAG = "MainActivity";      // Permissions to write to files
    private static final int REQUEST_WRITE_STORAGE = 112;
    private int currentApiVersion;
    private double Bw, sym_end;
    private String hast;
    public double threshold;
    public boolean anime = false;
    public int fstart;

    //設定初始時間
    int tt = 0;
    int start_tt = 0;
    Timer timer = new Timer();
    private String str_tt,all_time;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //*防止螢幕關閉*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //sendPOST();
        //*基礎數值設定*/
        sample_rate = 48000.0;//設定取樣率
        sample_period = 1 / sample_rate;
        symbol_size = 0.125;//設定symbol頻率時間長度
        fstart = 18000;//設定起始symbol頻率

        Bw = 20048.0;//設定sync頻率和最大頻率
        sym_end = 20400.0;//設定END頻率
        threshold = 1;//找sync頻率和END頻率的ESD值

        text = (TextView) findViewById(R.id.dm_text);
        bs = (Button) findViewById(R.id.reStart);
        //initSocketClient();



        //加載動畫
        owLoading = (OWLoading) findViewById(R.id.owloading);
        bs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (text.getText().equals("Loading") || text.getText().equals("Loading...")) {
                    Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show();
                } else {
                    text.setText("Loading");
                    recorder(context);
                    Toast.makeText(context, "Loading", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //**權限設定*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId  = "default_notification_channel_id";
            String channelName = "default_notification_channel_name";
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }
        currentApiVersion = Build.VERSION.SDK_INT;

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        // This work only for android 4.4+
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT) {

            getWindow().getDecorView().setSystemUiVisibility(flags);

            // Code below is to handle presses of Volume up or Volume down.
            // Without this, after pressing volume buttons, the navigation bar will
            // show up and won't hide
            final View decorView = getWindow().getDecorView();
            decorView
                    .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                        @Override
                        public void onSystemUiVisibilityChange(int visibility) {
                            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                                decorView.setSystemUiVisibility(flags);
                            }
                        }
                    });
        }

        //**firebase推播設定*/
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener( new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.i("MainActivity", "getInstanceId failed");
                    return;
                }
                // Get new Instance ID token
                String token = task.getResult().getToken();
                Log.i("MainActivity","token "+token);
            }
        });

        //**上下文*/
        context = getApplicationContext();
        recorder(context);//19_1_29_讀音檔



        //**執行線程*/
        requestRecordPermissions();
        requestWritePermissions();

    }

    //**錄音主流程*/
    private void recorder(final Context context) {

        new Thread() {
            public void run() {
                try {
                    receiver = new Receiver("recorded.wav", fstart, Bw, sym_end, sample_rate, symbol_size, duration, context,MainActivity.this);
                    receiver.record();//錄音
                    receiver.demodulate();//解調
                    recovered_string = receiver.getRecoverd_string();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //**根據辨識引導目標*/
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (recovered_string != null) {
                                sendPOST(recovered_string);
                                text.setText("解調完成:\n" + recovered_string);
                                /*if (recovered_string.equals("AA")) {
                                    sendPOST(recovered_string);
                                    text.setText("解調完成:\n" + recovered_string);
                                }
                                 else {
                                    text.setText("持續收音:\n" + recovered_string);//更改為繼續收音
                                    recorder(context);
                                }*/
                            }
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    //**設定計數器*/
    public void StartTimer(boolean Open_or_Close){
        if (Open_or_Close == true){
            try {
                if (tt != 0){
                    tt = 0;
                }
                Log.v("TimeView","Time起始時間：" + tt);
                //開始計時
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        tt++;
                    }
                };
                timer.schedule(task,100,100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if(Open_or_Close == false){
            //紀錄Sync至解調完成時間
            float flt_tt = (float) (tt - start_tt) / 10;
            str_tt = String.valueOf(flt_tt);
            Log.v("TimeView","Sync - End經過時間" + str_tt);

            //紀錄登入完成至解調完成時間
            float all_flt_tt = (float)tt / 10;
            all_time = String.valueOf(all_flt_tt);
            Log.v("TimeView","完整登入時間" + all_time);

            //Toast無法在子線程中執行，因此需要 runOnUiThread() 來訪問UI線程
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Sync - End經過時間" + str_tt + "秒 ; 完整登入時間：" + all_time, Toast.LENGTH_SHORT).show();
                }
            });
                timer.cancel();
                timer.purge();
        }
    }
    public void Record(){
        start_tt = tt;
        Log.v("TimeView","Sync時間紀錄" + (float)start_tt / 10);
    }
    //**數據傳送*/
    private void sendPOST(String doorpassword) {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .build();
        /**設置傳送所需夾帶的內容*/
        //取得登入資訊
        Intent intent = getIntent();
        String studenNumber = intent.getStringExtra("StudenNumber");
        String door = intent.getStringExtra("door");
        // 建立 JSON 物件
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Account", studenNumber);
            jsonObject.put("post_door_number", door);
            jsonObject.put("doorpassword", doorpassword);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // 將 JSON 物件轉換成字串
        String json = jsonObject.toString();
        // 建立 RequestBody 物件
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json,JSON);

        /**設置傳送需求*/
        Request request = new Request.Builder()
                //.url("http://192.168.50.194/sound_networking/PHP/AudioLogin.php")//https://soundnet-server.herokuapp.com/api/server/verify;http://192.168.50.172:3000/api/login
                .url("http://192.168.50.192:3000/send")
                //.url("http://192.168.0.103:3000/send")
                .post(body)
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
                Log.v("Response",reponseString);
                try {
                    JSONObject json = new JSONObject(reponseString);
                    if (json.getString("message").equals("success")){
                        Log.v("Response","登入成功");
                    }
                    else {
                        Log.v("Response","登入失敗" + json.getString("message"));
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    text.setText("再次驗證:\n");
                                }
                            });
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                        recorder(context);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    //*連線*/
    private void interNet(String str) {

        Uri uri = Uri.parse("https://t.ly/" + str);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    //請求權限
    public void requestRecordPermissions() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to Record Audio denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to Record Audio")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        makeRecordRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeRecordRequest();
            }
        }
    }

    protected void makeRecordRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_WRITE_STORAGE);
    }

    public void requestWritePermissions() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Permission to access the SD-CARD is required for this app to Download PDF.")
                        .setTitle("Permission required");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        Log.i(TAG, "Clicked");
                        makeWriteRequest();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeWriteRequest();
            }
        }
    }

    protected void makeWriteRequest() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_WRITE_STORAGE);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE: {

                if (grantResults.length == 0
                        || grantResults[0] !=
                        PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission has been denied by user");

                } else {

                    Log.i(TAG, "Permission has been granted by user");

                }
                return;
            }
        }
    }
    //4/22-------------------------------------------------------------
    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
    public Handler mowLoading = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {

                case 1:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            owLoading.startAnim();

                        }
                    });
                    break;
                case 0:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            owLoading.stopAnim();
                        }
                    });
                    break;
            }
        }
    };
}