function getInputValue(event) {
    if (event.keyCode != 13) {
        return;
    }
    event.preventDefault();
    let input = document.getElementById("input").value;
    // alert(inputValue);

    let response = readData("/api", { "text": input });
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
            if (result?.results?.[index]?.text?.[textIndex]?.removed) {
                textDiv.innerHTML += "<inlineText class=\"removed\">" + result?.results?.[index]?.text?.[textIndex]?.char + "</inlineText>";
            } else if (result?.results?.[index]?.text?.[textIndex]?.type) {
                textDiv.innerHTML += "<inlineText><inlineText class=\"above\">" + result?.results?.[index]?.text?.[textIndex]?.type + "</inlineText>" + result?.results?.[index]?.text?.[textIndex]?.char + "</inlineText>";
            } else {
                textDiv.innerHTML += result?.results?.[index]?.text?.[textIndex]?.char;
            }
         }
         div.appendChild(textDiv);
    }
}

async function readData(url = '', data = {}) {
  // Default options are marked with *
  const response = await fetch(url, {
    method: 'POST', // *GET, POST, PUT, DELETE, etc.
    mode: 'cors', // no-cors, *cors, same-origin
    cache: 'no-cache', // *default, no-cache, reload, force-cache, only-if-cached
    credentials: 'omit', // include, *same-origin, omit
    headers: {
      'Content-Type': 'application/json'
      // 'Content-Type': 'application/x-www-form-urlencoded',
    },
    redirect: 'follow', // manual, *follow, error
    referrerPolicy: 'no-referrer', // no-referrer, *no-referrer-when-downgrade, origin, origin-when-cross-origin, same-origin, strict-origin, strict-origin-when-cross-origin, unsafe-url
    body: JSON.stringify(data) // body data type must match "Content-Type" header
  });
  return response.json(); // parses JSON response into native JavaScript objects
}
