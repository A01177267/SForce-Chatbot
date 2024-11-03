package com.springboot.MyTodoList.service;

import com.springboot.MyTodoList.model.Proyecto;
import com.springboot.MyTodoList.repository.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Service
public class ProyectoService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    public Proyecto crearProyecto(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public List<Proyecto> findAll() {
        return proyectoRepository.findAll();
    }

    public Optional<Proyecto> actualizarProyecto(Long id, Proyecto proyecto) {
        return proyectoRepository.findById(id)
            .map(proyectoExistente -> {
                proyectoExistente.setNombre(proyecto.getNombre());
                proyectoExistente.setEstatus(proyecto.getEstatus());
                proyectoExistente.setFechaInicio(proyecto.getFechaInicio());
                proyectoExistente.setFechaFin(proyecto.getFechaFin());
                return proyectoRepository.save(proyectoExistente);
            });
    }

    public boolean eliminarProyecto(Long id) {
        if (proyectoRepository.existsById(id)) {
            proyectoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public ResponseEntity<Proyecto> obtenerProyectoPorId(Long id) {
        Optional<Proyecto> proyectoData = proyectoRepository.findById(id);
        if (proyectoData.isPresent()) {
            return new ResponseEntity<>(proyectoData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    public ResponseEntity<Proyecto> obtenerProyectoPorNombre(String nombre) {
        List<Proyecto> proyectos = proyectoRepository.findByNombre(nombre);
        if (!proyectos.isEmpty()) {
            return new ResponseEntity<>(proyectos.get(0), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
