import React, {memo, useEffect, useState} from 'react'
import {connect} from "react-redux";
import {ajaxDataAction, ajaxInterlocAction, ajaxMessagesAction} from "../../redux/actionCreators";
import Interlocutors from "./Interlocutors/Interlocutors";
import Messages from "./Messages/Messages";
import MessageInput from "./MessageInput";
import Card from "react-bootstrap/Card";
import {loc} from "../../config";

const Messaging = ({activeInterlocutor,fetchMessages,fetchInterlocutors}) => {
    console.log("Messaging RENDER")
    useEffect(() => {fetchInterlocutors()},[])
    useEffect(() => {
        activeInterlocutor && fetchMessages(activeInterlocutor)
    },[activeInterlocutor])

    return <>
        <div id={"interlocutors"}><Interlocutors/></div>
        {(activeInterlocutor)
            ?   <div id={"messages"}>
                    <Messages activeInterlocutor={activeInterlocutor} />
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
    activeInterlocutor  : state.messageReducer.activeInterlocutor,
}),dispatch => ({
    fetchInterlocutors  : (...args) => dispatch(ajaxInterlocAction(...args)),
    fetchMessages       : (...args) => dispatch(ajaxMessagesAction(...args)),
}))(Messaging)