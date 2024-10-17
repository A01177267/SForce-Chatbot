import React, { useState, useEffect } from "react";
import NewItem from "./NewItem";
import EditModal from "./EditModal";
import NewProject from "./NewProyect";
import API_LIST from "./API";
import DeleteIcon from "@mui/icons-material/Delete";
import { Button, TableBody, CircularProgress, TextField, Container, Box, Typography } from "@mui/material";
import Moment from "react-moment";

const API_PROYECTOS = "/api/proyectos"
const API_TAREAS = "/api/tareas"

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

    // New state variables for authentication
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userRole, setUserRole] = useState('');
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');

    useEffect(() => {
        fetchUserRole();
    }, []);

    const fetchUserRole = async () => {
        try {
            const response = await fetch('/api/user/role', {
                credentials: 'include',
            });
            if (response.ok) {
                const data = await response.json();
                setIsAuthenticated(true);
                setUserRole(data.role);
                loadProjects();
            } else {
                setIsAuthenticated(false);
                setUserRole('');
            }
        } catch (error) {
            console.error('Error fetching user role:', error);
            setIsAuthenticated(false);
            setUserRole('');
        }
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await fetch('/perform_login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `username=${encodeURIComponent(username)}&password=${encodeURIComponent(password)}`,
                credentials: 'include',
            });
            if (response.ok) {
                fetchUserRole();
            } else {
                alert('Login failed. Please check your credentials.');
            }
        } catch (error) {
            console.error('Login error:', error);
            alert('An error occurred during login.');
        }
    };

    const handleLogout = async () => {
        try {
            await fetch('/logout', {
                method: 'POST',
                credentials: 'include',
            });
            setIsAuthenticated(false);
            setUserRole('');
        } catch (error) {
            console.error('Logout error:', error);
        }
    };

    const openEditModal = (item, type) => {
        setCurrentEditItem(item);
        setCurrentEditType(type);
        setEditModalOpen(true);
    };

    const closeEditModal = () => {
        setEditModalOpen(false);
        setCurrentEditItem(null);
        setCurrentEditType(null);
    };

    const getCsrfToken = () => {
        const token = document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1');
        console.log('CSRF Token:', token);
        return token;
    };

// Modify the fetchWithCsrf function to log the headers
    const fetchWithCsrf = (url, options = {}) => {
        const csrfToken = getCsrfToken();
        const headers = {
            ...options.headers,
            'X-XSRF-TOKEN': csrfToken,
            'Content-Type': 'application/json',
        };
        console.log('Request headers:', headers);
        return fetch(url, {
            ...options,
            headers: headers,
            credentials: 'include',
        });
    };

    // Update existing fetch calls to use fetchWithCsrf
    const saveItemChanges = (item) => {
        const apiUrl = item.id && currentEditType === 'project' ? `${API_PROYECTOS}/${item.id}` : `${API_TAREAS}/${item.id}`;
        fetchWithCsrf(apiUrl, {
            method: "PUT",
            body: JSON.stringify(item),
        })
            .then((response) => response.ok ? response.json() : Promise.reject('Failed to save'))
            .then(() => {
                closeEditModal();
                loadProjects();
                loadTasks();
            })
            .catch((error) => setError(error.toString()));
    };

    function deleteItem(deleteId) {
        fetchWithCsrf(`${API_TAREAS}/${deleteId}`, {
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
        fetchWithCsrf(`${API_PROYECTOS}/${deleteId}`, {
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
                    setProjects(remainingProjects);
                },
                (error) => {
                    setError(error);
                }
            );
    }

    const loadProjects = () => {
        setLoading(true);
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
    };

    const loadTasks = () => {
        if (currentProjectId) {
            setLoading(true);
            fetch(`${API_TAREAS}/project/${currentProjectId}`)
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
        }
    };

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
                    return response.json();
                } else {
                    throw new Error("Something went wrong ...");
                }
            })
            .then(
                (result) => {
                    const newProject = { id: result.id, nombre: text };
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
        const data = { descripcion: text, estatus: "In Progress" };

        fetch(`${API_TAREAS}/project/${currentProjectId}`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(data),
        })
            .then((response) => {
                if (response.ok) {
                    return response.json();
                } else {
                    throw new Error("Something went wrong ...");
                }
            })
            .then(
                (result) => {
                    const newItem = { id: result.id, descripcion: text };
                    setItems([newItem, ...items]);
                    setInserting(false);
                },
                (error) => {
                    setInserting(false);
                    setError(error);
                }
            );
    }

    const handleClickCurrentProject = (id, currentitems, project) => {
        setCurrentProjectId(id);
        setItems(currentitems);
        setCurrentProjectO(project);
        loadTasks();
    };

    const handleClickCurrentTask = (id, currenttask) => {
        setCurrentTaskId(id);
        setTask(currenttask);
    };

    const LoginForm = () => (
        <Container component="main" maxWidth="xs">
            <Box
                sx={{
                    marginTop: 8,
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                }}
            >
                <Typography component="h1" variant="h5">
                    Sign in
                </Typography>
                <Box component="form" onSubmit={handleLogin} noValidate sx={{ mt: 1 }}>
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        id="username"
                        label="Username"
                        name="username"
                        autoComplete="username"
                        autoFocus
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                    />
                    <TextField
                        margin="normal"
                        required
                        fullWidth
                        name="password"
                        label="Password"
                        type="password"
                        id="password"
                        autoComplete="current-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                    <Button
                        type="submit"
                        fullWidth
                        variant="contained"
                        sx={{ mt: 3, mb: 2 }}
                    >
                        Sign In
                    </Button>
                </Box>
            </Box>
        </Container>
    );

    const AdminView = () => (
        <div className="App" style={{ display: "flex", flexDirection: "column" }}>
            <header>
                <div className="logo" style={{ display: "flex", alignItems: "center" }}>
                    <img
                        src="https://logos-world.net/wp-content/uploads/2020/09/Oracle-Logo.png"
                        alt="Oracle Logo"
                        style={{ width: "100px", height: "auto" }}
                    />
                </div>
                <h3>Oracle Todo App (Admin View)</h3>
                <Button onClick={handleLogout}>Logout</Button>
            </header>

            <div style={{ display: "flex" }}>
                {/* Projects Section (Left) */}
                <aside className="projects" style={{ flex: 1 }}>
                    <div id="mainprojects">
                        <h3>Proyectos</h3>
                        <NewProject addProject={addProject} isInserting={isInserting} />
                        {error && <p>Error: {error.message}</p>}
                        {isLoading && <CircularProgress />}
                        {!isLoading && (
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
                        )}
                    </div>
                </aside>

                {/* Tasks List (Center) */}
                <main style={{ flex: 2 }}>
                    <div id="TaskList">
                        <h3>Lista de Tareas</h3>
                        {currentProjectId && <NewItem addItem={addItem} isInserting={isInserting} />}
                        {error && <p>Error: {error.message}</p>}
                        {isLoading && <CircularProgress />}
                        {!isLoading && currentProjectId && (
                            <div>
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
                        )}
                    </div>
                </main>

                {/* Done Items and Task Details (Right) */}
                <aside className="doneItems" style={{ flex: 1 }}>
                    <div className="task-details">
                        <h3>Detalles de la Tarea</h3>
                        <p><strong>ID:</strong> {taskExp.id}</p>
                        <p><strong>Descripción:</strong> {taskExp.descripcion}</p>
                        <p><strong>Estatus:</strong> {taskExp.estatus}</p>
                        <p><strong>Tiempo Estimado:</strong> {taskExp.tiempoEstimado}</p>
                        <p><strong>Tiempo Invertido:</strong> {taskExp.tiempoReal}</p>
                        <p>
                            <strong>Fecha Entrega:</strong>{" "}
                            <Moment format="MMM Do hh:mm:ss">{taskExp.fechaFinalizacion}</Moment>
                        </p>
                        <p><strong>Calidad:</strong> {taskExp.puntuacionCalidad}</p>
                    </div>
                    <div className="Items-Done">
                        <h3>Tareas Completadas</h3>
                        {error && <p>Error: {error.message}</p>}
                        {isLoading && <CircularProgress />}
                        {!isLoading && currentProjectId && (
                            <div>
                                <table id="itemlistDone" className="itemlist">
                                    <TableBody>
                                        {items.map(
                                            (item) =>
                                                item.estatus === "Done" && (
                                                    <tr key={item.id}>
                                                        <td className="description">{item.descripcion}</td>
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

    const UserView = () => (
        <div className="App" style={{ display: "flex", flexDirection: "column" }}>
            <header>
                <div className="logo" style={{ display: "flex", alignItems: "center" }}>
                    <img
                        src="https://logos-world.net/wp-content/uploads/2020/09/Oracle-Logo.png"
                        alt="Oracle Logo"
                        style={{ width: "100px", height: "auto" }}
                    />
                </div>
                <h3>Oracle Todo App (User View)</h3>
                <Button onClick={handleLogout}>Logout</Button>
            </header>

            <div style={{ display: "flex" }}>
                {/* Projects Section (Left) */}
                <aside className="projects" style={{ flex: 1 }}>
                    <div id="mainprojects">
                        <h3>Proyectos</h3>
                        {error && <p>Error: {error.message}</p>}
                        {isLoading && <CircularProgress />}
                        {!isLoading && (
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
                                                    </tr>
                                                )
                                        )}
                                    </TableBody>
                                </table>
                            </div>
                        )}
                    </div>
                </aside>

                {/* Tasks List (Center) */}
                <main style={{ flex: 2 }}>
                    <div id="TaskList">
                        <h3>Lista de Tareas</h3>
                        {error && <p>Error: {error.message}</p>}
                        {isLoading && <CircularProgress />}
                        {!isLoading && currentProjectId && (
                            <div>
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
                                                    </tr>
                                                )
                                        )}
                                    </TableBody>
                                </table>
                            </div>
                        )}
                    </div>
                </main>

                {/* Done Items and Task Details (Right) */}
                <aside className="doneItems" style={{ flex: 1 }}>
                    <div className="task-details">
                        <h3>Detalles de la Tarea</h3>
                        <p><strong>ID:</strong> {taskExp.id}</p>
                        <p><strong>Descripción:</strong> {taskExp.descripcion}</p>
                        <p><strong>Estatus:</strong> {taskExp.estatus}</p>
                        <p><strong>Tiempo Estimado:</strong> {taskExp.tiempoEstimado}</p>
                        <p><strong>Tiempo Invertido:</strong> {taskExp.tiempoReal}</p>
                        <p>
                            <strong>Fecha Entrega:</strong>{" "}
                            <Moment format="MMM Do hh:mm:ss">{taskExp.fechaFinalizacion}</Moment>
                        </p>
                        <p><strong>Calidad:</strong> {taskExp.puntuacionCalidad}</p>
                    </div>
                    <div className="Items-Done">
                        <h3>Tareas Completadas</h3>
                        {error && <p>Error: {error.message}</p>}
                        {isLoading && <CircularProgress />}
                        {!isLoading && currentProjectId && (
                            <div>
                                <table id="itemlistDone" className="itemlist">
                                    <TableBody>
                                        {items.map(
                                            (item) =>
                                                item.estatus === "Done" && (
                                                    <tr key={item.id}>
                                                        <td className="description">{item.descripcion}</td>
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
                        )}
                    </div>
                </aside>
            </div>
        </div>
    );


    if (!isAuthenticated) {
        return <LoginForm />;
    }

    return (
        <>
            {userRole === 'ADMIN' ? <AdminView /> : <UserView />}
        </>
    );
}

export default App;