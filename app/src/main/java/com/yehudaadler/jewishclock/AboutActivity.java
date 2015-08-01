package com.yehudaadler.jewishclock;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Yehuda on 26-Jul-15.
 */
public class AboutActivity extends Activity {

    private TextView mAboutTextView;
    private ImageView mClockImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_about_layout);

        mAboutTextView = (TextView) findViewById(R.id.about_text);

        mAboutTextView.setText(
                    "Jewish Clock\n\n"
                    + "Jewish Clock is a 24 hour clock which attempts to visualize the concepts of the clock that is used in jewish law: Shaot Zmaniyot (or Zmanim).\n\n"
                    + "The basic idea is that certain daily deadlines were defined as portions of the day. For example, according to jewish law, the latest time one can pray the morning prayer, Shacharit, is when a third of the day passed, or in other words, 4 out of the 12 hours of the day have passed.\n\n"
                    + "The modern way of using charts which translate the laws defined by Shaot Zmaniyot into our familiar clock, are not needed anymore when using this clock. Shacharit deadline is alway at 4. Shabbat, or any new day for that matter, always start at 12.\n\n"
                    + "In particular, the clock divides the day, starting at sunrise, and ending at sunset, into 12 equal parts, which are each called one day hour. The night is divided in the same manner.\n\n"
                    + "An important observation is that day lengths keep on changing, as do the lengths of the nights. In the summer the days are longer, and they leave much less for the night, and in the winter vice versa.\n\n"
                    + "A non trivial way of showing the fact that we have more day time during the summer, is showing the time moving slower during summer days! the clock ticks the same number of times as usual: 60 seconds * 60 minutes * 12 hours a day. But those seconds are longer! So the clock ticks slower.\n\n"
                    + "Location. Sunrise and sunset are strongly tied to ones location. That's why Jewish Clock needs to know the device's location before showing the time. In a device with no location capabilities, Jewish Clock can not function.\n\n"
                    + "The inspiration for Jewish Clock came from Jack Kustanowitz, from MountainPass Technology, and his iPhone app/website: ZmanimClock.com .\n"
                    + "I owe Jack many thanks for originating this idea, and for the elaborate explanations that appear on ZmanimClock.com and for the ones the were given in person.\n\n"
                    + "One more thank you goes to Github user mikereedell, who open sources a java based sunrise/sunset library, which is used by Jewish Clock. You can find his code at https://github.com/mikereedell/sunrisesunsetlib-java.\n\n"
                    + "Enjoy,\n\n"
                    + "Yehuda Adler\n"
                    + "yehuda.adler@gmail.com"
        );

        mClockImageView = (ImageView) findViewById(R.id.about_image);
        mClockImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
