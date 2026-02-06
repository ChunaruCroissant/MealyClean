// Mealplan.js
// Backend stores: day (YYYY-MM-DD) + time (HH:mm) + recipe id.
// GET /mealplan returns { meals: [{ name, day, time, calories, protein, carbs, fats }] } (as JSON string).

$(document).ready(function () {
  if (!window.AUTH.requireAuth()) return;

  const API = window.API;
  const apiMealplan = `${API}/mealplan`;
  const apiCollection = `${API}/collection`;

  const $mealForm = $("#mealForm");
  const $mealAddForm = $("#mealAddForm");
  const $mealDate = $("#mealDate");
  const $mealTime = $("#mealTime");
  const $mealSelect = $("#meal");

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

  function numOrZero(v) {
    if (v === null || v === undefined || v === "") return 0;
    const n = Number(String(v).replace(",", "."));
    return Number.isFinite(n) ? n : 0;
  }

  function buildTitle(time, recipeName, calories) {
    const base = `${time}: ${recipeName}`;
    const kcal = numOrZero(calories);
    return kcal > 0 ? `${base} (${kcal} kcal)` : base;
  }

  function isoStart(day, time) {
    // expects day=YYYY-MM-DD, time=HH:mm
    if (!day || !time) return null;
    return `${day}T${time}:00`;
  }

  function addMinutesToIso(iso, minutes) {
    try {
      const d = new Date(iso);
      if (isNaN(d.getTime())) return null;
      d.setMinutes(d.getMinutes() + minutes);
      return d.toISOString();
    } catch {
      return null;
    }
  }

  function upsertEvent(calendar, meal) {
    const day = meal.day;
    const time = meal.time;
    const recipeName = meal.name || "";
    const startIso = isoStart(day, time);
    if (!startIso) return;

    // one event per slot (day+time)
    calendar.getEvents().forEach((ev) => {
      if (ev.extendedProps?.slotDay === day && ev.extendedProps?.slotTime === time) {
        ev.remove();
      }
    });

    const endIso = addMinutesToIso(startIso, 30); // visual duration

    calendar.addEvent({
      title: buildTitle(time, recipeName, meal.calories),
      start: startIso,
      end: endIso || undefined,
      allDay: false,
      extendedProps: {
        slotDay: day,
        slotTime: time,
        recipeName: recipeName,
        calories: numOrZero(meal.calories),
        protein: numOrZero(meal.protein),
        carbs: numOrZero(meal.carbs),
        fats: numOrZero(meal.fats),
      },
    });
  }

  function resetForm() {
    $mealTime.val("");
    $mealSelect.val("");
    // keep date, because user may add multiple slots for same day
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
      $mealDate.val(info.dateStr);
      if (!$mealTime.val()) $mealTime.val("12:00");
      $mealForm.show();
    },

    eventClick: function (info) {
      const day = info.event.extendedProps?.slotDay;
      const time = info.event.extendedProps?.slotTime;
      const recipeName = info.event.extendedProps?.recipeName || info.event.title;

      if (!day || !time) return;

      const details = [
        `Datum: ${day}`,
        `Uhrzeit: ${time}`,
        `Rezept: ${recipeName}`,
        `Kalorien: ${info.event.extendedProps?.calories || 0}`,
        `Protein: ${info.event.extendedProps?.protein || 0} g`,
        `Kohlenhydrate: ${info.event.extendedProps?.carbs || 0} g`,
        `Fette: ${info.event.extendedProps?.fats || 0} g`,
        "\nLöschen?",
      ].join("\n");

      if (!confirm(details)) return;

      // Optimistic UI
      info.event.remove();

      $.ajax({
        url: apiMealplan,
        type: "DELETE",
        headers: window.AUTH.authHeaders({ "Content-Type": "application/json" }),
        data: JSON.stringify({ day: day, time: time }),
        contentType: "application/json",
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

    const day = ($mealDate.val() || "").trim();
    const time = ($mealTime.val() || "").trim();
    const recipeId = $mealSelect.val();
    const recipeName = $mealSelect.find("option:selected").text() || "";

    if (!day) {
      alert("Bitte ein Datum auswählen.");
      return;
    }
    if (!time) {
      alert("Bitte eine Uhrzeit angeben.");
      return;
    }
    if (!recipeId) {
      alert("Bitte ein Rezept auswählen.");
      return;
    }

    $.ajax({
      url: apiMealplan,
      type: "POST",
      headers: window.AUTH.authHeaders({ "Content-Type": "application/json" }),
      data: JSON.stringify({ day: day, time: time, id: String(recipeId) }),
      contentType: "application/json",
      success: function () {
        upsertEvent(calendar, { day, time, name: recipeName });
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
        // Backend returns a JSON string. Handle both.
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

        meals.forEach((m) => upsertEvent(calendar, m));
      },
      error: function (xhr) {
        console.error("GET /mealplan failed", xhr.status, xhr.responseText);
        const msg = xhr.responseJSON?.reason || xhr.responseText || "Unbekannter Fehler";
        alert("Fehler beim Laden der Mahlzeiten: " + msg);
      },
    });
  }

  // init
  loadRecipeDropdown();
  loadMealsFromDatabase();
});
