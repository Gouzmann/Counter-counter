package com.george.counter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.widget.RemoteViews;

public class CounterWidget extends AppWidgetProvider {

    static final String PLUS = "com.george.counter.PLUS";
    static final String MINUS = "com.george.counter.MINUS";

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("counters", Context.MODE_PRIVATE);
    }

    @Override
    public void onUpdate(Context c, AppWidgetManager m, int[] ids) {
        for (int id : ids) update(c, m, id);
    }

    @Override
    public void onDeleted(Context c, int[] ids) {
        SharedPreferences.Editor e = prefs(c).edit();
        for (int id : ids) {
            e.remove("n" + id).remove("c" + id).remove("g" + id).remove("k" + id);
        }
        e.apply();
    }

    @Override
    public void onReceive(Context c, Intent i) {
        super.onReceive(c, i);
        String a = i.getAction();
        if (PLUS.equals(a) || MINUS.equals(a)) {
            int id = i.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (id < 0) return;
            SharedPreferences p = prefs(c);
            int n = p.getInt("c" + id, 0);
            n = PLUS.equals(a) ? n + 1 : Math.max(0, n - 1);
            p.edit().putInt("c" + id, n).apply();
            update(c, AppWidgetManager.getInstance(c), id);
        }
    }

    static PendingIntent action(Context c, String action, int id) {
        Intent i = new Intent(c, CounterWidget.class).setAction(action);
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        i.setData(Uri.parse("w://" + id + "/" + action));
        return PendingIntent.getBroadcast(c, 0, i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    static void update(Context c, AppWidgetManager m, int id) {
        SharedPreferences p = prefs(c);
        String name = p.getString("n" + id, "Counter");
        int count = p.getInt("c" + id, 0);
        int goal = Math.max(1, p.getInt("g" + id, 10));
        int color = p.getInt("k" + id, Color.parseColor("#3B82F6"));

        RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget);
        rv.setTextViewText(R.id.tv_name, name);
        rv.setTextColor(R.id.tv_name, color);
        rv.setTextViewText(R.id.tv_count, String.valueOf(count));
        rv.setTextColor(R.id.tv_count, count >= goal ? color : Color.WHITE);
        rv.setTextViewText(R.id.tv_goal, "of " + goal);
        rv.setImageViewBitmap(R.id.iv_bar, bar(count / (float) goal, color));

        rv.setOnClickPendingIntent(R.id.btn_plus, action(c, PLUS, id));
        rv.setOnClickPendingIntent(R.id.btn_minus, action(c, MINUS, id));

        Intent cfg = new Intent(c, ConfigActivity.class);
        cfg.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        cfg.setData(Uri.parse("w://" + id + "/cfg"));
        rv.setOnClickPendingIntent(R.id.tv_name, PendingIntent.getActivity(c, 0, cfg,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));

        m.updateAppWidget(id, rv);
    }

    static Bitmap bar(float frac, int color) {
        int w = 400, h = 12;
        Bitmap b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(b);
        Paint pt = new Paint(Paint.ANTI_ALIAS_FLAG);
        pt.setColor(Color.parseColor("#3A3A3C"));
        cv.drawRoundRect(new RectF(0, 0, w, h), h / 2f, h / 2f, pt);
        float f = Math.max(0f, Math.min(1f, frac));
        if (f > 0) {
            pt.setColor(color);
            cv.drawRoundRect(new RectF(0, 0, Math.max(h, w * f), h), h / 2f, h / 2f, pt);
        }
        return b;
    }

    static void refresh(Context c, int id) {
        update(c, AppWidgetManager.getInstance(c), id);
    }
}
