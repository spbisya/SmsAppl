package com.okunev.smsappl;

/**
 * Created by 777 on 11/27/2015.
 */

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;


public class IncomingSms extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                final Object[] pdusObj = (Object[]) bundle.get("pdus");
                String message="";//getDisplayMessageBody();
                String senderNum="";
                for (int i = 0; i < pdusObj.length; i++) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();
                    senderNum = phoneNumber;
                    message += currentMessage.getMessageBody();


                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);
                }
                String ns = Context.NOTIFICATION_SERVICE;
                NotificationManager nMgr = (NotificationManager)context.getSystemService(ns);
                nMgr.cancelAll();
              //  Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                Intent i1 = new Intent("broadCastName");
                // Data you need to pass to activity
                i1.putExtra("NUMBER", senderNum);
                i1.putExtra("MESSAGE", message);
                context.sendBroadcast(i1);
            }
        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver " + e);
        }
    }


}
