package com.springboot.MyTodoList.util;

public enum BotMessages {

    HELLO_MYTODO_BOT(
        "Hello! I'm MyTodoList Bot! Please choose an option below or type a command:\n- /todolist to manage todo items\n- /listprojects to see all projects\n- /listtasks to see all tasks"),
    BOT_REGISTERED_STARTED("Bot registered and started successfully!"),
    ITEM_DONE("Item marked as done! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
    ITEM_UNDONE("Item marked as undone! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
    ITEM_DELETED("Item deleted! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
    TYPE_NEW_TODO_ITEM("Type a new todo item below and press the send button (blue arrow) on the right-hand side."),
    NEW_ITEM_ADDED("New item added to your ToDo list! Select /todolist to return to the list of todo items, or /start to go to the main screen."),
    PROJECT_CREATED("Project created successfully! Use /listprojects to see all projects."),
    PROJECT_UPDATED("Project updated successfully! Use /listprojects to see all projects."),
    PROJECT_DELETED("Project deleted successfully! Use /listprojects to see all projects."),
    TASK_CREATED("Task created successfully! Use /listtasks to see all tasks."),
    TASK_UPDATED("Task updated successfully! Use /listtasks to see all tasks."),
    TASK_DELETED("Task deleted successfully! Use /listtasks to see all tasks."),
    BYE("Bye! Select /start to resume!");

    private String message;

    BotMessages(String enumMessage) {
        this.message = enumMessage;
    }

    public String getMessage() {
        return message;
    }

}
