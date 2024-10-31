package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    // Encuentra tareas por estatus
    List<Tarea> findByEstatus(String estatus);

    // Encuentra tareas asignadas a un proyecto específico
    List<Tarea> findByProyectoId(Long proyectoId);

    // Encuentra tareas que deben completarse antes de una fecha específica
    List<Tarea> findByFechaFinalizacionBefore(Date fecha);

    // Encuentra tareas que tienen una puntuación de calidad por debajo de un umbral
    List<Tarea> findByPuntuacionCalidadLessThan(Integer puntuacionCalidad);

    // Encuentra tareas ordenadas por su productividad descendiente
    @Query("SELECT t FROM Tarea t ORDER BY t.productividadTarea DESC")
    List<Tarea> findTareasByProductividadDesc();

    // Encuentra tareas por eficiencia específica
    List<Tarea> findByEficienciaTarea(Float eficiencia);

    // Encuentra tareas por tiempo estimado mayor que un valor dado
    List<Tarea> findByTiempoEstimadoGreaterThan(Float tiempoEstimado);
}
