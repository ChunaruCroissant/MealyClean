package mosbach.dhbw.de.tasks.model;

import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "owner",
        "ingredients",
        "description",
        "Calories_(kcal)",
        "Total_Fat_(g)",
        "Saturated_Fat_(g)",
        "Cholesterol_(mg)",
        "Sodium_(mg)",
        "Total_Carbohydrates_(g)",
        "Dietary_Fiber_(g)",
        "Sugars_(g)",
        "Protein_(g)"
})
public class LargeRecipeConv {

    @JsonProperty("id")
    private int id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("owner")
    private String owner;

    @JsonProperty("ingredients")
    private List<IngredientConv> ingredients = new ArrayList<>();

    @JsonProperty("description")
    private String description;

    @JsonProperty("Calories_(kcal)")
    private double caloriesKcal;

    @JsonProperty("Total_Fat_(g)")
    private double totalFatG;

    @JsonProperty("Saturated_Fat_(g)")
    private double saturatedFatG;

    @JsonProperty("Cholesterol_(mg)")
    private double cholesterolMg;

    @JsonProperty("Sodium_(mg)")
    private double sodiumMg;

    @JsonProperty("Total_Carbohydrates_(g)")
    private double totalCarbohydratesG;

    @JsonProperty("Dietary_Fiber_(g)")
    private double dietaryFiberG;

    @JsonProperty("Sugars_(g)")
    private double sugarsG;

    @JsonProperty("Protein_(g)")
    private double proteinG;

    // Standardkonstruktor
    public LargeRecipeConv() {
    }

    // Konstruktor mit den ursprünglichen Eigenschaften, neue Werte mit 0 initialisiert
    public LargeRecipeConv(int id, String name, String owner, List<IngredientConv> ingredients, String description) {
        this(id, name, owner, ingredients, description, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    // Konstruktor mit den ursprünglichen Eigenschaften ohne Owner, neue Werte mit 0 initialisiert
    public LargeRecipeConv(int id, String name, List<IngredientConv> ingredients, String description) {
        this(id, name, null, ingredients, description, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

    // Konstruktor, der alle Eigenschaften einschließlich der Nährwerte abdeckt
    public LargeRecipeConv(int id, String name, String owner, List<IngredientConv> ingredients, String description,
                           double caloriesKcal, double totalFatG, double saturatedFatG, double cholesterolMg, double sodiumMg,
                           double totalCarbohydratesG, double dietaryFiberG, double sugarsG, double proteinG) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.ingredients = ingredients;
        this.description = description;
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

    // Getter und Setter für alle Felder (inklusive Nährwerteigenschaften)
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("owner")
    public String getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    @JsonProperty("ingredients")
    public List<IngredientConv> getIngredients() {
        return ingredients;
    }

    @JsonProperty("ingredients")
    public void setIngredients(List<IngredientConv> ingredients) {
        this.ingredients = ingredients;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("Calories_(kcal)")
    public double getCaloriesKcal() {
        return caloriesKcal;
    }

    @JsonProperty("Calories_(kcal)")
    public void setCaloriesKcal(double caloriesKcal) {
        this.caloriesKcal = caloriesKcal;
    }

    @JsonProperty("Total_Fat_(g)")
    public double getTotalFatG() {
        return totalFatG;
    }

    @JsonProperty("Total_Fat_(g)")
    public void setTotalFatG(double totalFatG) {
        this.totalFatG = totalFatG;
    }

    @JsonProperty("Saturated_Fat_(g)")
    public double getSaturatedFatG() {
        return saturatedFatG;
    }

    @JsonProperty("Saturated_Fat_(g)")
    public void setSaturatedFatG(double saturatedFatG) {
        this.saturatedFatG = saturatedFatG;
    }

    @JsonProperty("Cholesterol_(mg)")
    public double getCholesterolMg() {
        return cholesterolMg;
    }

    @JsonProperty("Cholesterol_(mg)")
    public void setCholesterolMg(double cholesterolMg) {
        this.cholesterolMg = cholesterolMg;
    }

    @JsonProperty("Sodium_(mg)")
    public double getSodiumMg() {
        return sodiumMg;
    }

    @JsonProperty("Sodium_(mg)")
    public void setSodiumMg(double sodiumMg) {
        this.sodiumMg = sodiumMg;
    }

    @JsonProperty("Total_Carbohydrates_(g)")
    public double getTotalCarbohydratesG() {
        return totalCarbohydratesG;
    }

    @JsonProperty("Total_Carbohydrates_(g)")
    public void setTotalCarbohydratesG(double totalCarbohydratesG) {
        this.totalCarbohydratesG = totalCarbohydratesG;
    }

    @JsonProperty("Dietary_Fiber_(g)")
    public double getDietaryFiberG() {
        return dietaryFiberG;
    }

    @JsonProperty("Dietary_Fiber_(g)")
    public void setDietaryFiberG(double dietaryFiberG) {
        this.dietaryFiberG = dietaryFiberG;
    }

    @JsonProperty("Sugars_(g)")
    public double getSugarsG() {
        return sugarsG;
    }

    @JsonProperty("Sugars_(g)")
    public void setSugarsG(double sugarsG) {
        this.sugarsG = sugarsG;
    }

    @JsonProperty("Protein_(g)")
    public double getProteinG() {
        return proteinG;
    }

    @JsonProperty("Protein_(g)")
    public void setProteinG(double proteinG) {
        this.proteinG = proteinG;
    }
}

