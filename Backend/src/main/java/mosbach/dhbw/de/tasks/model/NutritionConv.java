package mosbach.dhbw.de.tasks.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NutritionConv {

    @JsonProperty("Calories (kcal)")
    private double caloriesKcal;

    @JsonProperty("Total Fat (g)")
    private double totalFatG;

    @JsonProperty("Saturated Fat (g)")
    private double saturatedFatG;

    @JsonProperty("Cholesterol (mg)")
    private double cholesterolMg;

    @JsonProperty("Sodium (mg)")
    private double sodiumMg;

    @JsonProperty("Total Carbohydrates (g)")
    private double totalCarbohydratesG;

    @JsonProperty("Dietary Fiber (g)")
    private double dietaryFiberG;

    @JsonProperty("Sugars (g)")
    private double sugarsG;

    @JsonProperty("Protein (g)")
    private double proteinG;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<>();

    // No-args constructor for use in serialization
    public NutritionConv() {
    }

    // All-args constructor
    public NutritionConv(double caloriesKcal, double totalFatG, double saturatedFatG, double cholesterolMg, double sodiumMg,
                         double totalCarbohydratesG, double dietaryFiberG, double sugarsG, double proteinG) {
        this.caloriesKcal = caloriesKcal;
        this.totalFatG = totalFatG;
        this.saturatedFatG = saturatedFatG;
        this.cholesterolMg = cholesterolMg;
        this.sodiumMg = sodiumMg;
        this.totalCarbohydratesG = totalCarbohydratesG;
        this.dietaryFiberG = dietaryFiberG;
        this.sugarsG = sugarsG;
        this.proteinG = proteinG;
    }

    @Override
    public String toString() {
        return "NutritionConv{" +
                "caloriesKcal=" + caloriesKcal +
                ", totalFatG=" + totalFatG +
                ", saturatedFatG=" + saturatedFatG +
                ", cholesterolMg=" + cholesterolMg +
                ", sodiumMg=" + sodiumMg +
                ", totalCarbohydratesG=" + totalCarbohydratesG +
                ", dietaryFiberG=" + dietaryFiberG +
                ", sugarsG=" + sugarsG +
                ", proteinG=" + proteinG +
                '}';
    }

    @JsonProperty("Calories (kcal)")
    public double getCaloriesKcal() {
        return caloriesKcal;
    }

    @JsonProperty("Calories (kcal)")
    public void setCaloriesKcal(double caloriesKcal) {
        this.caloriesKcal = caloriesKcal;
    }

    @JsonProperty("Total Fat (g)")
    public double getTotalFatG() {
        return totalFatG;
    }

    @JsonProperty("Total Fat (g)")
    public void setTotalFatG(double totalFatG) {
        this.totalFatG = totalFatG;
    }

    @JsonProperty("Saturated Fat (g)")
    public double getSaturatedFatG() {
        return saturatedFatG;
    }

    @JsonProperty("Saturated Fat (g)")
    public void setSaturatedFatG(double saturatedFatG) {
        this.saturatedFatG = saturatedFatG;
    }

    @JsonProperty("Cholesterol (mg)")
    public double getCholesterolMg() {
        return cholesterolMg;
    }

    @JsonProperty("Cholesterol (mg)")
    public void setCholesterolMg(double cholesterolMg) {
        this.cholesterolMg = cholesterolMg;
    }

    @JsonProperty("Sodium (mg)")
    public double getSodiumMg() {
        return sodiumMg;
    }

    @JsonProperty("Sodium (mg)")
    public void setSodiumMg(double sodiumMg) {
        this.sodiumMg = sodiumMg;
    }

    @JsonProperty("Total Carbohydrates (g)")
    public double getTotalCarbohydratesG() {
        return totalCarbohydratesG;
    }

    @JsonProperty("Total Carbohydrates (g)")
    public void setTotalCarbohydratesG(double totalCarbohydratesG) {
        this.totalCarbohydratesG = totalCarbohydratesG;
    }

    @JsonProperty("Dietary Fiber (g)")
    public double getDietaryFiberG() {
        return dietaryFiberG;
    }

    @JsonProperty("Dietary Fiber (g)")
    public void setDietaryFiberG(double dietaryFiberG) {
        this.dietaryFiberG = dietaryFiberG;
    }

    @JsonProperty("Sugars (g)")
    public double getSugarsG() {
        return sugarsG;
    }

    @JsonProperty("Sugars (g)")
    public void setSugarsG(double sugarsG) {
        this.sugarsG = sugarsG;
    }

    @JsonProperty("Protein (g)")
    public double getProteinG() {
        return proteinG;
    }

    @JsonProperty("Protein (g)")
    public void setProteinG(double proteinG) {
        this.proteinG = proteinG;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
