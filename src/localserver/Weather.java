package localserver;

import java.util.HashMap;

public class Weather extends AnonymousCommand {

    private static HashMap<Integer, WeatherManager> zipcodeMap = new HashMap<>();

    public Weather(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {
        Integer zipcode = getArgumentAsInteger(0);
        if (zipcode == 0)
            return "error : invalid zip code";
        WeatherManager manager = zipcodeMap.computeIfAbsent(zipcode, m -> new WeatherManager(zipcode));
        manager.updateIfExpired();
        if (manager.getDescription() == null)
            return "error : weather server down or connection error";
        return makeReturn(zipcode, manager.getLastUpdate(), manager.getTempCelsius(), manager.getCurrent(), manager.getDescription().replaceAll(" ", "_"));
    }
}