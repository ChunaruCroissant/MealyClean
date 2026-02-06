$(function () {
  // --- Guards ---
  if (!window.API_BASE) {
    console.error("window.API_BASE ist nicht gesetzt. Fehlt config.js im HTML?");
    alert("Konfigurationsfehler: API_BASE fehlt (config.js nicht geladen).");
    return;
  }

  const API = window.API;
  const DETAIL_URL = `${API}/recipe/detail`;
  const IMG_KEY = "mealy_recipe_images";
  const numOrNull = (v) => {
    if (v === null || v === undefined || v === "") return null;
    const n = Number(String(v).replace(",", "."));
    return Number.isFinite(n) ? n : null;
  };


  const id = new URLSearchParams(location.search).get("id");
  if (!id) {
    alert("Rezept-ID nicht gefunden.");
    location.href = "RecipeCollection.html";
    return;
  }

  if (!window.AUTH.requireAuth()) {
    return;
  }

  const loadImages = () => {
    try {
      return JSON.parse(localStorage.getItem(IMG_KEY) || "{}") || {};
    } catch (e) {
      console.warn("Konnte Bild-Map nicht lesen:", e);
      return {};
    }
  };

  const removeImageByName = (recipeName) => {
    if (!recipeName) return;
    const map = loadImages();
    if (!map[recipeName]) return;
    delete map[recipeName];
    try {
      localStorage.setItem(IMG_KEY, JSON.stringify(map));
    } catch (e) {
      console.warn("Konnte Bild-Map nicht schreiben:", e);
    }
  };

  let currentRecipeName = null;

  const showRecipe = (r) => {
    currentRecipeName = r?.name || null;

    $("#recipe-name").text(r?.name || "");
    $("#recipe-description").text(r?.description || "");

    // Nährwerte anzeigen (falls vorhanden)
    const $nutri = $("#nutrition-container");
    if ($nutri.length) {
      const cal = numOrNull(r?.["Calories_(kcal)"] ?? r?.calories ?? r?.caloriesKcal);
      const prot = numOrNull(r?.["Protein_(g)"] ?? r?.protein);
      const carb = numOrNull(r?.["Total_Carbohydrates_(g)"] ?? r?.carbs);
      const fat = numOrNull(r?.["Total_Fat_(g)"] ?? r?.fats ?? r?.fat);

      const hasAny = [cal, prot, carb, fat].some((x) => x !== null && x > 0);
      if (hasAny) {
        $("#nutri-cal").text(cal ?? "-");
        $("#nutri-prot").text(prot ?? "-");
        $("#nutri-carb").text(carb ?? "-");
        $("#nutri-fat").text(fat ?? "-");
        $nutri.show();
      } else {
        $nutri.hide();
      }
    }

    // Bild anzeigen (nur wenn <img id="recipe-image"> existiert)
    const $img = $("#recipe-image");
    if ($img.length) {
      const map = loadImages();

      // Primär: nach Name (Recipe.js speichert unter Name)
      // Fallbacks: nach ID (falls du später umstellst)
      const imgData =
        (r && map[String(r.id)]) ||
        map[String(id)] ||
        (r && map[r.name]) ||
        null;

      if (imgData) {
        $img.attr({ src: imgData, alt: r?.name || "Rezeptbild" }).show();
      } else {
        $img.hide();
      }
    }

    $("#ingredient-list").empty();
    (r?.ingredients || []).forEach((i) => {
      $("#ingredient-list").append(
        `<li>${i.name} (${i.amount} ${i.unit})</li>`
      );
    });
  };

  // --- GET Recipe ---
  $.ajax({
    url: `${DETAIL_URL}/${id}`,
    type: "GET",
    headers: window.AUTH.authHeaders(),
    success: showRecipe,
    error: (xhr) => {
      console.error("GET Fehler:", xhr.status, xhr.responseText);
      alert("Fehler beim Abrufen des Rezepts.");
      location.href = "RecipeCollection.html";
    },
  });

  // --- DELETE Recipe ---
  $("#delete-recipe-btn").on("click", (e) => {
    e.preventDefault();

    if (!confirm("Möchten Sie dieses Rezept wirklich löschen?")) return;

    $.ajax({
      url: `${DETAIL_URL}/${id}`,
      type: "DELETE",
      headers: window.AUTH.authHeaders(),
      success: () => {
        // Optional: lokales Bild entfernen
        removeImageByName(currentRecipeName);

        alert("Rezept gelöscht!");
        location.href = "RecipeCollection.html";
      },
      error: (xhr) => {
        console.error("DELETE Fehler:", xhr.status, xhr.responseText);
        alert(`Löschen fehlgeschlagen (${xhr.status}).`);
      },
    });
  });
});
