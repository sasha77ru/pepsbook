import React, {memo, useEffect, useState} from "react";
import PropTypes from "prop-types";
import {ajax, placeCaretAtEnd} from "../../utils";
import {loc, restPrefix} from "../../config";
import {Alert, Button, Modal} from "react-bootstrap";

/** Renders modalWindow to create and edit minds and answers */
const NewMindWindow = (props) => {
    const [state,setMyState] = useState({
        show:   false, // is modal active
        error:  false, // should we render the error banner
    })
    const setState = (x) => {setMyState({...state,...x})}

    useEffect(() => {
        // just place the caret to the end of the text
        if ("mindTextArea" in window) {
            mindTextArea.focus()
            placeCaretAtEnd(mindTextArea)
        }
    })

    const setShow = (show) => {
        setState({show: show})
    }

    /**
     * Shows modalWindow ands sets state
     * @param what - what to create/edit, "mind" or "answer"
     * @param mind - edited mind or answer parent mind
     * @param answer - edited answer ()if what = answer
     * @param text - initial text in input field (if we create a new answer, answering to smds answer eg "@OtherGuy ")
     */
    const openNewMindWindow = (what, mind, answer, text) => {
        setState({show: true, error: false, what: what, mind: mind, answer: answer, text: text})
    }
    props.setONMWFun(openNewMindWindow)

    const handleSubmit = () => {
        // input field is too long
        if (mindTextArea.innerText.length > 4000) {
            setState({error: true})
            return
        }
        // to replace &nbsp; with space at the end of text
        const text = mindTextArea.innerText.replace(/\xa0/g, " ")
        const {what, mind, answer} = state
        let params = {text: text}
        // form ajax params
        if (what === "mind") {
            mind && (params.id = mind.id)
        } else {
            answer && (params.id = answer.id)
            params.parentMind = mind.id
        }
        ajax(restPrefix + "save" + what[0].toUpperCase() + what.slice(1), params, "POST")
            .then(() => {
                setShow(false)
                props.freshPage((what === "mind" && !mind) ? 0 : undefined)
            })
    }

    return <>
        <Modal
            id="mindWindow"
            show={state.show}
            onHide={() => setShow(false)}
            dialogClassName="modalWindow"
            aria-labelledby="styling-title">
            <Modal.Header closeButton>
                <Modal.Title id="styling-title">{loc.newMindWhat[state.what]}</Modal.Title>
            </Modal.Header>
            <Modal.Body>
                {state.error && <Alert id="mindErrSign" variant="danger">{loc.mindIsTooLong}</Alert>}
                <div contentEditable id="mindTextArea" suppressContentEditableWarning={true}>
                    {
                        state.text // initial text like "@OtherGuy ", also on rerender text will be kept
                        || state.answer && state.answer.text // if we edit an answer
                        || state.what === "mind" && state.mind && state.mind.text // if we edit a mind
                    }
                </div>
            </Modal.Body>
            <Modal.Footer>
                <Button variant="primary" onClick={handleSubmit}>{loc.publish}</Button>
            </Modal.Footer>
        </Modal>
    </>
}
NewMindWindow.propTypes = {
    freshPage:  PropTypes.func,
    setONMWFun: PropTypes.func,
}
export default (NewMindWindow)