/*
if (window.location.protocol !== 'https:') {
    window.location.protocol = 'https:';
    window.location.port = "8443";
    //window.location.replace(`https:${location.href.substring(location.protocol.length)}`);
}
 */

let add_star = "";
let add_movie = "";

function alertLoadedXML(resultData) {
    console.log(resultData);
    if (resultData["status"] == "success") alert("XML Successfuly Loaded to Database");
    else alert("ERROR: Unable to Load to Database");
}

function alertAddedStar(resultData) {
    console.log(resultData);
    alert(`${resultData["name"]} Successfully Added to DB | Star ID: ${resultData["id"]}`);
}

function alertAddedMovie(resultData) {
    console.log(resultData);
    if (resultData["added"]) alert(`${resultData["title"]} Successfully Added to DB with Values: \nMovie ID: ${resultData["id"]} | Star ID: ${resultData["starId"]} | Genre ID: ${resultData["genreId"]}`);
    else alert(`Movie ${resultData["title"]} already exists with same Director and Year!`);
}

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

function handleAdmin(resultData) {
    console.log("handleAdmin: populating page from resultData");
    console.log(resultData);

    let mainHeadElement = jQuery("#main_head");
    let infoHeaderElement = jQuery("#info_header");
    let infoBodyElement = jQuery("#info_body");
    let ActionHeaderElement = jQuery("#action_header");
    let ActionBodyElement = jQuery("#action_body");

    mainHeadElement.append(": Admin");

    infoHeaderElement.append("<h1>Metadata</h1>");

    let schemaData = "";
    let tables = resultData["tables"];
    for (let ndxTables = 0; ndxTables < tables.length; ndxTables++) {
        let table = tables[ndxTables];
        schemaData += "<div class='schema_div'>\n<h1>" + table["name"] + "</h1>\n\n<p>";

        schemaData += "<table id='" + table["name"] + "_table' class='table table-striped'><thead><tr><th>Attribute</th><th>Type</th></tr></thead><tbody>";

        for (let ndxAttrs = 0; ndxAttrs < table["attributes"].length; ndxAttrs++) {
            let attr = table["attributes"][ndxAttrs];
            schemaData += "<tr><th>" + attr["name"] + "</th><th>" + attr["type"] + "</th></tr>\n"
        }

        schemaData += "</tbody></table></p>\n</div>";
    }

    infoBodyElement.append(schemaData);

    ActionHeaderElement.append("<h1>Manage Database</h1>");

    ActionBodyElement.append("<div><hr></div>\n" +
        "\n" +
        "                <h2>Add a Star</h2>\n" +
        "                <form class=\"in-blk\" id=\"add_star\" method=\"post\" action=\"#\">\n" +
        "                    <label><b>Star Name</b></label>\n" +
        "                    <input name=\"star_name\" placeholder=\"Enter Star Name\" type=\"text\" required>\n" +
        "                    <label><b>Star Birth Year</b></label>\n" +
        "                    <input name=\"star_birth\" type=\"text\">\n" +
        "                    <input type=\"submit\" value=\"submit\">\n" +
        "                </form>\n" +
        "\n" +
        "                <div><hr></div>\n" +
        "\n" +
        "                <h2>Add a Movie</h2>\n" +
        "                <form class=\"in-blk\" id=\"add_movie\" method=\"post\" action=\"#\">\n" +
        "                    <label><b>Movie Title</b></label>\n" +
        "                    <input name=\"movie_title\" placeholder=\"Enter Movie Title\" type=\"text\" required>\n" +
        "                    <label><b>Movie Year</b></label>\n" +
        "                    <input name=\"movie_year\" type=\"text\" required>\n" +
        "                    <br>\n" +
        "                    <label><b>Movie Director</b></label>\n" +
        "                    <input name=\"movie_director\" type=\"text\" required>\n" +
        "                    <label><b>Movie Price</b></label>\n" +
        "                    <input name=\"movie_price\" type=\"text\">\n" +
        "                    <br>\n" +
        "                    <label><b>Star Name</b></label>\n" +
        "                    <input name=\"star_name\" type=\"text\" required>\n" +
        "                    <label><b>Genre Name</b></label>\n" +
        "                    <input name=\"genre_name\" type=\"text\">\n" +
        "                    <br>\n" +
        "                    <input type=\"submit\" value=\"submit\">\n" +
        "                </form>\n" +
        "\n" +
        "                <div><hr></div>\n\n" +
        "                <h2>Load from XML</h2>\n" +
        "                <form id='xml' method='post' action='#' enctype='multipart/form-data'>\n" +
        "                \t<input type='submit' value='load xml'>\n" +
        "                </form>\n" +
        "                <div><hr></div>\n" +
        "                <h2>Search Averages</h2>\n" +
        "                <p>" + resultData["search_avg"] + "</p>\n" +
        "                <div><hr></div>\n");

    add_star = $("#add_star");
    add_movie = $("#add_movie");
    xml = $("#xml");
    console.log("before submit redef");
    add_star.submit(submitAddStar);
    add_movie.submit(submitAddMovie);
    xml.submit(submitXML);
    console.log("after submit redef");
}

function handleBasic(resultData) {
    console.log("handleBasic: populating page from resultData");
    console.log(resultData);

    let mainHeadElement = jQuery("#main_head");
    let infoHeaderElement = jQuery("#info_header");
    let infoBodyElement = jQuery("#info_body");
    let ActionHeaderElement = jQuery("#action_header");
    let ActionBodyElement = jQuery("#action_body");

    mainHeadElement.append(": Customer");

    infoHeaderElement.append("<h1>Orders</h1>");

    let ordersTable = "<table id='orders_table' class='table table-striped'><thead><tr><th>Order Number</th><th>Title</th><th>Price</th><th>Date</th></tr></thead><tbody>";
    let sales = resultData["sales"];

    for (let ndxSales = 0; ndxSales < sales.length; ndxSales++) {
        let sale = sales[ndxSales];
        ordersTable += "<tr><th>" + sale["id"] + "</th><th>" + sale["title"] + "</th><th>" + sale["price"] + "</th><th>" + sale["date"] + "</th></tr>";
    }

    ordersTable += "</tbody></table>";

    infoBodyElement.append(ordersTable);
}

function handleAccountResults(resultData) {
    if (resultData["admin"]) handleAdmin(resultData);
    else handleBasic(resultData);
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/account",
    success: (resultData) => handleAccountResults(resultData)
});

function submitAddStar(formSubmitEvent) {
    console.log("submit add star form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/account?type=star", {
            method: "POST",
            data: add_star.serialize(),
            success: alertAddedStar
        }
    );
}

function submitAddMovie(formSubmitEvent) {
    console.log("submit add movie form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/account?type=movie", {
            method: "POST",
            data: add_movie.serialize(),
            success: alertAddedMovie
        }
    );
}

function submitXML(formSubmitEvent) {
    console.log("submit xml form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/account?type=xml", {
            method: "POST",
            data: xml,
            success: alertLoadedXML
        }
    );
}


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