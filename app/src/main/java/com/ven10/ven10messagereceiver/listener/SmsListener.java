package com.ven10.ven10messagereceiver.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

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

                            //Returns a string array of each new line of the received sms
                            String[] strings = splitMessage(msgBody);

                            bundle = messageExtract(strings);

                            if (bundle.size() >= 7) {
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
        return message == null ? new String[0] : message.trim().split("\n");
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

        if (!lineOne.startsWith("DT:")) return bundle;

        if (!lineThree.startsWith("SZ")) return bundle;

        lineOne = lineOne.substring(3, lineOne.length());

        String datePattern = "[0-9]{1,2}(/|-)[0-9]{1,2}(/|-)[0-9]{4}",
                timePattern = "[0-9]{1,2}(:|/)[0-9]{1,2}\\s[a-zA-Z]{2}",
                dimensionPattern = "(\\d+)([a-zA-Z]*)(\\d+)",
                colorPattern = "([a-zA-Z0-9]{6})([^a-zA-Z0-9]*)([a-zA-Z0-9]{6})$";

        Pattern p = Pattern.compile(datePattern);

        Matcher m = p.matcher(lineOne);

        if (m.find())
            bundle.putString("date", m.group(0));

        p = Pattern.compile(timePattern);

        m = p.matcher(lineOne);

        if (m.find())
            bundle.putString("time", m.group(0));

        p = Pattern.compile(dimensionPattern);

        m = p.matcher(lineThree);

        if (m.find()) {
            bundle.putString("dimensionW", m.group(1));
            bundle.putString("dimensionL", m.group(3));
        }

        p = Pattern.compile(colorPattern);

        m = p.matcher(lineThree);

        if (m.find()) {
            bundle.putString("colorCodeOne", m.group(1));
            bundle.putString("colorCodeTwo", m.group(3));
        }

        bundle.putString("codedMessage", lineTwo);

        return bundle;
    }
}
