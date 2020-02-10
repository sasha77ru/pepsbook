import React, {Component} from 'react'
import {ajax, noTag, placeCaretAtEnd} from "./utils";
import {loc, restPrefix} from "./config";
import {Alert, Button, Modal} from "react-bootstrap";
import PropTypes from "prop-types";

class Mind extends Component {
    static propTypes = {
        mind : PropTypes.exact({
            id      : PropTypes.number.isRequired,
            text    : PropTypes.string.isRequired,
            author  : PropTypes.string.isRequired,
            time    : PropTypes.string.isRequired,
            isAuthor: PropTypes.bool.isRequired,
            answers : PropTypes.arrayOf(PropTypes.object).isRequired,
        }),
        openNewMindWindow   : PropTypes.func.isRequired,
        freshPage           : PropTypes.func,
    }
    handleClickAnswer = (e) => {
        e.preventDefault()
        this.props.openNewMindWindow("answer",this.props.mind)
    }
    handleClickEdit = (e) => {
        e.preventDefault()
        this.props.openNewMindWindow("mind",this.props.mind)
    }
    handleClickDel = (e) => {
        e.preventDefault()
        ajax(restPrefix+"removeMind",{id:this.props.mind.id},"DELETE")
            .then(() => this.props.freshPage())
    }
    /** Items that are shown for author only */
    authorMenuItems = () => {
        if (this.props.mind.isAuthor) return (
            <React.Fragment>
                <a className="dropdown-item ownerMenu editMind" href="#"
                   onClick={this.handleClickEdit}>{loc.toEdit}</a>
                <a className="dropdown-item ownerMenu delMind" href="#"
                   onClick={this.handleClickDel}>{loc.toDelete}</a>
            </React.Fragment>
        )
    }
    dropDown = () => {
        return (
            <span className="nav-item dropdown mindDropDown">
                <a style={{display: "inline"}} className="nav-link dropdown-toggle"
                   data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"/>
                   <div className="dropdown-menu forDropDown" x-placement="bottom-start">
                       <a className="dropdown-item answerMind" href="#"
                          onClick={this.handleClickAnswer}>{loc.toAnswer}</a>
                       {this.authorMenuItems()}
                   </div>
            </span>
        )
    }
    render () {
        let {mind} = this.props;
        return (
            <div className={"card mb-3 mindEntity " + (mind.isAuthor ? "border-primary" : "border-light")}>
                <div className="card-header">
                    <span className="mindTime">{mind.time}</span>
                    {this.dropDown()}
                </div>
                <div className="card-body">
                    <h4 className="card-title mindUser">{noTag(mind.author)}</h4>
                    <p className="card-text mindText">{noTag(mind.text)}</p>
                    {mind.answers.map((answer) =>
                        <Answer key={answer.id} mind={mind} answer={answer}
                                openNewMindWindow={this.props.openNewMindWindow}
                                freshPage={this.props.freshPage}
                        />)}
                </div>
            </div>
        );
    }
}

class Answer extends Component {
    static propTypes = {
        mind    : PropTypes.object,
        answer  : PropTypes.exact({
            id      : PropTypes.number.isRequired,
            text    : PropTypes.string.isRequired,
            author  : PropTypes.string.isRequired,
            time    : PropTypes.string.isRequired,
            isAuthor: PropTypes.bool.isRequired,
        }).isRequired,
        openNewMindWindow   : PropTypes.func.isRequired,
        freshPage           : PropTypes.func,
    }
    handleClickAnswer = (e) => {
        e.preventDefault()
        let {answer,mind} = this.props;
        this.props.openNewMindWindow("answer",mind,undefined,"@"+answer.author+"\xa0")
    }
    handleClickEdit = (e) => {
        e.preventDefault()
        let {mind,answer} = this.props;
        this.props.openNewMindWindow("answer",mind,answer)
    }
    handleClickDel = (e) => {
        e.preventDefault()
        ajax(restPrefix+"removeAnswer",{id:this.props.answer.id},"DELETE")
            .then(() => this.props.freshPage())
    }
    /** Items that are shown for author only */
    authorMenuItems = () => {
        if (this.props.answer.isAuthor) return (
            <React.Fragment>
                <a className="dropdown-item ownerMenu editAnswer" href="#"
                   onClick={this.handleClickEdit}>{loc.toEdit}</a>
                <a className="dropdown-item ownerMenu delAnswer" href="#"
                   onClick={this.handleClickDel}>{loc.toDelete}</a>
            </React.Fragment>
        )
    }
    dropDown = () => {
        return (
            <span className="nav-item dropdown answerDropDown">
                <a style={{display: "inline"}} className="nav-link dropdown-toggle"
                   data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"/>
                   <div className="dropdown-menu forDropDown" x-placement="bottom-start">
                       <a className="dropdown-item answerAnswer" href="#"
                          onClick={this.handleClickAnswer}>{loc.toAnswer}</a>
                       {this.authorMenuItems()}
                   </div>
            </span>
        )
    }
    render() {
        let {answer} = this.props;
        return (
            <div className="answerEntity">
                <strong className="answerUser" style={{marginRight: "1em"}}>{noTag(answer.author)}</strong>
                <span className="answerText">{noTag(answer.text)}</span>
                <em className="answerTime" style={{marginLeft: "1em"}}>{answer.time}</em>
                {this.dropDown()}
            </div>
        )
    }
}

export class Minds extends Component {
    static propTypes = {
        freshPage   : PropTypes.func.isRequired,
    }
    // NewMindWindow sends itself back in constructor using this callback
    newMindWindow
    setNewMindWindow = (newMindWindow) => {
        this.newMindWindow = newMindWindow
    }
    handleNewMind = () => {
        window.scrollTo(0,0)
        this.newMindWindow.openNewMindWindow("mind")
    }
    render () {
        return <>
            {this.props.data.content.map((mind) =>
                <Mind key={mind.id} mind={mind}
                      openNewMindWindow={(...args) => this.newMindWindow.openNewMindWindow(...args)}
                      freshPage={this.props.freshPage}
                />)}
            <Button id="newMind" variant="primary"
                    onClick={this.handleNewMind}>{loc.newMind}</Button>
            <NewMindWindow setNewMindWindow={this.setNewMindWindow} freshPage={this.props.freshPage}/>
        </>
    }
}

/** Rendesr modalWindow to create and edit minds and answers */
class NewMindWindow extends Component {
    static propTypes = {
        setNewMindWindow: PropTypes.func,
        freshPage: PropTypes.func,
    }

    constructor(props) {
        super(props);
        this.state = {
            show: false, // is modal active
            error: false, // should we render the error banner
        }
        this.props.setNewMindWindow(this)
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

