import React from 'react'
import {connect} from "react-redux";
import MessagesScroll from "./MessagesScroll";
import {ajaxMessagesAction} from "../../../redux/actionCreators";

const Messages = props => {
    console.log("Messages RENDER data=",props.data)
    if (props.isLoaded) {
        // todo crutch with random key
        return <MessagesScroll key={Math.random()}
                               data={props.data}
                               activeInterlocutor={props.activeInterlocutor} />
    } else return false
}
export default connect(state => ({
    isLoaded: state.messageReducer.isLoaded,
    data    : state.messageReducer.data,
}),dispatch => ({
    fetchMessages       : (...args) => dispatch(ajaxMessagesAction(...args)),
}),null,{areStatesEqual : (next,prev) => {
        return !next.messageReducer.isLoaded
    }})(Messages)