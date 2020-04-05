import React, {memo} from 'react'
import {loc} from "../../config";
import User from "./User";
import PropTypes from "prop-types";

const Users = props => {
    return <table className="table table-hover">
        <thead>
        <tr>
            <th scope="col">{loc.name}</th>
            <th scope="col">{loc.country}</th>
            <th scope="col"/>
        </tr>
        </thead>
        <tbody>
        {props.data.map((user) => <User key={user.id} user={user} freshPage={props.freshPage} switchTo={props.switchTo}/>)}
        </tbody>
    </table>
}
Users.proptypes = {
    freshPage : PropTypes.func.isRequired,
    switchTo : PropTypes.func.isRequired,
}
export default memo(Users)