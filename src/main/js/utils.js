import SockJS from "sockjs-client";

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

export function wSocket (endpoint, target, callBack) {
    const socket = SockJS(`/ws/${endpoint}?jwt=${getJwtToken()}`);
    const stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        stompClient.subscribe(`/topic/${target}`, (message) => {
            callBack(JSON.parse(message.body))
        });
    });
    return socket
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

// export function cursorTo(el,x) {
//     el.focus()
//     let range = document.createRange();
//     let sel = window.getSelection();
//     console.log(el )
//     range.setStart(el.firstChild, x);
//     range.collapse(true);
//     sel.removeAllRanges();
//     sel.addRange(range);
// }

//not mine
function getTextNodesIn(node) {
    var textNodes = [];
    if (node.nodeType == 3) {
        textNodes.push(node);
    } else {
        var children = node.childNodes;
        for (var i = 0, len = children.length; i < len; ++i) {
            textNodes.push.apply(textNodes, getTextNodesIn(children[i]));
        }
    }
    return textNodes;
}
//not mine
export function cursorTo(el, start, end) {
    if (end === undefined) end = start
    if (document.createRange && window.getSelection) {
        var range = document.createRange();
        range.selectNodeContents(el);
        var textNodes = getTextNodesIn(el);
        var foundStart = false;
        var charCount = 0, endCharCount;

        for (var i = 0, textNode; textNode = textNodes[i++]; ) {
            endCharCount = charCount + textNode.length;
            if (!foundStart && start >= charCount
                && (start < endCharCount ||
                    (start == endCharCount && i <= textNodes.length))) {
                range.setStart(textNode, start - charCount);
                foundStart = true;
            }
            if (foundStart && end <= endCharCount) {
                range.setEnd(textNode, end - charCount);
                break;
            }
            charCount = endCharCount;
        }

        var sel = window.getSelection();
        sel.removeAllRanges();
        sel.addRange(range);
    } else if (document.selection && document.body.createTextRange) {
        var textRange = document.body.createTextRange();
        textRange.moveToElementText(el);
        textRange.collapse(true);
        textRange.moveEnd("character", end);
        textRange.moveStart("character", start);
        textRange.select();
    }
}

export function getCursorPos(el) {
    el.focus()
    let _range = document.getSelection().getRangeAt(0)
    let range = _range.cloneRange()
    range.selectNodeContents(el)
    range.setEnd(_range.endContainer, _range.endOffset)
    return range.toString().length;
}
