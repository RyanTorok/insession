package gui;

import main.Root;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import main.UnknownZipCodeException;
import org.json.*;

/**
 * Created by 11ryt on 7/24/2017.
 */

public class WeatherManager {
    private int zipCode;
    private WeatherState current;
    private String description;
    private Double tempCelsius;
    private Double tempFahrenheit;
    private static final Double HEAVY_THRESHOLD = .00635;

    public WeatherManager(int zipCode) {
        this.setZipCode(zipCode);
    }

    public void update() {
        JSONObject properties = null;
        try {
            boolean success = false;
            int stationIndex = 0;
            while (!success) {
                double[] latlon = Root.getActiveUser().getLatlon();
                if (latlon == null || latlon[0] == 0 && latlon[1] == 0) {
                    try {
                        Root.getActiveUser().setLocation(getZipCode());
                    } catch (UnknownZipCodeException e) {
                        Root.getActiveUser().setLocation(77379);
                    }
                    latlon = Root.getActiveUser().getLatlon();
                }

                URL api = new URL("https://api.weather.gov/");
                URL stations = new URL(api, "points/" + latlon[0] + "," + latlon[1] + "/stations/");
                BufferedReader stationsIn = new BufferedReader(new InputStreamReader(stations.openStream()));
                String stationFile = "", line = "";
                while ((line = stationsIn.readLine()) != null) {
                    stationFile += line + "\n";
                }
                stationsIn.close();
                JSONObject stationsObj = new JSONObject(stationFile);
                JSONArray features = stationsObj.getJSONArray("features");
                int numStations = features.length();
                if (stationIndex >= numStations)
                    return;
                JSONObject closest = features.getJSONObject(stationIndex);

                //get station current observation
                URL current_station_url = new URL(closest.getString("id") + "/observations/current/");
                BufferedReader observationIn = new BufferedReader(new InputStreamReader(current_station_url.openStream()));
                String observation = "";
                while ((line = observationIn.readLine()) != null) {
                    observation += line + "\n";
                }
                observationIn.close();
                JSONObject observationObj = new JSONObject(observation);
                properties = observationObj.getJSONObject("properties");
                setDescription(properties.getString("textDescription"));
                Object tempObj = properties.getJSONObject("temperature").get("value");
                if (tempObj instanceof Integer)
                    setTempCelsius((double) (int) tempObj);
                else
                    setTempCelsius(tempObj.equals(null) ? null : (double) tempObj);
                setTempFahrenheit(getTempCelsius() == null ? null : getTempCelsius() * 1.8 + 32);
                success = !tempObj.equals(null);
                stationIndex++;
            }
            setCurrentState(properties);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void setCurrentState(JSONObject properties) {
        //set current state
        String descLC = description.toLowerCase();
        Double lastHr = null;
        Object lastHrObj = properties.getJSONObject("precipitationLastHour").get("value");
        if (lastHrObj.equals(null))
            lastHr = 0.0;
        else {
            if (lastHrObj instanceof Integer) {
                lastHr = (double) (int) lastHrObj;
            } else {
                lastHr = (Double) lastHrObj;
            }
        }
        if (lastHr == null)
            lastHr = 0.0;
        boolean fog = false;
        boolean isHeavy = lastHr > HEAVY_THRESHOLD || descLC.contains("heavy") || descLC.contains("blizzard");
        if (descLC.contains("fog")) {
            fog = true;
            setCurrent(WeatherState.Fog);
        }
        if (descLC.contains("snow") || descLC.contains("ice") || descLC.contains("icy") || descLC.contains("mix")
                || descLC.contains("sleet") || descLC.contains("freez") || descLC.contains("blizzard")) {
            if (fog) {
                if (isHeavy)
                    setCurrent(WeatherState.Fog_And_Blizzard);
                else
                    setCurrent(WeatherState.Fog_And_Snow);
            } else {
                if (isHeavy)
                    setCurrent(WeatherState.Blizzard);
                else
                    setCurrent(WeatherState.Snow);
            }
            return;
        }
        if (descLC.contains("storm") || descLC.contains("thunder")) {
            if (fog)
                setCurrent(WeatherState.Fog_And_Thunderstorm);
            else
                setCurrent(WeatherState.Thunderstorm);
            return;
        }
        if (descLC.contains("rain") || descLC.contains("shower") || descLC.contains("drizzle")) {
            if (fog) {
                if (isHeavy)
                    setCurrent(WeatherState.Fog_And_Heavy_Rain);
                else
                    setCurrent(WeatherState.Fog_And_Light_Rain);
            } else {
                if (isHeavy)
                    setCurrent(WeatherState.Heavy_Rain);
                else
                    setCurrent(WeatherState.Light_Rain);
            }
            return;
        }
        if (descLC.contains("cloud") || descLC.contains("overcast")) {
            if (descLC.contains("partly") || descLC.contains("few") || descLC.contains("mostly")) {
                setCurrent(WeatherState.Partly_Cloudy);
            } else setCurrent(WeatherState.Cloudy);
            return;
        }
        if (!fog)
            setCurrent(WeatherState.Sunny);
    }

    public void update(int zipCode){
        this.setZipCode(zipCode);
        update();
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
