import React, {memo} from "react";
import PropTypes from "prop-types";
import {ajax, noTag} from "../../utils";
import {loc, restPrefix} from "../../config";
import {store} from "../../App"
import {connect} from "react-redux";
import {setMessagesParamAction} from "../../redux/actionCreators";

const User = props => {
    const jump2Messages = interlocutorId => {
        let interlocutor = store.getState().interlocReducer.data.find(x => (x.userId === interlocutorId))
        props.changeActiveInterlocutor(interlocutor)
        props.switchTo("messages")
    }
    const startMessaging = e => {
        e.preventDefault()
        // try to find the User in interlocutors in store
        let interlocutor = (store.getState().interlocReducer.isLoaded)
            ? store.getState().interlocReducer.data.find((x) => {if (x.userId == props.user.id) return x})
            : undefined
        //if it isn't found - report to server, to add a new interlocutor
        if (!interlocutor) {
            ajax(restPrefix + "startMessaging", {whomId: props.user.id}, "POST")
                .then(() => {
                    jump2Messages(props.user.id)
                })
        } else jump2Messages(props.user.id)
    }
    const toFriends = e => {
        e.preventDefault()
        ajax(restPrefix + "toFriends", {friend_id: props.user.id}, "PATCH")
            .then(() => props.freshPage())
    }
    const fromFriends = e => {
        e.preventDefault()
        ajax(restPrefix + "fromFriends", {friend_id: props.user.id}, "PATCH")
            .then(() => props.freshPage())
    }
    const dropDown = () => {
        let {user} = props
        return <>
            <a className="nav-link dropdown-toggle"
               data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"/>
            <div className="dropdown-menu" x-placement="bottom-start"
                 style={{
                     position: "absolute",
                     willChange: "transform",
                     top: "0px",
                     left: "0px",
                     transform: "translate3d(0px, 40px, 0px)"
                 }}>
                <a className="dropdown-item startMessaging"
                   onClick={startMessaging}
                   href="#">{loc.startMessaging}</a>
                <a className="dropdown-item toFriends"
                   style={{display: (user.isFriend ? "none" : "")}}
                   onClick={toFriends}
                   href="#">{loc.toFriends}</a>
                <a className="dropdown-item fromFriends"
                   style={{display: (!user.isFriend ? "none" : "")}}
                   onClick={fromFriends}
                   href="#">{loc.fromFriends}</a>
            </div>
        </>
    }
    const usersBadge = () => {
        let {user} = props
        if (user.isFriend || user.isMate) {
            return <span
                className={"badge " + (user.isFriend ? (user.isMate ? "badge-success" : "badge-primary") : "badge-secondary")}>
                {(user.isFriend ? (user.isMate ? loc.mutualFriend : loc.halfFriend) : loc.mate)}
            </span>
        } else return null
    }
    let {user} = props
    renderLog += "User\n"
    return <tr className="userEntity">
        <td className="userName">{noTag(user.name)}</td>
        <td className="userCountry">{noTag(user.country)}</td>
        <td>
            <ul className="nav">
                <li className="nav-item dropdown">
                    {dropDown()}
                </li>
                <li className="friendship">
                    {usersBadge()}
                </li>
            </ul>
        </td>
    </tr>
}
User.propTypes = {
    user: PropTypes.exact({
        id: PropTypes.number.isRequired,
        name: PropTypes.string.isRequired,
        country: PropTypes.string.isRequired,
        isFriend: PropTypes.bool.isRequired,
        isMate: PropTypes.bool.isRequired,
    }).isRequired,
    switchTo : PropTypes.func.isRequired,
}
export default connect(null,dispatch => ({
    changeActiveInterlocutor : (interlocutor) => dispatch(setMessagesParamAction({activeInterlocutor: interlocutor})),
}))(User)