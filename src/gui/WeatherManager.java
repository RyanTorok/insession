package gui;

import java.net.URL;

/**
 * Created by 11ryt on 7/24/2017.
 */
public class WeatherManager {
    private int zipCode;
    private WeatherState current;

    public WeatherManager(int zipCode){
        this.zipCode = zipCode;
    }

    public void update(){
        current = WeatherState.Sunny;
    }

    public void update(int zipCode){
        this.zipCode = zipCode;
        update();
    }
    public WeatherState getCurrent(){
        return current;
    }
}
