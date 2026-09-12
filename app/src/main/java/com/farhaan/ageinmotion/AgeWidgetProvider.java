package com.farhaan.ageinmotion;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Locale;

public class AgeWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_TICK = "com.farhaan.ageinmotion.ACTION_TICK";
    private static final long MINUTE_MS = 60_000L;
    private static final DecimalFormat AGE_FORMAT = new DecimalFormat("0.000000000");
    private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.00");

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id);
        scheduleTick(context);
    }

    @Override
    public void onEnabled(Context context) {
        scheduleTick(context);
    }

    @Override
    public void onDisabled(Context context) {
        cancelTick(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_TICK.equals(intent.getAction())) {
            refreshAll(context);
            scheduleTick(context);
        }
    }

    static void update(Context c, AppWidgetManager m, int id) {
        long dob = ConfigStore.dob(c, id);
        boolean progressMode = ConfigStore.progress(c, id);
        long now = System.currentTimeMillis();

        double ageYears = Math.max(0.0, (now - dob) / 31_557_600_000.0);
        double dayProgress = dayProgress(now);
        String age = String.format(Locale.US, "%.9f", ageYears);
        String pct = String.format(Locale.US, "%.2f%%", dayProgress * 100.0);

        RemoteViews v = new RemoteViews(c.getPackageName(), R.layout.widget_age_in_motion);
        v.setTextViewText(R.id.age, age);
        v.setTextViewText(R.id.unit, "YEARS");
        v.setViewVisibility(R.id.progress_area, progressMode ? android.view.View.VISIBLE : android.view.View.GONE);
        v.setViewVisibility(R.id.percent, progressMode ? android.view.View.VISIBLE : android.view.View.GONE);
        v.setProgressBar(R.id.progress, 10_000, (int) Math.round(dayProgress * 10_000), false);
        v.setTextViewText(R.id.percent, pct);
        v.setTextViewText(R.id.detail, progressMode ? "TODAY IN MOTION" : "TIME IS MOVING");

        // Detailed values are kept available in the layout for larger widget sizes.
        if (progressMode) {
            v.setTextViewText(R.id.day_detail, dayBreakdown(now));
            v.setTextViewText(R.id.remaining, remainingToday(now));
        }

        m.updateAppWidget(id, v);
    }

    private static double dayProgress(long millis) {
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(millis);
        Calendar start = (Calendar) now.clone();
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 1);
        double fraction = (millis - start.getTimeInMillis()) / (double) (end.getTimeInMillis() - start.getTimeInMillis());
        return Math.max(0.0, Math.min(1.0, fraction));
    }

    private static String dayBreakdown(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return String.format(Locale.US, "%02d HOURS   %02d MINUTES   %02d SECONDS",
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
    }

    private static String remainingToday(long millis) {
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(millis);
        Calendar end = (Calendar) now.clone();
        end.add(Calendar.DAY_OF_MONTH, 1);
        end.set(Calendar.HOUR_OF_DAY, 0);
        end.set(Calendar.MINUTE, 0);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        long remaining = Math.max(0, end.getTimeInMillis() - millis);
        long seconds = remaining / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format(Locale.US, "%02d:%02d:%02d REMAINING", hours, minutes, secs);
    }

    private static void scheduleTick(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = tickIntent(context);
        long first = SystemClock.elapsedRealtime() + MINUTE_MS;
        alarm.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, first, MINUTE_MS, pi);
    }

    private static void cancelTick(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(tickIntent(context));
    }

    private static PendingIntent tickIntent(Context context) {
        Intent intent = new Intent(context, AgeWidgetProvider.class).setAction(ACTION_TICK);
        return PendingIntent.getBroadcast(context, 9381, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void refreshAll(Context c) {
        AppWidgetManager m = AppWidgetManager.getInstance(c);
        ComponentName n = new ComponentName(c, AgeWidgetProvider.class);
        for (int id : m.getAppWidgetIds(n)) update(c, m, id);
    }

    @Override
    public void onDeleted(Context c, int[] ids) {
        for (int id : ids) ConfigStore.remove(c, id);
        if (AppWidgetManager.getInstance(c).getAppWidgetIds(new ComponentName(c, AgeWidgetProvider.class)).length == 0) {
            cancelTick(c);
        }
    }
}
