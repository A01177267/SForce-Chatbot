package com.springboot.MyTodoList.util;

public enum BotCommands {
    // Comandos Generales
    START_COMMAND("/start"),
    HIDE_COMMAND("/hide"),
    
    // Comandos específicos de ToDoList
    TODO_LIST("/todolist"), // Este comando se mantiene sin cambios
    ADD_ITEM("/additem"), // Este comando se mantiene para añadir ítems a la ToDoList

    // Comandos para la gestión de Proyectos
    CREATE_PROJECT("/createproject"), // Nuevo comando para crear un proyecto
    LIST_PROJECTS("/listprojects"), // Nuevo comando para listar proyectos
    UPDATE_PROJECT("/updateproject"), // Nuevo comando para actualizar un proyecto
    DELETE_PROJECT("/deleteproject"), // Nuevo comando para eliminar un proyecto
    GET_PROJECT("/getproject"), // Nuevo comando para obtener detalles de un proyecto

    // Comandos para la gestión de Tareas
    LIST_TASKS("/listtasks"), // Nuevo comando para listar tareas
    UPDATE_TASK("/updatetask"), // Nuevo comando para actualizar una tarea
    DELETE_TASK("/deletetask"), // Nuevo comando para eliminar una tarea
    GET_TASK("/gettask"); // Nuevo comando para obtener detalles de una tarea

    private String command;

    BotCommands(String enumCommand) {
        this.command = enumCommand;
    }

    public String getCommand() {
        return command;
    }
}
