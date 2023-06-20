package com.example.user.audio_soundnet.ROOT;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.audio_soundnet.Animation;
import com.example.user.audio_soundnet.MainActivity;
import com.example.user.audio_soundnet.R;
import com.example.user.audio_soundnet.Receiver;
import com.example.user.audio_soundnet.UserLogin;
import com.example.user.audio_soundnet.faceID_Register.Login;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class ROOT extends AppCompatActivity {

    public static double sample_rate;
    public static double duration;
    public static double symbol_size;
    public static double sample_period;
    public static String recovered_string;
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

    TextView mViewString,mCompleteTime,mSyncToEndTime;
    Button mOnceBt,mLoopBt,mClear;

    //設定初始時間
    //int tt = 0;
    //Timer timer = new Timer();

    //Timer設定
    Timer timer;
    TimerTask timerTask;
    private int time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_root);

        //*防止螢幕關閉*/
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //*基礎數值設定*/
        sample_rate = 48000.0;//設定取樣率
        sample_period = 1 / sample_rate;
        symbol_size = 0.125;//設定symbol頻率時間長度
        fstart = 18000;//設定起始symbol頻率
        Bw = 20048.0;//設定sync頻率和最大頻率
        sym_end = 20400.0;//設定END頻率
        threshold = 1;//找sync頻率和END頻率的ESD值


        //Layout設置
        mViewString = (TextView) findViewById(R.id.ViewString);
        mCompleteTime = (TextView) findViewById(R.id.CompleteTime);
        mSyncToEndTime = (TextView) findViewById(R.id.SyncToEndTime);
        mOnceBt = (Button) findViewById(R.id.OnceBt);
        mLoopBt = (Button) findViewById(R.id.LoopBt);
        mClear = (Button) findViewById(R.id.Clear);

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

        //按鈕監聽
        mOnceBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getUserName,getLoginPassword;
                int MusicNumber = 1;
                getUserName = "2";
                getLoginPassword = "4";
                sendPOST(getUserName,getLoginPassword,MusicNumber);
                //開始錄音
                recorder(context);
            }
        });
        mLoopBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                time = 0;
                //timer = null;
                mViewString.setText("");
                mCompleteTime.setText("");
                mSyncToEndTime.setText("");
            }
        });
        //**上下文*/
        context = getApplicationContext();
        //**執行線程*/
        requestRecordPermissions();
        requestWritePermissions();
    }
    //**錄音主流程*/
    private void recorder(final Context context) {

        new Thread() {
            public void run() {
                try {
                    //receiver = new Receiver("recorded.wav", fstart, Bw, sym_end, sample_rate, symbol_size, duration, context,ROOT.this);
                    receiver.record();//錄音
                    receiver.demodulate();//解調
                    recovered_string = receiver.getRecoverd_string();
                    mViewString.setText(recovered_string);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //**根據辨識引導目標*/
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //181217----偵測否是網址
                            if (recovered_string != null) {
                                if (recovered_string.equals("CbST")) {
                                    Intent intent = new Intent(ROOT.this, Animation.class);
                                    startActivity(intent);
                                    //interNet(recovered_string);//4/23//5/1//19/9/17
                                    //text.setText("成功解碼");
                                    //text.setText("解調完成:\n" + recovered_string);

                                } else if (recovered_string.equals("a")) {
                                    //text.setText("解調完成:\n" + recovered_string);
                                }
                            }
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    //文字顯示
    public void TimeView(int TTView){
        float flt_tt = (float)TTView / 10;
        String str_tt = String.valueOf(flt_tt);
        mCompleteTime.setText(str_tt);
    }
    //**重新設置計數器*/
    public void Starttime(){
        timer = new Timer();
        if (time != 0){
            time = 0;
        }
        try {
            //開始計時
            //Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    time++;
                }
            };
            timer.schedule(task,0,100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //**數據傳送*/
    public void sendPOST(String getStudenNumber,String getPassword,int MusicNumber) {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES) // read timeout
                .build();
        /**設置傳送所需夾帶的內容*/
        FormBody formBody = new FormBody.Builder()
                .add("StudenNumber", getStudenNumber)
                .add("password", getPassword)
                .add("OpenDoor", "N")
                .add("MusicNumber",String.valueOf(MusicNumber))
                .build();
        final Request request = new Request.Builder()

                .url("http://192.168.50.194/sound_networking/PHP/AudioLogin.php") //2022/10/17後IP更改
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
                    Toast.makeText(ROOT.this,reponseString,Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                else{
                    Looper.prepare();
                    Toast.makeText(ROOT.this,reponseString,Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
                Log.v("Response",reponseString);
            }
        });
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

}