package com.yehudaadler.jewishclock;

import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Yehuda on 17-Jul-15.
 */
public class AnglesComputer {

    private float mDayHours;
    private float mNightHours;
    private float mDayAngle;
    private float mNightAngle;
    private float mOneDayHourAngle;
    private float mOneNightHourAngle;
    private boolean mIsNight;
    private Calendar mLastSunChange;
    private Calendar mNextSunChange;
    private boolean mIsLastTickIntervalValid;
    private Location mLocation;

    public AnglesComputer(Location location) {
        mLocation = location;
        recomputeHours();
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public static Angles getInitAngles() {
        return new Angles(0, 0, 0, 180, 180);
    }

    public long getTickIntervalInMicroSeconds() {
        mIsLastTickIntervalValid = true;
        return (long)((mIsNight ? mNightHours : mDayHours) * 1000000 / 12);
    }

    public Angles getAngles() {
        if (Calendar.getInstance().compareTo(mNextSunChange) > 0) {
            recomputeHours();
        }

        float hoursSinceSunChange = hoursDiff(mLastSunChange, Calendar.getInstance());

        float hours = 12 * hoursSinceSunChange / (mIsNight ? mNightHours : mDayHours);
        float minutes = 60*(hours - (int)hours);
        float seconds = 60*(minutes - (int)minutes);
        int secondsInt = (int)seconds;

        float hoursAngle = ((mIsNight ? 0 : mNightAngle) + hours * (mIsNight ? mOneNightHourAngle : mOneDayHourAngle));
        float minutesAngle = (minutes * 360/60);
        float secondsAngle = (secondsInt * 360/60);

        return new Angles(
                hoursAngle,
                minutesAngle,
                secondsAngle,
                mDayAngle,
                mNightAngle
                );
    }

    public boolean getIsLastTickIntervalValid() {
        return mIsLastTickIntervalValid;
    }

    private void recomputeHours() {

        mIsLastTickIntervalValid = false;

        TimeZone timeZone = TimeZone.getDefault();
        String tzString = timeZone.getID();

        SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(mLocation, tzString);

        Calendar now = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DATE, +1);

        Calendar todaySunset = calculator.getOfficialSunsetCalendarForDate(now);
        Calendar todaySunrise = calculator.getOfficialSunriseCalendarForDate(now);
        Calendar yesterdaySunset = calculator.getOfficialSunsetCalendarForDate(yesterday);
        Calendar tomorrowSunset = calculator.getOfficialSunsetCalendarForDate(tomorrow);
        Calendar tomorrowSunrise = calculator.getOfficialSunriseCalendarForDate(tomorrow);

        // case 1: we are before today's sunrise
        if (now.compareTo(todaySunrise) <= 0) {
            mIsNight = true;
            mLastSunChange = yesterdaySunset;
            mNextSunChange = todaySunrise;
            mNightHours = hoursDiff(yesterdaySunset, todaySunrise);
            mDayHours = hoursDiff(todaySunrise, todaySunset);

        // case 2: we are between today's sunrise and sunset
        } else if (now.compareTo(todaySunset) <= 0) {
            mIsNight = false;
            mLastSunChange = todaySunrise;
            mNextSunChange = todaySunset;
            mDayHours = hoursDiff(todaySunrise, todaySunset);
            mNightHours = hoursDiff(todaySunset, tomorrowSunrise);

        // case 3: we are after today's sunset
        } else {
            mIsNight = true;
            mLastSunChange = todaySunset;
            mNextSunChange = tomorrowSunrise;
            mNightHours = hoursDiff(todaySunset, tomorrowSunrise);
            mDayHours = hoursDiff(tomorrowSunrise, tomorrowSunset);
        }

        mDayAngle = 360 * mDayHours / (mDayHours + mNightHours);
        mNightAngle = 360 * mNightHours / (mDayHours + mNightHours);
        mOneDayHourAngle = mDayAngle / 12;
        mOneNightHourAngle = mNightAngle / 12;

    }

    private float hoursDiff(Calendar from, Calendar to) {
        return (to.getTimeInMillis() - from.getTimeInMillis()) /
                (1000*60*60f)
                ;
    }

    public static class Angles {
        private float mHoursDialAngle;
        private float mMinutesDialAngle;
        private float mSecondsDialAngle;
        private float mDayAngle;
        private float mNightAngle;

        public Angles(float mHoursDialAngle, float mMinutesDialAngle, float mSecondsDialAngle, float mDayAngle, float mNightAngle) {
            this.mHoursDialAngle = mHoursDialAngle;
            this.mMinutesDialAngle = mMinutesDialAngle;
            this.mSecondsDialAngle = mSecondsDialAngle;
            this.mDayAngle = mDayAngle;
            this.mNightAngle = mNightAngle;
        }

        public float getHoursDialAngle() {
            return mHoursDialAngle;
        }

        public float getMinutesDialAngle() {
            return mMinutesDialAngle;
        }

        public float getSecondsDialAngle() {
            return mSecondsDialAngle;
        }

        public float getDayAngle() {
            return mDayAngle;
        }

        public float getNightAngle() {
            return mNightAngle;
        }

        public void setmHoursDialAngle(float mHoursDialAngle) {
            this.mHoursDialAngle = mHoursDialAngle;
        }

        public void setmMinutesDialAngle(float mMinutesDialAngle) {
            this.mMinutesDialAngle = mMinutesDialAngle;
        }

        public void setmSecondsDialAngle(float mSecondsDialAngle) {
            this.mSecondsDialAngle = mSecondsDialAngle;
        }

        public void setmDayAngle(float mDayAngle) {
            this.mDayAngle = mDayAngle;
        }

        public void setmNightAngle(float mNightAngle) {
            this.mNightAngle = mNightAngle;
        }
    }
}


