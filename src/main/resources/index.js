function getInputValue(event) {
    if (event.keyCode != 13) {
        return;
    }
    let inputValue = document.getElementsByTagName("input")[0].value;
    alert(inputValue);
}