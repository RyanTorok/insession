package localserver;

import gui.WeatherState;
import main.UnknownZipCodeException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
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
    private long sunriseMillis, sunsetMillis;

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
                StringBuilder stationFile;
                String line;
                try (BufferedReader stationsIn = new BufferedReader(new InputStreamReader(stations.openStream()))) {
                    stationFile = new StringBuilder();
                    line = "";
                    while ((line = stationsIn.readLine()) != null) {
                        stationFile.append(line).append("\n");
                    }
                } catch (IOException e) {
                    return;
                }
                JSONObject stationsObj = new JSONObject(stationFile.toString());
                JSONArray features = stationsObj.getJSONArray("features");
                int numStations = features.length();
                if (stationIndex >= numStations)
                    return;
                JSONObject closest = features.getJSONObject(stationIndex);

                //get station current observation
                URL current_station_url = new URL(closest.getString("id") + "/observations/current/");
                System.out.println(current_station_url.toString());
                BufferedReader observationIn = new BufferedReader(new InputStreamReader(current_station_url.openStream()));
                StringBuilder observation = new StringBuilder();
                while ((line = observationIn.readLine()) != null) {
                    observation.append(line).append("\n");
                }
                observationIn.close();
                JSONObject observationObj = new JSONObject(observation.toString());
                properties = observationObj.getJSONObject("properties");
                setDescription(properties.getString("textDescription"));
                Object tempObj = properties.getJSONObject("temperature").get("value");
                if (tempObj instanceof Integer)
                    setTempCelsius((double) (int) tempObj);
                else
                    setTempCelsius(tempObj == null || tempObj.equals(JSONObject.NULL) ? null : (double) tempObj);
                setTempFahrenheit(getTempCelsius() == null ? null : getTempCelsius() * 1.8 + 32);
                success = tempObj != null;
                stationIndex++;
            }
            calculateSunriseSunset();
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
        if (lastHrObj.equals(JSONObject.NULL))
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

    //models the spreadsheet https://www.esrl.noaa.gov/gmd/grad/solcalc/NOAA_Solar_Calculations_day.xls
    private void calculateSunriseSunset() {
        int timezone = 0;
        double minutesPastMidnight = 0.0; //irrelevant, but left in to maintain the integrity to the original
        double B3 = latlon[0];
        double B4 = latlon[1];
        double B5 = timezone;
        long excelDate = (System.currentTimeMillis() / 1000 / 86400) + 25567;

        //All cell addresses could be any row as long as they're all the same, I just used row 2 because it was the first data row.
        double D2 = excelDate;
        double E2 = minutesPastMidnight;
        double F2 = D2+2415018.5+E2-B5/24;
        double G2 =(F2-2451545)/36525;
        double I2 =MOD(280.46646+G2*(36000.76983+G2*0.0003032),360);
        double J2 =357.52911+G2*(35999.05029-0.0001537*G2);
        double K2 = 0.016708634-G2*(0.000042037+0.0000001267*G2);
        double L2 = SIN(RADIANS(J2))*(1.914602-G2*(0.004817+0.000014*G2))+SIN(RADIANS(2*J2))*(0.019993-0.000101*G2)+SIN(RADIANS(3*J2))*0.000289;
        double M2 = I2 + L2;
        double N2 = J2 + L2;
        double O2 = (1.000001018*(1-K2*K2))/(1+K2*COS(RADIANS(N2)));
        double P2 = M2-0.00569-0.00478*SIN(RADIANS(125.04-1934.136*G2)) ;
        double Q2 =23+(26+((21.448-G2*(46.815+G2*(0.00059-G2*0.001813))))/60)/60;
        double R2 =Q2+0.00256*COS(RADIANS(125.04-1934.136*G2));
        double S2 =DEGREES(ATAN2(COS(RADIANS(P2)),COS(RADIANS(R2))*SIN(RADIANS(P2))));
        double T2 = DEGREES(ASIN(SIN(RADIANS(R2))*SIN(RADIANS(P2))));
        double U2 =TAN(RADIANS(R2/2))*TAN(RADIANS(R2/2));
        double V2 = 4*DEGREES(U2*SIN(2*RADIANS(I2))-2*K2*SIN(RADIANS(J2))+4*K2*U2*SIN(RADIANS(J2))*COS(2*RADIANS(I2))-0.5*U2*U2*SIN(4*RADIANS(I2))-1.25*K2*K2*SIN(2*RADIANS(J2)));
        double W2 = DEGREES(ACOS(COS(RADIANS(90.833))/(COS(RADIANS(B3))*COS(RADIANS(T2)))-TAN(RADIANS(B3))*TAN(RADIANS(T2))));
        double X2 = (720 - 4 * B4 - V2 + B5*60)/1440;

        long today = System.currentTimeMillis() - (System.currentTimeMillis() % (1000 * 86400));

        double sunriseDayFrac = X2 - W2*4/1440;
        double sunsetDayFrac = X2 + W2*4/1440;

        sunriseMillis = (long) ((today + (sunriseDayFrac * 86400 * 1000)) % (1000 * 86400));
        sunsetMillis = (long) ((today + (sunsetDayFrac * 86400 * 1000)) % (1000 * 86400));
    }

    private double MOD(double dividend, double divisor) {
        return dividend % divisor;
    }

    public long getSunriseMillis() {
        return sunriseMillis;
    }

    public long getSunsetMillis() {
        return sunsetMillis;
    }

    private double DEGREES(double r) {
        return Math.toDegrees(r);
    }

    private double RADIANS(double d) {
        return Math.toRadians(d);
    }

    private double SIN(double a) {
        return Math.sin(a);
    }

    private double ASIN(double l) {
        return Math.asin(l);
    }

    private double COS(double a) {
        return Math.cos(a);
    }

    private double ACOS(double l) {
        return Math.acos(l);
    }

    private double TAN(double a) {
        return Math.tan(a);
    }

    private double ATAN2(double x, double y) {
        return Math.atan2(y, x);
    }

}
