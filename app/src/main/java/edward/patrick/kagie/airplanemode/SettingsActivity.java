package edward.patrick.kagie.airplanemode;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import static edward.patrick.kagie.airplanemode.R.xml.preferences;


public class SettingsActivity extends PreferenceActivity {

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
        addPreferencesFromResource(preferences);


    }
}
