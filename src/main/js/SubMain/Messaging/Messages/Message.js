import React, {memo} from 'react'
import * as PropTypes from "prop-types";
import Alert from "react-bootstrap/Alert";
import Badge from "react-bootstrap/Badge";
import Button from "react-bootstrap/Button";
import OverlayTrigger from "react-bootstrap/OverlayTrigger";
import Tooltip from "react-bootstrap/Tooltip";

const Message = ({x}) => {
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
                <div style={{whiteSpace: "pre-wrap"}}>{x.text}</div>
            </Alert>
        </OverlayTrigger>
    </div>
}
Message.propTypes = {
    x : PropTypes.shape({
        text    : PropTypes.string.isRequired,
        time    : PropTypes.string.isRequired,
        userId  : PropTypes.string.isRequired,
    }).isRequired,
}
export default memo(Message)