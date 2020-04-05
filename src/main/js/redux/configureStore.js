import { createStore, applyMiddleware } from "redux"
import {apiMidW} from "./apiMidW"
import {initialState, reducer} from "./reducer";

export const configureStore = () => {
    return createStore(
        reducer,
        {},
        applyMiddleware(apiMidW)
    )
};