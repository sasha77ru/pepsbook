import React, {memo, useEffect} from 'react'
import {connect} from "react-redux";
import Interlocutor from "./Interlocutor";
import Card from "react-bootstrap/Card";
import {loc} from "../../../config"
import {ajaxMessagesAction} from "../../../redux/actionCreators";

const Interlocutors = props => {
    if (props.isLoaded) { // noinspection EqualityComparisonWithCoercionJS
        if (props.data.length > 0) {
            return props.data.map(x => {
                if (props.activeInterlocutor && (x.hasPreMessages || x.numNewMessages))
                    props.fetchMessages(props.activeInterlocutor)
                return <Interlocutor key={x._id} x={x} isActive={props.activeInterlocutor == x}/>
            });
        } else {
            return <div style={{position:"absolute",left:"1rem",right:"1rem",zIndex : 1}}>
                <Card border="danger">
                    <Card.Header>{loc.noInterlocutors}</Card.Header>
                    <Card.Body>
                        <Card.Text>{loc.addInterlocutors}</Card.Text>
                    </Card.Body>
                </Card>
            </div>
        }
    } else return false
}
export default connect(state => ({
    isLoaded            : state.interlocReducer.isLoaded,
    data                : state.interlocReducer.data,
    activeInterlocutor  : state.messageReducer.activeInterlocutor,
}),dispatch => ({
    fetchMessages       : (...args) => dispatch(ajaxMessagesAction(...args)),
}),null,{areStatesEqual : (next,prev) => {
        return !next.interlocReducer.isLoaded
    }})(Interlocutors)