
export const initialState = {
    isLoaded: false,
    data    : {},
}

export const reducer = (state = initialState,action) => {
    switch (action.type) {
        case "fetchData" : {
            // console.log(`reducer return=${JSON.stringify({ ...state,...action.payload})}`)
            return { ...state,...action.payload}
        }
        default: return state
    }
}
