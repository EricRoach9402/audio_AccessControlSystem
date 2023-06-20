package com.example.user.audio_soundnet;

import android.util.Log;

import java.util.ArrayList;

public class GoertzelDetector {

    private static final String TAG = "Goertzel";
    private double symbol_size;
    private double sample_rate;
    private double sample_period;
    public int symbol_N;
    private ArrayList<Double> recovered_signal;

    public GoertzelDetector(double symbol_size, double sample_rate) {
        this.symbol_size = symbol_size;
        this.sample_rate = sample_rate;
        this.sample_period = 1.0 / sample_rate;
        this.symbol_N = (int) (symbol_size * sample_rate);//6000; symbol頻率時間長度 * 取樣率
        /*
        取樣率指的是每秒鐘聲音取樣的次數
        因此 symbol_N 此算法可得出 symbol頻率時間長度中 可獲得多少取樣
        */
    }

    public double findCarrier_value(double[] x, double fin, int xlen) {//GoertzelDetector基本運算
        int k = (int) (0.5 + xlen * fin / sample_rate);//k = 100
        //Log.v("Goertzel","k:" + k + "xlen:" + xlen + "fin:" +fin );
        double w = 2 * Math.PI * k / xlen; //w = 2.6179938779914944
        //Log.v("Goertzel","w:" + w);
        double cosine = Math.cos(w); //cosine = -0.8660254037844387
        //Log.v("Goertzel","cosine:" + cosine);
        double coe = 2 * cosine; //coe = -1.7320508075688774
        //Log.v("Goertzel","coe:" + coe);
        double Q0 = 0;
        double Q1 = 0;
        double Q2 = 0;
        //Log.v("Goertzel","xlen:" + xlen);
        for (int i = 0; i < xlen; i++) {
            //Log.v("Goertzel","x[i]:" + x[i]);
            Q0 = coe * Q1 - Q2 + x[i];
            Q2 = Q1;
            Q1 = Q0;
        }
        //Log.v("Goertzel","sqrt:" + (Q1 * Q1 + Q2 * Q2 - Q1 * Q2 * coe));
        double y = Math.sqrt(Q1 * Q1 + Q2 * Q2 - Q1 * Q2 * coe);
        //Log.v("Goertzel","Goertzel y:" + y);
        return y;
    }

    public double[] findCarrier_array(double[] signal, double fin, int win_factor, int shift_factor, int len) {//音訊,目標頻率,音框長度,移動點數,長度 ;GoertzelDetector的個別音框陣列計算
        double[] res = new double[len];//資料總長度
        int win_N = symbol_N / win_factor;//計算音框的點數;計算sync時為240,計算字串時為6000 ;
        int shift_N = symbol_N / shift_factor;//移動音框的點數;計算sync時為 240,計算字串時為 600;
        double[] x = new double[win_N];
        //Log.v("Goertzel","symbol_N:" + symbol_N );
        for (int i = 0; i < len; i++) {
            //Log.v("Goertzel", "shift_N:" + shift_N+", i * shift_N:" + i * shift_N);
            if(signal.length-i*shift_N>=win_N){
                //Log.v("Goertzel", "i:" + i);
                //arraycopy:來源,起始索引,目的地,起始索引,複製長度
                System.arraycopy(signal, i * shift_N, x, 0, win_N);
                //Log.v("Goertzel", "i:" + i);
                /*for (int t = 0; t < x.length; t++) {
                    Log.v("Goertzel", "x:" + x[t]);
                }*/
                //音頻,目標頻率,音框大小
                res[i] = findCarrier_value(x, fin, win_N);
            }
        }
        return res;
    }
}

/*音框大小計算
第一步：求 symbol_N (時間內的樣本數)
    symbol_size 聲音持續時間,目前為0.125秒
    sample_rate 取樣率,目前為48000
    symbol_N = (int) (symbol_size * sample_rate)
    symbol_N 此算法可得出 symbol_size時間長度中 可獲得多少樣本數

第二步：計算 win_N (音框大小)
findCarrier_array 中第三個參數 win_factor 為音框率(時間內產生的音框數量)
備註:音框率越大計算量越大,硬體設備須能跟上
int win_N = symbol_N / win_factor
上述計算為 : 時間內的樣本數 / 音框數量
經過上述計算可得出需音框大小

第三步：計算 shift_N (移動點數)
findCarrier_array 中第四個參數 shift_factor 為移動點數
int shift_N = symbol_N / shift_factor
上述計算為：時間內樣本數 / 移動點數
經上述計算可以得出欲移動的點數

第四步：根據音框及移動點數，將音訊切割放入x陣列中
if(signal.length-i*shift_N>=win_N){
    System.arraycopy(signal, i * shift_N, x, 0, win_N);
    //arraycopy:來源,起始索引,目的地,起始索引,複製長度
}
解析：
    如果 訊號的長度 - i * 移動移動點數 >= 音框大小
    將每個音框內的數據放入 x 陣列中進入下一階段的計算
*/