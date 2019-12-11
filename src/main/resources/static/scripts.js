var nowInMain = "minds";
var mindsPage = 0;

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
            nameField.innerHTML = noTag(data.name);
            //Write minds in subMain
            changeView(mainMinds.parentNode,"minds")
        })
        .fail(onError);
}

/**
 * Hides all > and <
 */
function noTag (x) {
    return x.replace(/</g,"&lt;").replace(/>/g,"&gt;")
}

/**
 * Puts users page to subMain
 * @param data JSON from server
 */
function displayUsers (data) {
    /**
     * @param user
     * @returns {string} HTML of usersBage depends on it's relation to the current user
     */
    function usersBadge (user) {
        if (user.isFriend || user.isMate) {
            return '' +
            '                    <span class="badge '+(user.isFriend ? (user.isMate ? "badge-success":"badge-primary") : "badge-secondary")+'">\n' +
            '                        '+(user.isFriend ? (user.isMate ? "Взаимный Друг":"ПолуДруг") : "Почитатель")+'\n' +
            '                    </span>\n'
        } else return ""
    }
    var page = '<table class="table table-hover">\n' +
        '    <thead>\n' +
        '    <tr>\n' +
        '        <th scope="col">Имя</th>\n' +
        '        <th scope="col">Страна</th>\n' +
        '        <th scope="col"></th>\n' +
        '    </tr>\n' +
        '    </thead>\n' +
        '    <tbody>\n';
    $.each(data, function (i,user) {
        page +=
            '    <tr class="userEntity">\n' +
            '        <td class="userName">'+noTag(user.name)+'</td>\n' +
            '        <td class="userCountry">'+noTag(user.country)+'</td>\n' +
            '        <td>\n' +
            '            <ul class="nav">\n' +
            '                <li class="nav-item dropdown">\n' +
            '                    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"></a>\n' +
            '                    <div class="dropdown-menu" x-placement="bottom-start" style="position: absolute; will-change: transform; top: 0px; left: 0px; transform: translate3d(0px, 40px, 0px);">\n' +
            '                        <a class="dropdown-item toFriends"\n' +
            '                           style="'+(user.isFriend?"display : none":"")+'"\n' +
            '                           onclick="toFriends(this.parentNode,'+user.id+')"\n' +
            '                           href="javascript:undefined">В друзья</a>\n' +
            '                        <a class="dropdown-item fromFriends"\n' +
            '                           style="'+(!user.isFriend?"display : none":"")+'"\n' +
            '                           onclick="fromFriends(this.parentNode,'+user.id+')"\n' +
            '                           href="javascript:undefined">Из друзей</a>\n' +
            '                    </div>\n' +
            '                </li>\n' +
            '                <li class="friendship">\n' +
            usersBadge(user) +
            '                </li>\n' +
            '            </ul>\n' +
            '        </td>\n' +
            '    </tr>\n'


    });
    subMain.innerHTML = page +
        '    </tbody>\n' +
        '</table>'
}

/**
 * Puts minds page to subMain
 * @param data JSON from server
 */
function displayMinds (data) {
    /**
     * Returns menu items for mind or answer if the user is their author, otherwise ""
     * @param moa = "mind" or "answer"
     * @param x mind or answer object (got from server JSON)
     * @param answersMindId The mind of the answer if moa = "answer"
     * @returns {string} HTML of menu items
     */
    function authorMenuItems (moa,x,answersMindId) {
        if (x.isAuthor) {
            var capita = moa==="mind"?"Mind":"Answer";
            return '' +
                '<a class="dropdown-item ownerMenu edit'+capita+'" href="javascript:undefined"' +
                ' onclick="openNewMindWindow(\''+moa+'\',this,'+x.id+','+answersMindId+')">Редактировать</a>\n' +
                '<a class="dropdown-item ownerMenu del'+capita+'" href="javascript:undefined"\n' +
                ' onclick="removeMind(\''+moa+'\','+x.id+')">Удалить</a>\n'
        } else return ""
    }
    function mindsAnswers (mind) {
        var ret = "";
        $.each(mind.answers,function (i,answer) {
            ret += ''+
            '        <div class="answerEntity" style="margin-left: 2em">\n' +
            '            <strong class="answerUser" style="margin-right: 1em">'+noTag(answer.author)+'</strong>\n' +
            '            <span class="answerText">'+noTag(answer.text)+'</span>\n' +
            '            <em class="answerTime" style="margin-left: 1em">'+answer.time+'</em>\n' +
            '            <span class="nav-item dropdown" style="display: inline">\n' +
            '              <a style="display: inline" class="nav-link dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"></a>\n' +
            '                    <div class="dropdown-menu" x-placement="bottom-start" style="position: absolute; will-change: transform; top: 0px; left: 0px; transform: translate3d(0px, 40px, 0px);">\n' +
            authorMenuItems('answer',answer,mind.id) +
            '                        <a class="dropdown-item answerAnswer" href="javascript:undefined"\n' +
            '                           onclick="openNewMindWindow(\'answer\',this,undefined,'+mind.id+',\'@\')">\n' +
            '                            Ответить</a>\n' +
            '                    </div>\n' +
            '            </span>\n' +
            '        </div>\n'
        });
        return ret;
    }
    function paginator () {
        var out = '' +
            '<div style="margin: 1em auto">\n' +
            '  <ul class="pagination">\n' +
            '    <li class="page-item'+ (data.first?" disabled":"")+'">\n' +
            '      <a class="page-link" href="javascript:requestSubMain('+(data.number-1)+')">&laquo;</a>\n' +
            '    </li>\n';
        for (var page=0;page < data.totalPages;page++) {
            if (data.number===page) {
                out += '' +
                    '    <li class="page-item active">\n' +
                    '      <a class="page-link" href="javascript:undefined">'+(page+1)+'</a>\n';

            } else {
                //to do less page buttons
                if (data.totalPages > PAGINATOR_MAX_SIZE) {
                    if (page > 0 && page < data.number - PAGINATOR_WIDE+1) {
                        page = data.number - PAGINATOR_WIDE;
                        out += '' +
                            '    <li class="page-item disabled">\n' +
                            '      <a class="page-link" href="javascript:undefined">...</a>\n';
                    }
                    else if (page < data.totalPages - 2 && page > data.number + PAGINATOR_WIDE) {
                        page = data.totalPages - 1;
                        out += '' +
                            '    <li class="page-item disabled">\n' +
                            '      <a class="page-link" href="javascript:undefined">...</a>\n';
                    }
                }
                out += '' +
                    '    <li class="page-item">\n' +
                    '      <a class="page-link" href="javascript:requestSubMain(' + page + ')">' + (page + 1) + '</a>\n';

            }
        }
        out += ''+
            '    <li class="page-item'+ (data.last?" disabled":"")+'">\n' +
            '      <a class="page-link" href="javascript:requestSubMain('+(data.number+1)+')">&raquo;</a>\n' +
            '    </li>\n' +
            '  </ul>\n' +
            '</div>'
        return out
    }
    //ajax can return not requested page but the last one if requested doesn't exist anymore
    mindsPage = data.number;
    var out = "";
    if (data.totalPages > 1) out+=paginator();
    $.each(data.content, function (i,mind) {
        out +=
        '<div class="card mb-3 mindEntity ' + (mind.isAuthor?'border-primary':'border-light') + '">\n' +
        '    <div class="card-header">\n' +
        '        <span class="mindTime">' + mind.time + '</span>\n' +
        '        <span class="nav-item dropdown" style="display: inline">\n' +
        '              <a style="display: inline" class="nav-link dropdown-toggle" data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"></a>\n' +
        '                    <div class="dropdown-menu" x-placement="bottom-start" style="position: absolute; will-change: transform; top: 0px; left: 0px; transform: translate3d(0px, 40px, 0px);">\n' +
        authorMenuItems('mind',mind,undefined) +
        '                        <a class="dropdown-item answerMind" href="javascript:undefined"\n' +
        '                           onclick="openNewMindWindow(\'answer\',this,undefined,'+mind.id+')">Ответить</a>\n' +
        '                    </div>\n' +
        '        </span>\n' +
        '    </div>\n' +
        '    <div class="card-body">\n' +
        '        <h4 class="card-title mindUser">'+noTag(mind.author)+'</h4>\n' +
        '        <p class="card-text mindText">'+noTag(mind.text)+'</p>\n' +
        mindsAnswers(mind) +
        '    </div>\n' +
        '</div>\n'
    });
    if (data.totalPages > 1) out+=paginator();
    subMain.innerHTML = out + '<a class="btn btn-primary btn-lg" href="javascript:openNewMindWindow(\'mind\')" role="button" style="display: block;width: 40%;margin: 1em auto" id="newMind">Новая мысль</a>'
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
    if (!data) {onError({status:"No data"});return}
    if (nowInMain === "minds") displayMinds(data);//subMain.innerHTML = JSON.stringify(data);//
    else displayUsers(data);//subMain.innerHTML = JSON.stringify(data);//
    subMainReady = true
}

function requestSubMain (page) {
    subMainReady = false;
    var data = {subs : mainFilter.value};
    if (nowInMain==="minds") {
        if (page===undefined) page = mindsPage;
        mindsPage = page;
        data.page = page;data.size = MINDS_PAGE_SIZE;
    }
    $.ajax("/rest/"+nowInMain,{data:data,headers:jwtHeader(),method:"GET"})
        .done(onGotSubMain)
        .fail(onError)
}

/**
 * When filter string is changed, retreives data for subMain
 */
function onChangeFilter() {
    requestSubMain(0)
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
    requestSubMain();
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
    if (startText==='@') startText += $(menuThis).closest("."+moa+"Entity").find("."+moa+"User")[0].innerHTML+"&nbsp;"; //CRUTCH: user is got from the page
    var text = startText || "";
    text += mindId ? $(menuThis).closest("."+moa+"Entity").find("."+moa+"Text")[0].innerHTML : ""; //CRUTCH: text is got from the page
    // noinspection WithStatementJS
    with (win) {
        setAttribute("id","mindWindow");
        setAttribute("class","alert alert-dismissible alert-success");
        setAttribute("style","z-index:100");
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
            requestSubMain()
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
            requestSubMain()
        })
        .fail(onError);
}