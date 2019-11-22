var nowInMain = "minds";

function onError(x) {
    if (x.status === 401 || x.status === 403) document.location.assign("/login"); else alert(x.status);
}
function getJwtToken() {
    return localStorage.getItem("jwtToken");
}

function removeJwtToken() {
    localStorage.removeItem("jwtToken");
}

function jwtHeader() {
    var token = getJwtToken();
    if (token) {
        return {"Authorization": "Bearer " + token};
    } else {
        return {};
    }
}

function myOnLoad() {
    //Get user info and write their name in a nameField
    $.ajax("/rest/getUser", {headers:jwtHeader(),method:"GET"})
        .done(function (data) {
            nameField.innerHTML = data.name;
            //Write minds in subMain
            $.ajax("/minds",{headers:jwtHeader(),method:"GET"})
                .done(onGotSubMain)
                .fail(onError)
        })
        .fail(onError);
}
/**
 * Magical global variable for testing. Allow QA to know when subMain is loaded
 */
var subMainReady = false;
/**
 * When data for subMain is received writes then to subMain
 * @param data
 */
function onGotSubMain(data) {
    subMain.innerHTML = data||onError({status:"No data"});
    subMainReady = true
}

/**
 * When filter string is changed, retreives data for subMain
 */
function onChangeFilter() {
    subMainReady = false;
    $.ajax("/"+nowInMain,{data:{subs : mainFilter.value},headers:jwtHeader(),method:"GET"})
        .done(onGotSubMain)
        .fail(onError)
}

/**
 * On click to upper menu item changes their appearence (activeness) and retreives data
 * @param newActive Upper menu item
 * @param toWhat Name of what is in subMain (minds,users,friends,mates)
 * @param filter Filter string
 */
function changeView(newActive,toWhat,filter) {
    var children = menuBar.children;
    for (var i in children) {
        if (!children.hasOwnProperty(i)) continue;
        children[i].classList.remove("active")
    }
    newActive.classList.add("active");
    mainFilter.value = filter||"";
    nowInMain = toWhat;
    //show or hide mainFilter
    mainFilter.style.display = (nowInMain==="minds"||nowInMain==="users")?"":"none";
    subMain.innerHTML = "";
    //Show "loading" bunner
    setTimeout(function () {
        if (subMain.innerHTML === "") {
            subMain.innerHTML = "<div style=\"text-align: center\"><h1 class=\"display-3\" style=\"margin: 1em auto\">Загрузка</h1></div>" +
                "<div class=\"progress\">\n" +
                "  <div class=\"progress-bar progress-bar-striped progress-bar-animated\" role=\"progressbar\" aria-valuenow=\"75\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width: 75%\"></div>\n" +
                "</div>"
        }
    },1000);
    onChangeFilter();
}

/**
 * LogOff. REST response erases keyCookie and change keyCookie on server if LogOff from all devices
 * @param all LogOff from all devices
 */
function logOff(all) {
    removeJwtToken();
    document.location.assign("/login")
}

// noinspection JSUnusedGlobalSymbols
/**
 * Adds friend
 * @param container Container with frienship bages to find them and change visibility
 * @param friend_id
 */
function toFriends(container,friend_id) {
    $.ajax("/rest/toFriends",{data:{friend_id : friend_id},headers:jwtHeader(),method:"PATCH"})
        .done(function (data) {
            //REST returns "mate" if they are mutual friends now or "noMate" otherwise
            $(container).closest(".userEntity").find(".friendship")[0].innerHTML =
                data==="mate"?"<span class=\"badge badge-success\">Взаимный Друг</span>":
                    "<span class=\"badge badge-primary\">ПолуДруг</span>";
            $(container).find (".toFriends")[0].style.display = "none";
            $(container).find (".fromFriends")[0].style.display = ""

        })
        .fail(onError)
}

// noinspection JSUnusedGlobalSymbols
/**
 * Removes friend
 * @param container Container with frienship bages to find them and change visibility
 * @param friend_id
 */
function fromFriends(container,friend_id) {
    $.ajax("/rest/fromFriends",{data:{friend_id : friend_id},headers:jwtHeader(),method:"PATCH"})
        .done(function (data) {
            //REST returns "mate" if another is a mate or "noMate" otherwise
            $(container).closest(".userEntity").find(".friendship")[0].innerHTML =
                data==="mate"?"<span class=\"badge badge-secondary\">Почитатель</span>":"";
            $(container).find (".toFriends")[0].style.display = "";
            $(container).find (".fromFriends")[0].style.display = "none";
            onChangeFilter();//If a friend not a friend now, we must rid of them
        })
        .fail(onError)
}

//not mine
function placeCaretAtEnd(el) {
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

/**
 * Opens mind/answer editing window and let edit or add new mind
 * @param moa = 'mind' or 'answer' depending of what we work with
 * @param menuThis Undefined if new mind. Menu element which was clicked. To find mindText and get mind text.
 * @param parentMind Only for answers
 * @param startText Text, that will be inserted in the beginning. e.g. "@vasya " to write answers like "@vasya blablabla"
 * @param mindId undefined if new mind
 */
function openNewMindWindow(moa, menuThis, mindId, parentMind, startText) {
    if (document.getElementById("mindWindow")) return; //to not open second window
    var win = document.createElement("div");
    if (startText==='@') startText += $(menuThis).closest("."+moa+"Entity").find("."+moa+"User")[0].innerHTML+' '; //CRUTCH: user is got from the page
    var text = startText || "";
    text += mindId ? $(menuThis).closest("."+moa+"Entity").find("."+moa+"Text")[0].innerHTML : ""; //CRUTCH: text is got from the page
    // noinspection WithStatementJS
    with (win) {
        setAttribute("id","mindWindow");
        setAttribute("class","alert alert-dismissible alert-success");
        innerHTML = '\n' +
            '<button type="button" class="close" data-dismiss="alert" id="closeMind">&times;</button>\n' +
            '<label for="mindTextArea">'+(moa === 'mind' ? 'Мысль' : 'Ответ')+':</label>\n' +
            '<div contenteditable id="mindTextArea">'+text+'</div>\n' +
            '<div id="mindErrSign" style="display: none"></div>\n' +
            '<button type="button" class="btn btn-primary" ' +
            '  onclick="saveMind(\''+moa+'\','+mindId+(parentMind ? (','+parentMind) : '')+')" ' +
            '  style="margin-top: 1em">Опубликовать</button>'
    }
    document.body.append(win);
    mindTextArea.focus();
    placeCaretAtEnd(mindTextArea)
}

/**
 * On click of "Опубликовать" closes  the mindWindow, posts the mind/answer and renew subMain with minds
 * @param moa = 'mind' or 'answer' depending of what we work with
 * @param id
 * @param parentMind Only for answers
 */
function saveMind(moa,id,parentMind) {
    /* Length validation */
    if (mindTextArea.innerText.length > 4000) {
        // noinspection WithStatementJS
        with (mindErrSign) {
            style.display = "";
            innerHTML =
                '<div class="alert alert-dismissible alert-danger">\n' +
                '  <button type="button" class="close" data-dismiss="alert">&times;</button>\n' +
                '  <p class="mb-0">Слишком длинная мысль. Максимальная длинна 4000</p>' +
                '</div>'
        }
        return
    }
    $.ajax("/rest/save"+moa[0].toUpperCase()+moa.slice(1),
        {data:{text : mindTextArea.innerText.replace(/\xa0/g," "), id : id||0, parentMind : parentMind},
            headers:jwtHeader(),method:"POST"})
        .done(function () {
            onChangeFilter()
        })
        .fail(onError);
    mindWindow.remove()
}
// noinspection JSUnusedGlobalSymbols
/**
 * Removes mind/answer and renew subMain with minds
 * @param moa = 'mind' or 'answer' depending of what we work with
 * @param id
 */
function removeMind(moa,id) {
    $.ajax("/rest/remove"+moa[0].toUpperCase()+moa.slice(1),{data:{id : id},headers:jwtHeader(),method:"DELETE"})
        .done (function (data, status) {
            if (status !== "success"||data.search(/^error/)>=0) {onError(data||"bad status="+status);return}
            onChangeFilter()
        })
        .fail(onError);
}