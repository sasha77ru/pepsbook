import React, {memo, useEffect, useState} from 'react'
import {connect} from "react-redux";
import {ajaxDataAction, ajaxInterlocAction, ajaxMessagesAction} from "../../redux/actionCreators";
import Button from "react-bootstrap/Button";
import {ajax, placeCaretAtEnd} from "../../utils";
import {loc, restPrefix} from "../../config";
import {Alert} from "react-bootstrap";
import {store} from "../../App"

/**
 * Edit and send unReady or Ready message. See messageSendDiagrams.svg
 */
const MessageInput = ({fetchMessages}) => {
    const initialState = {
        text        : "",
        error       : false, // should we render the error banner
    }
    const [state,setState] = useState(initialState)
    const [id,setId] = useState(null)

    useEffect(() => {
        // just place the caret to the end of the text
        if ("messageTextArea" in window) {
            messageTextArea.focus()
            placeCaretAtEnd(messageTextArea)
        }
    })

    let interlocutorId = store.getState().messageReducer.activeInterlocutorId

    const updateMessage = (text, unReady) => {
        console.log(`updateMessage ${text} ${unReady} ${id}`)
        ajax(restPrefix + "updateMessage", {_id : id, text: text, unReady : unReady}, "PATCH")
            .then((result) => {
                fetchMessages(interlocutorId) //todo optimize
            })
    }

    const handleChange = () => {
        let text = messageTextArea.innerText
        if (text.length > 4000) {
            setState({...state,...{error: true},...{text:messageTextArea.innerText}})
            return
        }
        //if text has been changed
        if (state.text !== text) {
            let newState = {text:text}
            //if it is the first change
            if (id === null) {
                setId("0")
                console.log(`newMessage "${text}"`)
                ajax(restPrefix + "newMessage", {whomId : interlocutorId, text: text, unReady : true}, "POST")
                    .then((result) => {
                        // if text has been changed during waiting for _id
                        setId(result)
                        if (messageTextArea.innerText !== text) updateMessage(messageTextArea.innerText, true) /* I */
                        fetchMessages(interlocutorId) //todo optimize
                    })
            } else if (id !== "0") {
                updateMessage(text, true) /* II */
            }
            setState(newState)
        }
    }

    const handleSubmit = () => {
        let text = messageTextArea.innerText
        setState({text: text}) // to force render
        // input field is too long
        if (text.length > 4000) {
            setState({...state,error: true})
            return
        }
        updateMessage(messageTextArea.innerText,false) /* III */
        setId(null)
        setState(initialState)
    }

    console.log(`INPUT RENDER state=`,state)

    //If user has deleted all chars - remove the message
    if (id !== null && id !== "0" && state.text === "") {
        ajax(restPrefix + "removeMessage", {_id : id }, "DELETE")
            .then((result) => {
                setId(null)
                fetchMessages(interlocutorId) //todo optimize
            })
    }

    return <div>
        {state.error && <div><Alert id="messageErrSign" variant="danger">{loc.messageIsTooLong}</Alert></div>}
        <div contentEditable id="messageTextArea" onKeyUp={handleChange} suppressContentEditableWarning={true}>
            {state.text}
        </div>
        <Button variant="primary" id="messageSendButton"
                onClick={handleSubmit}
                disabled={id === null || id === "0" || state.error || state.text === ""}
        >{loc.sendMessage}</Button>
    </div>
}
export default connect(null,dispatch => ({
    fetchMessages       : (...args) => dispatch(ajaxMessagesAction(...args)),
}),null,{pure : false})(MessageInput)