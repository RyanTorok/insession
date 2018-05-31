package cafeteria;

import java.util.HashMap;
import java.util.List;

public class NutritionLabel {

    private String servingSize;
    private double Calories;
    private double CaloriesFromFat;
    private double TotalFat;
    private double SatFat;
    private double TransFat;
    private double PolyUSFat;
    private double MonoUSFat;
    private double Cholesterol;
    private double Sodium;
    private double Potassium;
    private double TotalCarbs;
    private double DFiber;
    private double SolFiber;
    private double InsolFiber;
    private double Sugars;
    private double SugarAlcohol;
    private double Protien;

    private HashMap<String, Integer> vitaminPercents;

    private List<String> ingredients;
    private List<String> contains;
    private List<String> mayContain;
    private List<String> manufacturedOnEquipWith;

    public NutritionLabel(String servingSize, double calories, double caloriesFromFat, double totalFat, double satFat, double transFat, double polyUSFat, double monoUSFat, double cholesterol, double sodium, double potassium, double totalCarbs, double DFiber, double solFiber, double insolFiber, double sugars, double sugarAlcohol, double protien) {
        this.servingSize = servingSize;
        Calories = calories;
        CaloriesFromFat = caloriesFromFat;
        TotalFat = totalFat;
        SatFat = satFat;
        TransFat = transFat;
        PolyUSFat = polyUSFat;
        MonoUSFat = monoUSFat;
        Cholesterol = cholesterol;
        Sodium = sodium;
        Potassium = potassium;
        TotalCarbs = totalCarbs;
        this.DFiber = DFiber;
        SolFiber = solFiber;
        InsolFiber = insolFiber;
        Sugars = sugars;
        SugarAlcohol = sugarAlcohol;
        Protien = protien;
    }
}
