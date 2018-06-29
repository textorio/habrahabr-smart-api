time = arguments[0];
console.log("front-script: making screenshot at "+time);
window.screenshotFired = undefined;
requestId = 0;

(async function () {
    var ScreenshotRequest = (function () {

        function getData() {
            var id = requestId++;

            return new Promise(function (resolve, reject) {

                var listener = function (evt) {
                    if (evt.detail.requestId == id) {
                        window.removeEventListener("make_screenshot_data", listener);
                        resolve(evt.detail.data);
                    }
                };
                window.addEventListener("make_screenshot_data", listener);

                var payload = {id: id, time: time};

                window.dispatchEvent(new CustomEvent("make_screenshot", {detail: payload}));
                console.log("front-script: make screenshot event dispatched");
            });
        }

        return {getData: getData};
    })();

    ScreenshotRequest.getData().then(function (data) {
        window.screenshotFired = data;
    });
})();

console.log("screenshot-maker ok");