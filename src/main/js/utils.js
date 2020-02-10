'use strict';

/**
 * Hides all > and <
 * Became unused with React
 */
export function noTag (x) {
    return x
    // return x.replace(/</g,"&lt;").replace(/>/g,"&gt;")
}

export function getJwtToken() {
    return localStorage.getItem("jwtToken");
}

export function removeJwtToken() {
    localStorage.removeItem("jwtToken");
}

function jwtHeader() {
    let token = getJwtToken();
    return (token) ? {"Authorization": "Bearer " + token} : {}
}

/** Error handling */
export function noInternet (error) {
    alert("No Internet: "+error.message)
}

/**
 * Sends Ajax request to Rest. With jwt. And handle errors.
 * @param url
 * @param data Object with key : value (will be converted to GET or form-data)
 * @param method
 * @returns {Promise<Response | void>} Returns text, not JSON object (to prevent empty response errors)
 */
export function ajax (url,data = {},method = 'GET') {
    let init = {
        method: method, // *GET, POST, PUT, DELETE, etc.
        mode: 'same-origin', // no-cors, cors, *same-origin
        cache: 'default', // *default, no-cache, reload, force-cache, only-if-cached
        credentials: 'same-origin', // include, *same-origin, omit
        headers: {
            // 'Content-Type': 'application/json',
            "Authorization": "Bearer " + getJwtToken()
        },
        redirect: 'follow', // manual, *follow, error
        referrer: 'client', // no-referrer, *client
    }
    if (method === "GET") {
        url += "?"+$.param(data)
    } else {
        init.body = Object.keys(data).reduce((formData,key) => {
            formData.append(key,data[key]);return formData
        },new FormData())
    }
    try {
        return fetch(url, init)
            .then(response => {
                if (!response.ok) {
                    if (response.status === 401 || response.status === 403) document.location.assign("/login");
                    else alert(response.status);
                    return {}
                } else {
                    return response.text()
                }
            }, noInternet)
    } catch (e) {}
}

//not mine
export function placeCaretAtEnd(el) {
    el.focus();
    if (typeof window.getSelection != "undefined"
        && typeof document.createRange != "undefined") {
        var range = document.createRange();
        range.selectNodeContents(el);
        range.collapse(false);
        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    } else if (typeof document.body.createTextRange != "undefined") {
        var textRange = document.body.createTextRange();
        textRange.moveToElementText(el);
        textRange.collapse(false);
        textRange.select();
    }
}
