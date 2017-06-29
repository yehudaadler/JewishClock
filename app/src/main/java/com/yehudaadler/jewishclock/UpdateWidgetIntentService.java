package com.yehudaadler.jewishclock;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.util.List;

/**
 * Created by yehudaadler on 6/27/17.
 */

public class UpdateWidgetIntentService extends IntentService {

    private static final long REFRESH_INTERVAL = 60*1000;

    public static final String ACTION_SETUP_ALARM = "com.yehudaadler.jewishclock.actions.SETUP_ALARM";
    public static final String ACTION_UPDATE_WIDGET = "com.yehudaadler.jewishclock.actions.UPDATE_WIDGET";

    public UpdateWidgetIntentService() {
        super("UpdateWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        switch (intent.getAction()) {

            case ACTION_SETUP_ALARM:
                setupAlarm();
                updateWidget();
                break;

            case ACTION_UPDATE_WIDGET:
                updateWidget();
                break;
        }
    }

    private void setupAlarm() {
        Intent alarmIntent = new Intent(this, UpdateWidgetIntentService.class);
        alarmIntent.setAction(ACTION_UPDATE_WIDGET);
        PendingIntent pending = PendingIntent.getService(this, 0, alarmIntent, 0);
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pending);
        alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), REFRESH_INTERVAL, pending);
    }

    private void updateWidget() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.widget_layout);

        // set up on click intent
        Intent intent = new Intent(this, ClockActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.imageView, pendingIntent);

        // create clock view in code
        ClockView clockView = new ClockView(this);
        clockView.setAngles(AnglesComputer.getInitAngles());
        clockView.hideSeconds();

        Location location = getLocationSynchronously();

        if (location != null) {

            AnglesComputer computer = new AnglesComputer(
                    new com.luckycatlabs.sunrisesunset.dto.Location(
                            location.getLatitude(), location.getLongitude())
            );

            clockView.setAngles(computer.getAngles());
        }

        // we can't use a custom view in a widget so this is the hack
        clockView.measure(500, 500);
        clockView.layout(0, 0, 500, 500);
        clockView.setDrawingCacheEnabled(true);
        Bitmap bitmap = clockView.getDrawingCache();
        remoteViews.setImageViewBitmap(R.id.imageView, bitmap);

        // update widget
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.updateAppWidget(new ComponentName(this.getPackageName(), WidgetProvider.class.getName()), remoteViews);
    }

    private Location getLocationSynchronously() {

        LocationManager locationManager;

        if (null == (locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE))) {
            return null;
        }

        List<String> matchingProviders = locationManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = locationManager.getLastKnownLocation(provider);
            if (null != location) {
                return location;
            }
        }

        return null;
    }
}
