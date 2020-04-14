import React, {memo, useEffect} from 'react'
import {connect} from "react-redux";
import Message from "./Message";
import {placeCaretAtEnd} from "../../../utils";
import MessagesScroll from "./MessagesScroll";
import {ajaxMessagesAction} from "../../../redux/actionCreators";
import {store} from "../../../App"

const Messages = props => {
    useEffect(() => {
        // let timer = setInterval(() => {
        //     props.fetchMessages(store.getState().messageReducer.activeInterlocutor.userId)
        // },2000)
        // return () => {
        //     clearInterval(timer)
        // }
    })
    console.log("Messages RENDER data=",props.data)
    if (props.isLoaded) {
        // todo crutch with random key
        return <MessagesScroll key={Math.random()}
                               data={props.data}
                               activeInterlocutor={props.activeInterlocutor}
                               setLastMessageFunc={props.setLastMessageFunc}/>
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