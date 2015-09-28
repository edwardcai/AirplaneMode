package edward.patrick.kagie.airplanemode;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;

import static edward.patrick.kagie.airplanemode.R.xml.preferences;


public class AboutActivity extends ActionBarActivity {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    private float lastX;
    private float[] queueX;
    private float lastY;
    private float[] queueY;
    private float lastZ;
    private float[] queueZ;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);


    }
}
