/*
## MyToDoReact version 1.0.
##
## Copyright (c) 2022 Oracle, Inc.
## Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
*/
/*
 * This is the application main React component. We're using "function"
 * components in this application. No "class" components should be used for
 * consistency.
 * @author  jean.de.lavarene@oracle.com
 */
import React, { useState, useEffect } from "react";
import NewItem from "./NewItem";
import EditModal from "./EditModal";
import NewProject from "./NewProyect";
import API_LIST from "./API";
import DeleteIcon from "@mui/icons-material/Delete";
import { Button, TableBody, CircularProgress } from "@mui/material";
import Moment from "react-moment";

const API_PROYECTOS = "/api/proyectos"
const API_TAREAS = "/api/tareas"
/* In this application we're using Function Components with the State Hooks
 * to manage the states. See the doc: https://reactjs.org/docs/hooks-state.html
 * This App component represents the entire app. It renders a NewItem component
 * and two tables: one that lists the todo items that are to be done and another
 * one with the items that are already done.
 */
function App() {
  const [isLoading, setLoading] = useState(false);
  const [isInserting, setInserting] = useState(false);
  const [items, setItems] = useState([]);
  const [taskExp, setTask] = useState([]);
  const [projects, setProjects] = useState([]);
  const [error, setError] = useState();
  const [currentProjectId, setCurrentProjectId] = useState(null);
  const [currentTaskId, setCurrentTaskId] = useState(null);
  const [currentProjectO, setCurrentProjectO] = useState(null);

  const [editModalOpen, setEditModalOpen] = useState(false);
  const [currentEditItem, setCurrentEditItem] = useState(null);
  const [currentEditType, setCurrentEditType] = useState(null);
  

      // Function to open the modal with the current item and type
  const openEditModal = (item, type) => {
    setCurrentEditItem(item);
    setCurrentEditType(type);
    setEditModalOpen(true);
  };

  // Function to close the modal
  const closeEditModal = () => {
    setEditModalOpen(false);
    setCurrentEditItem(null);
    setCurrentEditType(null);
  };

  // Function to handle saving changes
  const saveItemChanges = (item) => {
    console.log(item.id)
    const apiUrl = item.id && currentEditType === 'project' ? `${API_PROYECTOS}/${item.id}` : `${API_TAREAS}/${item.id}`;
    fetch(apiUrl, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(item),
    })
      .then((response) => response.ok ? response.json() : Promise.reject('Failed to save'))
      .then(() => {
        console.log("Sending data to server:", JSON.stringify(item));
        closeEditModal();
        // Reload data from server or optimistically update the UI
        loadProjects(); // Assuming you have this function or similar to fetch projects/tasks again
        loadTasks();
      })
      .catch((error) => setError(error.toString()));
    };

  function deleteItem(deleteId) {
    fetch(API_TAREAS + "/" + deleteId, {
      method: "DELETE",
    })
      .then((response) => {
        if (response.ok) {
          return response;
        } else {
          throw new Error("Something went wrong ...");
        }
      })
      .then(
        () => {
          const remainingItems = items.filter((item) => item.id !== deleteId);
          setItems(remainingItems);
        },
        (error) => {
          setError(error);
        }
      );
  }

  function deleteProyect(deleteId) {
    fetch(API_PROYECTOS + "/" + deleteId, {
      method: "DELETE",
    })
      .then((response) => {
        if (response.ok) {
          return response;
        } else {
          throw new Error("Something went wrong ...");
        }
      })
      .then(
        () => {
          const remainingProjects = projects.filter((project) => project.id !== deleteId);
          setItems(remainingProjects);
        },
        (error) => {
          setError(error);
        }
      );
  }

  function toggleDone(event, id, description) {
    event.preventDefault();
    modifyItem(id, description, true).then(
      () => reloadOneIteam(id),
      (error) => setError(error)
    );
  }

  function reloadOneIteam(id) {
    fetch(API_LIST + "/" + id)
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error("Error");
        }
      })
      .then(
        (result) => {
          const items2 = items.map((x) =>
            x.id === id
              ? {
                  ...x,
                  description: result.description,
                  done: result.done,
                }
              : x
          );
          setItems(items2);
        },
        (error) => setError(error)
      );
  }

  function reloadOneProject(id) {
    fetch(API_LIST + "/" + id)
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error("La cagaste");
        }
      })
      .then(
        (result) => {
          const project2 = projects.map((x) =>
            x.id === id
              ? {
                  ...x,
                  description: result.description,
                  done: result.done,
                }
              : x
          );
          setProjects(project2);
        },
        (error) => setError(error)
      );
  }

  function modifyItem(id, description, done) {
    const data = { description, done };
    return fetch(API_LIST + "/" + id, {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    }).then((response) => {
      if (response.ok) {
        return response;
      } else {
        throw new Error("Something went wrong ...");
      }
    });
  }

  const loadProjects = () => {
    setLoading(true);
    fetch("/api/proyectos") // Adjust this URL to match your API endpoint
      .then((response) => response.json())
      .then((data) => {
        setProjects(data);
        setLoading(false);
      })
      .catch((error) => {
        setError("Failed to load projects: " + error.message);
        setLoading(false);
      });
  };

  // Function to load tasks
  const loadTasks = () => {
    setLoading(true);
    fetch("/api/tareas") // Adjust this URL to match your API endpoint
      .then((response) => response.json())
      .then((data) => {
        setProjects(data);
        setLoading(false);
      })
      .catch((error) => {
        setError("Failed to load projects: " + error.message);
        setLoading(false);
      });
  };

  useEffect(() => {
    setLoading(true);
    loadProjects();
    fetch(API_PROYECTOS)
      .then((response) => {
        if (response.ok) {
          return response.json();
        } else {
          throw new Error("Something went wrong ...");
        }
      })
      .then(
        (result) => {
          setLoading(false);
          setProjects(result);
        },
        (error) => {
          setLoading(false);
          setError(error);
        }
      );
  }, []);

  function addProject(text) {
    setInserting(true);
    const data = { nombre: text, estatus: "Active", fechaInicio: new Date() };
    

    fetch(API_PROYECTOS, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    })
      .then((response) => {
        if (response.ok) {
          return response;
        } else {
          throw new Error("Something went wrong ...");
        }
      })
      .then(
        (result) => {
          const id = result.headers.get("location");
          const newProject = { id, description: text };
          setProjects([newProject, ...projects]);
          setInserting(false);
        },
        (error) => {
          setInserting(false);
          setError(error);
        }
      );
  }

  function addItem(text) {
    setInserting(true);
    const data = { descripcion: text, estatus: "In Progress"};
    console.count(data);

    fetch(API_TAREAS+"/project/"+currentProjectId, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
    })
        .then((response) => {
            if (response.ok) {
            return response.json();  // Ensure this is expecting JSON
            } else {
            throw new Error("Something went wrong ...");
            }
        })
        .then((result) => {
            const newItem = { id: result.id, description: text }; // Ensure 'id' is obtained correctly
            setItems([newItem, ...items]);
            setInserting(false);
        },
        (error) => {
            setInserting(false);
            setError(error);
        });
  }

  const handleClickCurrentProject = (id, currentitems, project) => {
    setCurrentProjectId(id)
    setItems(currentitems)
    setCurrentProjectO(project)
  }
  const handleClickCurrentTask = (id, currenttask) => {
    setCurrentTaskId(id)
    setTask(currenttask)
  }

  return (
    <div className="App" style={{ display: "flex", flexDirection: "column" }}>
      <header>
        <div className="logo" style={{ display: "flex", alignItems: "center" }}>
          <img
            src="https://logos-world.net/wp-content/uploads/2020/09/Oracle-Logo.png"
            alt="Oracle Logo"
            style={{ width: "100px", height: "auto" }}
          />
        </div>
        <h3>Oracle Todo App</h3>
      </header>

      <div>
        {/* Projects Section (Left) */}
        <aside className="projects">
            <div id="mainprojects">
                <h3>Proyectos</h3>
                <NewProject addProject={addProject} isInserting={isInserting} />

                {error && <p>Error: {error.message}</p>}
                {isLoading && <CircularProgress />}
                {!isLoading && (
                <div>
                    <div>
                    <table id="ProjectlistNotDone" className="Projectlist">
                        <TableBody>
                        {projects.map(
                            (project) =>
                            project.estatus === "Active" && (
                                <tr key={project.id}>
                                <td className="description" onClick={() => handleClickCurrentProject(project.id, project.tareas, project)}>
                                    {project.nombre}
                                </td>
                                <td className="date">
                                    <Moment format="MMM Do hh:mm:ss">
                                    {project.fechaInicio}
                                    </Moment>
                                </td>
                                <td>
                                    <Button
                                    variant="contained"
                                    className="editButton"
                                    onClick={() => openEditModal(project, 'project')}
                                    size="small"
                                    style={{ backgroundColor: "blue", color: "white" }}
                                    >
                                    Edit
                                    </Button>
                                </td>
                                <td>
                                    <Button
                                    variant="contained"
                                    className="delete"
                                    size="small"
                                    onClick={() => deleteProyect(project.id)}
                                    style={{
                                        backgroundColor: "red",
                                        color: "white",
                                        borderRadius: "12px",
                                        borderBottom: "1px solid #ddd",
                                    }}
                                    >
                                    <DeleteIcon />
                                    </Button>
                                </td>
                                </tr>
                            )
                        )}
                        </TableBody>
                    </table>
                    </div>
                </div>
                )}
                </div>
            </aside>

        {/* Tasks List (Center) */}
        <main>
            <div id="TaskList">
                <h3>Lista de Tareas</h3>
                {currentProjectId && <NewItem addItem={addItem} isInserting={isInserting} />}

                {error && <p>Error: {error.message}</p>}
                {isLoading && <CircularProgress />}
                {!isLoading && currentProjectId && (
                <div>
                    <div id="maincontent">
                    <table id="itemlistNotDone" className="itemlist">
                        <TableBody>
                        {items.map(
                            (item) =>
                            item.estatus === "In Progress" && (
                                <tr key={item.id}>
                                <td className="description" onClick={() => handleClickCurrentTask(item.id, item)}>
                                    {item.descripcion}
                                </td>
                                <td className="date">
                                    <Moment format="MMM Do hh:mm:ss">
                                    {item.fechaFinalizacion}
                                    </Moment>
                                </td>
                                <td>
                                    <Button
                                    variant="contained"
                                    className="editButton"
                                    onClick={() => openEditModal(item, 'task')}
                                    size="small"
                                    style={{ backgroundColor: "blue", color: "white" }}
                                    >
                                    Edit
                                    </Button>
                                </td>
                                <td>
                                    <Button
                                    variant="contained"
                                    className="delete"
                                    size="small"
                                    onClick={() => deleteItem(item.id)}
                                    style={{
                                        backgroundColor: "red",
                                        color: "white",
                                        borderRadius: "12px",
                                        borderBottom: "1px solid #ddd",
                                    }}
                                    >
                                    <DeleteIcon />
                                    </Button>
                                </td>
                                </tr>
                            )
                        )}
                        </TableBody>
                    </table>
                    </div>
                </div>
                )}
            </div>
        </main>

        {/* Done Items (Right) */}
        <aside className="doneItems">
          <div className="task-details">
            <h3>Detalles de la Tarea</h3>
            <p>
              <strong>ID:</strong> {taskExp.id}
            </p>
            <p>
              <strong>Descripción:</strong> {taskExp.descripcion}
            </p>
            <p>
              <strong>Estatus:</strong> {taskExp.estatus}
            </p>
            <p>
              <strong>Tiempo Estimado:</strong> {taskExp.tiempoEstimado}
            </p>
            <p>
              <strong>Tiempo Invertido:</strong> {taskExp.tiempoReal}
            </p>
            <p>
              <strong>Fecha Entrega:</strong> <td className="date">  <Moment format="MMM Do hh:mm:ss"> {taskExp.fechaFinalizacion} </Moment>  </td>
            </p>
            <p>
              <strong>Calidad:</strong> {taskExp.puntuacionCalidad}
            </p>
          </div>
          <div className="Items-Done">
            <h3>Tareas Completadas</h3>
                {error && <p>Error: {error.message}</p>}
                {isLoading && <CircularProgress />}
                {!isLoading && currentProjectId && (
                <div>
                    <div id="maincontent">
                    <table id="itemlistNotDone" className="itemlist">
                        <TableBody>
                        {items.map(
                            (item) =>
                            item.estatus === "Done" && (
                                <tr key={item.id}>
                                <td className="description">
                                    {item.descripcion}
                                </td>
                                <td className="date">
                                    <Moment format="MMM Do hh:mm:ss">
                                    {item.fechaFinalizacion}
                                    </Moment>
                                </td>
                                </tr>
                            )
                        )}
                        </TableBody>
                    </table>
                    </div>
              </div>
            )}
          </div>
        </aside>
      </div>
      <EditModal
        open={editModalOpen}
        handleClose={closeEditModal}
        item={currentEditItem}
        saveChanges={saveItemChanges}
        type={currentEditType}
      />
    </div>
  );
}

export default App;