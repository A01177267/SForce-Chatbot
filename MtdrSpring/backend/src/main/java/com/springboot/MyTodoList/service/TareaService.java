package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.repository.TareaRepository;
import com.springboot.MyTodoList.repository.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TareaService {

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private ProyectoRepository proyectoRepository;

    public Tarea crearTarea(Long proyectoId, Tarea tarea) {
        return proyectoRepository.findById(proyectoId).map(proyecto -> {
            tarea.setProyecto(proyecto);
            return tareaRepository.save(tarea);
        }).orElseThrow(() -> new RuntimeException("Proyecto no encontrado"));
    }

    public List<Tarea> findAll() {
        return tareaRepository.findAll();
    }

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

    public boolean eliminarTarea(Long id) {
        if (tareaRepository.existsById(id)) {
            tareaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Tarea> obtenerTareaPorId(Long id) {
        return tareaRepository.findById(id);
    }
}
