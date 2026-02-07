$(document).ready(function() {
    const IMAGE_STORAGE_KEY = "mealy_recipe_images";
    const API = window.API;
    const apiUrl = `${API}/collection`;

    if (!window.AUTH.requireAuth()) {
        return;
    }

    fetchRecipes();

    // NOTE: Der Button im HTML nutzt aktuell onclick. Dieser Listener ist harmless,
    // aber das Element existiert evtl. nicht. Daher entfernen wir den Zwang.

    function fetchRecipes() {
        $.ajax({
            url: apiUrl,
            type: 'GET',
            headers: window.AUTH.authHeaders(),
            success: function(response) {
                console.log('Rezepte erfolgreich abgerufen:', response);

                const recipesArray = Object.keys(response).map(key => ({
                    plan_id: key,
                    name: response[key]
                }));

                if (recipesArray.length > 0) {
                    displayRecipes(recipesArray);
                } else {
                    console.warn('Keine Rezepte im Antwort-Objekt gefunden.');
                    alert('Keine Rezepte verfügbar.');
                }
            },
            error: function(xhr) {
                console.error('Fehler beim Abrufen der Rezepte:', xhr);
                alert('Ein Fehler ist aufgetreten. Bitte versuche es später erneut.');
            }
        });
    }

    function displayRecipes(recipes) {
        $('#recipe-list').empty();
        recipes.sort((a, b) => a.name.localeCompare(b.name));

        const imagesMap = getFromStorage(IMAGE_STORAGE_KEY) || {};

        recipes.forEach(recipe => {
            const rid = recipe.plan_id;
            const rname = recipe.name || "";

            // Bilder liegen lokal in localStorage (meist nach Rezeptname). Fallback: nach ID.
            const imageDataUrl = imagesMap[rname] || imagesMap[String(rid)] || null;

            // Kleine "Migration": falls wir das Bild nur unter Name haben, legen wir auch einen Key unter ID an.
            if (imageDataUrl && !imagesMap[String(rid)]) {
                imagesMap[String(rid)] = imageDataUrl;
                try {
                    localStorage.setItem(IMAGE_STORAGE_KEY, JSON.stringify(imagesMap));
                } catch (_) {
                    // ignorieren (Quota etc.)
                }
            }

            const imageHtml = imageDataUrl
                ? `<img src="${imageDataUrl}" alt="${escapeHtml(rname)}" class="recipe-image">`
                : `<div class="recipe-image recipe-image--placeholder">Kein Bild</div>`;

            const recipeItem = `
                <div class="recipe-item clickable" data-id="${escapeHtml(String(rid))}">
                    ${imageHtml}
                    <div class="recipe-info">
                        <h2>${escapeHtml(rname)}</h2>
                    </div>
                </div>
            `;
            $('#recipe-list').append(recipeItem);
        });
    }

    // Click auf Card -> Detail
    $(document).on('click', '.recipe-item.clickable', function() {
        const recipeId = $(this).data('id');
        if (!recipeId) return;
        console.log('Rezept-ID:', recipeId);
        window.location.href = `RecipeDetail.html?id=${encodeURIComponent(String(recipeId))}`;
    });

    function getFromStorage(key) {
        try {
            const data = localStorage.getItem(key);
            return data ? JSON.parse(data) : null;
        } catch (e) {
            console.error('Fehler beim LocalStorage Zugriff:', e);
            return null;
        }
    }

    function escapeHtml(str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
});
