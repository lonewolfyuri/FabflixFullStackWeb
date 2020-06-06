/*
if (window.location.protocol !== 'https:') {
    window.location.protocol = 'https:';
    window.location.port = "8443";
    //window.location.replace(`https:${location.href.substring(location.protocol.length)}`);
}
 */

function recapCallback() {
    $("#recaptcha_error_message").remove();
}

let login_form = $("#login_form");

function handleLoginResult(resultDataString) {
    console.log(resultDataString);
    let resultDataJson = resultDataString;
    //let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    if (resultDataJson["status"] === "success") {
        window.location.replace("mainPage.html");
    } else {
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/login", {
            method: "POST",
            data: login_form.serialize(),
            success: handleLoginResult
        }
    );
}

login_form.submit(submitLoginForm);