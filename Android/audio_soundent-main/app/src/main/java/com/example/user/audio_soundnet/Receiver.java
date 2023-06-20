package com.example.user.audio_soundnet;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.example.user.audio_soundnet.ROOT.ROOT;

public class Receiver {
    private static final String TAG = "Receiver";

    private String file_name;
    private double sample_rate;
    private double sample_period;
    private double symbol_size;
    private double duration;

    public ArrayList<Double> modulated;
    //private ArrayList<Double> modulated;

    private ArrayList<Byte> ByteToArrayList;
    private ArrayList<Double> recoverd_signal;
    private String recoverd_string;
    private Context context;

    //private Recorder r;
    public Recorder r;
    private GoertzelDetector goertzel_det;
    private int start_index;
    private int win_factor;
    private int shift_factor;
    private ArrayList<Integer> decode;
    private int total_len;
    private int symbol_N;
    private int shift_N;
    private int fstart;
    private double Bw,sym_end;
    private byte[] bt2;
    private ArrayList<Double> AL;
    static final String dataFile = "FSK_Goertzel.txt";
    public static boolean mPd;
    //測試ROOT用，正常使用時ROOT改為MainActivity
    private MainActivity myact;

    int cont=0;
//測試ROOT用，正常使用時ROOT改為MainActivity
    public Receiver(String file_name, int fstart, double Bw,double sym_END, double sample_rate, double symbol_size, double duration, Context context,final MainActivity act) {
        this.file_name = file_name;
        this.sample_rate = sample_rate;
        this.sample_period = 1 / sample_rate;
        this.symbol_size = symbol_size;
        this.duration = duration;
        this.context = context;
        this.start_index = 0;
        this.win_factor = 1;
        this.shift_factor = 10;
        this.fstart = fstart;
        this.Bw = Bw;
        this.sym_end=sym_END;
        this.mPd = false;

        this.myact=act;

        decode = new ArrayList<Integer>();//解調陣列

        myact.anime=false;

    }

    public void record() {
        try {
            r = new Recorder(symbol_size, Bw, sym_end,myact);//19_1_29_啟動錄音
            r.start();
            while (!r.getEND) {//判斷有沒有找到END頻率

                    if(myact.anime){

                        myact.anime=false;
                        myact.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                myact.text.setText("Loading...");
                            }
                        });
                        star_anime_OWL();
                        cont++;
                        Log.d("star_anime_OWL", "cont: " + cont);
                    }

                Thread.sleep(100);
            }

            r.stop();//錄音結束
            stopanime_OWL();

            modulated = r.abc;

            Log.d("Receiver", "This is the size of modulated(audio_read): " + modulated.size());
            concatinateRecording();
            goertzel_det = new GoertzelDetector(symbol_size, sample_rate);//goertzel計算設定
            total_len = modulated.size();//錄音檔的總長度
            symbol_N = goertzel_det.symbol_N;//symbol頻率的取樣點數
            shift_N = symbol_N / shift_factor;//移動的取樣點數(600)
            start_index = getStar(modulated, (int) Bw, win_factor, shift_factor) + symbol_N;//取得sync頻率的位置
            Log.d(TAG, "start_index = " + start_index);

            recoverd_signal = new ArrayList<Double>();//取得錄音的取樣資料

            for (int i = 0; i < modulated.size() - start_index; i++)
                recoverd_signal.add(modulated.get(start_index + i));//去掉sync訊號的位置，從之後開始存入錄音的取樣資料


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Double[] toArray(ArrayList<Double> input) {

        Double[] ret = new Double[input.size()];
        for (int i = 0; i < input.size(); i++) {
            ret[i] = input.get(i);
        }
        return ret;
    }

    private int getStar(ArrayList<Double> modulated, int f, int win_factor, int shift_factor) {
        int findLen = (int) ((total_len - symbol_N) / shift_N);
        double[] signal = new double[total_len];
        Log.d(TAG, "total_len = " + total_len);
        for (int i = 0; i < total_len; i++) {
            signal[i] = modulated.get(i);
        }
        double[] detVal = new double[findLen];
        Log.d(TAG, "findLen = " + findLen);
        detVal = goertzel_det.findCarrier_array(signal, (double) (f), win_factor, shift_factor, findLen);
        int start_count = findMax(detVal, findLen);
        int start_index = start_count * shift_N;

        //fMax=shift_factor;

        Log.d(TAG, "Sync start index = " + start_index);

        return start_index;
    }

    private int findMax(double[] arr, int len) {
        int index = 0;
        double max = 0;
        for (int i = 0; i < len; i++) {
            if (arr[i] > max) {
                max = arr[i];
                index = i;
            }
        }
        Log.d(TAG, "Max = " + max + ", max index = " + index);
        if (max < 1) index = -1;

        return index;
    }

    public void demodulate() {
        if(recoverd_signal.get(0)==-1.0){

            recoverd_string="-1";
            return;
        }

        recoverd_string=goertzel_demodulate(recoverd_signal,fstart,win_factor,shift_factor);//解調後得到字串

        Log.d(TAG,"recoverd_string: "+recoverd_string);

    }

    private String goertzel_demodulate(ArrayList<Double> recoverd_signal, int fstart, int win_factor, int shift_factor) {

        int signal_len = recoverd_signal.size();
        int findLen = signal_len / symbol_N;//找到symbol訊號的長度
        double[] signal = new double[signal_len];
        Log.d(TAG, "signal_len = " + signal_len + ", findLen = " + findLen);
        for (int i = 0; i < signal_len; i++) {
            signal[i] = recoverd_signal.get(i);
        }
        double[][] s = new double[16][findLen];
        int[] f = new int[16];
        Log.d(TAG, "findLen = " + findLen);
        for (int i = 0; i < 16; i++) {
            f[i] = fstart + 128 * i;//產生FSK調變對應表
        }
        for (int j = 0; j < 16; j++) {
            s[j] = goertzel_det.findCarrier_array(signal, (double) (f[j]), win_factor, 1, findLen);
            //Log.e("SArray", "s[" + j + "] = " + s[j]);
         //   s[j] = goertzel_det.findCarrier_array2(signal, (double) (f[j]), win_factor, 1, findLen);//19/1/27

        }//{0101,1011,0000,0010,0011}

        double[] temp = new double[16];
        int symNumber = findLen / win_factor;
        Log.d(TAG, "symNumber = " + symNumber + "= findLen / win_factor =" +findLen +"/"+win_factor);

        //尋找各FSK對照所對應的能量最大值
        int ii;
        for (int i = 0; i < symNumber; i++) {
            ii = i * win_factor;
            for (int j = 0; j < 16; j++) {
                temp[j] = s[j][ii];
            }
            int maxIndex = findMax(temp, 16);
            if (maxIndex != -1) {
                decode.add(maxIndex);
                Log.d(TAG, "decode[" + decode.size() + "]=" + maxIndex);
            }
            else
                break;
        }
        String str = "";
        for (int i = 0; i < symNumber; i = i + 2) {
            if (decode.size() - i < 2) break;
            Log.d("decode", "" + decode.get(i) + ", " + decode.get(i + 1));
            if (decode.get(i) == 0 && decode.get(i + 1) == 0) {
                break;
            } else {
                char val = (char) ((decode.get(i) << 4) + (decode.get(i + 1)));
                //Log.e("Dolphinval", "val = " + Character.toString(val));
                str += Character.toString(val);
                Log.e("Dolphin", "str = " + str);
            }
        }
        Log.d(TAG, "decode String = " + str);
        return str;
    }

    public String getRecoverd_string() {

        Log.d(TAG, "recoverd_string_boolean: " + mPd);

        return recoverd_string;
    }

    private void concatinateRecording() {
        while (modulated.size() > 48000 * 20) {
            modulated.remove(modulated.size() - 1);
        }
        //Log.d("Receiver", "This is the size of concatinated waveform: " + modulated.size());
    }

    private void star_anime_OWL(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                //測試註解，正常使用時刪除註解
                myact.mowLoading.sendMessage(msg);
            }
        }).start();
    }

    private void stopanime_OWL(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 0;
                //測試用，正常使用時刪除註解
                myact.mowLoading.sendMessage(msg);
            }
        }).start();
    }
}
