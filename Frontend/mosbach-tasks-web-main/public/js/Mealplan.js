// Mealplan.js
// Backend stores: day + time(meal label) + recipe id.
// Nutrition values are kept locally (Phase 3 frontend, option B).

$(document).ready(function () {
  if (!window.AUTH.requireAuth()) return;

  const API = window.API;
  const apiMealplan = `${API}/mealplan`;
  const apiCollection = `${API}/collection`;

  const NUTRI_KEY = "mealy_mealplan_nutrition_v1"; // slotKey -> {recipeId, recipeName, calories, protein, carbs, fats}

  const $mealForm = $("#mealForm");
  const $selectedDate = $("#selectedDate");
  const $mealAddForm = $("#mealAddForm");
  const $mealTime = $("#mealTime");
  const $mealSelect = $("#meal");
  const $calories = $("#calories");
  const $protein = $("#protein");
  const $carbs = $("#carbs");
  const $fats = $("#fats");

  let lastClickedDay = null;

  // ----------------------------
  // Local nutrition storage
  // ----------------------------
  function loadNutriMap() {
    try {
      return JSON.parse(localStorage.getItem(NUTRI_KEY) || "{}") || {};
    } catch {
      return {};
    }
  }

  function saveNutriMap(map) {
    try {
      localStorage.setItem(NUTRI_KEY, JSON.stringify(map || {}));
    } catch (e) {
      console.warn("Could not persist nutrition map:", e);
    }
  }

  function slotKey(day, time) {
    return `${day}||${time}`;
  }

  function getNutri(day, time) {
    const map = loadNutriMap();
    return map[slotKey(day, time)] || null;
  }

  function setNutri(day, time, nutri) {
    const map = loadNutriMap();
    map[slotKey(day, time)] = nutri;
    saveNutriMap(map);
  }

  function deleteNutri(day, time) {
    const map = loadNutriMap();
    const k = slotKey(day, time);
    if (map[k]) {
      delete map[k];
      saveNutriMap(map);
    }
  }

  function numOrZero(v) {
    if (v === null || v === undefined || v === "") return 0;
    const n = Number(String(v).replace(",", "."));
    return Number.isFinite(n) ? n : 0;
  }

  // ----------------------------
  // UI helpers
  // ----------------------------
  function resetForm() {
    $mealTime.val("");
    $mealSelect.val("");
    $calories.val("");
    $protein.val("");
    $carbs.val("");
    $fats.val("");
  }

  function buildTitle(timeLabel, recipeName, nutri) {
    const base = `${timeLabel}: ${recipeName}`;
    const kcal = nutri ? numOrZero(nutri.calories) : 0;
    return kcal > 0 ? `${base} (${kcal} kcal)` : base;
  }

  function upsertEvent(calendar, day, timeLabel, recipeName, nutri) {
    // one event per slot (day+timeLabel)
    calendar.getEvents().forEach((ev) => {
      if (ev.extendedProps?.slotDay === day && ev.extendedProps?.slotTime === timeLabel) {
        ev.remove();
      }
    });

    calendar.addEvent({
      title: buildTitle(timeLabel, recipeName, nutri),
      start: day,
      allDay: true,
      extendedProps: {
        slotDay: day,
        slotTime: timeLabel,
        recipeName: recipeName,
        recipeId: nutri?.recipeId ?? null,
        calories: nutri ? numOrZero(nutri.calories) : 0,
        protein: nutri ? numOrZero(nutri.protein) : 0,
        carbs: nutri ? numOrZero(nutri.carbs) : 0,
        fats: nutri ? numOrZero(nutri.fats) : 0,
      },
    });
  }

  // ----------------------------
  // Calendar
  // ----------------------------
  const calendarEl = document.getElementById("calendar");
  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: "dayGridMonth",
    locale: "de",
    headerToolbar: {
      left: "prev,next today",
      center: "title",
      right: "dayGridMonth,timeGridWeek,timeGridDay",
    },
    selectable: true,
    editable: false,
    events: [],

    dateClick: function (info) {
      lastClickedDay = info.dateStr;
      $selectedDate.text(info.dateStr);
      $mealForm.show();
    },

    eventClick: function (info) {
      const day = info.event.extendedProps?.slotDay;
      const timeLabel = info.event.extendedProps?.slotTime;

      if (!day || !timeLabel) return;

      const details = [
        `Datum: ${day}`,
        `Mahlzeit: ${timeLabel}`,
        `Rezept: ${info.event.extendedProps?.recipeName || info.event.title}`,
        `Kalorien: ${info.event.extendedProps?.calories || 0}`,
        `Protein: ${info.event.extendedProps?.protein || 0} g`,
        `Kohlenhydrate: ${info.event.extendedProps?.carbs || 0} g`,
        `Fette: ${info.event.extendedProps?.fats || 0} g`,
        "\nLöschen?",
      ].join("\n");

      if (!confirm(details)) return;

      // Optimistic UI
      info.event.remove();
      deleteNutri(day, timeLabel);

      $.ajax({
        url: apiMealplan,
        type: "DELETE",
        headers: window.AUTH.authHeaders({ "Content-Type": "application/json" }),
        data: JSON.stringify({ day: day, time: timeLabel }),
        contentType: "application/json",
        success: function () {
          // ok
        },
        error: function (xhr) {
          console.error("DELETE /mealplan failed", xhr.status, xhr.responseText);
          const msg = xhr.responseJSON?.reason || xhr.responseText || "Unbekannter Fehler";
          alert("Fehler beim Entfernen der Mahlzeit: " + msg);
          // Reload to recover
          calendar.removeAllEvents();
          loadMealsFromDatabase();
        },
      });
    },
  });

  calendar.render();

  // ----------------------------
  // Load recipe dropdown
  // ----------------------------
  function loadRecipeDropdown() {
    $.ajax({
      url: apiCollection,
      type: "GET",
      headers: window.AUTH.authHeaders(),
      success: function (response) {
        // Response is a map: { "1": "Pasta", "2": "Salat" }
        const entries = Object.keys(response || {}).map((id) => ({
          id: String(id),
          name: response[id],
        }));

        entries.sort((a, b) => (a.name || "").localeCompare(b.name || ""));

        // Reset select
        $mealSelect.empty();
        $mealSelect.append('<option value="" disabled selected>Wähle ein Rezept</option>');

        entries.forEach((e) => {
          $mealSelect.append(
            `<option value="${escapeHtml(e.id)}">${escapeHtml(e.name)}</option>`
          );
        });
      },
      error: function (xhr) {
        console.error("GET /collection failed", xhr.status, xhr.responseText);
        alert("Rezepte konnten nicht geladen werden.");
      },
    });
  }

  // ----------------------------
  // Submit: add/update meal slot
  // ----------------------------
  $mealAddForm.on("submit", function (e) {
    e.preventDefault();

    const day = lastClickedDay;
    if (!day) {
      alert("Bitte zuerst ein Datum im Kalender anklicken.");
      return;
    }

    const timeLabel = ($mealTime.val() || "").trim();
    const recipeId = $mealSelect.val();
    const recipeName = $mealSelect.find("option:selected").text() || "";

    if (!timeLabel) {
      alert("Bitte eine Mahlzeit-Zeit angeben (z.B. Frühstück)." );
      return;
    }
    if (!recipeId) {
      alert("Bitte ein Rezept auswählen.");
      return;
    }

    const nutri = {
      recipeId: String(recipeId),
      recipeName: recipeName,
      calories: numOrZero($calories.val()),
      protein: numOrZero($protein.val()),
      carbs: numOrZero($carbs.val()),
      fats: numOrZero($fats.val()),
    };

    // Save backend (upsert)
    $.ajax({
      url: apiMealplan,
      type: "POST",
      headers: window.AUTH.authHeaders({ "Content-Type": "application/json" }),
      data: JSON.stringify({ day: day, time: timeLabel, id: String(recipeId) }),
      contentType: "application/json",
      success: function () {
        // Store nutrition locally (option B)
        setNutri(day, timeLabel, nutri);

        // Upsert calendar event
        upsertEvent(calendar, day, timeLabel, recipeName, nutri);

        // Done
        $mealForm.hide();
        resetForm();
      },
      error: function (xhr) {
        console.error("POST /mealplan failed", xhr.status, xhr.responseText);
        const msg = xhr.responseJSON?.reason || xhr.responseText || "Unbekannter Fehler";
        alert("Fehler beim Hinzufügen der Mahlzeit: " + msg);
      },
    });
  });

  // ----------------------------
  // Load meals from backend
  // ----------------------------
  function loadMealsFromDatabase() {
    $.ajax({
      url: apiMealplan,
      type: "GET",
      headers: window.AUTH.authHeaders(),
      success: function (response) {
        // Backend currently returns a JSON string (MealPlanConverter). Handle both.
        if (typeof response === "string") {
          try {
            response = JSON.parse(response);
          } catch (e) {
            console.error("Invalid JSON from /mealplan:", e, response);
            alert("Fehler: Antwort vom Server konnte nicht verarbeitet werden.");
            return;
          }
        }

        const meals = response?.meals;
        if (!Array.isArray(meals)) {
          console.error("Unexpected /mealplan response:", response);
          return;
        }

        meals.forEach((m) => {
          const day = m.day;
          const timeLabel = m.time;
          const recipeName = m.name;

          if (!day || !timeLabel) return;

          // overlay local nutrition (option B)
          const localNutri = getNutri(day, timeLabel);
          const nutri = localNutri || {
            recipeId: null,
            recipeName: recipeName,
            calories: numOrZero(m.calories),
            protein: numOrZero(m.protein),
            carbs: numOrZero(m.carbs),
            fats: numOrZero(m.fats),
          };

          upsertEvent(calendar, day, timeLabel, recipeName, nutri);
        });
      },
      error: function (xhr) {
        console.error("GET /mealplan failed", xhr.status, xhr.responseText);
        const msg = xhr.responseJSON?.reason || xhr.responseText || "Unbekannter Fehler";
        alert("Fehler beim Laden der Mahlzeiten: " + msg);
      },
    });
  }

  // ----------------------------
  // Helpers
  // ----------------------------
  function escapeHtml(str) {
    if (str === null || str === undefined) return "";
    return String(str)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/\"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }

  // init
  loadRecipeDropdown();
  loadMealsFromDatabase();
});
