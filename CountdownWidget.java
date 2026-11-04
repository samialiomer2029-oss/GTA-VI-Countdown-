package com.gtavi.countdown;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;
import java.util.concurrent.TimeUnit;

public class CountdownWidget extends AppWidgetProvider {
    // 18 Nov 2026, 4:00 PM Yemen time (UTC+3) = 13:00 UTC.
    private static final long LAUNCH_UTC = 1795006800000L;
    private static final String ACTION_UPDATE = "com.gtavi.countdown.UPDATE";

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        updateAll(context);
        schedule(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_UPDATE.equals(intent.getAction())) {
            updateAll(context);
            schedule(context);
        }
    }

    static void updateAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, CountdownWidget.class);
        int[] ids = manager.getAppWidgetIds(component);
        long remaining = Math.max(0L, LAUNCH_UTC - System.currentTimeMillis());

        String title;
        String value;
        String sub;
        if (remaining <= 0) {
            title = "GTA VI";
            value = "IS OUT NOW!";
            sub = "WELCOME TO LEONIDA";
        } else {
            long days = TimeUnit.MILLISECONDS.toDays(remaining);
            long hours = TimeUnit.MILLISECONDS.toHours(remaining) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60;
            title = "GTA VI";
            value = String.format(java.util.Locale.US, "%02dD  %02dH  %02dM", days, hours, minutes);
            sub = "18 NOV • 4:00 PM • YEMEN";
        }

        for (int id : ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.countdown_widget);
            views.setTextViewText(R.id.widget_title, title);
            views.setTextViewText(R.id.widget_count, value);
            views.setTextViewText(R.id.widget_subtitle, sub);
            Intent open = new Intent(context, MainActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, pi);
            manager.updateAppWidget(id, views);
        }
    }

    static void schedule(Context context) {
        Intent intent = new Intent(context, CountdownWidget.class);
        intent.setAction(ACTION_UPDATE);
        PendingIntent pending = PendingIntent.getBroadcast(context, 77, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarm != null) {
            // Inexact repeating updates are battery-friendly; Android may batch them.
            alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + 60_000L,
                    60_000L, pending);
        }
    }
}
