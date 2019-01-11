package gui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;

import gui.Moon;
import gui.WeatherState;
import main.UnknownZipCodeException;
import main.User;
import net.ServerSession;
import org.json.*;

/**
 * Created by 11ryt on 7/24/2017.
 */

public class WeatherManager{

    private int zipCode;
    private WeatherState current;
    private String description;
    private Double tempCelsius;
    private Double tempFahrenheit;
    private static final Double HEAVY_THRESHOLD = .00635;
    private long lastUpdate = 0;

    public WeatherManager(int zipCode) {
        this.setZipCode(zipCode);
    }

    public void update() {
        try (ServerSession session = new ServerSession()) {
            session.open();
            String[] result = session.callAndResponse("weather", Integer.toString(zipCode));
            System.out.println("result: " + Arrays.toString(result));
            setLastUpdate(Long.parseLong(result[1]));
            setTempCelsius(Double.parseDouble(result[2]));
            setCurrent(WeatherState.valueOf(result[3]));
            setDescription(result[4]);
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

    static final double CYCLE_LENGTH_MILLIS = 2551442801.5584;
    static final long zero_point = 1530161580000L; // 6/28/18 at 4:53 AM UTC - Full Moon

    public Moon getMoonState() {
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
}
