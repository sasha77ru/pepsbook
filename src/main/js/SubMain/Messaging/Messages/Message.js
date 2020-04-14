import React, {memo, useEffect, useState} from 'react'
import * as PropTypes from "prop-types";
import Alert from "react-bootstrap/Alert";
import Badge from "react-bootstrap/Badge";
import Button from "react-bootstrap/Button";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Tooltip from "react-bootstrap/Tooltip";
import Messages from "./Messages";
import SockJS from "sockjs-client";
import {ajaxInterlocAction} from "../../../redux/actionCreators";
import {store} from "../../../App";
import {getJwtToken, wSocket} from "../../../utils";

const Message = ({x, setLastMessageFunc}) => {
    const [text,setText] = useState(x.text)
    console.log("Message RENDER text=",text)
    if (x.manual) setLastMessageFunc((x) => {setText(x)})
    if (x.unReady && x.userId !== window.userId) {
        useEffect(() => {
            let socket = wSocket("messageUpdate","updateMessage",(answer) => {
                if (answer._id === x._id) setText(answer.text)
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
                <Tooltip>
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
export default memo(Message)