'use strict';

import React from 'react'
import PropTypes from 'prop-types'
import {connect} from "react-redux"
import {loc, WAIT_BEFORE_SUBMIT_FILTER} from "../config";
import {ajaxDataAction} from "../redux/actionCreators";
import {Minds} from "./Minds/Minds";
import {Users} from "./Users/Users";
import {Paginator} from "./Paginator";

export const SubMain = connect(state => ({
    isLoaded: state.isLoaded,
    data    : state.data,
}),dispatch => ({
    fetchData: (...args) => dispatch(ajaxDataAction(...args))
}))(class SubMain extends React.PureComponent {
    static propTypes = {
        nowInMain   : PropTypes.string.isRequired,
        isLoaded    : PropTypes.bool,
        data        : PropTypes.any, // sometimes it's null
    }
    constructor(props) {
        super(props);
        this.state = {page : props.page}
        this.y = null // scroll position
    }

    /** Remember scroll position */
    getSnapshotBeforeUpdate(prevProps, prevState) {
        if (!this.props.isLoaded) this.y = window.pageYOffset
        return null
    }

    /** Rstor scroll position */
    componentDidUpdate(prevProps, prevState, snapshot) {
        if (this.y !== null) window.scrollTo(0,this.y)
    }

    /** Change the pages and refresh it. Eventual page number will be gotten from Rest */
    setPage = (page) => {
        this.freshPage(page)
    }
    /** Refreshes the page. Eventual page number will be gotten from Rest */
    freshPage = (page) => {
        page = page!==undefined ? page : this.state.page
        let filter = ("mainFilter" in window) ? mainFilter.value : ""
        this.props.fetchData(this.props.nowInMain,{subs:filter, page:page})
    }
    onChangeFilter = () => {
        this.timer && clearTimeout(this.timer)
        this.timer = setTimeout(() => this.freshPage(0),WAIT_BEFORE_SUBMIT_FILTER)
    }
    /** Shows the filter for "minds" and "users" only. Not for "friends" and "mates" */
    filter () {
        if (this.props.nowInMain === "minds" || this.props.nowInMain === "users") {
            return <input className="form-control form-control-lg" type="text" placeholder={loc.filterPlaceHolder}
                          id="mainFilter"
                          onKeyUp={this.onChangeFilter}/>
        } else return null
    }
    /** Renders SubMain body depends on nowInMain */
    subSub () {
        subMainReady = false
        if (!this.props.isLoaded) return null
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
        let {nowInMain,data} = this.props
        switch (nowInMain) {
            case "minds"    :
                return <Minds data={data} freshPage={this.freshPage}/>
            case "users"    :
                return <Users what={nowInMain} data={data} freshPage={this.freshPage}/>
            case "friends"  :
                return <Users what={nowInMain} data={data} freshPage={this.freshPage}/>
            case "mates"    :
                return <Users what={nowInMain} data={data} freshPage={this.freshPage}/>
        }
    }
    render () {
        this.state.page = this.props.data.number
        return <div id="subMain">
            {this.filter()}
            {(this.props.nowInMain === "minds" && this.props.data.totalPages > 1)?<Paginator data={this.props.data} setPage={this.setPage}/>:""}
            {this.subSub()}
            {(this.props.nowInMain === "minds" && this.props.data.totalPages > 1)?<Paginator data={this.props.data} setPage={this.setPage}/>:""}
        </div>
    }
})


