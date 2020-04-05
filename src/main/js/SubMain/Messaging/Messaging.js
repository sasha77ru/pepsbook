import React, {memo, useEffect, useState} from 'react'
import PropTypes from "prop-types";
import {connect} from "react-redux";
import {ajaxDataAction, ajaxInterlocAction, ajaxMessagesAction} from "../../redux/actionCreators";
import Interlocutors from "./Interlocutors/Interlocutors";
import Messages from "./Messages/Messages";
import {store} from "../../App";
import MessageInput from "./MessageInput";

const Messaging = ({activeInterlocutorId,fetchMessages,fetchInterlocutors}) => {
    console.log("Messaging RENDER")
    useEffect(() => {fetchInterlocutors()},[])
    useEffect(() => {
        activeInterlocutorId && fetchMessages(activeInterlocutorId)
    },[activeInterlocutorId])
    return <>
        <div id={"interlocutors"}><Interlocutors/></div>
        {activeInterlocutorId && <div id={"messages"}>
            <Messages activeInterlocutorId={activeInterlocutorId}/>
            <div id={"messageInput"}><MessageInput /></div>
        </div>}
    </>
}
export default connect(state => ({
    activeInterlocutorId: state.messageReducer.activeInterlocutorId,
}),dispatch => ({
    fetchInterlocutors  : (...args) => dispatch(ajaxInterlocAction(...args)),
    fetchMessages       : (...args) => dispatch(ajaxMessagesAction(...args)),
}))(Messaging)