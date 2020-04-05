import React, {memo, useEffect, useState} from 'react'
import PropTypes from 'prop-types'
import {connect} from "react-redux"
import {loc, WAIT_BEFORE_SUBMIT_FILTER} from "../config";
import {ajaxDataAction} from "../redux/actionCreators";
import Minds from "./Minds/Minds";
import Users from "./Users/Users";
import Paginator from "./Paginator";
import Messages from "./Messaging/Messages/Messages";
import Interlocutors from "./Messaging/Interlocutors/Interlocutors";
import Messaging from "./Messaging/Messaging";
import User from "./Users/User";

const SubMain = props => {
    console.log("SubMain RENDER")
    const [state,setMyState] = useState({page : props.page})
    const setState = (x) => {setMyState({...state,...x})}
    const [y] = useState({value : null})

    /** Remember scroll position */
    if (!props.isLoaded) y.value = window.pageYOffset
    useEffect(() => {if (y.value !== null) window.scrollTo(0,y.value)})

    /** Change the pages and refresh it. Eventual page number will be gotten from Rest */
    const setPage = (page) => {
        freshPage(page)
    }
    /** Refreshes the page. Eventual page number will be gotten from Rest */
    const freshPage = (page) => {
        page = page!==undefined ? page : state.page
        let filter = ("mainFilter" in window) ? mainFilter.value : ""
        props.fetchData(props.nowInMain,{subs:filter, page:page})
    }
    const onChangeFilter = () => {
        window.filterTimer && clearTimeout(window.filterTimer)
        window.filterTimer = setTimeout(() => freshPage(0),WAIT_BEFORE_SUBMIT_FILTER)
    }
    /** Shows the filter for "minds" and "users" only. Not for "friends" and "mates" */
    const filter = () => {
        if (props.nowInMain === "minds" || props.nowInMain === "users") {
            return <input className="form-control form-control-lg" type="text" placeholder={loc.filterPlaceHolder}
                          id="mainFilter"
                          onKeyUp={onChangeFilter}/>
        } else return null
    }
    /** Renders SubMain body depends on nowInMain */
    const subSub = () => {
        subMainReady = false
        if (!props.isLoaded) return null
        subMainReady = true // sets testing global var
        {/*<>*/}
        {/*<div style={{textAlign: "center"}}>*/}
        {/*    <h1 className="display-3" style={{margin: "1em auto"}}>{loc.loading}</h1>*/}
        {/*</div>*/}
        {/*<div className="progress">*/}
        {/*    <div className="progress-bar progress-bar-striped progress-bar-animated"*/}
        {/*         role="progressbar" aria-valuenow="75" aria-valuemin="0" aria-valuemax="100"*/}
        {/*         style={{width: "75%"}}>*/}
        {/*    </div>*/}
        {/*</div>*/}
        {/*</>*/}
        let {nowInMain,data} = props
        switch (nowInMain) {
            case "minds"    :
                return <Minds data={data} freshPage={freshPage}/>
            case "users"    :
                return <Users what={nowInMain} data={data} freshPage={freshPage} switchTo={props.switchTo}/>
            case "friends"  :
                return <Users what={nowInMain} data={data} freshPage={freshPage} switchTo={props.switchTo}/>
            case "mates"    :
                return <Users what={nowInMain} data={data} freshPage={freshPage} switchTo={props.switchTo}/>
            case "messages" :
                return <Messaging/>
        }
    }

    state.page = props.data.number
    return <div id="subMain">
        {filter()}
        {(props.nowInMain === "minds" && props.data.totalPages > 1)?<Paginator data={props.data} setPage={setPage}/>:""}
        {subSub()}
        {(props.nowInMain === "minds" && props.data.totalPages > 1)?<Paginator data={props.data} setPage={setPage}/>:""}
    </div>
}
SubMain.propTypes = {
    nowInMain   : PropTypes.string.isRequired,
    switchTo    : PropTypes.func,
    isLoaded    : PropTypes.bool,
    data        : PropTypes.any, // sometimes it's null
}
export default connect(state => ({
    isLoaded: state.mainReducer.isLoaded,
    data    : state.mainReducer.data,
}),dispatch => ({
    fetchData: (...args) => dispatch(ajaxDataAction(...args))
}))(SubMain)


