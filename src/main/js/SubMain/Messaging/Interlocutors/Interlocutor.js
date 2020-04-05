import React, {memo, useEffect} from 'react'
import {connect} from "react-redux";
import {setMessagesParamAction} from "../../../redux/actionCreators";
import * as PropTypes from "prop-types";
import Button from "react-bootstrap/Button";
import Badge from "react-bootstrap/Badge";

const Interlocutor = props => {
    return <div>
        <Button
            className={"interlocutor"}
            variant={props.isActive ? "success" : "primary"}
            onClick={() => props.changeActiveInterlocutorId(props.x.userId)}
        >
            {props.x.userName}{" "}
            {(props.x.numNewMessages > 0)
                ? <Badge variant="danger">{props.x.numNewMessages}</Badge>
                : ((props.x.hasPreMessages)
                    ? <Badge variant="warning">*</Badge>
                    : "")
            }
        </Button>
    </div>
}
Interlocutor.propTypes = {
    x : PropTypes.shape({
        userName        : PropTypes.string.isRequired,
        userId          : PropTypes.string.isRequired,
        numNewMessages  : PropTypes.number.isRequired,
        hasPreMessages  : PropTypes.bool.isRequired,
    }).isRequired,
    isActive : PropTypes.bool,
    fetchMessages : PropTypes.func,
}
export default connect(null,dispatch => ({
    changeActiveInterlocutorId: (interlocutorId) => dispatch(setMessagesParamAction({activeInterlocutorId: interlocutorId})),
}))(Interlocutor)