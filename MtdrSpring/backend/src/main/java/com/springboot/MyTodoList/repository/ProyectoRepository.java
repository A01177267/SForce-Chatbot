package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.Proyecto;
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
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    // Encuentra proyectos por su estatus
    List<Proyecto> findByEstatus(String estatus);

    // Encuentra proyectos que comienzan en una fecha específica
    List<Proyecto> findByFechaInicio(Date fechaInicio);

    // Encuentra proyectos que terminan en una fecha específica
    List<Proyecto> findByFechaFin(Date fechaFin);

    // Encuentra proyectos por nombre
    List<Proyecto> findByNombreContaining(String nombre);

    // Encuentra proyectos que están en curso
    @Query("SELECT p FROM Proyecto p WHERE p.fechaInicio <= CURRENT_DATE AND (p.fechaFin IS NULL OR p.fechaFin >= CURRENT_DATE)")
    List<Proyecto> findProyectosEnCurso();

    // Encuentra proyectos con tareas pendientes (Estatus no es 'Completado')
    @Query("SELECT p FROM Proyecto p JOIN p.tareas t WHERE t.estatus != 'Completado'")
    List<Proyecto> findProyectosConTareasPendientes();
}
