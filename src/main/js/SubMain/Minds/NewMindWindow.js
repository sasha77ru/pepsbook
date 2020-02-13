import React, {Component} from "react";
import PropTypes from "prop-types";
import {ajax, placeCaretAtEnd} from "../../utils";
import {loc, restPrefix} from "../../config";
import {Alert, Button, Modal} from "react-bootstrap";

/** Renders modalWindow to create and edit minds and answers */
export class NewMindWindow extends Component {
    static propTypes = {
        setNewMindWindow: PropTypes.func,
        freshPage: PropTypes.func,
    }

    constructor(props) {
        super(props);
        this.state = {
            show:   false, // is modal active
            error:  false, // should we render the error banner
        }
    }

    setShow = (show) => {
        this.setState({show: show})
    }
    /**
     * Shows modalWindow ands sets state
     * @param what - what to create/edit, "mind" or "answer"
     * @param mind - edited mind or answer parent mind
     * @param answer - edited answer ()if what = answer
     * @param text - initial text in input field (if we create a new answer, answering to smds answer eg "@OtherGuy ")
     */
    openNewMindWindow = (what, mind, answer, text) => {
        this.setState({show: true, error: false, what: what, mind: mind, answer: answer, text: text})
    }
    handleSubmit = () => {
        // input field is too long
        if (mindTextArea.innerText.length > 4000) {
            this.setState({error: true})
            return
        }
        // to replace &nbsp; with space at the end of text
        const text = mindTextArea.innerText.replace(/\xa0/g, " ")
        const {what, mind, answer} = this.state
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
                this.setShow(false)
                this.props.freshPage((what === "mind" && !mind) ? 0 : undefined)
            })
    }

    componentDidUpdate() {
        // just place the caret to the end of the text
        if ("mindTextArea" in window) {
            mindTextArea.focus()
            placeCaretAtEnd(mindTextArea)
        }
    }

    render() {
        return <>
            <Modal
                id="mindWindow"
                show={this.state.show}
                onHide={() => this.setShow(false)}
                dialogClassName="modalWindow"
                aria-labelledby="styling-title">
                <Modal.Header closeButton>
                    <Modal.Title id="styling-title">{loc.newMindWhat[this.state.what]}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    {this.state.error && <Alert id="mindErrSign" variant="danger">{loc.mindIsTooLong}</Alert>}
                    <div contentEditable id="mindTextArea" suppressContentEditableWarning={true}>
                        {
                            this.state.text // id we create an answer to another answer, initial text will like "@OtherGuy "
                            || this.state.answer && this.state.answer.text // if we edit an answer
                            || this.state.what === "mind" && this.state.mind && this.state.mind.text // if we edit a mind
                        }
                    </div>
                </Modal.Body>
                <Modal.Footer>
                    <Button variant="primary" onClick={this.handleSubmit}>{loc.publish}</Button>
                </Modal.Footer>
            </Modal>
        </>
    }
}