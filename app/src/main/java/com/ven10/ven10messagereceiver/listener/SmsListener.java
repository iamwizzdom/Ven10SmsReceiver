package com.ven10.ven10messagereceiver.listener;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ven10.ven10messagereceiver.activity.MainActivity;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        if (Objects.equals(intent.getAction(), "android.provider.Telephony.SMS_RECEIVED")) {

            //---get the SMS message passed in---
            Bundle bundle = intent.getExtras();
            SmsMessage[] smsMessages;
            String msgFrom = null, msgBody = null;

            if (bundle != null) {
                //---retrieve the SMS message received---
                try {

                    Object[] pdus = (Object[]) bundle.get("pdus");
                    assert pdus != null;
                    smsMessages = new SmsMessage[pdus.length];
                    for (int i = 0; i < smsMessages.length; i++) {
                        smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        msgFrom = smsMessages[i].getDisplayOriginatingAddress();
                        msgBody = smsMessages[i].getDisplayMessageBody();
                    }

                    assert msgFrom != null;

                    Cursor cursor = context.getApplicationContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, null, null, null);

                    assert cursor != null;

                    while (cursor.moveToNext()) {

                        String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)),
                                name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));

                        phone = phone.replaceAll("[^0-9]", "");
                        msgFrom = msgFrom.replaceAll("[^0-9]", "");

                        if (Objects.equals(phone, msgFrom) && name.equalsIgnoreCase("Ven10")) {

                            System.out.println(msgBody);

                            bundle = messageExtract(splitMessage(msgBody));

                            if (bundle.size() > 0) {
                                intent = new Intent(context, MainActivity.class);
                                intent.putExtra(MainActivity.MESSAGE_BUNDLE, bundle);
                                context.getApplicationContext().startActivity(intent);
                            }

                            cursor.close();
                            break;
                        }
                    }

                } catch (Exception e) {
                    Log.d("Exception caught", e.getMessage());
                }
            }
        }
    }

    private String[] splitMessage(String message) {

        if (message == null) return new String[0];

        // Create a Pattern object
        Pattern p = Pattern.compile("^*(.*)");

        // Now create matcher object.
        Matcher m = p.matcher(message);

        int size = message.split("\n").length, i = 0;

        String[] strings = new String[size];
        while (m.find()) {
            String txt = m.group(0);
            if (txt.isEmpty()) continue;
            strings[i++] = txt;
        }

        return strings;
    }

    private Bundle messageExtract(String[] message) {
        Bundle bundle = new Bundle();

        if (message.length < 3) return bundle;

        String lineOne, lineTwo, lineThree;

        if (message[0] == null || message[1] == null || message[2] == null)
            return bundle;

        lineOne = message[0];
        lineTwo = message[1];
        lineThree = message[2];

        if (!lineOne.substring(0, 3).equals("DT:")) return bundle;

        if (!lineThree.substring(0, 2).equals("SZ")) return bundle;

        lineOne = lineOne.substring(3, lineOne.length());

        String[] lineOneArray = lineOne.split(":", 2);

        bundle.putString("date", lineOneArray[0].trim());
        bundle.putString("time", lineOneArray[1].trim());
        bundle.putString("codedMessage", lineTwo);

        lineThree = lineThree.substring(3, lineThree.length());

        String[] lineThreeArray = lineThree.split("/", 2);

        String dimension = lineThreeArray[0].trim(), colorCode = lineThreeArray[1].trim();

        bundle.putString("dimensionW", dimension.substring(0, dimension.indexOf("w")).trim());
        bundle.putString("dimensionL", dimension.substring(dimension.indexOf("w") + 1, dimension.length() - 1).trim());

        String[] colorCodeArray = colorCode.split("-");

        bundle.putString("colorCodeOne", colorCodeArray[0].trim());
        bundle.putString("colorCodeTwo", colorCodeArray[1].trim());

        return bundle;
    }
}