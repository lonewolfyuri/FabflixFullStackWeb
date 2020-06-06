/*
if (window.location.protocol !== 'https:') {
    window.location.protocol = 'https:';
    window.location.port = "8443";
    //window.location.replace(`https:${location.href.substring(location.protocol.length)}`);
}
 */

function handleSearch(input) {
    let searchBoxTerms = $("#searchBox").split(" ");
    // search on text and redirect
    let target = "movieList.html?search=";
    searchBoxTerms.forEach((term) => {
        target += term;
    });
    window.location.href = target;

    return false;
}

let confirmationHeader = $("#confirmation_header");

let order = window.location.search.substring(1).split("=");

confirmationHeader.append(" * Order #" + order[1] + " was Successfully Processed! * ");

function handleCartResults(resultData) {
    console.log(resultData);
    let cartBodyElement = $("#cart_list_body");
    let totalBodyElement = $("#total_list_body");

    if(resultData["empty"]) totalBodyElement.append("<tr><th>0</th><th> $ 0.00 </th><th> $ 0.00 </th><th> $ 0.00 </th></tr>");
    else {
        let curProd;
        for (let ndx = 0; ndx < resultData["movies"].length; ndx++) {
            let curProd = resultData["movies"][ndx];
            console.log(curProd);
            let rowHTML = "<tr>";
            rowHTML += "<th><a href='moviePage.html?id=" + curProd["id"] + "'>" + curProd["title"] + "</a></th>";
            rowHTML += "<th>" + curProd["price"].toFixed(2) + "</th>";
            rowHTML += '<th>' + curProd["quantity"] + '</th>';
            rowHTML += "<th> $ " + curProd["subtotal"].toFixed(2) + " </th></tr>";

            cartBodyElement.append(rowHTML);
        }

        let rowHTML = "<tr>";
        rowHTML += "<th>" + resultData["quantity"] + "</th>";
        rowHTML += "<th> $ " + resultData["subtotal"].toFixed(2) + " </th>";
        rowHTML += "<th> $ " + resultData["tax"].toFixed(2) + " </th>";
        rowHTML += "<th> $ " + resultData["total"].toFixed(2) + " </th></tr>";
        totalBodyElement.append(rowHTML);
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/cart?complete=true",
    success: (resultData) => handleCartResults(resultData)
});


var queries = {};

function handleLookup(query, doneCallback) {
    console.log("Autocomplete Query Initiated")
    console.log("sending AJAX request to backend Java Servlet")

    // TODO: if you want to check past query results first, you can do it here

    if (queries.hasOwnProperty(query)) {
        handleLookupAjaxSuccess(queries[query], query, doneCallback, true);
    } else {
        // sending the HTTP GET request to the Java Servlet endpoint hero-suggestion
        // with the query data
        jQuery.ajax({
            "method": "GET",
            // generate the request url from the query.
            // escape the query string to avoid errors caused by special characters
            "url": "movie-suggestion?query=" + escape(query),
            "success": function (data) {
                // pass the data, query, and doneCallback function into the success handler
                handleLookupAjaxSuccess(data, query, doneCallback, false)
            },
            "error": function (errorData) {
                console.log("There was an Error when Processing Query")
                console.log(errorData)
            }
        })
    }
}

function handleLookupAjaxSuccess(data, query, doneCallback, cached) {
    if (cached) {
        console.log("Query was Cached - Results are from Cache")
    } else {
        console.log("Query was not Cached - Results are from new Query")
        queries[query] = data;
    }

    // parse the string into JSON
    var jsonData = JSON.parse(data);
    console.log(jsonData)

    // TODO: if you want to cache the result into a global variable you can do it here

    // call the callback function provided by the autocomplete library
    // add "{suggestions: jsonData}" to satisfy the library response format according to
    //   the "Response Format" section in documentation
    doneCallback( { suggestions: jsonData } );
}

function handleSelectSuggestion(suggestion) {
    // TODO: jump to the specific result page based on the selected suggestion
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"]["id"] + " from Year " + suggestion["data"]["year"]);
    window.location.href = "moviePage.html?id=" + suggestion["data"]["id"];
}

$('#autocomplete').autocomplete({
    // documentation of the lookup function can be found under the "Custom lookup function" section
    lookup: function (query, doneCallback) {
        handleLookup(query, doneCallback)
    },
    onSelect: function(suggestion) {
        handleSelectSuggestion(suggestion)
    },
    // set delay time
    deferRequestBy: 300,
    // there are some other parameters that you might want to use to satisfy all the requirements
    // TODO: add other parameters, such as minimum characters
    minChars: 3,
    classes: {
        "autocomplete-suggestion": "highlight",
    },
    formatResult: function(suggestion, currentValue) {
        console.log(suggestion);
        console.log(currentValue);
        return suggestion["value"] + "  -  ( " + suggestion["data"]["year"] + " )";
    },
    lookupLimit: 10,
});

$('#autocomplete').keypress(function(event) {
    // keyCode 13 is the enter key
    if (event.keyCode == 13) {
        // pass the value of the input box to the handler function
        handleSearch($('#autocomplete').val())
    }
})