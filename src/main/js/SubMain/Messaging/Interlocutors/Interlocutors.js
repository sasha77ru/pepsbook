import React, {memo, useEffect} from 'react'
import {connect} from "react-redux";
import Interlocutor from "./Interlocutor";
import Card from "react-bootstrap/Card";
import {loc} from "../../../config"

const Interlocutors = props => {
    if (props.isLoaded) { // noinspection EqualityComparisonWithCoercionJS
        if (props.data.length > 0) {
            return props.data.map(x => <Interlocutor key={x._id} x={x}
                                                     isActive={props.activeInterlocutorId == x.userId}/>);
        } else {
            return <div style={{position:"absolute",left:"1rem",right:"1rem"}}>
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
    activeInterlocutorId: state.messageReducer.activeInterlocutorId,
}),null,null,{areStatesEqual : (next,prev) => {
        return !next.interlocReducer.isLoaded
    }})(Interlocutors)