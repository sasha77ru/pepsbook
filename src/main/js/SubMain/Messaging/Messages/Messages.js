import React, {memo, useEffect} from 'react'
import {connect} from "react-redux";
import Message from "./Message";
import {placeCaretAtEnd} from "../../../utils";
import MessagesScroll from "./MessagesScroll";

const Messages = props => {
    console.log("Messages render",props.data)
    if (props.isLoaded) {
        // todo crutch with random key
        return <MessagesScroll key={Math.random()} data={props.data} activeInterlocutorId={props.activeInterlocutorId}/>
    } else return false
}
export default connect(state => ({
    isLoaded: state.messageReducer.isLoaded,
    data    : state.messageReducer.data,
}),null,null,{areStatesEqual : (next,prev) => {
        return !next.messageReducer.isLoaded
    }})(Messages)