import React, {memo} from "react";
import PropTypes from "prop-types";
import {mainMenuIds} from "../config";
import MainMenuItem from "./MainMenuItem";

const MainMenu = props => {
    return (
        <ul className="navbar-nav mr-auto" id="menuBar">
            {Object.keys(mainMenuIds).map((i) => {
                return (<li className="nav-item" key={i}>
                    <MainMenuItem key={i} what={i} {...props}/>
                </li>)
            })}
        </ul>
    )
}
MainMenu.propTypes = {
    switchTo: PropTypes.func.isRequired,
    nowInMain: PropTypes.oneOf(["minds", "users", "friends", "mates"]).isRequired,
}
export default memo(MainMenu)