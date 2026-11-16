package com.george.counter;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        TextView t = new TextView(this);
        t.setText("Long-press home screen → Widgets → Goal Counter.\nTap a widget's name to edit it.");
        t.setTextColor(Color.WHITE);
        t.setTextSize(16);
        t.setGravity(Gravity.CENTER);
        t.setPadding(48, 48, 48, 48);
        t.setBackgroundColor(Color.parseColor("#111111"));
        setContentView(t);
    }
}
