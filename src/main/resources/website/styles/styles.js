function swapStyleSheet(sheet) {
    document.getElementById("stylesheet").setAttribute("href", 'styles/' + sheet);
}

// Styles
function defaultLightStyle() {
    swapStyleSheet("light/default.css");
}

function defaultDarkStyle() {
    swapStyleSheet("dark/default.css");
}