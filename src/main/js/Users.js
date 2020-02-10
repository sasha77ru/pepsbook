import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {connect} from "react-redux"
import {ajax, noTag} from "./utils"
import {loc, restPrefix} from "./config";

class User extends Component {
    static propTypes = {
        user    : PropTypes.exact({
            id      : PropTypes.number.isRequired,
            name    : PropTypes.string.isRequired,
            country : PropTypes.string.isRequired,
            isFriend: PropTypes.bool.isRequired,
            isMate  : PropTypes.bool.isRequired,
        }).isRequired,
    }
    toFriends = e => {
        e.preventDefault()
        ajax(restPrefix+"toFriends",{friend_id : this.props.user.id},"PATCH")
            .then(() => this.props.freshPage())
    }
    fromFriends = e => {
        e.preventDefault()
        ajax(restPrefix+"fromFriends",{friend_id : this.props.user.id},"PATCH")
            .then(() => this.props.freshPage())
    }
    dropDown = () => {
        let {user} = this.props
        return <>
        <a className="nav-link dropdown-toggle"
           data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"/>
        <div className="dropdown-menu" x-placement="bottom-start"
             style={{position : "absolute", willChange : "transform", top : "0px", left : "0px",transform : "translate3d(0px, 40px, 0px)"}}>
            <a className="dropdown-item toFriends"
               style={{display : (user.isFriend ? "none" : "")}}
               onClick={this.toFriends}
               href="#">{loc.toFriends}</a>
            <a className="dropdown-item fromFriends"
               style={{display : (!user.isFriend ? "none" : "")}}
               onClick={this.fromFriends}
               href="#">{loc.fromFriends}</a>
        </div>
        </>
    }
    usersBadge () {
        let {user} = this.props
        if (user.isFriend || user.isMate) {
            return <span className={"badge "+(user.isFriend ? (user.isMate ? "badge-success":"badge-primary") : "badge-secondary")}>
                {(user.isFriend ? (user.isMate ? loc.mutualFriend : loc.halfFriend) : loc.mate)}
            </span>
        } else return null
    }
    render() {
        let {user} = this.props
        return <tr className="userEntity">
            <td className="userName">{noTag(user.name)}</td>
            <td className="userCountry">{noTag(user.country)}</td>
            <td>
                <ul className="nav">
                <li className="nav-item dropdown">
                    {this.dropDown()}
                </li>
                <li className="friendship">
                    {this.usersBadge()}
                </li>
            </ul>
            </td>
        </tr>
    }
}

export const Users = connect(state => ({
    isLoaded: state.isLoaded,
    data    : state.data,
}))(class extends Component {

    render() {
        return <table className="table table-hover">
            <thead>
                <tr>
                    <th scope="col">{loc.name}</th>
                    <th scope="col">{loc.country}</th>
                    <th scope="col"/>
                </tr>
            </thead>
            <tbody>
                {this.props.data.map((user) => <User key={user.id} user={user} freshPage={this.props.freshPage}/>)}
            </tbody>
        </table>
    }
})

