package gui;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import net.AnonymousServerSession;
import net.ServerSession;

/**
 * Created by 11ryt on 7/24/2017.
 */

public class WeatherManager{

    private int zipCode;
    private WeatherState current;
    private String description;
    private Double tempCelsius;
    private Double tempFahrenheit;
    private long sunriseMillis, sunsetMillis;
    private static final Double HEAVY_THRESHOLD = .00635;
    private long lastUpdate;


    public WeatherManager(int zipCode) {
        this.setZipCode(zipCode);
        current = null;
        description = null;
        tempCelsius = 0.0;
        sunriseMillis = 0;
        sunsetMillis = 0;
        lastUpdate = 0;
    }

    public void update() {
        try {
            AnonymousServerSession session = new AnonymousServerSession();
            session.setEnableProgressBar(false);
            boolean open = session.open();
            String[] result = session.callAndResponse("weather", Integer.toString(zipCode));
            session.close();
            if (ServerSession.isError(result)) {
                return;
            }
            setLastUpdate(Long.parseLong(result[1]));
            setTempCelsius(Double.parseDouble(result[2]));
            setTempFahrenheit(getTempCelsius() * 1.8 + 32);
            setCurrent(WeatherState.valueOf(result[3]));
            setDescription(result[4].replaceAll("_", " "));
            // today in current locale
            java.util.Calendar date = new GregorianCalendar();
            // reset hour, minutes, seconds and millis
            date.set(java.util.Calendar.HOUR_OF_DAY, 0);
            date.set(java.util.Calendar.MINUTE, 0);
            date.set(java.util.Calendar.SECOND, 0);
            date.set(java.util.Calendar.MILLISECOND, 0);
            long todayUTC = System.currentTimeMillis() - (System.currentTimeMillis() % (1000 * 86400));
            long todayLocale = date.getTimeInMillis();
            long sunriseMillisUTC = todayUTC + Long.parseLong(result[5]);
            long sunsetMillisUTC = todayUTC + Long.parseLong(result[6]);

            //must divide as floating point to avoid truncation of fractional difference
            int sunriseDayDiff = (int) Math.floor((sunriseMillisUTC - todayLocale) / (1000.0 * 86400.0));
            int sunsetDayDiff = (int) Math.floor((sunsetMillisUTC - todayLocale) / (1000.0 * 86400.0));

            //add or subtract a day if the UTC offset + UTC midnight calculated the time for the wrong day in the current locale.
            sunriseMillis = sunriseMillisUTC - (sunriseDayDiff * 86400 * 1000);
            sunsetMillis = sunsetMillisUTC - (sunsetDayDiff * 86400 * 1000);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void update(int zipCode){
        this.setZipCode(zipCode);
        update();
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public WeatherState getCurrent(){
        return current;
    }

    public int getZipCode() {
        return zipCode;
    }

    public void setZipCode(int zipCode) {
        this.zipCode = zipCode;
    }

    public void setCurrent(WeatherState current) {
        this.current = current;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTempCelsius() {
        return tempCelsius;
    }

    public void setTempCelsius(Double tempCelsius) {
        this.tempCelsius = tempCelsius;
    }

    public Double getTempFahrenheit() {
        return tempFahrenheit;
    }

    public void setTempFahrenheit(Double tempFahrenheit) {
        this.tempFahrenheit = tempFahrenheit;
    }

    private static final double CYCLE_LENGTH_MILLIS = 2551442801.5584;
    private static final long zero_point = 1530161580000L; // 6/28/18 at 4:53 AM UTC - Full Moon

    Moon getMoonState() {
        double segment_length = CYCLE_LENGTH_MILLIS / 8.0;
        long diff = System.currentTimeMillis() - zero_point;
        double offset = (diff) % CYCLE_LENGTH_MILLIS;
        int phaseNo = (int) Math.round(offset / segment_length);
        switch (phaseNo) {
            case 0: return Moon.Full_Moon;
            case 1: return Moon.Waning_Gibbous;
            case 2: return Moon.Last_Quarter;
            case 3 : return Moon.Waning_Crescent;
            case 4: return Moon.New_Moon;
            case 5: return Moon.Waxing_Crescent;
            case 6: return Moon.First_Quarter;
            case 7: return Moon.Waxing_Gibbous;
            case 8: return Moon.Full_Moon;
            default: return Moon.New_Moon;
        }
    }


    public long getSunriseMillis() {
        return sunriseMillis;
    }

    public long getSunsetMillis() {
        return sunsetMillis;
    }
}
