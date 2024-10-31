package com.springboot.MyTodoList.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.springboot.MyTodoList.service.ProyectoService;

import com.springboot.MyTodoList.model.Proyecto;


import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.repository.TareaRepository;
import com.springboot.MyTodoList.repository.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;


@Service
public class TareaService {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private ProyectoRepository pr;

    private static final Logger logger = LoggerFactory.getLogger(TareaService.class);

    // Create a new task
    public Tarea crearTarea(Long proyectId, Tarea tarea) {
        //inicializr obj Proyecto
        logger.info("SET PROYECTO "+proyectId);
        //ProyectoService ps = new ProyectoService();
        //ResponseEntity<Proyecto> pid = ps.obtenerProyectoPorId(tarea.proyectoID);

        pr.findById(proyectId).map(proyecto -> {
            logger.info("SET PROYECTO 2");
            tarea.setProyecto(proyecto);
            logger.info("SET PROYECTO 3");
            return tareaRepository.save(tarea);
        });

        //tarea.setProyecto(pid.getBody());
        logger.info("set proyecto = SUCCESS");
        return tarea;
    }

    // List all tasks
    public List<Tarea> findAll() {
        return tareaRepository.findAll();
    }

    // Update an existing task
    public Optional<Tarea> actualizarTarea(Long id, Tarea tarea) {
        return tareaRepository.findById(id)
            .map(tareaExistente -> {
                tareaExistente.setDescripcion(tarea.getDescripcion());
                tareaExistente.setEstatus(tarea.getEstatus());
                tareaExistente.setTiempoEstimado(tarea.getTiempoEstimado());
                tareaExistente.setTiempoReal(tarea.getTiempoReal());
                tareaExistente.setFechaFinalizacion(tarea.getFechaFinalizacion());
                tareaExistente.setPuntuacionCalidad(tarea.getPuntuacionCalidad());
                tareaExistente.setEficienciaTarea(tarea.getEficienciaTarea());
                tareaExistente.setProductividadTarea(tarea.getProductividadTarea());
                return tareaRepository.save(tareaExistente);
            });
    }

    // Delete a task
    public boolean eliminarTarea(Long id) {
        if (tareaRepository.existsById(id)) {
            tareaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Get a task by ID
    public Optional<Tarea> obtenerTareaPorId(Long id) {
        return tareaRepository.findById(id);
    }
}
