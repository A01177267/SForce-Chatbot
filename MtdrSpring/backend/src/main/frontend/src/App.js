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
import NewProject from "./NewProject";
import API_LIST from "./API";
import DeleteIcon from "@mui/icons-material/Delete";
import { Button, TableBody, CircularProgress } from "@mui/material";
import Moment from "react-moment";

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
  const [projects, setProjects] = useState([]);
  const [error, setError] = useState();

  function deleteItem(deleteId) {
    fetch(API_LIST + "/" + deleteId, {
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

  function toggleDone(event, id, description, done) {
    event.preventDefault();
    modifyItem(id, description, done).then(
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
          throw new Error("La cagaste");
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

  useEffect(() => {
    setLoading(true);
    fetch(API_LIST)
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
          setItems(result);
        },
        (error) => {
          setLoading(false);
          setError(error);
        }
      );
  }, []);

  function addProject(text) {
    setInserting(true);
    const data = { description: text };

    fetch(API_LIST, {
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
    const data = { description: text };

    fetch(API_LIST, {
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
          const newItem = { id, description: text };
          setItems([newItem, ...items]);
          setInserting(false);
        },
        (error) => {
          setInserting(false);
          setError(error);
        }
      );
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
                          !project.done && (
                            <tr key={project.id}>
                              <td className="description">
                                {project.description}
                              </td>
                              <td className="date">
                                <Moment format="MMM Do hh:mm:ss">
                                  {project.createdAt}
                                </Moment>
                              </td>
                              <td>
                                <Button
                                  variant="contained"
                                  className="DoneButton"
                                  onClick={(event) =>
                                    toggleDone(
                                      event,
                                      project.id,
                                      project.description,
                                      !project.done
                                    )
                                  }
                                  size="small"
                                  style={{
                                    backgroundColor: "green", // Color de fondo
                                    color: "white", // Color de texto
                                    borderRadius: "12px", // Bordes redondeados
                                    borderBottom: "1px solid #ddd",
                                  }}
                                >
                                  Hecho
                                </Button>
                              </td>
                            </tr>
                          )
                      )}
                    </TableBody>
                  </table>
                </div>

                <div id="ProjectStatus">
                  <p>
                    <strong>ESTATUS:</strong> En Progreso
                  </p>
                  <p>
                    <strong>FECHA INICIO:</strong> 7/5/2024
                  </p>
                  <p>
                    <strong>FECHA FINAL:</strong> 12/1/2024
                  </p>
                </div>

                <div
                  id="ProjectMembers"
                  style={{
                    height: "30%",
                    overflow: "auto",
                    border: "1px solid #ddd",
                    backgroundColor: "#EBEBEB",
                  }}
                >
                  <h4>Miembros y Roles</h4>
                  <p>Jorge Mata - Developer</p>
                  <p>Pablo Sepulveda - Scrum Master</p>
                  <p>Baruc Ramirez - Product Owner</p>
                  <p>Gael Gaytan - Developer</p>
                  <p>Luis Cabral - Developer</p>
                </div>
              </div>
            )}
          </div>
        </aside>

        {/* Tasks List (Center) */}
        <main>
          <div id="TaskList">
            <h3>Lista de Tareas</h3>
            <NewItem addItem={addItem} isInserting={isInserting} />

            {error && <p>Error: {error.message}</p>}
            {isLoading && <CircularProgress />}
            {!isLoading && (
              <div>
                <div id="maincontent">
                  <table id="itemlistNotDone" className="itemlist">
                    <TableBody>
                      {items.map(
                        (item) =>
                          !item.done && (
                            <tr key={item.id}>
                              <td className="description">
                                {item.description}
                              </td>
                              <td className="date">
                                <Moment format="MMM Do hh:mm:ss">
                                  {item.createdAt}
                                </Moment>
                              </td>
                              <td>
                                <Button
                                  variant="contained"
                                  className="DoneButton"
                                  onClick={(event) =>
                                    toggleDone(
                                      event,
                                      item.id,
                                      item.description,
                                      !item.done
                                    )
                                  }
                                  size="small"
                                  style={{
                                    backgroundColor: "green",
                                    color: "white",
                                    borderRadius: "12px",
                                    borderBottom: "1px solid #ddd",
                                  }}
                                >
                                  Hecho
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

                {/* Nuevo div para agrupar contenido */}
                <div id="codigo" style={{ marginTop: "20px" }}>
                  <h3>Ejemplo de Código:</h3>
                  <pre>
                    <code>
                      {`function saludar(nombre) {
                return "Hola, " + nombre + "!";
              }
              
              console.log(saludar("Mundo"));`}
                    </code>
                  </pre>
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
              <strong>ID:</strong> 1023
            </p>
            <p>
              <strong>Sprint:</strong> 1
            </p>
            <p>
              <strong>Descripción:</strong> Botones Telegram
            </p>
            <p>
              <strong>Estatus:</strong> Incompleto
            </p>
            <p>
              <strong>Tiempo Estimado:</strong> 3 días
            </p>
            <p>
              <strong>Tiempo Invertido:</strong> 4 días
            </p>
            <p>
              <strong>Fecha Entrega:</strong> 12/1/2024
            </p>
            <p>
              <strong>Calidad:</strong> 85
            </p>
            <p>
              <strong>Productividad:</strong> 0.85
            </p>
            <p>
              <strong>Asignado a:</strong> Baruc Ramirez
            </p>
          </div>

          <div className="review-section">
            <h3>Review</h3>
            <p>
              <strong>Comentarios:</strong> Esta tarea se encuentra en progreso
              y se espera su finalización en la fecha establecida.
            </p>
            <p>
              <strong>Notas:</strong> Asegurarse de revisar las dependencias
              antes de cerrar la tarea.
            </p>
          </div>
          <div className="Items-Done">
            <h3>Tareas Completadas</h3>

            {isLoading && <CircularProgress />}
            {!isLoading && (
              <div className="Items-Donedone">
                <table id="itemlistDone" className="itemlist">
                  <TableBody>
                    {items.map(
                      (item) =>
                        item.done && (
                          <tr key={item.id}>
                            <td className="description">{item.description}</td>
                            <td className="date">
                              <Moment format="MMM Do hh:mm:ss">
                                {item.createdAt}
                              </Moment>
                            </td>
                          </tr>
                        )
                    )}
                  </TableBody>
                </table>
              </div>
            )}
          </div>
        </aside>
      </div>
    </div>
  );
}

export default App;
