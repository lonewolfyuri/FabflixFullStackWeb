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

function redirectLogout(resultData) {
    window.location.replace("login.html");
}

$.ajax({
    dataType: "json",
    method: "GET",
    url: "api/logout",
    success: (resultData) => redirectLogout(resultData)
});