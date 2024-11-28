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
import HighlightOffSharpIcon from '@mui/icons-material/HighlightOffSharp';
import MoreHorizOutlinedIcon from '@mui/icons-material/MoreHorizOutlined';
import TelegramIcon from '@mui/icons-material/Telegram';
import { Button, TableBody, CircularProgress } from "@mui/material";
import Moment from "react-moment";
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer
} from 'recharts';

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

  const calculateAverages = (projects) => {
    return projects.map(project => {
      const tasks = project.tareas;
      let totalQuality = 0, totalEfficiency = 0, totalProductivity = 0;
      tasks.forEach(task => {
        totalQuality += task.puntuacionCalidad || 0;
        totalEfficiency += task.eficienciaTarea || 0;
        totalProductivity += task.productividadTarea || 0;
      });
      const numTasks = tasks.length || 1; // Prevent division by zero
      return {
        ...project,
        avgQuality: totalQuality / numTasks,
        avgEfficiency: totalEfficiency / numTasks,
        avgProductivity: totalProductivity / numTasks
      };
    });
  }

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

  function renderChart(data) {
    return (
      <ResponsiveContainer width="100%" height={300}>
        <BarChart
          data={data}
          margin={{
            top: 20, right: 30, left: 20, bottom: 5,
          }}
        >
          <CartesianGrid strokeDasharray="3 3" />
          <XAxis dataKey="nombre" />
          <YAxis />
          <Tooltip />
          <Legend />
          <Bar dataKey="avgQuality" fill="#8884d8" name="Average Quality Score" />
          <Bar dataKey="avgEfficiency" fill="#82ca9d" name="Task Efficiency" />
          <Bar dataKey="avgProductivity" fill="#ffc658" name="Task Productivity" />
        </BarChart>
      </ResponsiveContainer>
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
        <Button
          startIcon={<TelegramIcon />}
          variant="contained"
          color="primary"
          onClick={() => window.open('https://web.telegram.org/a/#7422535800', '_blank')}
          style={{ marginLeft: 'auto', marginRight: '20px' }}
        >
          Open Telegram
        </Button>
      </header>

      <div>
        {/* Projects Section (Left) */}
        <aside className="projects">
            <div className="mainprojects">
                <h3>Sprints</h3>
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
                                <tr key={project.id} tabIndex="0" className="clickable-row">
                                <td className="description" onClick={() => handleClickCurrentProject(project.id, project.tareas, project)}>
                                    {project.nombre}
                                </td>
                                <td className="date" onClick={() => handleClickCurrentProject(project.id, project.tareas, project)}>
                                    <Moment format="MMM Do hh:mm:ss">
                                    {project.fechaInicio}
                                    </Moment>
                                </td>
                                <td>
                                    <Button
                                    className="editButton"
                                    onClick={() => openEditModal(project, 'project')}
                                    style={{color: "#3A3632" }}
                                    >
                                   <MoreHorizOutlinedIcon/>
                                    </Button>
                                </td>
                                <td>
                                    <Button
                                    className="delete"
                                    onClick={() => deleteProyect(project.id)}
                                    style={{
                                        color: "red",
                                    }}
                                    >
                                    <HighlightOffSharpIcon/>
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
            <div className="TaskList">
                <h3>Lista de Tareas</h3>
                {currentProjectId && <NewItem addItem={addItem} isInserting={isInserting} />}

                {error && <p>Error: {error.message}</p>}
                {isLoading && <CircularProgress />}
                {!isLoading && currentProjectId && (
                <div>
                    <div className="maincontent">
                    <table className="itemlist">
                        <TableBody>
                        {items.map(
                            (item) =>
                            item.estatus === "In Progress" && (
                                <tr key={item.id} tabIndex="0" className="clickable-row">
                                <td className="description" onClick={() => handleClickCurrentTask(item.id, item)}>
                                    {item.descripcion}
                                </td>
                                <td className="date"  onClick={() => handleClickCurrentTask(item.id, item)}>
                                    <Moment format="MMM Do hh:mm:ss">
                                    {item.fechaFinalizacion}
                                    </Moment>
                                </td>
                                <td>
                                    <Button
                                    className="editButton"
                                    onClick={() => openEditModal(item, 'task')}
                                    style={{color: "#3A3632" }}
                                    >
                                    <MoreHorizOutlinedIcon/>
                                    </Button>
                                </td>
                                <td>
                                    <Button
                                    className="delete"
                                    onClick={() => deleteItem(item.id)}
                                    style={{
                                        color: "red",
                                    }}
                                    >
                                    <HighlightOffSharpIcon/>
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

            <div className="chart-section">
              <h3>Sprint Estadisticas</h3>
              {projects.length > 0 ? renderChart(calculateAverages(projects)) : <p>Loading charts...</p>}
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
                    <div id="maincontentdone">
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