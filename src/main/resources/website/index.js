function getInputValue(event) {
    if (event.keyCode != 13) {
        return;
    }
    event.preventDefault();
    scansion();
}

function getToggleClick(event) {
    let input = document.getElementById("button").value;
    if (input == '↓') {
        document.getElementById("button").value = '-';
    } else if (input == '-') {
        document.getElementById("button").value = '↑';
    } else {
        document.getElementById("button").value = '↓';
    }
    scansion();
}

function getOrder() {
    let input = document.getElementById("button").value;
    if (input == '↑') {
        return "ascending";
    } else if (input == '-') {
        return "normal";
    } else {
        return "descending";
    }
}

function scansion() {
    let input = document.getElementById("input").value;
    // alert(inputValue);

    let ruleset = document.getElementById("myInput").value;

    let response = readData("/api/scansion", { "text": input, "ruleset": ruleset, "order": getOrder() });
        response.then(result => {
            console.log(result);
            display(result);
        })
        .catch(function (error) {
            console.log(error);
            display({});
        })
}

function display(result) {
    let div = document.getElementById("output");
    div.innerHTML = '';
    for (var index in result?.results) {
         var oneElement = result?.results?.[index];
         if (!(oneElement?.text)) {
             var paragraph = document.createElement('br');
             div.appendChild(paragraph);
             continue;
         }
         if (oneElement?.percent) {
             var percentDiv = document.createElement('div');
             percentDiv.setAttribute('class', 'percent');
             percentDiv.innerHTML = oneElement?.percent + '%';
             div.appendChild(percentDiv);
         }
         var textDiv = document.createElement('div');
         textDiv.setAttribute('class', 'text');
         for (var textIndex in result?.results?.[index]?.text) {
            if (result?.results?.[index]?.text?.[textIndex]?.over) {
                textDiv.innerHTML += "<inlineText class=\"above\">" + result?.results?.[index]?.text?.[textIndex]?.over + "</inlineText>";
            }
            if (result?.results?.[index]?.text?.[textIndex]?.under) {
                textDiv.innerHTML += "<inlineText class=\"below\">\u23DD</inlineText>";
            }
            if (result?.results?.[index]?.text?.[textIndex]?.removed) {
                textDiv.innerHTML += "<inlineText class=\"removed\">" + result?.results?.[index]?.text?.[textIndex]?.char + "</inlineText>";
            } else {
                textDiv.innerHTML += result?.results?.[index]?.text?.[textIndex]?.char;
            }
         }
         div.appendChild(textDiv);
    }
}
