import React, { useState, useEffect } from 'react';
import { Dialog, DialogTitle, DialogContent, TextField, DialogActions, Button, Select, MenuItem } from '@mui/material';


const EditModal = ({ open, handleClose, item, saveChanges, type }) => {
  const [nombre, setNombre] = useState('');
  const [estatus, setEstatus] = useState('');
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [tiempoEstimado, setTiempoEstimado] = useState('');
  const [tiempoReal, setTiempoReal] = useState('');
  const [fechaFinalizacion, setFechaFinalizacion] = useState('');
  const [puntuacionCalidad, setPuntuacionCalidad] = useState('');
  const [eficienciaTarea, setEficienciaTarea] = useState('');
  const [productividadTarea, setProductividadTarea] = useState('');

  useEffect(() => {
    if (type === 'project') {
      setNombre(item.nombre || '');
      setEstatus(item.estatus || '');
      setFechaInicio(item.fechaInicio || '');
      setFechaFin(item.fechaFin || '');
    } else if (type === 'task') {
      setDescripcion(item.descripcion || '');
      setEstatus(item.estatus || '');
      setTiempoEstimado(item.tiempoEstimado || '');
      setTiempoReal(item.tiempoReal || '');
      setFechaFinalizacion(item.fechaFinalizacion || '');
      setPuntuacionCalidad(item.puntuacionCalidad || '');
      setEficienciaTarea(item.eficienciaTarea || '');
      setProductividadTarea(item.productividadTarea || '');
    }
  }, [item, type]);

  const handleSave = () => {
    const editedItem = type === 'project' ? {
      ...item,
      nombre,
      estatus,
      fechaInicio,
      fechaFin
    } : {
      ...item,
      descripcion,
      estatus,
      tiempoEstimado,
      tiempoReal,
      fechaFinalizacion,
      puntuacionCalidad,
      eficienciaTarea,
      productividadTarea
    };
    saveChanges(editedItem);
    handleClose();
  };

  return (
    
    <Dialog open={open} onClose={handleClose} maxWidth="md" fullWidth>
      <DialogTitle>Edit {type === 'project' ? 'Project' : 'Task'}</DialogTitle>
      <DialogContent>
        {type === 'project' ? (
          <>
            <TextField label="Nombre" fullWidth variant="standard" value={nombre} onChange={e => setNombre(e.target.value)} />
            <Select value={estatus} onChange={e => setEstatus(e.target.value)} displayEmpty fullWidth variant="standard">
            <MenuItem value=""><em>None</em></MenuItem>
            <MenuItem value="Active">In Progress</MenuItem>
            <MenuItem value="Done">Done</MenuItem>
            </Select>
            <TextField label="Fecha de Inicio" fullWidth variant="standard" type="date" InputLabelProps={{ shrink: true }} value={fechaInicio} onChange={e => setFechaInicio(e.target.value)} />
            <TextField label="Fecha de Fin" fullWidth variant="standard" type="date" InputLabelProps={{ shrink: true }} value={fechaFin} onChange={e => setFechaFin(e.target.value)} />
          </>
        ) : (
          <>
            <TextField label="Descripción" fullWidth variant="standard" value={descripcion} onChange={e => setDescripcion(e.target.value)} />
            <Select value={estatus} onChange={e => setEstatus(e.target.value)} displayEmpty fullWidth variant="standard">
            <MenuItem value=""><em>None</em></MenuItem>
            <MenuItem value="In Progress">In Progress</MenuItem>
            <MenuItem value="Done">Done</MenuItem>
            </Select>
            <TextField label="Tiempo Estimado" fullWidth variant="standard" value={tiempoEstimado} onChange={e => setTiempoEstimado(e.target.value)} />
            <TextField label="Tiempo Real" fullWidth variant="standard" value={tiempoReal} onChange={e => setTiempoReal(e.target.value)} />
            <TextField label="Fecha de Finalización" fullWidth variant="standard" type="date" InputLabelProps={{ shrink: true }} value={fechaFinalizacion} onChange={e => setFechaFinalizacion(e.target.value)} />
            <TextField label="Puntuación de Calidad" fullWidth variant="standard" value={puntuacionCalidad} onChange={e => setPuntuacionCalidad(e.target.value)} />
            <TextField label="Eficiencia de la Tarea" fullWidth variant="standard" value={eficienciaTarea} onChange={e => setEficienciaTarea(e.target.value)} />
            <TextField label="Productividad de la Tarea" fullWidth variant="standard" value={productividadTarea} onChange={e => setProductividadTarea(e.target.value)} />
          </>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Cancel</Button>
        <Button onClick={handleSave}>Save</Button>
      </DialogActions>
    </Dialog>
  );
};

export default EditModal;
