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

function alertAdded(resultData) {
    alert(`${resultData["title"]} Added to Cart for \$${resultData["price"]}`);
}

function addToCart(movieId) {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/add?id=" + movieId,
        success: (resultData) => alertAdded(resultData)
    });
}

function getSizeValue() {
    console.log("getSizeValue");
    let queryString = window.location.search.split("&");
    for (let ndx = 0; ndx < queryString.length; ndx++) {
        let queryTerm = queryString[ndx].split("=");
        if (queryTerm[0] == "limit") return queryTerm[1];
    }
    return 25;
}

function getSortValue() {
    console.log("getSortValue");
    let queryString = window.location.search.split("&");
    for (let ndx = 0; ndx < queryString.length; ndx++) {
        let queryTerm = queryString[ndx].split("=");
        if (queryTerm[0] == "sort") return queryTerm[1];
    }
    return 0;
}

function handleLimitChange() {
    let newLoc = "movieList.html?";
    let queryString = window.location.search.substring(1).split("&");
    let select = document.getElementById("size");
    let seen = false;

    for (let ndx = 0; ndx < queryString.length; ndx++) {
        if (ndx != 0) newLoc += "&";
        let queryTerm = queryString[ndx].split("=");

        newLoc += queryTerm[0] + "=";

        if (queryTerm[0] == "limit") {
            newLoc += select.value;
            seen = true;
        } else {
            newLoc += queryTerm[1];
        }
    }

    if (!seen) newLoc += "&limit=" + select.value;

    console.log("handleLimitChange: " + newLoc);

    window.location.href = newLoc;
    return false;
}

function handleSortChange() {
    let newLoc = "movieList.html?";
    let queryString = window.location.search.substring(1).split("&");
    let select = document.getElementById("sorter");
    let seen = false;

    for (let ndx = 0; ndx < queryString.length; ndx++) {
        if (ndx != 0) newLoc += "&";
        let queryTerm = queryString[ndx].split("=");

        newLoc += queryTerm[0] + "=";

        if (queryTerm[0] == "sort") {
            seen = true;
            newLoc += select.value;
        } else {
            newLoc += queryTerm[1];
        }
    }

    if (!seen) newLoc += "&sort=" + select.value;

    console.log("handleSortChange: " + newLoc + " | sort: " + select.value);

    window.location.href = newLoc;
    return false;
}

function handlePrev() {
    let newLoc = "movieList.html?";
    let queryString = window.location.search.substring(1).split("&");
    let seen = false;

    for (let ndx = 0; ndx < queryString.length; ndx++) {
        if (ndx != 0) newLoc += "&";
        let queryTerm = queryString[ndx].split("=");

        newLoc += queryTerm[0] + "=";

        if (queryTerm[0] == "page") {
            seen = true;
            if (parseInt(queryTerm[1]) == 0) newLoc += 0;
            else newLoc += parseInt(queryTerm[1]) - 1;
        } else {
            newLoc += queryTerm[1];
        }
    }

    if (!seen) return false;

    console.log("handlePrev: " + newLoc);

    window.location.href = newLoc;
    return false;
}

function handleNext() {
    let newLoc = "movieList.html?";
    let queryString = window.location.search.substring(1).split("&");
    let seen = false;

    for (let ndx = 0; ndx < queryString.length; ndx++) {
        if (ndx != 0) newLoc += "&";
        let queryTerm = queryString[ndx].split("=");

        newLoc += queryTerm[0] + "=";

        if (queryTerm[0] == "page") {
            seen = true;
            newLoc += parseInt(queryTerm[1]) + 1;
        } else {
            newLoc += queryTerm[1];
        }
    }

    if (!seen) newLoc += "&page=1";

    console.log("handleNext: " + newLoc);

    window.location.href = newLoc;
    return false;
}

function handleMoviesResult(resultData) {
    console.log("handleMoviesResult: populating movie table from resultData");

    console.log(resultData);

    // handle resultData
    if (resultData.hasOwnProperty('redir')) {
        window.location.href = resultData["redir"];
        return false;
    }

    let movieTableBodyElement = jQuery("#movie_list_body");

    // handle page element
    let pageHeaderElement = jQuery("#page_header");
    let pageFooterElement = jQuery("#page_footer");
    let page = parseInt(resultData["page"]);
    if (page >= 0) {
        pageHeaderElement.append("Page " + (page + 1));
        pageFooterElement.append("Page " + (page + 1));
    }

    // handle sort buttons

    // handle limit buttons
    let limit = parseInt(resultData["limit"]);

    let results = 0;
    for (let ndx = 0; ndx < resultData["movies"].length; ndx++) {
        results++;
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<th>" + '<a href="moviePage.html?id=' + resultData["movies"][ndx]['movieId'] + '">' + resultData["movies"][ndx]["movieTitle"] + '</a>' + "</th>";
        rowHTML += "<th>" + resultData["movies"][ndx]["year"] + "</th>";
        rowHTML += "<th>" + resultData["movies"][ndx]["director"] + "</th>";

        let genres = resultData["movies"][ndx]["genres"].split(",");
        for (let ndxGenres = 0; ndxGenres < 3; ndxGenres++) {
            rowHTML += "<th>";
            if (genres.length > ndxGenres) rowHTML += genres[ndxGenres];
            rowHTML += "</th>";
        }

        let starIds = resultData["movies"][ndx]["starIds"].split(",");
        let starNames = resultData["movies"][ndx]["starNames"].split(",");
        for (let ndxStars = 0; ndxStars < 3; ndxStars++) {
            rowHTML += "<th>";
            if (starIds.length > ndxStars) rowHTML += '<a href="starPage.html?id=' + starIds[ndxStars] + '">' + starNames[ndxStars] + '</a>';
            rowHTML += "</th>";
        }

        rowHTML += "<th>" + resultData["movies"][ndx]["rating"] + "</th>";

        rowHTML += "<th>" + resultData["movies"][ndx]["price"] + "</th>";
        rowHTML += "<th>" + '<button value="' + resultData["movies"][ndx]["movieId"] + '" class="add_to_cart" onclick="addToCart(this.value)">Add to Cart</button></th>';

        rowHTML += "</tr>";

        movieTableBodyElement.append(rowHTML);
    }

    console.log("results: " + results + " | limit: " + limit);

    if (results < limit) {
        console.log("removing next event listeners");
        document.getElementById("next").removeEventListener("click", handleNext);
        document.getElementById("next2").removeEventListener("click", handleNext);
    }

    if (page <= 0) {
        console.log("removing prev event listeneres");
        document.getElementById("prev").removeEventListener("click", handlePrev);
        document.getElementById("prev2").removeEventListener("click", handlePrev);
    }
}

let size = document.getElementById("size");
size.value = getSizeValue();

console.log("size.value: " + size.value);

let sort = document.getElementById("sorter");
sort.value = getSortValue();

console.log("sort.value: " + sort.value);

document.getElementById("prev").addEventListener("click", handlePrev);
document.getElementById("prev2").addEventListener("click", handlePrev);
document.getElementById("next").addEventListener("click", handleNext);
document.getElementById("next2").addEventListener("click", handleNext);

console.log(window.location.search);

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/movies" + window.location.search,
    success: (resultData) => handleMoviesResult(resultData)
});

size.addEventListener("change", handleLimitChange);
sort.addEventListener("change", handleSortChange);


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
