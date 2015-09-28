package edward.patrick.kagie.airplanemode;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private float lastX;
    private float[] queueX;
    private float lastY;
    private float[] queueY;
    private float lastZ;
    private float[] queueZ;
    private float[] fall;
    private int qIndex;

    private AudioManager audioManager;
    private SoundPool soundPool;
    private float actVolume;
    private float maxVolume;
    private float volume;
    private int soundID;
    private boolean loaded;
    private float rate;
    private boolean isAirplaneMode;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button soundButton = (Button)  findViewById(R.id.button2);
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordAudio(Environment.getExternalStorageDirectory().getAbsolutePath() + "/audiorecordtest.mp4");
            }
        });
        rate = 1;
        isAirplaneMode = false;
        fall = new float[5];
        queueX = new float[5];
        queueY = new float[5];
        queueZ = new float[5];
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        actVolume = (float) audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = (float) audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actVolume / maxVolume;

        //Hardware buttons setting to adjust the media sound
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        // the counter will help us recognize the stream id of the sound played  now
        // Load the sounds
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 44100);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            //boolean loaded;
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                String noiseID = prefs.getString("key_airplane_noises", "1");
                soundID = soundPool.load(MainActivity.this, getResources().getIdentifier("f" + noiseID, "raw", getPackageName()), 1);
            }
        };

        sharedPref.registerOnSharedPreferenceChangeListener(listener);

        String noiseID = sharedPref.getString("key_airplane_noises", "1");
        soundID = soundPool.load(MainActivity.this, getResources().getIdentifier("f" + noiseID, "raw", getPackageName()), 1);


        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        Button settings = (Button) findViewById(R.id.button);
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(
                        MainActivity.this,
                        SettingsActivity.class);
                startActivity(i);
            }
        });;

        Button about = (Button) findViewById(R.id.button3);
        about.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i=new Intent(
                        MainActivity.this,
                        AboutActivity.class);
                startActivity(i);
            }
        });;

        Switch onOffSwitch = (Switch)  findViewById(R.id.switch1);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    soundPool.play(soundID, volume, volume, 1, 0x7FFFFFFF, rate);
                    isAirplaneMode = true;

                }
                else {
                    soundPool.autoPause();
                    isAirplaneMode = false;
                }
            }

        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isAirplaneMode) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float deltX = Math.abs(lastX - x);
            float deltY = Math.abs(lastY - y);
            float deltZ = (lastZ - z);


            queueX[qIndex % 5] = (deltX);
            queueY[qIndex % 5] = (deltY);
            queueZ[qIndex % 5] = (deltZ);

            fall[qIndex % 5] = z;

            float avgFall = queueAverage(fall);
            float avgX = queueAverage(queueX);
            float avgY = queueAverage(queueY);
            float avgZ = queueAverage(queueZ);

            lastX = x;
            lastY = y;
            lastZ = z;
            //(float) (Math.log(1 + avgX + avgY + avgZ)/5 + 1)
            if (avgZ > 0.01 && rate < 2.0) {
                rate = rate + .008f + z/1000;
            } else if (avgZ < -0.01 && rate > 0.6){
                rate = rate - .008f;
            } else {
                if (rate > 1.05) { rate -= .05f;}
                else if (rate <= 1.05 && rate >= .95) rate = 1;
                else {rate += .05f;}
            }

            if (Math.abs(avgFall) < .05) {

            }

            float scaleUp = (avgX + avgY)/1000;
            soundPool.setRate(soundID, rate + scaleUp);
            qIndex++;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void recordAudio(String fileName) {
        final MediaRecorder recorder = new MediaRecorder();
        ContentValues values = new ContentValues(3);
        values.put(MediaStore.MediaColumns.TITLE, fileName);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(fileName);
        try {
            recorder.prepare();
        } catch (Exception e){
            e.printStackTrace();
        }
        final ProgressDialog mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setTitle("Recording Sound");
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setButton("Stop recording", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mProgressDialog.dismiss();
                recorder.stop();
                recorder.release();
                soundID = soundPool.load(Environment.getExternalStorageDirectory().getAbsolutePath()+"/audiorecordtest.mp4",1);
            }
        });
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener(){
            public void onCancel(DialogInterface p1) {
                recorder.stop();
                recorder.release();
            }
        });
        recorder.start();
        mProgressDialog.show();
    }

    private float queueAverage(float[] arr) {
        float sum = 0;
        for (int i = 0; i < arr.length; i++) {
          sum += (float) arr[i];
        }

        return sum/arr.length;

    }
}
