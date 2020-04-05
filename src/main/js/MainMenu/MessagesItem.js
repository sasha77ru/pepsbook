import React, {memo} from "react";
import PropTypes from "prop-types";
import {loc} from "../config";
import {connect} from "react-redux";
import Badge from "react-bootstrap/Badge";

const MessageItem = props => {
    const handleClick = e => {
        e.preventDefault()
        props.switchTo("messages")
    }
    // count overall numNewMessages in interlocutors
    let numNewMessages = (props.isLoaded)
        ? props.data.reduce((a,x) => a+x.numNewMessages,0)
        : 0
    // is some hasPreMessages in interlocutors
    let hasPreMessages = (props.isLoaded)
        ? props.data.some(x => x.hasPreMessages)
        : false
    return (
        <a className={props.nowInMain === "messages" ? "nav-link active" : "nav-link"}
           id={"mainMessages"}
           href={"#" + loc.mainMenuTexts["messages"]}
           onClick={handleClick}
        >{loc.mainMenuTexts["messages"]}{" "}
        {// badges numNewMessages or hasPreMessages
            (numNewMessages > 0)
                ? <Badge variant="danger">{numNewMessages}</Badge>
                : ((hasPreMessages)
                    ? <Badge variant="warning">*</Badge>
                    : "")
        }
        </a>
    )
}
MessageItem.propTypes = {
    switchTo:   PropTypes.func.isRequired,
    nowInMain:  PropTypes.string.isRequired,
}
export default connect(state => ({
    isLoaded            : state.interlocReducer.isLoaded,
    data                : state.interlocReducer.data,
}))(MessageItem)