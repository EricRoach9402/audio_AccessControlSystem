 package com.example.user.audio_soundnet;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.os.Environment;
import android.util.Log;

import com.example.user.audio_soundnet.ROOT.ROOT;
import com.example.user.audio_soundnet.ROOT.TimerTool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;



 public class Recorder {
    private AudioRecord recorder;
    private GoertzelDetector goertzelDetector;
    private Receiver receiver;
    private TimerTool timerTool;
    private int lenght,old_lenght=0;
    private boolean timeout = false;

    static final String TAG = "Recorder";
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = "recorded.wav";
    private static final String AUDIO_RECORDER_FOLDER = "My_Audio_FSK";
    private static final String AUDIO_RECORDER_TEMP_FILE = "recorded.raw";
    private static final int RECORDER_SAMPLERATE = 48000; //取樣率
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;//單聲道
    private static final int RECORDER_CHANNELS_INT = 1; //
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    static final String dataFile = "goertzelData.txt";

    //private int bufferSize = 200000;
    short[] buffer;//-215 ~ 215
    private Thread recordingThread = null;
    private boolean isRecording = false;
    public boolean recStart,getEND;
    private double symbol_size;
    private double Bw,sym_end;

    private double maxIndex;
    public ArrayList<Double> abc;
     //測試ROOT用，正常使用ROOT改為 MainActivity
    private MainActivity may;
    private boolean stop_anime;
    public static byte [] ArrSteaming = new byte[300000];
    public static byte [] zeroArray = new byte[4044],mixArray = new byte[400000];
//測試ROOT用，正常使用ROOT改為MainActivity
    public Recorder(double symbol_size, double Bw, double sym_END, final MainActivity act) {
        this.symbol_size = symbol_size;
        this.Bw = Bw;//20048
        this.sym_end = sym_END;//20400
        this.may = act;
        recStart = false;
        getEND = false;
        stop_anime=true;
        int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
       // int buffercount = 4800 / bufferSize;

        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, RECORDER_SAMPLERATE, RECORDER_CHANNELS
                , RECORDER_AUDIO_ENCODING, RECORDER_SAMPLERATE);

        if (AutomaticGainControl.isAvailable()) {
            AutomaticGainControl agc = AutomaticGainControl.create(recorder.getAudioSessionId());
            agc.setEnabled(false);
        } else {
            System.out.println("AGC NOT AVAILIABLE");
        }
    }

    public void start() throws IllegalStateException, IOException {
        buffer = new short[4800];
        recorder.startRecording();

        isRecording = true;

        recordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioData();
            }
        });
        recordingThread.start();
    }

    public void stop() {
        System.out.println("Told to stop");
        stopRecording();
    }

    private void stopRecording() {
        if (null != recorder) {
            isRecording = false;
            Log.d("recorder_stop","stop");
            if (recorder.getState() == AudioRecord.STATE_INITIALIZED){
                recorder.stop();
                recorder.release();
            }
        }
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
        //Log.d(TAG, "goertzel data for head = "+max);

        return index;
    }

    private void writeAudioData() {

        int read = 0;
        boolean log1 = true;
        // int dataLen = 0;
        //double symbol_size = 0.125;
        int shift_factor = 25;                          //移動點數
        int win_factor = 25;                            //新理解：此為音框率(時間內產生的音框數量),經計算後音框大小為240(symbol_N/25)
        int symbol_N = (int) (symbol_size * RECORDER_SAMPLERATE);
        goertzelDetector = new GoertzelDetector(symbol_size, RECORDER_SAMPLERATE);
        int s_len = (buffer.length * 2 - symbol_N / win_factor) / (symbol_N / shift_factor) + 1;

        double[] s = new double[s_len];

        long recLen = 0;
        boolean stopAdd = false;

        //int cont=0;//計算if(stop_anime)進去次數
        abc = new ArrayList<Double>();
        may.StartTimer(true);
        while (isRecording) {
            //may.Starttime();
            recorder.read(buffer, 0, buffer.length);
            /*for (int t = 0;t < buffer.length; t ++) {
                //Log.d("Goertzel", "I16Array = " + buffer[t]);
            }*/
            if (AudioRecord.ERROR_INVALID_OPERATION != read) {

                double tmpData[] = new double[buffer.length];
                for (int i = 0; i < buffer.length; i++) {
                    tmpData[i] = (double) buffer[i] / 32768.0;//將數值歸一化
                    //Log.d("Goertzel","tmpData: " + tmpData[i]);
                }
                Double max = tmpData[0];
                for (int i = 0;i < tmpData.length;i++){
                    if (max < tmpData[i]){
                        max = tmpData[i];
                    }
                }
                s = goertzelDetector.findCarrier_array(tmpData, Bw, win_factor, shift_factor, s_len);
                /*for (int i = 0;i < s.length;i++){
                }*/
                int mInd = findMax(s, s_len);

                if(recStart) {

                    double[] s_end=new double[s_len];

                    //想像成使用sym_end的值除以所有已經過處理的訊號
                    s_end=goertzelDetector.findCarrier_array(tmpData, sym_end, win_factor, shift_factor, s_len);
                    //找到所有訊號裡最大值的位置
                    int mInd_end = findMax(s_end, s_len);
                    //如果最大值位置的訊號有 >1 則進入if
                    if (s_end[mInd_end] > may.threshold || timeout) {
                        may.StartTimer(false);
                        //may.Stoptime();
                        Log.d(TAG, "find Max_END, recLen = " + recLen);
                        Log.d(TAG, "find Max_END, s_end[mInd_end] = " + s_end[mInd_end]);
                        System.arraycopy(zeroArray,0,mixArray,0,zeroArray.length);
                        System.arraycopy(ArrSteaming,0,mixArray,4045, ArrSteaming.length);
                        getEND = true;
                    }
                }
                if (s[mInd] > may.threshold) {//19_1/27,19_3_5 取得Sync
                    //may.StartTimer(true);
                    //may.Starttime();
                    may.Record();//紀錄Sync時間

                    Log.d(TAG, "find Max, recLen = " + recLen);
                    maxIndex = s[mInd]; //振幅
                    stopAdd = true;
                    recStart = true;
                    if (stop_anime){
                        stop_anime=false;
                        may.anime=true;
                    }
                    Log.d(TAG, "find Max_sync, s[mInd] = " + s[mInd]);
                }
                if (!stopAdd)
                    recLen += buffer.length;
                if (recStart) {

                    byte[] bytes2 = new byte[buffer.length * 2];
                    ByteBuffer.wrap(bytes2).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(buffer);

                    for (int x = 0;x < buffer.length;x++){

                        abc.add((double)buffer[x]);
                    }
                    //設定TimeOut 一字元約需96000
                    old_lenght += 9600;
                    if (old_lenght >= 192000)timeout = true;
                }
            }
        }
    }
}
