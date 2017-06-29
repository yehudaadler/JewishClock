package com.yehudaadler.jewishclock;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

/**
 * Created by yehudaadler on 6/27/17.
 */

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        Intent intent = new Intent(context, UpdateWidgetIntentService.class);
        intent.setAction(UpdateWidgetIntentService.ACTION_SETUP_ALARM);
        context.startService(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // to be on the safe side: restart the alarm + trigger first update
        Intent intent = new Intent(context, UpdateWidgetIntentService.class);
        intent.setAction(UpdateWidgetIntentService.ACTION_SETUP_ALARM);
        context.startService(intent);
    }
}