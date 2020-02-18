import React, {memo} from "react";
import PropTypes from "prop-types";
import {ajax, noTag} from "../../utils";
import {Answer} from "./Answer"
import {loc, restPrefix} from "../../config";

export let Mind = (props) => {
    const handleClickAnswer = e => {
        e.preventDefault()
        props.openNewMindWindow("answer", props.mind)
    }
    const handleClickEdit = e => {
        e.preventDefault()
        props.openNewMindWindow("mind", props.mind)
    }
    const handleClickDel = e => {
        e.preventDefault()
        ajax(restPrefix + "removeMind", {id: props.mind.id}, "DELETE")
            .then(() => props.freshPage())
    }
    /** Items that are shown for author only */
    const authorMenuItems = () => {
        if (props.mind.isAuthor) return (
            <React.Fragment>
                <a className="dropdown-item ownerMenu editMind" href="#"
                   onClick={handleClickEdit}>{loc.toEdit}</a>
                <a className="dropdown-item ownerMenu delMind" href="#"
                   onClick={handleClickDel}>{loc.toDelete}</a>
            </React.Fragment>
        )
    }
    const dropDown = () => {
        return (
            <span className="nav-item dropdown mindDropDown">
                <a style={{display: "inline"}} className="nav-link dropdown-toggle"
                   data-toggle="dropdown" href="#" role="button" aria-haspopup="true" aria-expanded="false"/>
                   <div className="dropdown-menu forDropDown" x-placement="bottom-start">
                       <a className="dropdown-item answerMind" href="#"
                          onClick={handleClickAnswer}>{loc.toAnswer}</a>
                       {authorMenuItems()}
                   </div>
            </span>
        )
    }
    let {mind} = props;
    return (
        <div className={"card mb-3 mindEntity " + (mind.isAuthor ? "border-primary" : "border-light")}>
            <div className="card-header">
                <span className="mindTime">{mind.time}</span>
                {dropDown()}
            </div>
            <div className="card-body">
                <h4 className="card-title mindUser">{noTag(mind.author)}</h4>
                <p className="card-text mindText">{noTag(mind.text)}</p>
                {mind.answers.map((answer) =>
                    <Answer key={answer.id} mind={mind} answer={answer}
                            openNewMindWindow={props.openNewMindWindow}
                            freshPage={props.freshPage}
                    />)}
            </div>
        </div>
    );
}
Mind.propTypes = {
    mind: PropTypes.exact({
        id: PropTypes.number.isRequired,
        text: PropTypes.string.isRequired,
        author: PropTypes.string.isRequired,
        time: PropTypes.string.isRequired,
        isAuthor: PropTypes.bool.isRequired,
        answers: PropTypes.arrayOf(PropTypes.object).isRequired,
    }),
    openNewMindWindow: PropTypes.func.isRequired,
    freshPage: PropTypes.func,
}
// noinspection JSValidateTypes
Mind = memo(Mind)