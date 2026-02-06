package mosbach.dhbw.de.tasks.data.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import mosbach.dhbw.de.tasks.model.*;
import mosbach.dhbw.de.tasks.persistence.entity.IngredientValue;
import mosbach.dhbw.de.tasks.persistence.entity.RecipeEntity;
import mosbach.dhbw.de.tasks.persistence.entity.UserEntity;
import mosbach.dhbw.de.tasks.persistence.repo.RecipeRepository;
import mosbach.dhbw.de.tasks.persistence.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class RecipeManager {

    private final RecipeRepository recipeRepo;
    private final UserRepository userRepo;

    // Nutrition API settings (override via ENV/properties)
    @Value("${mealy.nutrition.api.url:https://gustar-io-deutsche-rezepte.p.rapidapi.com/nutrition}")
    private String nutritionApiUrl;

    @Value("${mealy.nutrition.api.key:}")
    private String nutritionApiKey;

    @Value("${mealy.nutrition.api.host:gustar-io-deutsche-rezepte.p.rapidapi.com}")
    private String nutritionApiHost;


    public RecipeManager(RecipeRepository recipeRepo, UserRepository userRepo) {
        this.recipeRepo = recipeRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public void saveRecipe(RecipeConv recipe, UserConv user) {
        UserEntity owner = userRepo.findByEmail(user.getEmail())
                .orElseThrow(() -> new IllegalStateException("Owner user not found in DB: " + user.getEmail()));

        RecipeEntity e = new RecipeEntity();
        e.setOwner(owner);
        e.setName(recipe.getName());
        e.setDescription(recipe.getDescription());

        // Ingredients -> Entity
        List<IngredientValue> ingValues = new ArrayList<>();
        if (recipe.getIngredients() != null) {
            for (IngredientConv ing : recipe.getIngredients()) {
                IngredientValue v = new IngredientValue();
                v.setName(ing.getName());
                v.setUnit(ing.getUnit());
                v.setAmount(ing.getAmount());
                ingValues.add(v);
            }
        }
        e.setIngredients(ingValues);

        // --- Nutrition API call + store result (optional) ---
        try {
            List<String> names = new ArrayList<>();
            List<Double> amounts = new ArrayList<>();

            if (recipe.getIngredients() != null) {
                for (IngredientConv ing : recipe.getIngredients()) {
                    names.add(ing.getName());

                    double amount = 0.0;
                    if (ing.getAmount() != null) {
                        String normalized = ing.getAmount().trim().replace(",", ".");
                        try {
                            amount = Double.parseDouble(normalized);
                        } catch (NumberFormatException ignored) { }
                    }
                    amounts.add(amount);
                }
            }

            String jsonPayload = generateIngredientString(names, amounts);
            NutritionConv result = sendNutritionRequest(jsonPayload);

            if (result != null) {
                e.setCaloriesKcal(result.getCaloriesKcal());
                e.setTotalFatG(result.getTotalFatG());
                e.setSaturatedFatG(result.getSaturatedFatG());
                e.setCholesterolMg(result.getCholesterolMg());
                e.setSodiumMg(result.getSodiumMg());
                e.setTotalCarbohydratesG(result.getTotalCarbohydratesG());
                e.setDietaryFiberG(result.getDietaryFiberG());
                e.setSugarsG(result.getSugarsG());
                e.setProteinG(result.getProteinG());
            }
        } catch (Exception ex) {
            // IMPORTANT: don’t throw, otherwise you rollback saving the recipe
            System.err.println("Nutrition API failed, saving recipe without nutrition: " + ex.getMessage());
        }

        RecipeEntity saved = recipeRepo.save(e);

        // Update conv like before
        recipe.setId(Math.toIntExact(saved.getId()));
        recipe.setOwner(user.getEmail());
    }

    @Transactional(readOnly = true)
    public Map<Integer, String> readRecipeNames(UserConv user) {
        Map<Integer, String> recipes = new LinkedHashMap<>();
        for (RecipeEntity r : recipeRepo.findByOwner_Email(user.getEmail())) {
            recipes.put(Math.toIntExact(r.getId()), r.getName());
        }
        return recipes;
    }

    @Transactional(readOnly = true)
    public List<Integer> readRecipeIDs(UserConv user) {
        List<Integer> ids = new ArrayList<>();
        for (RecipeEntity r : recipeRepo.findByOwner_Email(user.getEmail())) {
            ids.add(Math.toIntExact(r.getId()));
        }
        return ids;
    }

    @Transactional(readOnly = true)
    public List<String> readRecipeName(UserConv user) {
        List<String> names = new ArrayList<>();
        for (RecipeEntity r : recipeRepo.findByOwner_Email(user.getEmail())) {
            names.add(r.getName());
        }
        return names;
    }

    @Transactional(readOnly = true)
    public RecipeConv readRecipeById(int recipeId) {
        RecipeEntity r = recipeRepo.findById((long) recipeId).orElse(null);
        if (r == null) return null;

        List<IngredientConv> ingredients = new ArrayList<>();
        if (r.getIngredients() != null) {
            for (IngredientValue v : r.getIngredients()) {
                ingredients.add(new IngredientConv(v.getName(), v.getUnit(), v.getAmount()));
            }
        }

        return new RecipeConv(Math.toIntExact(r.getId()), r.getName(), ingredients, r.getDescription());
    }

    @Transactional(readOnly = true)
    public RecipeConv readRecipeByIdForOwner(int recipeId, UserConv user) {
        if (user == null || user.getEmail() == null) return null;
        RecipeEntity r = recipeRepo.findByIdAndOwner_Email((long) recipeId, user.getEmail()).orElse(null);
        if (r == null) return null;

        List<IngredientConv> ingredients = new ArrayList<>();
        if (r.getIngredients() != null) {
            for (IngredientValue v : r.getIngredients()) {
                ingredients.add(new IngredientConv(v.getName(), v.getUnit(), v.getAmount()));
            }
        }
        return new RecipeConv(Math.toIntExact(r.getId()), r.getName(), ingredients, r.getDescription());
    }


    @Transactional(readOnly = true)
    public LargeRecipeConv readRecipeDetailByIdForOwner(int recipeId, UserConv user) {
        if (user == null || user.getEmail() == null) return null;

        RecipeEntity r = recipeRepo.findByIdAndOwner_Email((long) recipeId, user.getEmail()).orElse(null);
        if (r == null) return null;

        List<IngredientConv> ingredients = new ArrayList<>();
        if (r.getIngredients() != null) {
            for (IngredientValue v : r.getIngredients()) {
                ingredients.add(new IngredientConv(v.getName(), v.getUnit(), v.getAmount()));
            }
        }

        return new LargeRecipeConv(
                Math.toIntExact(r.getId()),
                r.getName(),
                r.getOwner() != null ? r.getOwner().getEmail() : null,
                ingredients,
                r.getDescription(),
                safe0(r.getCaloriesKcal()),
                safe0(r.getTotalFatG()),
                safe0(r.getSaturatedFatG()),
                safe0(r.getCholesterolMg()),
                safe0(r.getSodiumMg()),
                safe0(r.getTotalCarbohydratesG()),
                safe0(r.getDietaryFiberG()),
                safe0(r.getSugarsG()),
                safe0(r.getProteinG())
        );
    }

    @Transactional
    public boolean deleteRecipeOwned(long recipeId, UserConv user) {
        if (user == null || user.getEmail() == null) return false;
        RecipeEntity r = recipeRepo.findByIdAndOwner_Email(recipeId, user.getEmail()).orElse(null);
        if (r == null) return false;
        recipeRepo.delete(r);
        return true;
    }

    @Transactional
    public boolean setRecipeShared(long recipeId, UserConv user, boolean shared) {
        if (user == null || user.getEmail() == null) return false;
        RecipeEntity r = recipeRepo.findByIdAndOwner_Email(recipeId, user.getEmail()).orElse(null);
        if (r == null) return false;
        r.setShared(shared);
        recipeRepo.save(r);
        return true;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> listSharedRecipes() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (RecipeEntity r : recipeRepo.findBySharedTrueOrderByIdAsc()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", r.getId());
            row.put("name", r.getName());
            // owner is useful for UI/debugging; you can remove later
            row.put("owner", r.getOwner() != null ? r.getOwner().getEmail() : null);
            out.add(row);
        }
        return out;
    }

    @Transactional(readOnly = true)
    public RecipeConv readSharedRecipeById(long recipeId) {
        RecipeEntity r = recipeRepo.findByIdAndSharedTrue(recipeId).orElse(null);
        if (r == null) return null;

        List<IngredientConv> ingredients = new ArrayList<>();
        if (r.getIngredients() != null) {
            for (IngredientValue v : r.getIngredients()) {
                ingredients.add(new IngredientConv(v.getName(), v.getUnit(), v.getAmount()));
            }
        }
        return new RecipeConv(Math.toIntExact(r.getId()), r.getName(), ingredients, r.getDescription());
    }


    @Transactional(readOnly = true)
    public LargeRecipeConv readSharedRecipeDetailById(long recipeId) {
        RecipeEntity r = recipeRepo.findByIdAndSharedTrue(recipeId).orElse(null);
        if (r == null) return null;

        List<IngredientConv> ingredients = new ArrayList<>();
        if (r.getIngredients() != null) {
            for (IngredientValue v : r.getIngredients()) {
                ingredients.add(new IngredientConv(v.getName(), v.getUnit(), v.getAmount()));
            }
        }

        return new LargeRecipeConv(
                Math.toIntExact(r.getId()),
                r.getName(),
                r.getOwner() != null ? r.getOwner().getEmail() : null,
                ingredients,
                r.getDescription(),
                safe0(r.getCaloriesKcal()),
                safe0(r.getTotalFatG()),
                safe0(r.getSaturatedFatG()),
                safe0(r.getCholesterolMg()),
                safe0(r.getSodiumMg()),
                safe0(r.getTotalCarbohydratesG()),
                safe0(r.getDietaryFiberG()),
                safe0(r.getSugarsG()),
                safe0(r.getProteinG())
        );
    }

    @Transactional
    public long deleteRecipesByUserEmail(String email) {
        if (email == null || email.isBlank()) return 0;
        return recipeRepo.deleteByOwner_Email(email);
    }

    @Transactional(readOnly = true)
    public List<String> readRecipeNamesByIds(List<Integer> recipeIds) {
        if (recipeIds == null || recipeIds.isEmpty()) {
            return List.of();
        }

        // Convert to Long list, but keep duplicates + order in original list
        List<Long> ids = recipeIds.stream()
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .toList();

        // One DB call (may return in any order!)
        Map<Long, String> nameById = new HashMap<>();
        for (RecipeEntity r : recipeRepo.findAllById(ids)) {
            nameById.put(r.getId(), r.getName());
        }

        // Rebuild list in the same order as recipeIds
        List<String> names = new ArrayList<>(recipeIds.size());
        for (Integer id : recipeIds) {
            if (id == null) {
                names.add("-"); // empty slot
            } else {
                names.add(nameById.getOrDefault(id.longValue(), "Unbekanntes Rezept"));
            }
        }
        return names;
    }

    @Transactional(readOnly = true)
    public List<String> readRecipeIngredientName(int id) {
        return recipeRepo.findById((long) id)
                .map(r -> r.getIngredients().stream().map(IngredientValue::getName).toList())
                .orElse(List.of());
    }

    @Transactional(readOnly = true)
    public List<Double> readRecipeIngredientAmount(int id) {
        return recipeRepo.findById((long) id)
                .map(r -> r.getIngredients().stream().map(v -> {
                    try { return Double.parseDouble(v.getAmount()); }
                    catch (Exception ignored) { return 0.0; }
                }).toList())
                .orElse(List.of());
    }

    // ---- Unverändert: Helper für Nutrition API ----

    public String generateIngredientString(List<String> names, List<Double> amounts) {
        if (names.size() != amounts.size()) {
            throw new IllegalArgumentException("Beide Listen müssen die gleiche Länge haben.");
        }

        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"ingredients\":[");

        for (int i = 0; i < names.size(); i++) {
            jsonBuilder.append("{")
                    .append("\"name\":\"").append(names.get(i)).append("\",")
                    .append("\"amount\":").append(amounts.get(i)).append(",")
                    .append("\"unit\":\"grams\"")
                    .append("}");

            if (i < names.size() - 1) jsonBuilder.append(",");
        }

        jsonBuilder.append("],\"portions\":1}");
        return jsonBuilder.toString();
    }

    public NutritionConv sendNutritionRequest(String ingredientsJson) {
        try {
            if (nutritionApiKey == null || nutritionApiKey.isBlank()) {
                // No key configured -> skip nutrition lookup
                return null;
            }
            URL url = new URL(nutritionApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("x-rapidapi-key", nutritionApiKey);
            connection.setRequestProperty("x-rapidapi-host", nutritionApiHost);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
                outputStream.writeBytes(ingredientsJson);
                outputStream.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    NutritionAnswer nutritionData = objectMapper.readValue(response.toString(), NutritionAnswer.class);
                    return nutritionData.getNutritionalValues();
                }
            } else {
                Logger.getLogger(RecipeManager.class.getName()).log(Level.SEVERE, "Fehlerhafte API-Antwort: Code " + responseCode);
                return null;
            }
        } catch (IOException e) {
            Logger.getLogger(RecipeManager.class.getName()).log(Level.SEVERE, "Verbindungsfehler zur Nährwert-API", e);
            return null;
        }
    }


    private static double safe0(Double v) {
        return v != null ? v : 0.0;
    }

}