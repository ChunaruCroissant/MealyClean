package mosbach.dhbw.de.tasks.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import mosbach.dhbw.de.tasks.data.impl.MealManager;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MealPlanConverter {

    private static MealPlanConverter mealPlanConverter = null;


    public static MealPlanConverter getMealPlanConverter() {
        if (mealPlanConverter == null) {
            mealPlanConverter = new MealPlanConverter();
        }
        return mealPlanConverter;
    }

    public static String convertToMealPlanJson(List<String> recipeNames, List<TimeConv> mealTimes, List<SendNutriConv> nutritionValues) {
        try {
            if (recipeNames.size() != mealTimes.size() || mealTimes.size() != nutritionValues.size()) {
                throw new IllegalArgumentException("Die Listenlängen stimmen nicht überein.");
            }

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode root = mapper.createObjectNode();
            ArrayNode mealsArray = mapper.createArrayNode();

            for (int i = 0; i < recipeNames.size(); i++) {
                ObjectNode mealNode = mapper.createObjectNode();
                mealNode.put("name", recipeNames.get(i));
                mealNode.put("day", mealTimes.get(i).getDay());
                mealNode.put("time", mealTimes.get(i).getTime());
                mealNode.put("calories", nutritionValues.get(i).getCaloriesKcal());
                mealNode.put("protein", nutritionValues.get(i).getProteinG());
                mealNode.put("carbs", nutritionValues.get(i).getTotalCarbohydratesG());
                mealNode.put("fats", nutritionValues.get(i).getTotalFatG());

                mealsArray.add(mealNode);
            }

            root.set("meals", mealsArray);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

        } catch (IllegalArgumentException e) {
            Logger.getLogger(MealPlanConverter.class.getName()).log(Level.SEVERE, "Listen sind nicht gleich lang", e);
            return "{\"error\": \"Interner Fehler: Daten können nicht verarbeitet werden.\"}";
        } catch (Exception e) {
            Logger.getLogger(MealPlanConverter.class.getName()).log(Level.SEVERE, "Fehler bei der JSON-Erstellung", e);
            return "{\"error\": \"Ein Fehler trat bei der JSON-Erstellung auf.\"}";
        }
    }

}

