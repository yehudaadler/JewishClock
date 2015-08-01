package com.yehudaadler.jewishclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import io.fabric.sdk.android.Fabric;


public class ClockActivity extends Activity {

    private static final String TAG = "ClockActivity";

    private static final String KEY_LOCATION = "key_location";
    private static final String SAVED_IS_FIRST_LOAD = "saved_is_first_load";

    private static final long MEASURE_TIME = 1000 * 20;
    private static final long POLLING_FREQ = 1000 * 10;
    private static final float MIN_DISTANCE = 10.0f;

    private ClockView mClockView;
    private AnglesComputer mComputer;
    private ScheduledFuture<?> mMoverFuture;
    private ScheduledFuture<?> mAnimationFuture;
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;
    private AnglesComputer.Angles mAnimationAngles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());

        if (setAndGetIsFirstLoad()) {
           startActivity(new Intent(this, AboutActivity.class));
        }

        if(savedInstanceState != null) {
            mLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        setContentView(R.layout.activity_clock_layout);

        mClockView = (ClockView) findViewById(R.id.zclock);
        mClockView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startActivity(new Intent(ClockActivity.this, AboutActivity.class));
                return true;
            }
        });

        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {

                mLocation = location;

                if (null != mLocationManager) {
                    mLocationManager.removeUpdates(mLocationListener);
                }

                runAfterKnownLocation();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };
    }

    private boolean setAndGetIsFirstLoad() {
        SharedPreferences sharedPrefs;

        sharedPrefs = getApplicationContext().getSharedPreferences(TAG, Context.MODE_PRIVATE);
        boolean isFirstLoad = sharedPrefs.getBoolean(SAVED_IS_FIRST_LOAD, true);

        if (isFirstLoad) {
            sharedPrefs.edit()
                    .putBoolean(SAVED_IS_FIRST_LOAD, false)
                    .commit()
            ;
        }

        return isFirstLoad;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mLocation == null) {
            startAnimation();
            refreshLocation();
        } else {
            runAfterKnownLocation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != mLocationManager) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mLocation != null) {
            outState.putParcelable(KEY_LOCATION, mLocation);
        }
    }

    private void runAfterKnownLocation() {
        mComputer = new AnglesComputer(
              new com.luckycatlabs.sunrisesunset.dto.Location(
                        mLocation.getLatitude(), mLocation.getLongitude())
        );

        startScheduler();
    }

    private void stopAnimation() {
        if (mAnimationFuture != null) {
            mAnimationFuture.cancel(false);
        }
    }

    private void startAnimation() {
        mAnimationAngles = new AnglesComputer.Angles(0, 120, 240, 180, 180);

        ScheduledExecutorService executor = Executors
                .newScheduledThreadPool(1);

        mAnimationFuture = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mClockView.post(new Runnable() {
                    @Override
                    public void run() {
                        mAnimationAngles.setmHoursDialAngle(mAnimationAngles.getHoursDialAngle() + 10);
                        mAnimationAngles.setmMinutesDialAngle(mAnimationAngles.getMinutesDialAngle() - 3);
                        mAnimationAngles.setmSecondsDialAngle(mAnimationAngles.getSecondsDialAngle() - 7);
                        mClockView.setAngles(mAnimationAngles);
                    }
                });
            }
        }, 0, 20000, TimeUnit.MICROSECONDS);
    }

    private void startScheduler() {

        ScheduledExecutorService executor = Executors
                .newScheduledThreadPool(1);

        mMoverFuture = executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (mComputer.getIsLastTickIntervalValid()) {
                    mClockView.post(new Runnable() {
                        @Override
                        public void run() {
                            stopAnimation();
                            mClockView.setAngles(mComputer.getAngles());
                        }
                    });
                } else {
                    mMoverFuture.cancel(true);
                    startScheduler(); //with new interval
                }
            }
        }, 0, mComputer.getTickIntervalInMicroSeconds(), TimeUnit.MICROSECONDS);
    }

    private void refreshLocation() {

        mLocation = null;

//        if no location service: error
       if (null == (mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE))) {
           failedLocation();
           return;
        }

//        else, try to get the last known location
        List<String> matchingProviders = mLocationManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = mLocationManager.getLastKnownLocation(provider);
            if (null != location) {
                mLocation = location;
                break;
            }
        }

        if (null != mLocation) {
            runAfterKnownLocation();
            return;
        }

        // else try to get current location from Network + GPS
        // Register for network location updates
        if (null != mLocationManager
                .getProvider(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, POLLING_FREQ,
                    MIN_DISTANCE, mLocationListener);
        }

        // Register for GPS location updates
        if (null != mLocationManager
                .getProvider(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, POLLING_FREQ,
                    MIN_DISTANCE, mLocationListener);
        }

        // Timer to warn failing to find location. Listeners aren't killed here. They will be in onDestory.
        Executors.newScheduledThreadPool(1).schedule(new Runnable() {

            @Override
            public void run() {
                if (null == mLocation) {
                    failedLocation();
                }
            }
        }, MEASURE_TIME, TimeUnit.MILLISECONDS);
    }

    private void failedLocation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ClockActivity.this, "This clock can't work without your location", Toast.LENGTH_LONG).show();
            }
        });
    }
}
