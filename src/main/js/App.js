import 'react-app-polyfill/ie9';
import React, {useEffect, useState} from 'react'
import ReactDOM from 'react-dom'
import PropTypes from 'prop-types'
import {ajax, noTag, removeJwtToken, wSocket} from "./utils";
import SubMain from './SubMain/SubMain.js'
import {loc} from './config.js'
import {connect, Provider} from "react-redux";
import {configureStore} from "./redux/configureStore";
import {ajaxDataAction, ajaxInterlocAction} from "./redux/actionCreators";
import MainMenu from "./MainMenu/MainMenu";
import User from "./SubMain/Users/User";
import SockJS from "sockjs-client"
import "stompjs"

let App = props => {
    const [state, setMyState] = useState({nowInMain : "minds"})
    const setState = (x) => {setMyState({...state,...x})}

    /** Get user to display it in upper right corner */
    useEffect(() => {
        ajax("/rest/getUser")
            .then((data) => {
                let result = JSON.parse(data)
                nameField.innerHTML = noTag(result.name);
                window.userId = result.id
                window.userName = result.name
                wSocket("interlocutorsUpdate","updateInterlocutors/"+window.userId,(answer) => {
                    store.dispatch(ajaxInterlocAction())
                })
            })
    },[])
    /** Fun that changes nowInMain and starts fetching data from Rest*/
    const switchTo = what => {
        if ('mainFilter' in window) mainFilter.value = ""
        setState({nowInMain : what})
        if (what !== "messages") props.fetchData(what,{page:0,size:MINDS_PAGE_SIZE})
    }
    const logOut = e => {
        removeJwtToken()
    }

    return <>
        <nav className="navbar navbar-expand-lg navbar-dark bg-primary">
            <a className="navbar-brand" href="/">Pepsbook</a>
            <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarColor01"
                    aria-controls="navbarColor01" aria-expanded="false" aria-label="Toggle navigation">
                <span className="navbar-toggler-icon"/>
            </button>

            <div className="collapse navbar-collapse" id="navbarColor01">
                <MainMenu switchTo={switchTo} nowInMain={state.nowInMain}/>
                <div className="btn-group" role="group" aria-label="Button group with nested dropdown"
                     style={{marginRight: "20px"}}>
                    <div className="btn-group" role="group">
                        <button id="btnGroupDrop2" type="button" className="btn btn-secondary dropdown-toggle"
                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"/>
                        <div className="dropdown-menu" aria-labelledby="btnGroupDrop2">
                            <a className="dropdown-item" onClick={logOut} href="/logout" id="exitMenuItem">{loc.toLogOut}</a>
                        </div>
                    </div>
                    <button type="button" className="btn btn-secondary" id="nameField"/>
                </div>
            </div>
        </nav>
        <SubMain nowInMain={state.nowInMain} switchTo={switchTo}/>
    </>
}
App.propTypes = {
    fetchData   : PropTypes.func,
}
App = connect(null,dispatch => ({
    fetchData: (...args) => dispatch(ajaxDataAction(...args)),
}))(App)

export const store = configureStore()
ReactDOM.render(
    <Provider store={store}>
        <App/>
    </Provider>,
    document.getElementById('react')
)
store.dispatch(ajaxDataAction("minds"))
store.dispatch(ajaxInterlocAction())

// setInterval(() => {
//     store.dispatch(ajaxInterlocAction())
// },10000)

//todo test
// setTimeout(() => {
//     mainMessages.click()
//     setTimeout(() => {
//         document.querySelectorAll("button.interlocutor")[0].click()
//         // mainMates.click()
//         // setTimeout(() => {
//         //     document.querySelector(".userEntity .dropdown-toggle").click()
//         //     document.querySelector(".startMessaging").click()
//         // },1000)
//     },1000)
// },1000)