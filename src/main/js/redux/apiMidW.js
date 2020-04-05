import {ajax} from "../utils";

export const apiMidW = store => next => action => {
    if (!action.meta || action.meta.type !== "api") return next(action);
    const {url, params, method} = action.meta
    ajax(url, params, method)
        .then((result) => {
            let newAction = {...action,
                payload : {
                    isLoaded: true,
                    data    : JSON.parse(result),
                }}
            delete newAction.meta
            store.dispatch(newAction)
        })
    return next({...action,payload: {...action.payload,isLoaded: false,data: {}}})
}