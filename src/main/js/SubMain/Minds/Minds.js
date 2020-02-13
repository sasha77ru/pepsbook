import React from 'react'
import {loc} from "../../config";
import {Button} from "react-bootstrap";
import PropTypes from "prop-types";
import {Mind} from "./Mind";
import {NewMindWindow} from "./NewMindWindow";

export let Minds = props => {
    const newMindWindowRef = React.createRef()
    const handleNewMind = () => {
        window.scrollTo(0, 0)
        newMindWindowRef.current.openNewMindWindow("mind")
    }
    return <>
        {props.data.content.map((mind) =>
            <Mind key={mind.id} mind={mind}
                  openNewMindWindow={(...args) => newMindWindowRef.current.openNewMindWindow(...args)}
                  freshPage={props.freshPage}
            />)}
        <Button id="newMind" variant="primary"
                onClick={handleNewMind}>{loc.newMind}</Button>
        <NewMindWindow ref={newMindWindowRef} freshPage={props.freshPage}/>
    </>
}
Minds.propTypes = {
    freshPage: PropTypes.func.isRequired,
}
// noinspection JSValidateTypes
Minds = React.memo(Minds)
