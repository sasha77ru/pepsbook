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