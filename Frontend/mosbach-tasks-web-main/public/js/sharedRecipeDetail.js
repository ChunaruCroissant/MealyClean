// sharedRecipeDetail.js
// Loads one shared recipe from backend public endpoint. Image (if any) is read from localStorage.

$(function () {
  const IMG_KEY = "mealy_recipe_images";

  const id = new URLSearchParams(location.search).get("id");
  if (!id) {
    alert("Rezept-ID nicht gefunden.");
    location.href = "SharedRecipes.html";
    return;
  }

  const loadImages = () => {
    try {
      return JSON.parse(localStorage.getItem(IMG_KEY) || "{}");
    } catch {
      return {};
    }
  };

  const renderRecipe = (r) => {
    $("#recipe-name").text(r?.name || "");
    $("#recipe-description").text(r?.description || "");

    const $img = $("#recipe-image");
    if ($img.length) {
      const images = loadImages();
      const img = (r && images[r.name]) || images[String(r?.id)] || images[String(id)] || null;
      if (img) {
        $img.attr({ src: img, alt: r?.name || "Rezeptbild" }).show();
      } else {
        $img.hide();
      }
    }

    $("#ingredient-list").empty();
    (r?.ingredients || []).forEach((i) => {
      $("#ingredient-list").append(
        `<li>${escapeHtml(i.name)} (${escapeHtml(i.amount)} ${escapeHtml(i.unit)})</li>`
      );
    });
  };

  $.ajax({
    url: `${window.API}/shared-recipes/${encodeURIComponent(id)}`,
    type: "GET",
    success: (data) => renderRecipe(data),
    error: (xhr) => {
      console.error("GET Fehler:", xhr.status, xhr.responseText);
      alert(`Rezept nicht gefunden (${xhr.status}).`);
      location.href = "SharedRecipes.html";
    },
  });

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }
});
