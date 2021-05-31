function getInputValue(event) {
    if (event.keyCode != 13) {
        return;
    }
    event.preventDefault();
    let inputValue = document.getElementsByTagName("input")[0].value;
    alert(inputValue);
}

function skandieren(input) {
    if (!(typeof(input) === "string")) {
        return;
    }
    console.log("Hello World 2")
    async () => {
        const response = await fetch('<API>', {
            method: 'POST',
            body: myBody, // string or object
            headers: {
                'Content-Type': 'application/json'
            }
        });
        const myJson = await response.json(); //extract JSON from the http response
        // do something with myJson
    }
}