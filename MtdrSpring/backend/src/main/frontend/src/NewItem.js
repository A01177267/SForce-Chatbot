/*
## MyToDoReact version 1.0.
##
## Copyright (c) 2022 Oracle, Inc.
## Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
/*
 * Component that supports creating a new todo item.
 * @author  jean.de.lavarene@oracle.com
 */

import React, { useState } from "react";
import Button from '@mui/material/Button';
import AddBoxIcon from '@mui/icons-material/AddBox';


function NewItem(props) {
  const [item, setItem] = useState('');
  function handleSubmit(e) {
    // console.log("NewItem.handleSubmit("+e+")");
    if (!item.trim()) {
      return;
    }
    // addItem makes the REST API call:
    props.addItem(item);
    setItem("");
    e.preventDefault();
  }
  function handleChange(e) {
    setItem(e.target.value);
  }
  return (
    <div className="newinputform" >
    <form>
      <input
        id="newiteminput"
        placeholder="Nueva Tarea"
        type="text"
        autoComplete="off"
        value={item}
        style={{ width: '90%', border: "none" ,backgroundColor:"transparent", outline:"none"}}
        onChange={handleChange}
        // No need to click on the "ADD" button to add a todo item. You
        // can simply press "Enter":
        onKeyDown={event => {
          if (event.key === 'Enter') {
            handleSubmit(event);
          }
        }}
      />
      <span>&nbsp;&nbsp;</span>
      <Button
        className="AddButton"
        disabled={props.isInserting}
        onClick={!props.isInserting ? handleSubmit : null}
        size="small"
      >
        {props.isInserting ? 'Añadiendo…' : <AddBoxIcon style={{ color: '#333' }}/> }
      </Button>
    </form>
    </div>
  );
}

export default NewItem;