import {restPrefix} from "../config";

export const ajaxDataAction = (nowInMain, params) => ({
    type: "fetchData",
    payload: null,
    meta: {
        type    : "api",
        url     : restPrefix + nowInMain,
        params  : {page:0,size:MINDS_PAGE_SIZE,...params},
        method  : "GET",
    }
})

export const updateDataAction = (nowInMain, data) => ({
    type: "fetchData",
    payload: {isLoaded:true,data:data},
})

export const ajaxInterlocAction = () => ({
    type: "fetchInterlocutors",
    payload: null,
    meta: {
        type    : "api",
        url     : restPrefix + "interlocutors",
        params  : {},
        method  : "GET",
    }
})

export const ajaxMessagesAction = (activeInterlocutorId,params) => ({
    type: "fetchMessages",
    payload: null,
    meta: {
        type    : "api",
        url     : restPrefix + "messages",
        params  : {whomId: activeInterlocutorId,page:0,size:MESSAGES_PAGE_SIZE,...params},
        method  : "GET",
    }
})

export const setMessagesParamAction = (params) => ({
    type: "setMessagesParam",
    payload: params,
})