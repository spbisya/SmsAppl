package com.okunev.smsappl;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.telephony.SmsManager;

import java.util.Calendar;

/**
 * Created by 777 on 12/6/2015.
 */
public class SmsLogger extends Service {
    private String parkNum;
    private int timeScale;
    private int c3BarProgress;
    private String c3BarTitle;
    private String costs;
    private String funds;
    private String log;
    private long mills;
    private long millisec;
    private String carNum;
    private Double thissum;
    private Double fund;
    private int NOTIFICATION = 81237;
    private Calendar calendar = Calendar.getInstance();
    private CountDownTimer countDownTimer;
   private String SENT_SMS_FLAG = "SENT_SMS";
   private String DELIVER_SMS_FLAG = "DELIVER_SMS";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        parkNum = intent.getStringExtra("PARKING_NUMBER");
        timeScale = intent.getIntExtra("TIMESCALE", 29);
        c3BarProgress = intent.getIntExtra("CIRCULAR_BAR_PROGRESS", 100);
        c3BarTitle = intent.getStringExtra("CIRCULAR_BAR_VALUE");
        costs = intent.getStringExtra("PARKING_COSTS");
        funds = intent.getStringExtra("FUNDS");
        log = intent.getStringExtra("LOG");
        mills = intent.getLongExtra("MILLS", 570000);
        millisec = intent.getLongExtra("MILLISEC", 870000);
        carNum = intent.getStringExtra("CAR_NUMBER");
        thissum = intent.getDoubleExtra("THISSUM", 0.0);
        fund = intent.getDoubleExtra("FUND", 0.0);
        enableBroadcastReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter("broadCastName"));
        registerReceiver(sentReceiver, new IntentFilter(SENT_SMS_FLAG));
        registerReceiver(deliverReceiver, new IntentFilter(DELIVER_SMS_FLAG));
       // Toast.makeText(this, "Started", Toast.LENGTH_SHORT).show();
        startTimer(millisec);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(sentReceiver);
        unregisterReceiver(deliverReceiver);
        disableBroadcastReceiver();
        try{countDownTimer.cancel();}
        catch (Exception l){}
        super.onDestroy();
    }

    public void smsSend(String text) {
        try {
            if (!log.substring(log.length() - 19, log.length() - 1).contains(" доставлено!")) {
                Intent sentIn = new Intent(SENT_SMS_FLAG);
                final PendingIntent sentPIn = PendingIntent.getBroadcast(this, 0,
                        sentIn, 0);
                Intent deliverIn = new Intent(DELIVER_SMS_FLAG);
                final PendingIntent deliverPIn = PendingIntent.getBroadcast(this, 0,
                        deliverIn, 0);
                SmsManager smsManager = SmsManager.getDefault();
                // отправляем сообщение
                smsManager.sendTextMessage("7757", null, text, sentPIn, deliverPIn);
                log += calendar.getInstance().getTime().toString().split(" ")[3] + ": SMS " + text;
                sendData();
            }
        }
        catch (Exception l){
            Intent sentIn = new Intent(SENT_SMS_FLAG);
            final PendingIntent sentPIn = PendingIntent.getBroadcast(this, 0,
                    sentIn, 0);
            Intent deliverIn = new Intent(DELIVER_SMS_FLAG);
            final PendingIntent deliverPIn = PendingIntent.getBroadcast(this, 0,
                    deliverIn, 0);
            SmsManager smsManager = SmsManager.getDefault();
            // отправляем сообщение
            smsManager.sendTextMessage("7757", null, text, sentPIn, deliverPIn);
            log += calendar.getInstance().getTime().toString().split(" ")[3] + ": SMS " + text;
            sendData();
        }
    }

    BroadcastReceiver sentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent in) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    // sent SMS message successfully;
                    log +=" отправлено";
                    sendData();
                    break;
                default:
                    // sent SMS message failed
                    log += calendar.getInstance().getTime().toString().split(" ")[3] + " не отправлено!\n";
                    sendData();
                    stopSelf();
                    break;
            }
        }
    };

    BroadcastReceiver deliverReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent in) {
            // SMS delivered actions
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    // sent SMS message successfully;
                    log +=" и доставлено!\n";
                    sendData();
                    break;
                default:
                    // sent SMS message failed
                    log += calendar.getInstance().getTime().toString().split(" ")[3] + " и не доставлено\n";
                    sendData();
                    stopSelf();
                    break;
            }
        }
    };

    public void startTimer(long time) {
        final long percent = time / 100;
        c3BarTitle = "00:00:00";
        c3BarProgress = 100;
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
                c3BarTitle = "" + hour + ":" + minute + ":" + second;
                c3BarProgress = (int) (millisUntilFinished / percent);
                sendData();
            }

            public void onFinish() {
                c3BarTitle = "00:00:00";
                c3BarProgress = 100;
                smsSend(parkNum + "*" + carNum + "*1");
                sendData();
            }
        }.start();
    }

    public void sendData() {
        Intent intent = new Intent();
        intent.setAction("SERV-ACT");
        intent.putExtra("PARKING_NUMBER", parkNum);
        intent.putExtra("TIMESCALE", timeScale);
        intent.putExtra("CIRCULAR_BAR_PROGRESS", c3BarProgress);
        intent.putExtra("CIRCULAR_BAR_VALUE", c3BarTitle);
        intent.putExtra("PARKING_COSTS", costs);
        intent.putExtra("FUNDS", funds);
        intent.putExtra("LOG", log);
        intent.putExtra("ISWORKING", true);
        sendBroadcast(intent);
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
            if (number.equals("7757") & message.contains("авторизована")) {
                try {
                    if (!log.substring(log.length() - 15, log.length() - 1).contains("S has sent!")) {
                        log = log + calendar.getInstance().getTime().toString().split(" ")[3] + ": Парковка начата!\n";
                        smsSend("S");
                        sendData();
                    }
                }
                catch (Exception l){
                    log = log + calendar.getInstance().getTime().toString().split(" ")[3] + ": Парковка начата!\n";
                    smsSend("S");
                    sendData();
                }
            }
            if (number.equals("7757") & message.contains("Общая сумма")) {
                if(!log.substring(log.length()-15,log.length()-1).contains("has stopped!")) {
                    try {
                        thissum += Double.parseDouble(message.split("Общая сумма ")[1].split(" ")[0]);
                        fund = Double.parseDouble(message.split("счета: ")[1].split(" ")[0]);
                        String sum = thissum + "";
                        costs = sum.substring(0, sum.indexOf('.') + 3) + "p.";
                        funds = fund + "p.";
                    }
                    catch(Exception l){
                        costs = "0.00p.";
                        funds = "0.00p.";
                    }
                    log = log + calendar.getInstance().getTime().toString().split(" ")[3] + ": Парковка завершена!\n";
                    sendData();
                    startTimer(mills);
                }

            }
        }

    };


    private void enableBroadcastReceiver() {
        ComponentName receiver = new ComponentName(this, IncomingSms.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Следим за смс")
                .setContentText("Работаем...")
                .setSmallIcon(R.drawable.ic_sms_white_18dp)
                .build();
          notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        NotificationManager notifier = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notifier.notify(NOTIFICATION, notification);
    }

    public void disableBroadcastReceiver() {
        ComponentName receiver = new ComponentName(this, IncomingSms.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
     //   Toast.makeText(this, "Слежение прекращено", Toast.LENGTH_SHORT).show();
        ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION);
    }
}
