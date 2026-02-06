$(document).ready(function () {
  $("#submit").click(function (event) {
    event.preventDefault();

    const API = window.API;

    const email = $("#email").val()?.trim();
    const userName = $("#username").val()?.trim();
    const password = $("#password").val();
    const passwordConfirm = $("#passwordConfirm").val();

    if (!email || !userName || !password || !passwordConfirm) {
      alert("Bitte alle Felder ausfüllen.");
      return;
    }

    if (password !== passwordConfirm) {
      alert("Die Passwörter stimmen nicht überein.");
      return;
    }

    const payload = { email, userName, password };

    $.ajax({
      url: `${API}/register`,
      type: "POST",
      dataType: "json",
      contentType: "application/json; charset=utf-8",
      data: JSON.stringify(payload),
      success: function (response) {
        if (response?.message === "Account successfully registered") {
          window.location.href = "RegistBestaetigung.html";
          return;
        }
        alert(response?.reason || "Registrierung fehlgeschlagen.");
      },
      error: function (xhr) {
        const msg = xhr.responseJSON?.reason || xhr.responseText || "Unbekannter Fehler";
        alert("Registrierung fehlgeschlagen: " + msg);
      },
    });
  });
});
