import React, {memo} from "react";
import PropTypes from "prop-types";
import {ajax, noTag} from "../../utils";
import {loc, restPrefix} from "../../config";

export let Answer = props => {
    const handleClickAnswer = (e) => {
        e.preventDefault()
        let {answer, mind} = props;
        props.openNewMindWindow("answer", mind, undefined, "@" + answer.author + "\xa0")
    }
    const handleClickEdit = (e) => {
        e.preventDefault()
        let {mind, answer} = props;
        props.openNewMindWindow("answer", mind, answer)
    }
    const handleClickDel = (e) => {
        e.preventDefault()
        ajax(restPrefix + "removeAnswer", {id: props.answer.id}, "DELETE")
            .then(() => props.freshPage())
    }
    /** Items that are shown for author only */
    const authorMenuItems = () => {
        if (props.answer.isAuthor) return (
            <React.Fragment>
                <a className="dropdown-item ownerMenu editAnswer" href="#"
                   onClick={handleClickEdit}>{loc.toEdit}</a>
                <a className="dropdown-item ownerMenu delAnswer" href="#"
                   onClick={handleClickDel}>{loc.toDelete}</a>
            </React.Fragment>
        )
    }
    const dropDown = () => {
        return (
            <span className="nav-item dropdown answerDropDown">
                <a style={{display: "inline"}} className="nav-link dropdown-toggle"
                   data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"/>
                   <div className="dropdown-menu forDropDown" x-placement="bottom-start">
                       <a className="dropdown-item answerAnswer" href="#"
                          onClick={handleClickAnswer}>{loc.toAnswer}</a>
                       {authorMenuItems()}
                   </div>
            </span>
        )
    }
    const {answer} = props;
    return (
        <div className="answerEntity">
            <strong className="answerUser" style={{marginRight: "1em"}}>{noTag(answer.author)}</strong>
            <span className="answerText">{noTag(answer.text)}</span>
            <em className="answerTime" style={{marginLeft: "1em"}}>{answer.time}</em>
            {dropDown()}
        </div>
    )
}
Answer.propTypes = {
    mind: PropTypes.object,
    answer: PropTypes.exact({
        id: PropTypes.number.isRequired,
        text: PropTypes.string.isRequired,
        author: PropTypes.string.isRequired,
        time: PropTypes.string.isRequired,
        isAuthor: PropTypes.bool.isRequired,
    }).isRequired,
    openNewMindWindow: PropTypes.func.isRequired,
    freshPage: PropTypes.func,
}
// noinspection JSValidateTypes
Answer = memo(Answer)