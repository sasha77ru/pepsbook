import React, {memo, useEffect, useState} from 'react'
import PropTypes from "prop-types";
import {connect} from "react-redux";
import {ajaxDataAction, ajaxInterlocAction, ajaxMessagesAction} from "../../redux/actionCreators";
import Interlocutors from "./Interlocutors/Interlocutors";
import Messages from "./Messages/Messages";
import {store} from "../../App";
import MessageInput from "./MessageInput";
import Card from "react-bootstrap/Card";
import {loc} from "../../config";

const Messaging = ({activeInterlocutorId,fetchMessages,fetchInterlocutors}) => {
    console.log("Messaging RENDER")
    useEffect(() => {fetchInterlocutors()},[])
    useEffect(() => {
        activeInterlocutorId && fetchMessages(activeInterlocutorId)
    },[activeInterlocutorId])
    return <>
        <div id={"interlocutors"}><Interlocutors/></div>
        {(activeInterlocutorId)
            ?   <div id={"messages"}>
                    <Messages activeInterlocutorId={activeInterlocutorId}/>
                    <div id={"messageInput"}><MessageInput /></div>
                </div>
            :   <div style={{zIndex : -1}}>
                    <Card border="danger">
                        <Card.Body>
                            <Card.Text>{loc.chooseInterlocutors}</Card.Text>
                        </Card.Body>
                    </Card>
                </div>
        }
    </>
}
export default connect(state => ({
    activeInterlocutorId: state.messageReducer.activeInterlocutorId,
}),dispatch => ({
    fetchInterlocutors  : (...args) => dispatch(ajaxInterlocAction(...args)),
    fetchMessages       : (...args) => dispatch(ajaxMessagesAction(...args)),
}))(Messaging)