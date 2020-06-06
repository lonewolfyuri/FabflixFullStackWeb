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

function getParameterByName(target) {
    let url = window.location.href;

    target = target.replace(/[\[\]]/g, "\\$&");

    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"), results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

function handleStarResults(resultData) {
    console.log("handleMovieResults: populating movie page from resultData");

    let starNameElement = jQuery("#star_name");
    let starBirthElement = jQuery("#star_birth");
    let starMoviesTableBodyElement = jQuery("#star_movies_list_body");

    starNameElement.append("<p>" + resultData["name"] + "</p>");

    if (resultData["birth"]) starBirthElement.append("<p>( " + resultData["birth"]+ " )</p>");
    else starBirthElement.append("<p>( N/A )</p>");

    for (let ndxMovies = 0; ndxMovies < resultData["movies"].length; ndxMovies++) starMoviesTableBodyElement.append("<tr><th>" + '<a href="moviePage.html?id=' + resultData["movies"][ndxMovies]["id"] + '">' + resultData["movies"][ndxMovies]["title"] + '</a>' + "</th></tr>");
}

let starId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/singleStar?id=" + starId,
    success: (resultData) => handleStarResults(resultData)
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