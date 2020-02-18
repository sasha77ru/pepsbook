import React, {memo, useState} from 'react'
import {loc} from "../../config";
import {Button} from "react-bootstrap";
import PropTypes from "prop-types";
import {Mind} from "./Mind";
import {NewMindWindow} from "./NewMindWindow";

export let Minds = props => {
    const [state] = useState({onmwFun : null})
    const handleNewMind = () => {
        window.scrollTo(0, 0)
        state.onmwFun("mind")
    }
    const setONMWFun = (fun) => state.onmwFun = fun
    return <>
        {props.data.content.map((mind) =>
            <Mind key={mind.id} mind={mind}
                  openNewMindWindow={(...args) => state.onmwFun(...args)}
                  freshPage={props.freshPage}
            />)}
        <Button id="newMind" variant="primary"
                onClick={handleNewMind}>{loc.newMind}</Button>
        <NewMindWindow setONMWFun={setONMWFun} freshPage={props.freshPage}/>
    </>
}
Minds.propTypes = {
    freshPage: PropTypes.func.isRequired,
}
// noinspection JSValidateTypes
Minds = memo(Minds)
