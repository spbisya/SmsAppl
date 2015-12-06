package com.okunev.smsappl;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private CircularProgressBar c3;
    private SeekBar seekBar;
    TextView seekBarText;
    long millisec;//15
    TextView log, tv, tv2, tv3, tv4, parkCosts, funds;
    CountDownTimer countDownTimer;
    Button but, bstop;
    EditText parkNumber;
    Chronometer stopwatch;
    Chronometer chronometer;
    long mills;//10
    private int NOTIFICATION = 81237;
    String carNum;
    Calendar calendar = Calendar.getInstance();
    Double thissum = 0.0;
    Double fund = 0.0;
    MyReceiver myReceiver;
    Intent intent;
    Boolean isWorking = false;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        but = (Button) findViewById(R.id.start);
        bstop = (Button) findViewById(R.id.stop);
        parkNumber = (EditText) findViewById(R.id.parkingNumber);
        c3 = (CircularProgressBar) findViewById(R.id.circularprogressbar1);
        log = (TextView) findViewById(R.id.log);
        tv = (TextView) findViewById(R.id.textView);
        tv2 = (TextView) findViewById(R.id.textView2);
        tv3 = (TextView) findViewById(R.id.textView3);
        tv4 = (TextView) findViewById(R.id.textView4);
        parkCosts = (TextView) findViewById(R.id.parkcost);
        funds = (TextView) findViewById(R.id.funds);
        seekBarText = (TextView) findViewById(R.id.time_value);
        seekBar = (SeekBar) findViewById(R.id.timeScale);
        stopwatch = (Chronometer) findViewById(R.id.stopWatch);
        intent = new Intent(this,
                ServClass.class);
        // setVis(View.VISIBLE, View.INVISIBLE);
        // c3.setTitle("00:00:00");
        // c3.setProgress(100);

        log.setMovementMethod(new ScrollingMovementMethod());
         millisec = (long) ((double) seekBar.getProgress() / 2 * 60 * 1000 + (double) seekBar.getProgress() % 2 * 60 * 1000) - 60000;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seekBarText.setText("" + (double) progress / 2 + " minutes");
                millisec = (long) ((double) progress / 2 * 60 * 1000 + (double) progress % 2 * 60 * 1000) - 60000;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //    disableBroadcastReceiver();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
mills = prefs.getLong("MILLISECONDS",570000);
        getBool(isWorking);
    }

    public void start(View v) {
        if (!parkNumber.getText().toString().equals("")) {
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            setVis(View.INVISIBLE, View.VISIBLE);
            c3.setTitle("00:00:00");
            c3.setProgress(100);
            c3.requestLayout();
            //   startTimer(millisec, c3);
            chronometer = (Chronometer) findViewById(R.id.stopWatch);
            chronometer.setBase(SystemClock.elapsedRealtime());
            chronometer.start();
            //   registerReceiver(broadcastReceiver, new IntentFilter("broadCastName"));
            //  enableBroadcastReceiver();
            myReceiver = new MyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ServClass.MY_ACTION);
            registerReceiver(myReceiver, intentFilter);

            //Start our own service

            intent.putExtra("PARKING_NUMBER", parkNumber.getText().toString());
            intent.putExtra("TIMESCALE", seekBar.getProgress());
            intent.putExtra("CIRCULAR_BAR_PROGRESS", c3.getProgress());
            intent.putExtra("CIRCULAR_BAR_VALUE", c3.getTitle());
            intent.putExtra("PARKING_COSTS", parkCosts.getText().toString());
            intent.putExtra("FUNDS", funds.getText().toString());
            intent.putExtra("LOG", log.getText().toString());
            intent.putExtra("MILLS", mills);
            intent.putExtra("MILLISEC", millisec);
            intent.putExtra("CAR_NUMBER", carNum);
            intent.putExtra("THISSUM", thissum);
            intent.putExtra("FUND", fund);
            startService(intent);

        } else Toast.makeText(this, "Не заполнено одно из полей", Toast.LENGTH_SHORT).show();
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            parkNumber.setText(arg1.getStringExtra("PARKING_NUMBER"));
            seekBar.setProgress(arg1.getIntExtra("TIMESCALE", 29));
            c3.setProgress(arg1.getIntExtra("CIRCULAR_BAR_PROGRESS", 100));
            c3.setTitle(arg1.getStringExtra("CIRCULAR_BAR_VALUE"));
            parkCosts.setText(arg1.getStringExtra("PARKING_COSTS"));
            funds.setText(arg1.getStringExtra("FUNDS"));
            log.setText(arg1.getStringExtra("LOG"));
            mills = arg1.getLongExtra("MILLS", 570000);
            millisec = arg1.getLongExtra("MILLISEC", 870000);
            carNum = arg1.getStringExtra("CAR_NUMBER");
            thissum = arg1.getDoubleExtra("THISSUM", 0.0);
            fund = arg1.getDoubleExtra("FUND", 0.0);
            isWorking = arg1.getBooleanExtra("ISWORKING", false);

        }
    }

    public void stop(View v) {

        stopService(intent);

        try {
            //   countDownTimer.cancel();
            chronometer.stop();
        } catch (Exception l) {
            log.append("Chronometer isn't working!\n");
        }

        Toast.makeText(this, "Вам необходимо покинуть парковку в течение " + c3.getTitle(), Toast.LENGTH_LONG).show();
        // disableBroadcastReceiver();
        c3.setProgress(100);
        c3.setTitle("00:00:00");
        c3.requestLayout();
        setVis(View.VISIBLE, View.INVISIBLE);
    }

    public void setVis(int l, int h) {
        parkNumber.setVisibility(l);
        but.setVisibility(l);
        seekBar.setVisibility(l);
        seekBarText.setVisibility(l);

        log.setVisibility(h);
        c3.setVisibility(h);
        tv.setVisibility(h);
        tv2.setVisibility(h);
        tv3.setVisibility(h);
        tv4.setVisibility(h);
        parkCosts.setVisibility(h);
        funds.setVisibility(h);
        stopwatch.setVisibility(h);
        bstop.setVisibility(h);
    }

    public void getBool(Boolean val) {
        if (!val) setVis(View.VISIBLE, View.INVISIBLE);
        else setVis(View.INVISIBLE, View.VISIBLE);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            String number = b.getString("NUMBER");
            String message = b.getString("MESSAGE");
            //Спасибо! Парковка автомобиля А555МР97 авторизована до
            // 15.11.15 01:35. Чтобы остановить парковку отправьте "S" ли "C" на номер 7757.
            //Спасибо. Ваша парковка завершена 15.11.15 02:00. Общая сумма 55.46 руб.
            // Баланс лицевого счета: 35.33 руб.
            if (number.equals("MegaFon_web") & message.contains("авторизована до")) {
                log.append(calendar.getInstance().getTime().toString().split(" ")[3] + ": Parking is authorized!\n");
                smsSend("S");
            }
            if (number.equals("MegaFon_web") & message.contains("Общая сумма")) {
                thissum += Double.parseDouble(message.split("Общая сумма ")[1].split(" ")[0]);
                fund = Double.parseDouble(message.split("счета: ")[1].split(" ")[0]);
                String sum = thissum + "";
                parkCosts.setText(sum.substring(0, sum.indexOf('.') + 3) + "p.");
                funds.setText(fund + "p.");
                log.append(calendar.getInstance().getTime().toString().split(" ")[3] + ": Parking has stopped!\n");
                startTimer(mills, c3);
            }
        }
    };


    public void disableBroadcastReceiver() {
        ComponentName receiver = new ComponentName(this, IncomingSms.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        Toast.makeText(this, "Disabled logging", Toast.LENGTH_SHORT).show();
        ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION);
    }

    public void startTimer(long time, final CircularProgressBar c3) {
        final long percent = time / 100;
        c3.setTitle("00:00:00");
        c3.setProgress(100);
        c3.requestLayout();
        countDownTimer = new CountDownTimer(time, 1000) {
            public void onTick(final long millisUntilFinished) {
                long hours = millisUntilFinished / 1000 / 60 / 60;
                long minutes = (millisUntilFinished - hours * 60 * 60 * 1000) / 1000 / 60;
                long seconds = (millisUntilFinished - minutes * 60 * 1000 - hours * 60 * 60 * 1000) / 1000;
                String hour = "" + hours;
                String minute = "" + minutes;
                String second = "" + seconds;
                if (hour.length() < 2) hour = "0" + hour;
                if (minute.length() < 2) minute = "0" + minute;
                if (second.length() < 2) second = "0" + second;
                c3.setTitle("" + hour + ":"
                        + minute + ":"
                        + second);
                c3.setProgress((int) (millisUntilFinished / percent));
                c3.requestLayout();
            }

            public void onFinish() {
                c3.setTitle("00:00:00");
                c3.setProgress(100);
                c3.requestLayout();
                smsSend(parkNumber.getText().toString() + "*" + carNum + "*1");
            }
        }.start();
    }

    public void smsSend(String text) {//Direct sending
      /*  Intent sentIn = new Intent(SENT_SMS_FLAG);
        final PendingIntent sentPIn = PendingIntent.getBroadcast(this, 0,
                sentIn, 0);
        Intent deliverIn = new Intent(SENT_SMS_FLAG);
        final PendingIntent deliverPIn = PendingIntent.getBroadcast(this, 0,
                deliverIn, 0);
        SmsManager smsManager = SmsManager.getDefault();
        // отправляем сообщение
        smsManager.sendTextMessage("7757", null, text, sentPIn, deliverPIn);*/
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        log.append(calendar.getInstance().getTime().toString().split(" ")[3] + ": SMS " + text + " has sent!\n");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, Settings.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
    /*    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        parkNumber.setText(prefs.getString("PARKING_NUMBER", ""));
        c3.setTitle(prefs.getString("CIRCULAR_BAR_VALUE", ""));
        parkCosts.setText(prefs.getString("PARKING_COSTS", ""));
        funds.setText(prefs.getString("FUNDS", ""));
        log.setText(prefs.getString("LOG", ""));
        carNum = prefs.getString("CAR_NUMBER", "");
        c3.requestLayout();
        int progress = prefs.getInt("CIRCULAR_BAR_PROGRESS", 19);
        seekBar.setProgress(prefs.getInt("TIMESCALE", 29));
        seekBarText.setText("" + (double) seekBar.getProgress() / 2 + " minutes");
        mills = (long) ((double) progress / 2 * 60 * 1000 + (double) progress % 2 * 60 * 1000) - 60000;
        millisec = prefs.getLong("MILLISEC", 870000);
        fund = (double) prefs.getFloat("FUND", 0.0F);
        thissum = (double) prefs.getFloat("THISSUM", 0.0F);
        getBool(isWorking);*/
        //  registerReceiver(sentReceiver, new IntentFilter(SENT_SMS_FLAG));
        //   registerReceiver(deliverReceiver, new IntentFilter(DELIVER_SMS_FLAG));
        //   registerReceiver(broadcastReceiver, new IntentFilter("broadCastName"));
    }


    protected void onPause() {
        super.onPause();
      /*  SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("PARKING_NUMBER", parkNumber.getText().toString());
        editor.putInt("TIMESCALE", seekBar.getProgress());
        editor.putInt("CIRCULAR_BAR_PROGRESS", c3.getProgress());
        editor.putString("CIRCULAR_BAR_VALUE", c3.getTitle());
        editor.putString("PARKING_COSTS", parkCosts.getText().toString());
        editor.putString("FUNDS", funds.getText().toString());
        editor.putString("LOG", log.getText().toString());
        editor.putLong("MILLS", mills);
        editor.putLong("MILLISEC", millisec);
        editor.putString("CAR_NUMBER", carNum);
        editor.putFloat("THISSUM", thissum.floatValue());
        editor.putFloat("FUND", fund.floatValue());
        editor.commit();*/
    }

    protected void onDestroy() {
        super.onDestroy();
        disableBroadcastReceiver();
      //  prefs.edit().clear().commit();

        //     unregisterReceiver(sentReceiver);
        //    unregisterReceiver(deliverReceiver);
       //     unregisterReceiver(broadcastReceiver);
    }


}
