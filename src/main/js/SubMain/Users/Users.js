import React, {memo} from 'react'
import {loc} from "../../config";
import User from "./User";

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
        {props.data.map((user) => <User key={user.id} user={user} freshPage={props.freshPage}/>)}
        </tbody>
    </table>
}
export default memo(Users)