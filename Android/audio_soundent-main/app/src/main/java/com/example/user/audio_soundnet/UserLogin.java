package com.example.user.audio_soundnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class UserLogin extends AppCompatActivity {

    private Spinner spinner;
    TextView registerTextView;
    EditText UserName,LoginPassword;
    Button UserLoginButton;
    //String getRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_login);
        spinner = findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.selection_items, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int defaultSelectionIndex = 3; // 設定默認選項的索引，例如選項2
        spinner.setSelection(defaultSelectionIndex);

        UserName = (EditText)findViewById(R.id.username_edittext);
        LoginPassword = (EditText)findViewById(R.id.password_edittext);
        UserLoginButton = (Button)findViewById(R.id.login);
        registerTextView = findViewById(R.id.to_register);

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 在這裡添加跳轉到另一個頁面的程式碼
                Intent intent = new Intent(UserLogin.this, Register.class);
                startActivity(intent);
            }
        });

        UserLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getUserName,getLoginPassword;
                getUserName = UserName.getText().toString();
                getLoginPassword = LoginPassword.getText().toString();
                String selectedItem = spinner.getSelectedItem().toString();
                try {
                    sendPOST(getUserName,getLoginPassword,selectedItem);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                //選擇隨機音樂
//                long  t = System.currentTimeMillis();
//                int MusicNumber = RandomMusic(t);
//                try {
//                    sendPOST(getUserName,getLoginPassword,MusicNumber);
//                } catch (JSONException e) {
//                    throw new RuntimeException(e);
//                }
            }
        });
    }
    private int RandomMusic(long t){
        Random r = new Random(t);
        int musicnumber = r.nextInt(25);
        Log.v("Implement","RandomMusic : " + musicnumber);
        return musicnumber;
    }
    //**數據傳送*/
    public void sendPOST(final String getStudenNumber, String getPassword, final String getdoor) throws JSONException {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES) // read timeout
                .build();
        /**設置傳送所需夾帶的內容*/
        // 建立 JSON 物件
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("StudenNumber", getStudenNumber);
        jsonObject.put("password", getPassword);
        jsonObject.put("door", getdoor);
        //jsonObject.put("MusicNumber", MusicNumber);
        // 將 JSON 物件轉換成字串
        String json = jsonObject.toString();
        // 建立 RequestBody 物件
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json,JSON);
        final Request request = new Request.Builder()
                //.url("http://192.168.50.195/sound_networking/PHP/AudioLogin.php") //2023/04/22後IP更改
                .url("http://192.168.50.192/sound_networking/PHP/AudioLogin.php") //2023/02/19宿舍IP
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
                        Intent intent = new Intent(UserLogin.this,MainActivity.class);
                        intent.putExtra("StudenNumber",getStudenNumber);
                        intent.putExtra("door",getdoor);
                        startActivity(intent);
                    }
                    else {
                        Log.v("Response","登入失敗" + json.getString("message"));
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(UserLogin.this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}