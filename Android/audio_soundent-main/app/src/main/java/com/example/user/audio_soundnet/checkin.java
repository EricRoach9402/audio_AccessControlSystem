package com.example.user.audio_soundnet;

import android.animation.ObjectAnimator;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
//import android.support.annotation.RequiresApi;
//import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
//import android.support.v7.app.AppCompatActivity;

import androidx.annotation.RequiresApi;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class checkin extends AppCompatActivity {

    ImageView sin_to_cos_to_sin,imageView;
    Button button2,checkin;

    AnimatedVectorDrawable avd2;
    AnimatedVectorDrawableCompat avd;

    ObjectAnimator animator ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_checkin);

       // sin_to_cos_to_sin = (ImageView)findViewById(R.id.sin_to_cos_to_sin);
       // imageView =(ImageView)findViewById(R.id.imageView);
       // button2 = (Button)findViewById(R.id.button2);
       // checkin = (Button)findViewById(R.id.checkin);


        checkin.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                Drawable drawable = imageView.getDrawable();

                if(drawable instanceof AnimatedVectorDrawableCompat){
                    avd = (AnimatedVectorDrawableCompat) drawable;
                    avd.start();
                }else if(drawable instanceof AnimatedVectorDrawable) {
                    avd2 = (AnimatedVectorDrawable) drawable;
                    avd2.start();
                }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                Drawable drawable = sin_to_cos_to_sin.getDrawable();

                if(drawable instanceof AnimatedVectorDrawableCompat){
                    avd = (AnimatedVectorDrawableCompat) drawable;
                    avd.start();
                }else if(drawable instanceof AnimatedVectorDrawable){
                    avd2 = (AnimatedVectorDrawable) drawable;
                    avd2.start();

                }
            }
        });
    }
}