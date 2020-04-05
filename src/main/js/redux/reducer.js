import { combineReducers } from 'redux'

export const initialState = {
    isLoaded: false,
    data    : {},
}

const mainReducer = (state = initialState,action) => {
    switch (action.type) {
        case "fetchData" : {
            // console.log(`reducer return=${JSON.stringify({ ...state,...action.payload})}`)
            return { ...state,...action.payload}
        }
        default: return state
    }
}

const interlocReducer = (state = initialState,action) => {
    switch (action.type) {
        case "fetchInterlocutors" : {
            // console.log(`interlocReducer return=${JSON.stringify({ ...state,...action.payload})}`)
            return { ...state,...action.payload}
        }
        default: return state
    }
}

const messageReducer = (state = {
        isLoaded            : false,
        data                : {},
        activeInterlocutorId: null,
    },action) => {
    switch (action.type) {
        case "fetchMessages" : {
            // console.log(`messageReducer return=${JSON.stringify({ ...state,...action.payload})}`)
            return { ...state,...action.payload}
        }
        case "setMessagesParam" : {
            // console.log(`messageReducer return=${JSON.stringify({ ...state,...action.payload})}`)
            return { ...state,...action.payload}
        }
        default: return state
    }
}

export const reducer = combineReducers({
    mainReducer,
    interlocReducer,
    messageReducer
})
