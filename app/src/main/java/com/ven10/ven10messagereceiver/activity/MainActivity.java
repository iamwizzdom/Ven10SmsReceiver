package com.ven10.ven10messagereceiver.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ven10.ven10messagereceiver.R;

import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.RECEIVE_SMS;

public class MainActivity extends AppCompatActivity {

    public static final String MESSAGE_BUNDLE = "message_bundle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ScrollView scrollView = findViewById(R.id.scroll_view);
        LinearLayout dateTimeContainer = findViewById(R.id.date_time_container);
        TextView mDimension = findViewById(R.id.dimension);
        TextView mCodedMessage = findViewById(R.id.coded_message);
        TextView mDate = findViewById(R.id.date);
        TextView mTime = findViewById(R.id.time);

        Intent intent = getIntent();
        Bundle messageBundle = intent.getBundleExtra(MESSAGE_BUNDLE);

        if (!checkPermission()) {
            requestPermission();
            return;
        } else if (messageBundle == null || messageBundle.isEmpty()) {
            exit();
            return;
        }

        String dimensionW = messageBundle.getString("dimensionW"),
                dimensionL = messageBundle.getString("dimensionL"),
                colorCodeOne = messageBundle.getString("colorCodeOne"),
                colorCodeTwo = messageBundle.getString("colorCodeTwo");

        scrollView.setBackgroundColor(Color.parseColor(("#" + colorCodeTwo)));

        mDimension.setText(String.format("%s x %s", dimensionW, dimensionL));
        mDimension.setWidth(Integer.valueOf(dimensionW));
        mDimension.setHeight(Integer.valueOf(dimensionL));

        LayerDrawable layerDrawable = (LayerDrawable) mDimension.getBackground();
        GradientDrawable shape =  (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.background);
        shape.setColor(Color.parseColor(("#" + colorCodeOne)));

        mDimension.setTextColor(Color.parseColor(("#" + colorCodeTwo)));

        mCodedMessage.setText(messageBundle.getString("codedMessage"));

        mDate.setText(("Date: " + messageBundle.getString("date")));

        mTime.setText(("Time: " + messageBundle.getString("time")));
    }

    private boolean checkPermission() {
        int firstPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), RECEIVE_SMS);
        int secondPermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), READ_CONTACTS);

        return firstPermissionResult == PackageManager.PERMISSION_GRANTED &&
                secondPermissionResult == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {
                        RECEIVE_SMS,
                        READ_CONTACTS
                }, 7);
    }

    // Calling override method.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {

            case 7:

                if (grantResults.length > 0) {

                    boolean receiveSmsPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean readContactPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    Toast.makeText(MainActivity.this, (receiveSmsPermission && readContactPermission) ?
                            "Permission Granted" : "Permission Denied", Toast.LENGTH_LONG).show();

                } else Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();

                break;
            default:
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                break;
        }

        exit();
    }

    private void exit() {
        Toast.makeText(this, "No message received", Toast.LENGTH_SHORT).show();
        finish();
    }

}
