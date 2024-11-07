package com.springboot.MyTodoList.service;
import org.hibernate.Hibernate;
import com.springboot.MyTodoList.model.Proyecto;
import com.springboot.MyTodoList.repository.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProyectoService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    // Create a new project
    public Proyecto crearProyecto(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    // List all projects
    public List<Proyecto> findAll() {
        return proyectoRepository.findAll();
    }

    // Update an existing project
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

    // Delete a project
    public boolean eliminarProyecto(Long id) {
        if (proyectoRepository.existsById(id)) {
            proyectoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Get a project by ID
    @Transactional(readOnly = true)
    public ResponseEntity<Proyecto> obtenerProyectoPorId(Long id) {
        Optional<Proyecto> proyectoData = proyectoRepository.findById(id);
        if (proyectoData.isPresent()){
            Proyecto proyecto = proyectoData.get();
            // Initialize the tareas collection explicitly
            Hibernate.initialize(proyecto.getTareas());
            return new ResponseEntity<>(proyecto, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    public ResponseEntity<Proyecto> obtenerProyectoPorNombre(String nombre) {
        List<Proyecto> proyectos = proyectoRepository.findByNombre(nombre);
        if (!proyectos.isEmpty()) {
            return new ResponseEntity<>(proyectos.get(0), HttpStatus.OK); // Asumimos que el nombre es único
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}

