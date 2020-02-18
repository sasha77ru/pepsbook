import React, {memo} from "react";
import PropTypes from "prop-types";

/** Renders a paginator according to data from Rest*/
export let Paginator = props => {
    const handleClick = (e, page) => {
        e.preventDefault()
        props.setPage(page)
    }
    /** Renders very complicated paginator */
    const middle = () => {
        let {data} = props
        let out = []
        for (let page = 0; page < data.totalPages; page++) {
            if (data.number === page) {
                out.push(
                    <li key={out.length} className="page-item active">
                        <a className="page-link" href="#" onClick={(e,) => handleClick(e, page)}>{page + 1}</a>
                    </li>)
            } else {
                //to do less page buttons
                if (data.totalPages > PAGINATOR_MAX_SIZE) {
                    if (page > 0 && page < data.number - PAGINATOR_WIDE + 1) {
                        page = data.number - PAGINATOR_WIDE;
                        out.push(
                            <li key={out.length} className="page-item disabled">
                                <a className="page-link">...</a>
                            </li>)
                    } else if (page < data.totalPages - 2 && page > data.number + PAGINATOR_WIDE) {
                        page = data.totalPages - 1;
                        out.push(
                            <li key={out.length} className="page-item disabled">
                                <a className="page-link">...</a>
                            </li>)
                    }
                }
                out.push(
                    <li key={out.length} className="page-item">
                        <a className="page-link" href="#" onClick={(e) => handleClick(e, page)}>{page + 1}</a>
                    </li>)
            }
        }
        return out
    }

    let {data} = props
    return <div style={{margin: "1em auto"}}>
        <ul className="pagination">
            <li className={"page-item" + (data.first ? " disabled" : "")}>
                <a className="page-link" href="#" onClick={(e) => handleClick(e, data.number - 1)}>&laquo;</a>
            </li>
            {middle()}
            <li className={"page-item" + (data.last ? " disabled" : "")}>
                <a className="page-link" href="#" onClick={(e) => handleClick(e, data.number + 1)}>&raquo;</a>
            </li>
        </ul>
    </div>
}
Paginator.propTypes = {
    data: PropTypes.shape({
        number: PropTypes.number,
        totalPages: PropTypes.number,
    }),
    setPage: PropTypes.func,
}
// noinspection JSValidateTypes
Paginator = memo(Paginator)