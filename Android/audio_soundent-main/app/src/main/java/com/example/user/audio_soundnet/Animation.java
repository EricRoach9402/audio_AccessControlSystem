package com.example.user.audio_soundnet;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
//import android.support.annotation.RequiresApi;
//import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
//import android.support.v7.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


public class Animation extends AppCompatActivity {

    ImageView BtAnimation;
    Button checkinBt;
    AnimatedVectorDrawable avd2;
    AnimatedVectorDrawableCompat avd;
    TextView bttext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_animation);

        BtAnimation = (ImageView)findViewById(R.id.BtAnimation);
        checkinBt = (Button)findViewById(R.id.checkinBt);
        bttext = (TextView)findViewById(R.id.bttext);

        checkinBt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                Drawable drawable = BtAnimation.getDrawable();

                if(drawable instanceof AnimatedVectorDrawableCompat){
                    avd = (AnimatedVectorDrawableCompat) drawable;
                    avd.start();
                }else if(drawable instanceof AnimatedVectorDrawable){
                    bttext.setVisibility(View.INVISIBLE);
                    checkinBt.setVisibility(View.INVISIBLE);
                    avd2 = (AnimatedVectorDrawable) drawable;
                    avd2.start();

                }
            }
        });
    }
}