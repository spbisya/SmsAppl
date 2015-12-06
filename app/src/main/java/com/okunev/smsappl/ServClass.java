package com.okunev.smsappl;

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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import java.util.Calendar;

public class ServClass extends Service {
    MyThread myThread;
    String messages;
    private int NOTIFICATION = 81237;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        String parkNum;
        int timeScale;
        int c3BarProgress;
        String c3BarTitle;
        String costs;
        String funds;
        String log;
        long mills;
        long millisec;
        String carNum;
        Double thissum;
        Double fund;
        Boolean setV = true;

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
        Toast.makeText(this, "Starting service...", Toast.LENGTH_SHORT).show();
        myThread = new MyThread(parkNum, timeScale, c3BarProgress, c3BarTitle,
                costs, funds, log, mills,
                millisec, carNum, thissum, fund, setV);
        myThread.start();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        myThread.cancel();
        unregisterReceiver(broadcastReceiver);
        disableBroadcastReceiver();
        super.onDestroy();
    }

    final static String MY_ACTION = "MY_ACTION";

    public class MyThread extends Thread {
        String parkNum;
        int timeScale;
        int c3BarProgress;
        String c3BarTitle;
        String costs;
        String funds;
        String log;
        CountDownTimer countDownTimer;
        long mills;
        String carNum;
        long millisec;
        Double thissum;
        Double fund;
        Calendar calendar = Calendar.getInstance();
        Boolean setV;
        String SENT_SMS_FLAG = "SENT_SMS";
        String DELIVER_SMS_FLAG = "DELIVER_SMS";


        public MyThread(String parkNum, int timeScale, int c3BarProgress, String c3BarTitle,
                        String costs, String funds, String log, long mills,
                        long millisec, String carNum, Double thissum, Double fund, Boolean setV) {
            this.parkNum = parkNum;
            this.timeScale = timeScale;
            this.c3BarProgress = c3BarProgress;
            this.c3BarTitle = c3BarTitle;
            this.costs = costs;
            this.funds = funds;
            this.log = log;
            this.carNum = carNum;
            this.mills = mills;
            this.millisec = millisec;
            this.thissum = thissum;
            this.fund = fund;
            this.setV = setV;
        }

        @Override
        public void run() {
            //  registerReceiver(sentReceiver, new IntentFilter(SENT_SMS_FLAG));
            //    registerReceiver(deliverReceiver, new IntentFilter(DELIVER_SMS_FLAG));
            timer((int) (millisec / 1000));
            sendData();
        }

        public void cancel() {
            sendData();
            //  unregisterReceiver(sentReceiver);
            //  unregisterReceiver(deliverReceiver);
            try {
                countDownTimer.cancel();
            } catch (Exception l) {

            }
        }

        public void sendData() {
            Intent intent = new Intent();
            intent.setAction(MY_ACTION);
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

        private void timer(int seconds) {
            long percent = seconds / 100 + seconds % 100;
            for (int i = seconds - 1; i >= 0; i--) {
                try {
                    sleep(1000L);
                    percent = i * 100 / seconds;
                    sendData();
                    onTickc(i * 1000, percent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            c3BarTitle = "00:00:00";
            c3BarProgress = 0;
            if (funds == null) funds = "0p.";
            if (costs == null) costs = "0p.";
            if (log == null) log = "Log\n";
            sendData();
            smsSend(parkNum + "*" + carNum + "*1");
        }

        public void onTickc(final long millisUntilFinished, long percent) {
            long hours = millisUntilFinished / 1000 / 60 / 60;
            long minutes = (millisUntilFinished - hours * 60 * 60 * 1000) / 1000 / 60;
            long seconds = (millisUntilFinished - minutes * 60 * 1000 - hours * 60 * 60 * 1000) / 1000;
            String hour = "" + hours;
            String minute = "" + minutes;
            String second = "" + seconds;
            if (hour.length() < 2) hour = "0" + hour;
            if (minute.length() < 2) minute = "0" + minute;
            if (second.length() < 2) second = "0" + second;
            c3BarTitle = hour + ":" + minute + ":" + second;
            c3BarProgress = (int) (percent);
            if (funds == null) funds = "0p.";
            if (costs == null) costs = "0p.";
            if (log == null) log = "Log\n";
            sendData();
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
            //  Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            log += calendar.getInstance().getTime().toString().split(" ")[3] + ": SMS " + text + " has sent!\n";
            sendData();
            // log.append(calendar.getInstance().getTime().toString().split(" ")[3] + ": SMS "+text+" has sent!\n");
        }




        public void onReceiveAuth() {
            log += calendar.getInstance().getTime().toString().split(" ")[3] + ": Parking is authorized!\n";
            sendData();
            smsSend("S");
        }

        public void onReceiveMoney(String message) {
            thissum += Double.parseDouble(message.split("Общая сумма ")[1].split(" ")[0]);
            fund = Double.parseDouble(message.split("счета: ")[1].split(" ")[0]);
            String sum = thissum + "";
            costs = sum.substring(0, sum.indexOf('.') + 3) + "p.";
            funds = fund + "p.";
            log += calendar.getInstance().getTime().toString().split(" ")[3] + ": Parking has stopped!\n";
            sendData();
            timer((int) (mills / 1000));
        }

        Bundle myB = new Bundle();                 //used for creating the msgs
        private Handler inHandler = new Handler() {  //handles the INcoming msgs
            @Override
            public void handleMessage(Message msg) {
                myB = msg.getData();
                String sms = myB.getString("MESSAGE");
                if(sms.contains("авторизована до")){
                    onReceiveAuth();
                    return;
                }
                else if(sms.contains("Общая сумма"))
                {
                    onReceiveMoney(sms);
                    return;
                }
            }
        };

        //--------------------------
        public Handler getHandler() {
            //a Get method which return the handler which This Thread is connected with.
            return inHandler;
        }

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
            if (number.equals("MegaFon_web")) {
                sendMsgToThread(message);
            }
        }

        void sendMsgToThread(String message) {
            Bundle myB = new Bundle();
            Message msg = myThread.getHandler().obtainMessage();
            myB.putString("MESSAGE", message);
            msg.setData(myB);
            myThread.getHandler().sendMessage(msg);
        }
    };


    private void enableBroadcastReceiver() {
        ComponentName receiver = new ComponentName(this, IncomingSms.class);
        PackageManager pm = this.getPackageManager();
        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, NOTIFICATION, intent, 0);
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle("Член")
                .setContentText("Вставить член в мамку админа?")
                .setSmallIcon(R.drawable.ic_sms_white_18dp)
                .addAction(R.drawable.ic_settings_black_18dp, "Да", pIntent)
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
        Toast.makeText(this, "Disabled logging", Toast.LENGTH_SHORT).show();
        ((NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION);
    }

}

