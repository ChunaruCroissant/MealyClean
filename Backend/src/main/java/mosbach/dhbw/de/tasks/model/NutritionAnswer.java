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
        "nutritional_values"
})
public class NutritionAnswer {

    @JsonProperty("nutritional_values")
    private NutritionConv nutritionalValues;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new LinkedHashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     *
     */
    public NutritionAnswer() {
    }

    public NutritionAnswer(NutritionConv nutritionalValues) {
        super();
        this.nutritionalValues = nutritionalValues;
    }

    @JsonProperty("nutritional_values")
    public NutritionConv getNutritionalValues() {
        return nutritionalValues;
    }

    @JsonProperty("nutritional_values")
    public void setNutritionalValues(NutritionConv nutritionalValues) {
        this.nutritionalValues = nutritionalValues;
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