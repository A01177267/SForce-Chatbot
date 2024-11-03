package com.springboot.MyTodoList.util;

public enum BotCommands {
    START_COMMAND("/start"),
    HIDE_COMMAND("/hide"),
    TODO_LIST("/todolist"),
    ADD_ITEM("/additem"),
    CREATE_PROJECT("/createproject"),
    LIST_PROJECTS("/listprojects"),
    UPDATE_PROJECT("/updateproject"),
    DELETE_PROJECT("/deleteproject"),
    GET_PROJECT("/getproject"),
    LIST_TASKS("/listtasks"),
    UPDATE_TASK("/updatetask"),
    DELETE_TASK("/deletetask"),
    GET_TASK("/gettask");

    private String command;

    BotCommands(String enumCommand) {
        this.command = enumCommand;
    }

    public String getCommand() {
        return command;
    }
}
