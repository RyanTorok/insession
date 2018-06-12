package gui;

import main.Root;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.json.*;

/**
 * Created by 11ryt on 7/24/2017.
 */
public class WeatherManager {
    private int zipCode;
    private WeatherState current;
    private String description;
    private double tempCelsius;
    private double tempFahrenheit;

    public WeatherManager(int zipCode){
        this.setZipCode(zipCode);
    }

    public void update() {
        try {
            double[] latlon = Root.getActiveUser().getLatlon();
            if (latlon == null) {
                Root.getActiveUser().setLocation(getZipCode());
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
            JSONObject closest = features.getJSONObject(0);

            //get station current observation
            URL current_station_url = new URL(closest.getString("id") + "/observations/current/");
            BufferedReader observationIn = new BufferedReader(new InputStreamReader(current_station_url.openStream()));
            String observation = "";
            while ((line = observationIn.readLine()) != null) {
                observation += line + "\n";
            }
            observationIn.close();
            JSONObject observationObj = new JSONObject(observation);
            JSONObject properties = observationObj.getJSONObject("properties");
            setDescription(properties.getString("textDescription"));
            setTempCelsius(properties.getJSONObject("temperature").getDouble("value"));
            setTempFahrenheit(getTempCelsius() * 1.8 + 32);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        setCurrent(WeatherState.Snow);
    }

    public void update(int zipCode){
        this.setZipCode(zipCode);
        update();
    }
    public WeatherState getCurrent(){
        return current;
    }

    public int getZipCode() {
        this.setZipCode(77379);
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

    public double getTempCelsius() {
        return tempCelsius;
    }

    public void setTempCelsius(double tempCelsius) {
        this.tempCelsius = tempCelsius;
    }

    public double getTempFahrenheit() {
        return tempFahrenheit;
    }

    public void setTempFahrenheit(double tempFahrenheit) {
        this.tempFahrenheit = tempFahrenheit;
    }
}
