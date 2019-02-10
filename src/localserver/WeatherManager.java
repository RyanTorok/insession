package localserver;

import gui.WeatherState;
import main.UnknownZipCodeException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by 11ryt on 7/24/2017.
 */

public class WeatherManager {

    private static final long TIME_THRESHOLD = 5 * 60 * 1000;

    private int zipcode;
    private WeatherState current;
    private String description;
    private Double tempCelsius;
    private Double tempFahrenheit;
    private Long lastUpdate = 0L;
    private static final Double HEAVY_THRESHOLD = .00635;
    private double[] latlon = new double[]{0,0};

    public WeatherManager(int zipCode) {
        this.setZipcode(zipCode);
    }

    public void update() {
        JSONObject properties = null;
        try {
            boolean success = false;
            int stationIndex = 0;
            while (!success) {
                if (latlon == null || latlon[0] == 0 && latlon[1] == 0) {
                    try {
                        setLocation(getZipcode());
                    } catch (UnknownZipCodeException e) {
                        setLocation(77379);
                    }
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
                System.out.println(current_station_url.toString());
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
            lastUpdate = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
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

    public void update(int zipCode) {
        this.setZipcode(zipCode);
        update();
    }

    public void updateIfExpired() {
        long ctm = System.currentTimeMillis();
        long diff = ctm - lastUpdate;
        if (diff > TIME_THRESHOLD) {
            update();
            //conservative, use the time before the update occurs.
            lastUpdate = ctm;
        }
    }

    public WeatherState getCurrent(){
        return current;
    }

    public int getZipcode() {
        return zipcode;
    }

    public void setZipcode(int zipcode) {
        this.zipcode = zipcode;
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

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public void setLocation(int zip) throws UnknownZipCodeException {
        this.zipcode = zip;
        ZipMap.LatLon latLon = new ZipMap().get(zip);
        latlon = new double[2];
        try {
            this.latlon[0] = latLon.getLat();
            this.latlon[1] = latLon.getLon();
        } catch (NullPointerException e) {
            throw new UnknownZipCodeException("Zip Code not found: " + zip);
        }
    }
}
