window.screenshotOK = false;

var orderListener = function (evt) {
    console.log("screenshotOK event OK");
    window.screenshotOK = true;
};

window.addEventListener("screenshotOK", orderListener);
