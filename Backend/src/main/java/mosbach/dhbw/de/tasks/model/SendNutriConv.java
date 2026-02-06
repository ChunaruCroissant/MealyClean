package mosbach.dhbw.de.tasks.model;

import java.util.LinkedHashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Calories_(kcal)",
        "Protein_(g)",
        "Total_Carbohydrates_(g)",
        "Total_Fat_(g)"
})
public class SendNutriConv {

    @JsonProperty("Calories_(kcal)")
    private double caloriesKcal;
    @JsonProperty("Protein_(g)")
    private double proteinG;
    @JsonProperty("Total_Carbohydrates_(g)")
    private double totalCarbohydratesG;
    @JsonProperty("Total_Fat_(g)")
    private double totalFatG;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     */
    public SendNutriConv() {
    }

    public SendNutriConv(double caloriesKcal, double proteinG, double totalCarbohydratesG, double totalFatG) {
        super();
        this.caloriesKcal = caloriesKcal;
        this.proteinG = proteinG;
        this.totalCarbohydratesG = totalCarbohydratesG;
        this.totalFatG = totalFatG;
    }

    @JsonProperty("Calories_(kcal)")
    public double getCaloriesKcal() {
        return caloriesKcal;
    }

    @JsonProperty("Calories_(kcal)")
    public void setCaloriesKcal(double caloriesKcal) {
        this.caloriesKcal = caloriesKcal;
    }

    @JsonProperty("Protein_(g)")
    public double getProteinG() {
        return proteinG;
    }

    @JsonProperty("Protein_(g)")
    public void setProteinG(double proteinG) {
        this.proteinG = proteinG;
    }

    @JsonProperty("Total_Carbohydrates_(g)")
    public double getTotalCarbohydratesG() {
        return totalCarbohydratesG;
    }

    @JsonProperty("Total_Carbohydrates_(g)")
    public void setTotalCarbohydratesG(double totalCarbohydratesG) {
        this.totalCarbohydratesG = totalCarbohydratesG;
    }

    @JsonProperty("Total_Fat_(g)")
    public double getTotalFatG() {
        return totalFatG;
    }

    @JsonProperty("Total_Fat_(g)")
    public void setTotalFatG(double totalFatG) {
        this.totalFatG = totalFatG;
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
