'use strict';

import React from 'react'
import ReactDOM from 'react-dom'
import PropTypes from 'prop-types'
import {ajax, noTag, removeJwtToken} from "./utils";
import {SubMain} from './submain.js'
import {mainMenuIds,loc} from './config.js'
import {connect, Provider} from "react-redux";
import {configureStore} from "./redux/configureStore";
import {ajaxDataAction} from "./redux/actionCreators";

class MainMenuItem extends React.Component {
    static propTypes = {
        what    : PropTypes.string.isRequired,
        props   : PropTypes.exact({ //MainMenuItem receive all props of MainMenu as is
            switchTo    : PropTypes.func.isRequired,
            nowInMain   : PropTypes.string.isRequired,
        }).isRequired,
    }
    handleClick = (e) => {
        e.preventDefault()
        this.props.props.switchTo(this.props.what)
    }
    render () {
        return (
            <a className={this.props.props.nowInMain === this.props.what ? "nav-link active" : "nav-link"}
               id={mainMenuIds[this.props.what]}
               href={"#"+loc.mainMenuTexts[this.props.what]}
               onClick={this.handleClick}>{loc.mainMenuTexts[this.props.what]}</a>
        )
    }
}

class MainMenu extends React.Component {
    static propTypes = {
        switchTo    : PropTypes.func.isRequired,
        nowInMain   : PropTypes.oneOf(["minds","users","friends","mates"]).isRequired,
    }
    render() {
        return (
            <ul className="navbar-nav mr-auto" id="menuBar">
                {Object.keys(mainMenuIds).map((i) => {
                    return (<li className="nav-item" key={i}>
                        <MainMenuItem key={i} what={i} props={this.props}/>
                    </li>)
                })}
            </ul>
        )
    }
}

const App = connect(null,dispatch => ({
    fetchData: (...args) => dispatch(ajaxDataAction(...args))
}))(class extends React.Component {
        static propTypes = {
            fetchData   : PropTypes.func.isRequired,
        }
        constructor (props) {
            super(props);
            this.state = {
                nowInMain : "minds",
            };
        }
        /** Fun that changes nowInMain and starts fetching data from Rest*/
        switchTo = (what) => {
            if ('mainFilter' in window) mainFilter.value = ""
            this.setState({nowInMain : what})
            this.props.fetchData(what,{page:0,size:MINDS_PAGE_SIZE})
        }
        /** Get user to display it in upper right corner */
        componentDidMount () {
            ajax("/rest/getUser")
                .then((data) => {
                    nameField.innerHTML = noTag(JSON.parse(data).name);
                })
        }
        logOut = e => {
            removeJwtToken()
        }
        render() {
            return <>
                <nav className="navbar navbar-expand-lg navbar-dark bg-primary">
                    <a className="navbar-brand" href="/">Pepsbook</a>
                    <button className="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarColor01"
                            aria-controls="navbarColor01" aria-expanded="false" aria-label="Toggle navigation">
                        <span className="navbar-toggler-icon"/>
                    </button>

                    <div className="collapse navbar-collapse" id="navbarColor01">
                        <MainMenu switchTo={this.switchTo} nowInMain={this.state.nowInMain}/>
                        <div className="btn-group" role="group" aria-label="Button group with nested dropdown"
                             style={{marginRight: "20px"}}>
                            <div className="btn-group" role="group">
                                <button id="btnGroupDrop2" type="button" className="btn btn-secondary dropdown-toggle"
                                        data-toggle="dropdown" aria-haspopup="true" aria-expanded="false"/>
                                <div className="dropdown-menu" aria-labelledby="btnGroupDrop2">
                                    <a className="dropdown-item" onClick={this.logOut} href="/logout" id="exitMenuItem">{loc.toLogOut}</a>
                                </div>
                            </div>
                            <button type="button" className="btn btn-secondary" id="nameField"/>
                        </div>
                    </div>
                </nav>
                <SubMain nowInMain={this.state.nowInMain}/>
            </>
        }
    }
)

const store = configureStore()
ReactDOM.render(
    <Provider store={store}>
        <App/>
    </Provider>,
    document.getElementById('react')
)
store.dispatch(ajaxDataAction("minds"))