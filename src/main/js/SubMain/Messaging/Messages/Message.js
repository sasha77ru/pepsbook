import React, {memo, useEffect, useState} from 'react'
import * as PropTypes from "prop-types";
import Alert from "react-bootstrap/Alert";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Tooltip from "react-bootstrap/Tooltip";
import {ajaxMessagesAction} from "../../../redux/actionCreators";
import {store} from "../../../App";
import {wSocket} from "../../../utils";
import {connect} from "react-redux";

const Message = ({x,fetchMessages}) => {
    const [text,setText] = useState(x.text)
    let activeInterlocutor = store.getState().messageReducer.activeInterlocutor
    let activeEditField = store.getState().messageReducer.messageEditFields[activeInterlocutor._id]
    let activeMessageId = activeEditField && activeEditField.activeMessageId
    // if it is active message (owner message, that is edited). It can be changed by func
    if (x._id === activeMessageId) {
        store.getState().messageReducer.messageEditFields[activeInterlocutor._id].changeLastMessage = (x) => {setText(x)}
    }
    // if it's other guys unReady message. It can be changed by ws
    if (x.unReady && x.userId !== window.userId) {
        useEffect(() => {
            let socket = wSocket("messageUpdate","updateMessage/"+window.userId,(answer) => {
                if (answer._id === x._id) {
                    if (answer.text) setText(answer.text);
                    else fetchMessages(store.getState().messageReducer.activeInterlocutor) // if message is deleted
                }
            })
            return () => {socket.close()}
        },[])
    }
    // noinspection EqualityComparisonWithCoercionJS
    return <div
        className={"message "+(x.userId == window.userId ? "ownMessage" : "foreignMessage")}>
        <OverlayTrigger
            placement={"bottom"}
            overlay={
                <Tooltip id={x._id}>
                    <span className={"messageTime"}>{x.time}</span>
                </Tooltip>
            }
        >
            <Alert variant={x.unReady ? "warning" : (x.userId == window.userId ? "success" : "info")}>
                <div style={{whiteSpace: "pre-wrap"}}>{text}</div>
            </Alert>
        </OverlayTrigger>
    </div>
}
Message.propTypes = {
    x : PropTypes.shape({
        text    : PropTypes.string.isRequired,
        time    : PropTypes.string.isRequired,
        userId  : PropTypes.number.isRequired,
    }).isRequired,
}
export default connect(null,dispatch => ({
    fetchMessages       : (...args) => dispatch(ajaxMessagesAction(...args)),
}))(Message)