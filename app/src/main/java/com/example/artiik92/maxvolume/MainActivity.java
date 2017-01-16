package com.example.artiik92.maxvolume;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    final String TAG = "myLogs";
    TextView tv;
    Button start;
    AudioRecord ar;
    ProgressBar pb;
    boolean isReading = false;
    MyThread nr = new MyThread();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();

    }
    public void initUI(){
        tv = (TextView) this.findViewById(R.id.tv);
        start = (Button) this.findViewById(R.id.start);
        pb = (ProgressBar) this.findViewById(R.id.progressBar);
    }


    public void MyThread(View view){
       start.setClickable(false);
        start.setText(R.string.SkanStart);
        nr.start();
    }


    public void stopR(View view) {
        if (nr != null) nr.stopRecording();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        isReading = false;
        if (ar != null) {
            ar.release();
        }
    }


    class MyThread extends Thread {

        private boolean stop;

        @Override
        public void run() {
            short[] audioBuffer;
            int MHz = 16000;
            int ms  = 1;
            int minSize = AudioRecord.getMinBufferSize(MHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            ar = new AudioRecord(MediaRecorder.AudioSource.MIC, MHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minSize);
            audioBuffer = new short[minSize];
            ar.startRecording();
            stop = false;
            int delay = 0;
            while (!stop) {
                final int readSize = ar.read(audioBuffer, 0, minSize);
                delay += readSize;
                double amplitude = 0;
                double sum=0;
                for (int i = 0; i < readSize; i++) {
                    sum += audioBuffer[i] * audioBuffer[i];
                }
                amplitude = sum / readSize / 10000.;

                double ampl = amplitude;
                if (readSize > 0) {
                    pb.setProgress((int) ampl);
                }

                if(delay > MHz*ms/10) {
                    delay = 0;
                    final int finalAmplitude = (int) amplitude;
                    double pressure1 = finalAmplitude/654.;
                    double REFERENCE1 = 0.0003;


                   final double db = (20 * Math.log10(pressure1/REFERENCE1));

                    try {
                        sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if(minSize != 8000){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(getString(R.string.ZvAmpl) + finalAmplitude + '\n' + getString(R.string.UrHOOma)+ db);
                            Log.i(TAG,"Amplitude: "+finalAmplitude);
                            Log.e(TAG,"Db"+ db);

                        }

                    });}
                    else {ar.stop();}

                }
            }
            ar.stop();


        }

        void stopRecording() {stop = true;}
    }

}




