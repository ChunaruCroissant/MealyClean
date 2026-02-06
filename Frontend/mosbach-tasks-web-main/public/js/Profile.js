$(document).ready(function() {

    const API = window.API;

    if (!window.AUTH.requireAuth()) {
        return;
    }

    const token = window.AUTH.getToken();
    console.log("Token erfolgreich abgerufen:", token);


    function getProfileData() {
        $.ajax({
            url: `${API}/user`,
            type: 'GET',
            headers: {
                'Content-Type': 'application/json',
                ...window.AUTH.authHeaders()
            },
            success: function(data) {
            console.log("Erhaltene Profildaten:", data);

                 if (data.userName || data.username) {
                               $('#userName').val(data.userName || data.username);
                           } else {
                               alert('Fehler: Benutzername nicht gefunden.');
                           }

                           if (data.email) {
                               $('#useremail').val(data.email);
                           } else {
                               alert('Fehler: E-Mail nicht gefunden.');
                           }
                       },
            error: function(xhr) {
                handleError(xhr);
            }
        });
    }

    window.saveProfile = function() {
        const userName = $('#userName').val();
        const useremail = $('#useremail').val();
        const newPassword = $('#newPassword').val();
        const confirmPassword = $('#confirmPassword').val();

        if (newPassword && newPassword !== confirmPassword) {
            alert('Die Passwörter stimmen nicht überein.');
            return;
        }

        const fieldsToUpdate = {
            userName: userName,
            email: useremail,
            password: newPassword ? newPassword : undefined
        };

        $.ajax({
            url: `${API}/user`,
            type: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                ...window.AUTH.authHeaders()
            },
            data: JSON.stringify(fieldsToUpdate),
            success: function(result) {
                // Phase 3: Backend may return a new token (e.g. after email change)
                if (result && result.token) {
                    window.AUTH.setToken(result.token);
                }
                if (result.message === "Account details successfully changed") {
                    alert('Profil erfolgreich aktualisiert!');
                } else {
                    alert(result.reason || 'Fehler beim Speichern des Profils');
                }
            },
            error: function(xhr) {
                handleError(xhr);
            }
        });
    };

    $("#deleteAccount").click(function(event) {
        event.preventDefault();

        if (confirm('Möchtest du wirklich deinen Account löschen?')) {
            $.ajax({
                url: `${API}/user`,
                type: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                    ...window.AUTH.authHeaders()
                },
                success: function(result) {
                    if (result.message === "Account successfully deleted") {
                        alert('Account erfolgreich gelöscht.');
                        window.AUTH.clearToken();
                        window.location.href = "Login.html";
                    } else {
                        alert(result.reason || 'Fehler beim Löschen des Accounts');
                    }
                },
                error: function(xhr) {
                    handleError(xhr);
                }
            });
        }
    });

    $("#logOut").click(function(event) {
        event.preventDefault();
        alert('Erfolgreich abgemeldet!');
        window.AUTH.clearToken();
        window.location.href = "Login.html";
    });

    function handleError(xhr) {
        console.log('Fehler:', xhr.status);
        console.log('Antwort:', xhr.responseText);
        if (xhr.status === 0) {
            alert('CORS Fehler: Zugriff verweigert. Überprüfe die Serverkonfiguration.');
        } else if (xhr.status === 404) {
            alert('Die angeforderte Ressource wurde nicht gefunden (404).');
        } else if (xhr.status === 403) {
            alert('Zugriff verweigert (403). Bitte überprüfe deine Berechtigungen.');
        } else {
            alert('Ein Fehler ist aufgetreten. Bitte versuche es später erneut.');
        }
    }

    getProfileData();
});
