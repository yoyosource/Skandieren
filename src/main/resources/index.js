function getInputValue(event) {
    if (event.keyCode != 13) {
        return;
    }
    event.preventDefault();
    let inputValue = document.getElementById("input").value;
    // alert(inputValue);

    let div = document.getElementById("output");
    div.innerHTML = '';
    let result = skandieren(inputValue);

    for (var index in result?.results) {
        var oneElement = result?.results?.[index];
        if (!(oneElement?.text)) {
            var paragraph = document.createElement('br');
            div.appendChild(paragraph);
            continue;
        }
        console.log(oneElement)
        if (oneElement?.percent) {
            var percentDiv = document.createElement('div');
            percentDiv.setAttribute('class', 'percent');
            percentDiv.innerHTML = oneElement?.percent + '%';
            div.appendChild(percentDiv);
        }
        var textDiv = document.createElement('div');
        textDiv.setAttribute('class', 'text');
        textDiv.innerHTML = oneElement?.text;
        div.appendChild(textDiv);
    }
}

function skandieren(input) {
    if (!(typeof(input) === "string")) {
        return;
    }
    /*async () => {
        const response = await fetch('<API>', {
            method: 'POST',
            body: myBody, // string or object
            headers: {
                'Content-Type': 'application/json'
            }
        });
        const myJson = await response.json(); //extract JSON from the http response
        // do something with myJson
    }*/
    let results = [];
    let result = {
        "results": results
    };
    for (var i = 0; i < Math.floor((Math.random() * 40) + 1); i++) {
        let number = Math.floor(Math.random() * 1000) / 10.0;
        let random = Math.random();
        if (random > 0.7) {
            results.push({
                "text": input,
                "percent": number
            });
        } else if (random > 0.1) {
            results.push({
                "text": input,
            });
        } else {
            results.push({});
        }
    }
    console.log(result);
    return result;
}