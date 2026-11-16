package com.george.counter;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ConfigActivity extends Activity {

    static final String[] COLORS = {
            "#3B82F6", "#22C55E", "#F59E0B", "#EF4444", "#A855F7", "#EC4899", "#14B8A6", "#FFFFFF"
    };

    int id;
    int color = Color.parseColor(COLORS[0]);
    final View[] swatches = new View[COLORS.length];

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        id = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        Intent cancel = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        setResult(RESULT_CANCELED, cancel);
        if (id < 0) { finish(); return; }

        SharedPreferences p = CounterWidget.prefs(this);
        color = p.getInt("k" + id, color);

        int pad = dp(20);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad * 2, pad, pad);
        root.setBackgroundColor(Color.parseColor("#111111"));

        EditText name = field("Name", p.getString("n" + id, ""), InputType.TYPE_CLASS_TEXT);
        EditText goal = field("Goal", String.valueOf(p.getInt("g" + id, 10)), InputType.TYPE_CLASS_NUMBER);
        root.addView(label("Name"));
        root.addView(name);
        root.addView(label("Goal"));
        root.addView(goal);
        root.addView(label("Color"));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < COLORS.length; i++) {
            final int col = Color.parseColor(COLORS[i]);
            View v = new View(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(36), 1f);
            lp.setMargins(dp(3), dp(6), dp(3), dp(6));
            v.setLayoutParams(lp);
            v.setOnClickListener(x -> { color = col; mark(); });
            swatches[i] = v;
            row.addView(v);
        }
        root.addView(row);
        mark();

        Button save = new Button(this);
        save.setText("Save");
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        sp.topMargin = dp(24);
        save.setLayoutParams(sp);
        save.setOnClickListener(x -> {
            String n = name.getText().toString().trim();
            int g = 10;
            try { g = Integer.parseInt(goal.getText().toString().trim()); } catch (Exception e) {}
            if (g < 1) g = 1;
            p.edit()
                    .putString("n" + id, n.isEmpty() ? "Counter" : n)
                    .putInt("g" + id, g)
                    .putInt("k" + id, color)
                    .apply();
            CounterWidget.refresh(this, id);
            setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id));
            finish();
        });
        root.addView(save);
        setContentView(root);
    }

    void mark() {
        for (int i = 0; i < COLORS.length; i++) {
            int c = Color.parseColor(COLORS[i]);
            GradientDrawable d = new GradientDrawable();
            d.setShape(GradientDrawable.OVAL);
            d.setColor(c);
            if (c == color) d.setStroke(dp(3), Color.GRAY);
            swatches[i].setBackground(d);
        }
    }

    EditText field(String hint, String value, int type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setText(value);
        e.setInputType(type);
        e.setSingleLine(true);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.GRAY);
        return e;
    }

    TextView label(String s) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextColor(Color.GRAY);
        t.setTextSize(13);
        t.setGravity(Gravity.START);
        t.setPadding(0, dp(14), 0, 0);
        return t;
    }

    int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
