package mosbach.dhbw.de.tasks.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "meal_id",
        "owner",
        "day",
        "time",
        "id"
})

public class MealplanConv {

    @JsonProperty("meal_id")
    private int mealId;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("day")
    private String day;

    @JsonProperty("time")
    private String time;

    @JsonProperty("id")
    private String id;

    public MealplanConv() {
    }

    public MealplanConv(int mealId, String owner, String day, String time, String id) {
        this.mealId = mealId;
        this.owner = owner;
        this.day = day;
        this.time = time;
        this.id = id;
    }

    @JsonProperty("meal_id")
    public int getMealId() {
        return mealId;
    }

    @JsonProperty("meal_id")
    public void setMealId(int mealId) {
        this.mealId = mealId;
    }

    @JsonProperty("owner")
    public String getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @JsonProperty("day")
    public String getDay() {
        return day;
    }

    @JsonProperty("day")
    public void setDay(String day) {
        this.day = day;
    }

    @JsonProperty("time")
    public String getTime() {
        return time;
    }

    @JsonProperty("time")
    public void setTime(String time) {
        this.time = time;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }
}
