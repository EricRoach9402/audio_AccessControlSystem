package com.example.user.audio_soundnet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.user.audio_soundnet.faceID_Register.Login;
import com.example.user.audio_soundnet.faceID_Register.LoginFaceID;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
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

public class Register extends AppCompatActivity {

    EditText FirstName,LastName,StudenNumber,Password,Password2;
    Button Register_Button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final TextInputEditText emailEditText = findViewById(R.id.email_edit_text);
        final TextInputEditText usernameEditText = findViewById(R.id.username_edit_text);
        final TextInputEditText passwordEditText = findViewById(R.id.password_edit_text);
        Button Register_Button = findViewById(R.id.register_button);

        Register_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getFirstName,getLastName,getStudenNumber,getPassword,getPassword2;

                String userEmail = emailEditText.getText().toString();
                String userName = usernameEditText.getText().toString();
                String userPassword = passwordEditText.getText().toString();

                sendPOST(userEmail,userName,userPassword);
            }
        });
    }
    //**數據傳送*/
    private void sendPOST(String getuserEmail,String getuserName,String getuserPassword) {
        /**建立連線*/
        OkHttpClient client = new OkHttpClient().newBuilder()
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(5, TimeUnit.MINUTES) // connect timeout
                .writeTimeout(5, TimeUnit.MINUTES) // write timeout
                .readTimeout(5, TimeUnit.MINUTES) // read timeout
                .build();

        /** 建立 JSON 物件*/
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("email", getuserEmail);
            jsonObject.put("account_number", getuserName);
            jsonObject.put("account_password", getuserPassword);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        // 將 JSON 物件轉換成字串
            String json = jsonObject.toString();

        // 建立 RequestBody 物件
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(json,JSON);
        Request request = new Request.Builder()
                //.url("http://192.168.50.192/sound_networking/PHP/AudioLogin.php") //2023/02/16後IP更改
                .url("http://192.168.50.192/sound_networking/PHP/Register.php") //2023/02/19宿舍IP
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
                        Log.v("Response","註冊成功");
                        Intent intent = new Intent(Register.this,UserLogin.class);
                        startActivity(intent);
                    }
                    else {
                        Log.v("Response","註冊失敗" + json.getString("message"));
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(Register.this, "帳號或密碼錯誤", Toast.LENGTH_SHORT).show();
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