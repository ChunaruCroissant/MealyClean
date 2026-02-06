// sharedRecipeDetail.js
// Loads one shared recipe from backend public endpoint. Image (if any) is read from localStorage.
// Also loads backend-persisted ratings and allows submitting a rating (requires login).

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

    // rating modal title uses recipe name
    $("#modalRecipeTitle").text((r?.name || "") + " bewerten");
  };

  const loadRatings = () => {
    $.ajax({
      url: `${window.API}/shared-recipes/${encodeURIComponent(id)}/ratings`,
      type: "GET",
      success: (data) => renderRatings(data),
      error: (xhr) => {
        console.error("Ratings GET Fehler:", xhr.status, xhr.responseText);
        $("#avgRating").text("0.0");
        $("#ratingCount").text("(0)");
        $("#ratingList").html('<p class="empty-state">Bewertungen konnten nicht geladen werden.</p>');
      },
    });
  };

  const renderRatings = (data) => {
    const avg = Number(data?.avgRating || 0).toFixed(1);
    const cnt = Number(data?.ratingCount || 0);

    $("#avgRating").text(avg);
    $("#ratingCount").text(`(${cnt})`);

    const ratings = Array.isArray(data?.ratings) ? data.ratings : [];
    const $list = $("#ratingList");
    $list.empty();

    if (ratings.length === 0) {
      $list.html('<p class="empty-state">Noch keine Bewertungen vorhanden.</p>');
      return;
    }

    ratings.forEach((r) => {
      const stars = Number(r?.stars || 0);
      const user = r?.userName ? escapeHtml(String(r.userName)) : "Anonym";
      const comment = r?.comment ? escapeHtml(String(r.comment)) : "";
      const date = r?.date ? new Date(r.date).toLocaleString() : "";

      $list.append(`
        <div class="rating-item">
          <div class="rating-item-head">
            <div class="rating-stars">${"★".repeat(stars)}${"☆".repeat(Math.max(0, 5 - stars))}</div>
            <div class="rating-meta">${user}${date ? " · " + escapeHtml(date) : ""}</div>
          </div>
          ${comment ? `<div class="rating-comment">${comment}</div>` : ""}
        </div>
      `);
    });
  };

  // Load recipe
  $.ajax({
    url: `${window.API}/shared-recipes/${encodeURIComponent(id)}`,
    type: "GET",
    success: (data) => renderRecipe(data),
    error: (xhr) => {
      console.error("GET Fehler:", xhr.status, xhr.responseText);
      alert(`Rezept nicht gefunden (${xhr.status}).`);
      location.href = "SharedRecipes.html";
    },
    complete: () => loadRatings(),
  });

  // Modal open/close
  $("#openRatingModal").on("click", function () {
    if (!window.AUTH.requireAuth()) return;
    $("#ratingRecipeId").val(id);
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

  // Submit rating
  $("#ratingForm").on("submit", function (e) {
    e.preventDefault();
    if (!window.AUTH.requireAuth()) return;

    const stars = $('input[name="rating"]:checked').val();
    const comment = $("#ratingComment").val();

    if (!stars) {
      alert("Bitte wähle mindestens einen Stern aus.");
      return;
    }

    $.ajax({
      url: `${window.API}/shared-recipes/${encodeURIComponent(id)}/ratings`,
      type: "POST",
      headers: window.AUTH.authHeaders(),
      contentType: "application/json",
      data: JSON.stringify({
        stars: parseInt(stars, 10),
        comment: comment || "",
      }),
      success: function () {
        $("#ratingModal").fadeOut(200);
        loadRatings();
      },
      error: function (xhr) {
        console.error("Bewertung POST Fehler:", xhr.status, xhr.responseText);
        alert("Bewertung konnte nicht gespeichert werden.");
      },
    });
  });

  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }
});
