import React, {memo, useEffect, useState} from 'react'
import {connect} from "react-redux";
import {
    ajaxMessagesAction,
    setMessagesParamAction
} from "../../redux/actionCreators";
import Button from "react-bootstrap/Button";
import {ajax, cursorTo, getCursorPos} from "../../utils";
import {loc, restPrefix} from "../../config";
import {Alert} from "react-bootstrap";
import {store} from "../../App"

/**
 * Edit and send unReady or Ready message. See messageSendDiagrams.svg
 */
const MessageInput = ({activeInterlocutor,fetchMessages}) => {
    const zeroState = {
        cursorPos   : 0,
        text        : "",
        error       : false, // should we render the error banner
    }
    const messageEditFields = store.getState().messageReducer.messageEditFields
    if (!messageEditFields[activeInterlocutor._id]) {
        messageEditFields[activeInterlocutor._id] = {text : "", cursorPos : 0, activeMessageId : null}
    }
    const activeEditField = messageEditFields[activeInterlocutor._id]
    const initialState = {
        cursorPos   : activeEditField.cursorPos,
        text        : activeEditField.text,
        error       : false, // should we render the error banner
    }
    const [state,setState] = useState(initialState)
    console.log("MessageInput RENDER activeInterlocutor=",activeInterlocutor)
    console.log("store=",store.getState().messageReducer.messageEditFields)
    console.log("state=",state)
    const setId = (x) => {
        activeEditField.activeMessageId = x
    }
    const getId = () => activeEditField.activeMessageId

    useEffect(() => {
        setState(initialState)
    },[activeInterlocutor])

    useEffect(() => {
        cursorTo(messageTextArea,state.cursorPos)
    })

    const updateMessage = (text, unReady, manualUpdate = true) => {
        if (manualUpdate) activeEditField.changeLastMessage(text)
        activeEditField.text = text
        activeEditField.cursorPos = getCursorPos(messageTextArea)
        ajax(restPrefix + "updateMessage", {
            _id     : getId(),
            text    : text,
            whomId  : activeInterlocutor.userId,
            unReady : unReady
        }, "PATCH")
            .then((result) => {
                if (!unReady) fetchMessages(activeInterlocutor)
            })
    }

    const handleChange = e => {
        // console.log(`handleChange id=${getId()} messageTextArea=${messageTextArea.innerText} state=`,state)
        if (e.keyCode === 13 && !e.ctrlKey && !e.shiftKey && messageTextArea.innerText.match(/\n$/)) {
            messageTextArea.innerText = messageTextArea.innerText.replace(/\n+$/,"")
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
            if (getId() === null) {
                setId("0")
                ajax(restPrefix + "newMessage", {whomId : activeInterlocutor.userId, text: text, unReady : true}, "POST")
                    .then((result) => {
                        // if text has been changed during waiting for _id
                        setId(result)
                        // let data = store.getState().messageReducer.data
                        // let newContent = [{
                        //     _id     : result,
                        //     text    : messageTextArea.innerText,
                        //     time    : `${(new Date()).getHours()}:${(new Date()).getMinutes()}`,
                        //     unReady : true,
                        //     userId  : window.userId,
                        //     userName: window.userName,
                        //     whomName: activeInterlocutor.userName,
                        //     whomId  : activeInterlocutor.userId,
                        // },...data.content]
                        // let newData = {...data,content : newContent}
                        // setMessagesParam({data : newData})
                        fetchMessages(activeInterlocutor) // wo it MessageScroll will have dups
                        if (messageTextArea.innerText !== text) {
                            updateMessage(messageTextArea.innerText, true, false) /* I */
                        }
                    })
            } else if (getId() !== "0") {
                updateMessage(text, true) /* II */
            }
            setState(newState)
        }
    }

    const handleSubmit = () => {
        if (getId() === null || getId() === "0") return
        let text = messageTextArea.innerText
        // setState({text: text}) // to force render
        // input field is too long
        if (text.length > 4000) {
            setState({...state,error: true})
            return
        }
        updateMessage(text,false) /* III */

        activeEditField.text = ""
        activeEditField.cursorPos = 0
        activeEditField.activeMessageId = null

        setId(null)
        setState(zeroState)
    }

    //If user has deleted all chars - remove the message
    if (getId() !== null && getId() !== "0" && activeEditField.text === "") {
        ajax(restPrefix + "removeMessage", {_id : getId(), whomId : activeInterlocutor.userId}, "DELETE")
            .then((result) => {
                setId(null)
                fetchMessages(activeInterlocutor)
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
    activeInterlocutor  : state.messageReducer.activeInterlocutor,
}),dispatch => ({
    fetchMessages       : (...args) => dispatch(ajaxMessagesAction(...args)),
    setMessagesParam    : (...args) => dispatch(setMessagesParamAction(...args)),
}))(MessageInput)