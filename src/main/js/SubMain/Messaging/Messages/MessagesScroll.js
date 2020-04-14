import React, {memo, useEffect, useState} from 'react'
import {connect} from "react-redux";
import Message from "./Message";
import {ajax, placeCaretAtEnd} from "../../../utils";
import InfiniteScroll from 'react-infinite-scroller'
import {restPrefix} from "../../../config";
import Badge from "react-bootstrap/Badge";
import Messages from "./Messages";

const MessagesScroll = props => {
    console.log("MessagesScroll RENDER data=",props.data)
    const initialState = {
        messages    : props.data.content.reverse(),
        page        : 0,
        totalPages  : 1,
        lastPage    : props.data.last,
    }
    const [state,setState] = useState(initialState)
    const loadFunc = page => {
        ajax(restPrefix + "messages", {whomId: props.activeInterlocutor.userId,page:page,size:MESSAGES_PAGE_SIZE}, "GET")
            .then((response) => {
                let result = JSON.parse(response)
                setState({
                    messages    : [...result.content.reverse(), ...state.messages],
                    page        : result.number,
                    totalPages  : result.totalPages,
                    lastPage    : result.last})
            })
    }
    // useEffect(() =>  {
    //     setState(initialState)
    // },[props.data])
    useEffect(() => {
        if (state.page <= 1) {
            //Workaround For IE
            if (scrollTo in messagesScroll) messagesScroll.scrollTo(0,messagesScroll.scrollHeight);
            else messagesScroll.scrollTop = messagesScroll.scrollHeight;
        }
    })
    return <div id={"messagesScroll"}>
        <InfiniteScroll
            isReverse={true}
            pageStart={0}
            loadMore={loadFunc}
            hasMore={!state.lastPage}
            loader={<div key={0}><Badge pill variant={"secondary"}>{Math.round((state.page+1) / state.totalPages * 100) + "%"}</Badge></div>}
            useWindow={false}
        >
            {state.messages.map(x => <Message key={x._id} x={x} setLastMessageFunc={props.setLastMessageFunc}/>)}
        </InfiniteScroll>
    </div>
}
export default memo(MessagesScroll)