import React, {memo} from "react";
import PropTypes from "prop-types";
import {loc, mainMenuIds} from "../config";

const MainMenuItem = props => {
    const handleClick = e => {
        e.preventDefault()
        props.switchTo(props.what)
    }
    return (
        <a className={props.nowInMain === props.what ? "nav-link active" : "nav-link"}
           id={mainMenuIds[props.what]}
           href={"#" + loc.mainMenuTexts[props.what]}
           onClick={handleClick}>{loc.mainMenuTexts[props.what]}</a>
    )
}
MainMenuItem.propTypes = {
    what:       PropTypes.string.isRequired,
    switchTo:   PropTypes.func.isRequired,
    nowInMain:  PropTypes.string.isRequired,
}
export default memo(MainMenuItem)