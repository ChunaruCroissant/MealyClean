// SharedRecipes.js
// Loads shared recipes from backend public endpoint and keeps ratings locally.

$(document).ready(function () {
  const IMAGE_STORAGE_KEY = "mealy_recipe_images";
  const RATING_STORAGE_KEY = "mealy_recipe_ratings";

  const API_LIST = `${window.API}/shared-recipes`;

  loadSharedRecipes();

  // ==========================================
  // 1. Load + render shared recipes
  // ==========================================
  function loadSharedRecipes() {
    const $list = $("#shared-list");
    $list.empty();

    $.ajax({
      url: API_LIST,
      type: "GET",
      success: function (sharedList) {
        if (!Array.isArray(sharedList) || sharedList.length === 0) {
          $list.html(
            '<p class="empty-state">Es wurden noch keine Rezepte mit der Community geteilt.</p>'
          );
          return;
        }

        const imagesMap = getFromStorage(IMAGE_STORAGE_KEY) || {};
        const allRatings = getFromStorage(RATING_STORAGE_KEY) || {};

        sharedList.sort((a, b) => (a?.name || "").localeCompare(b?.name || ""));

        sharedList.forEach((recipe) => {
          const rid = recipe?.id;
          const rname = recipe?.name || "";

          // Image is stored locally (usually by recipe name). Fallback: by id.
          const imageDataUrl = imagesMap[rname] || imagesMap[String(rid)] || null;
          const imageHtml = imageDataUrl
            ? `<img src="${imageDataUrl}" alt="${escapeHtml(rname)}" class="recipe-image">`
            : `<div class="recipe-image" style="background:#f1f5f9; display:flex; align-items:center; justify-content:center; color:#94a3b8; font-size:0.9rem;">Kein Bild</div>`;

          const recipeRatings = allRatings[String(rid)] || [];
          const avgRating = calculateAverage(recipeRatings);
          const countRating = recipeRatings.length;

          const ownerMeta = recipe?.owner
            ? `<p class="recipe-meta">von ${escapeHtml(String(recipe.owner))}</p>`
            : "";

          const itemHtml = `
            <div class="recipe-item clickable" data-id="${escapeHtml(String(rid))}">
              ${imageHtml}
              <div class="recipe-info">
                <h2>${escapeHtml(rname)}</h2>
                ${ownerMeta}

                <div class="recipe-rating-footer">
                  <div class="rating-display">
                    <span>★</span> ${avgRating}
                    <span class="rating-count">(${countRating})</span>
                  </div>

                  <button class="rate-btn" data-id="${escapeHtml(
                    String(rid)
                  )}" data-name="${escapeHtml(rname)}">
                    Bewerten
                  </button>
                </div>
              </div>
            </div>
          `;

          $list.append(itemHtml);
        });
      },
      error: function (xhr) {
        console.error("Fehler beim Laden der Community-Rezepte:", xhr.status, xhr.responseText);
        $list.html(
          '<p class="empty-state">Fehler beim Laden der Community-Rezepte. Bitte später erneut versuchen.</p>'
        );
      },
    });
  }

  // ==========================================
  // 1b) Click recipe card -> detail page
  // ==========================================
  $(document).on("click", ".recipe-item.clickable", function (e) {
    if ($(e.target).closest(".rate-btn").length) return; // don't navigate when rating

    const id = $(this).data("id");
    if (!id) return;
    window.location.href = `SharedRecipeDetail.html?id=${encodeURIComponent(id)}`;
  });

  // ==========================================
  // 2) Modal control
  // ==========================================
  $(document).on("click", ".rate-btn", function (e) {
    e.stopPropagation();

    const id = $(this).data("id");
    const name = $(this).data("name");

    $("#ratingRecipeId").val(id);
    $("#ratingRecipeName").val(name);

    $("#modalRecipeTitle").text(name + " bewerten");
    $("#ratingForm")[0].reset();

    $("#ratingModal").fadeIn(200).css("display", "flex");
  });

  $(".close-modal").click(function () {
    $("#ratingModal").fadeOut(200);
  });

  $(window).click(function (event) {
    if (event.target.id === "ratingModal") {
      $("#ratingModal").fadeOut(200);
    }
  });

  // ==========================================
  // 3) Submit rating (local)
  // ==========================================
  $("#ratingForm").on("submit", function (e) {
    e.preventDefault();

    const recipeId = $("#ratingRecipeId").val();
    const stars = $('input[name="rating"]:checked').val();
    const comment = $("#ratingComment").val();

    if (!stars) {
      alert("Bitte wähle mindestens einen Stern aus.");
      return;
    }

    let allRatings = getFromStorage(RATING_STORAGE_KEY) || {};
    if (!allRatings[String(recipeId)]) allRatings[String(recipeId)] = [];

    allRatings[String(recipeId)].push({
      stars: parseInt(stars, 10),
      comment: comment,
      date: new Date().toISOString(),
    });

    localStorage.setItem(RATING_STORAGE_KEY, JSON.stringify(allRatings));

    $("#ratingModal").fadeOut(200);
    loadSharedRecipes();
  });

  // ==========================================
  // Helpers
  // ==========================================
  function getFromStorage(key) {
    try {
      const data = localStorage.getItem(key);
      return data ? JSON.parse(data) : null;
    } catch (e) {
      console.error("Fehler beim LocalStorage Zugriff:", e);
      return null;
    }
  }

  function calculateAverage(ratingsArray) {
    if (!ratingsArray || ratingsArray.length === 0) return "0.0";
    const sum = ratingsArray.reduce((acc, curr) => acc + (curr.stars || 0), 0);
    return (sum / ratingsArray.length).toFixed(1);
  }

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
