import React, {memo, useEffect, useState} from 'react'
import {connect} from "react-redux";
import {ajaxDataAction, ajaxInterlocAction, ajaxMessagesAction} from "../../redux/actionCreators";
import Button from "react-bootstrap/Button";
import {ajax, placeCaretAtEnd, cursorTo, getCursorPos} from "../../utils";
import {loc, restPrefix} from "../../config";
import {Alert} from "react-bootstrap";
import {store} from "../../App"

/**
 * Edit and send unReady or Ready message. See messageSendDiagrams.svg
 */
const MessageInput = ({activeInterlocutorId,fetchMessages}) => {
    const initialState = {
        cursorPos   : 0,
        text        : "",
        error       : false, // should we render the error banner
    }
    const [state,setState] = useState(initialState)
    const [idStore,setIdStore] = useState({id : null})
    const setId = (x) => idStore.id = x

    useEffect(() => {
        setState(initialState)
    },[activeInterlocutorId])

    useEffect(() => {
        cursorTo(messageTextArea,state.cursorPos)
    })

    // useEffect(() => {
    //     // just place the caret to the end of the text
    //     if ("messageTextArea" in window) {
    //         placeCaretAtEnd(messageTextArea)
    //     }
    // },[])

    let interlocutorId = store.getState().messageReducer.activeInterlocutorId

    const updateMessage = (text, unReady) => {
        ajax(restPrefix + "updateMessage", {_id : idStore.id, text: text, unReady : unReady}, "PATCH")
            .then((result) => {
                fetchMessages(interlocutorId) //todo optimize
            })
    }

    const handleChange = e => {
        console.log(`handleChange idStore.id=${idStore.id} messageTextArea=${messageTextArea.innerText} state=`,state)
        if (e.keyCode === 13 && !e.ctrlKey && !e.shiftKey && messageTextArea.innerText.match(/\n$/)) {
            messageTextArea.innerText = messageTextArea.innerText.replace(/\n$/,"")
            handleSubmit(e)
            return
        }
        let text = messageTextArea.innerText
        if (text.length > 4000) {
            setState({...state,...{error: true},...{text:messageTextArea.innerText}})
            return
        }
        //if text has been changed
        if (state.text !== text) {
            let newState = {text : text,cursorPos : getCursorPos(messageTextArea)}
            //if it is the first change
            if (idStore.id === null) {
                setId("0")
                console.log(`newMessage "${text}"`)
                ajax(restPrefix + "newMessage", {whomId : interlocutorId, text: text, unReady : true}, "POST")
                    .then((result) => {
                        // if text has been changed during waiting for _id
                        setId(result)
                        console.log("updateMessage I id="+result)
                        if (messageTextArea.innerText !== text) updateMessage(messageTextArea.innerText, true) /* I */
                        fetchMessages(interlocutorId) //todo optimize
                    })
            } else if (idStore.id !== "0") {
                console.log("updateMessage II text=",text)
                updateMessage(text, true) /* II */
            }
            setState(newState)
        }
    }

    const handleSubmit = () => {
        console.log("handleSubmit",messageTextArea.innerText,state)
        if (idStore.id === null || idStore.id === "0") return
        let text = messageTextArea.innerText
        // setState({text: text}) // to force render
        // input field is too long
        if (text.length > 4000) {
            setState({...state,error: true})
            return
        }
        console.log("updateMessage III")
        updateMessage(text,false) /* III */
        setState(initialState)
        setId(null)
    }

    console.log(`INPUT RENDER id=${idStore.id} state=`,state)

    //If user has deleted all chars - remove the message
    if (idStore.id !== null && idStore.id !== "0" && state.text === "") {
        ajax(restPrefix + "removeMessage", {_id : idStore.id }, "DELETE")
            .then((result) => {
                setId(null)
                fetchMessages(interlocutorId) //todo optimize
            })
    }

    return <div>
        {state.error && <div><Alert id="messageErrSign" variant="danger">{loc.messageIsTooLong}</Alert></div>}
        <div contentEditable id="messageTextArea" onKeyUp={handleChange} style={{whiteSpace: "pre-wrap"}} suppressContentEditableWarning={true}>
            {state.text}
        </div>
        <Button variant="primary" id="messageSendButton"
                onClick={handleSubmit}
                disabled={state.error || state.text === ""}
        >{loc.sendMessage}</Button>
    </div>
}
export default connect(state => ({
    activeInterlocutorId: state.messageReducer.activeInterlocutorId,
}),dispatch => ({
    fetchMessages       : (...args) => dispatch(ajaxMessagesAction(...args)),
}))(MessageInput)