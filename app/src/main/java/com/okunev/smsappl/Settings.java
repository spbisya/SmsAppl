package com.okunev.smsappl;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by 777 on 11/29/2015.
 */
public class Settings extends AppCompatActivity {
    EditText carnum;
    TextView freq;
    SeekBar frequency;
    long millisec;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        carnum = (EditText) findViewById(R.id.carNum);
        freq = (TextView) findViewById(R.id.freqText);
        frequency = (SeekBar) findViewById(R.id.parkBar);
        int progress = frequency.getProgress();
        millisec = (long) ((double) progress / 2 * 60 * 1000 + (double) progress % 2 * 60 * 1000) - 60000;
        frequency.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                freq.setText("" + (double) progress / 2 + " minutes");
                millisec = (long) ((double) progress / 2 * 60 * 1000 + (double) progress % 2 * 60 * 1000) - 60000;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        prefs =  PreferenceManager.getDefaultSharedPreferences(this);
        carnum.setText(prefs.getString("CAR_NUMBER", ""));
        freq.setText("" + (double)prefs.getInt("PROGRESS", 1)/2+ " minutes");
        frequency.setProgress(prefs.getInt("PROGRESS", 1));
        SharedPreferences.Editor editor =  PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("CAR_NUMBER", carnum.getText().toString());
        editor.putFloat("FREQUENCY", frequency.getProgress() / 2);
        editor.putInt("PROGRESS", frequency.getProgress());
        editor.putLong("MILLISECONDS", millisec);
        editor.commit();
        editor.apply();
    }

    public void save(View v) {
        SharedPreferences.Editor editor =  PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("CAR_NUMBER", carnum.getText().toString());
        editor.putFloat("FREQUENCY", frequency.getProgress() / 2);
        editor.putInt("PROGRESS", frequency.getProgress());
        editor.putLong("MILLISECONDS", millisec);
        editor.commit();
        editor.apply();
        Intent intent = new Intent(Settings.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        carnum = (EditText) findViewById(R.id.carNum);
        freq = (TextView) findViewById(R.id.freqText);
        frequency = (SeekBar) findViewById(R.id.parkBar);
        prefs =  PreferenceManager.getDefaultSharedPreferences(this);
        carnum.setText(prefs.getString("CAR_NUMBER", ""));
        freq.setText("" + (double)prefs.getInt("PROGRESS", 19)/2 + " minutes");
        frequency.setProgress(prefs.getInt("PROGRESS", 19));
    }
}
