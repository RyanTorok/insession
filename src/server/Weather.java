package server;

import java.util.HashMap;

public class Weather extends Command {

    private static HashMap<Integer, WeatherManager> zipcodeMap = new HashMap<>();

    public Weather(String[] arguments) {
        super(arguments);
    }

    @Override
    String execute() throws WrongArgumentTypeException {
        Integer zipcode = getArgumentAsInteger(0);
        WeatherManager manager = zipcodeMap.computeIfAbsent(zipcode, m -> new WeatherManager(zipcode));
        manager.updateIfExpired();
        return makeReturn(zipcode, manager.getLastUpdate(), manager.getTempCelsius(), manager.getCurrent(), manager.getDescription());
    }
}
