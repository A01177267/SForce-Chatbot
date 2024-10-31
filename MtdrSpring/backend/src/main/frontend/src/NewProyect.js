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


function NewProject(props) {
  const [project, setProject] = useState('');
  function handleSubmit(e) {
    // console.log("NewProject.handleSubmit("+e+")");
    if (!project.trim()) {
      return;
    }
    // addItem makes the REST API call:
    props.addProject(project);
    setProject("");
    e.preventDefault();
  }
  function handleChange(e) {
    setProject(e.target.value);
  }
  return (
    <div id="newprojectinputform" >
    <form>
      <input
        id="newprojectinput"
        placeholder="Nueva Proyecto"
        type="text"
        autoComplete="off"
        value={project}
        style={{ width: '70%' }}
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
        className="AddProjectButton"
        variant="contained"
        disabled={props.isInserting}
        onClick={!props.isInserting ? handleSubmit : null}
        size="small"
      >
        {props.isInserting ? 'Añadiendo…' : '+'}
      </Button>
    </form>
    </div>
  );
}

export default NewProject;