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
public class RecipeConv {

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
    private String caloriesKcal;

    @JsonProperty("Total_Fat_(g)")
    private String totalFatG;

    @JsonProperty("Saturated_Fat_(g)")
    private String saturatedFatG;

    @JsonProperty("Cholesterol_(mg)")
    private String cholesterolMg;

    @JsonProperty("Sodium_(mg)")
    private String sodiumMg;

    @JsonProperty("Total_Carbohydrates_(g)")
    private String totalCarbohydratesG;

    @JsonProperty("Dietary_Fiber_(g)")
    private String dietaryFiberG;

    @JsonProperty("Sugars_(g)")
    private String sugarsG;

    @JsonProperty("Protein_(g)")
    private String proteinG;

    // Standardkonstruktor
    public RecipeConv() {
    }

    // Konstruktor mit allen Eigenschaften
    public RecipeConv(int id, String name, String owner, List<IngredientConv> ingredients, String description) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.ingredients = ingredients;
        this.description = description;
    }

    // Methode, um eine Kopie ohne den Owner zurückzugeben
    public RecipeConv (int id, String name, List<IngredientConv> ingredients, String description) {
        this.id = id;
        this.name = name;
        owner = null;
        this.ingredients = ingredients;
        this.description = description;
    }

    // Getter und Setter für ID
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    // Getter und Setter für Name
    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    // Getter und Setter für Owner
    @JsonProperty("owner")
    public String getOwner() {
        return owner;
    }

    @JsonProperty("owner")
    public void setOwner(String owner) {
        this.owner = owner;
    }

    // Getter und Setter für Ingredients
    @JsonProperty("ingredients")
    public List<IngredientConv> getIngredients() {
        return ingredients;
    }

    @JsonProperty("ingredients")
    public void setIngredients(List<IngredientConv> ingredients) {
        this.ingredients = ingredients;
    }

    // Getter und Setter für Description
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }
}
