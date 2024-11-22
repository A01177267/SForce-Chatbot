package com.springboot.MyTodoList.util;

public enum BotLabels {
	
	SHOW_MAIN_SCREEN("Show Main Screen"), 
	HIDE_MAIN_SCREEN("Hide Main Screen"),
	LIST_ALL_ITEMS("List All Items"), 
	ADD_NEW_ITEM("Add New Item"),
	DONE("DONE"),
	UNDO("UNDO"),
	DELETE("DELETE"),
	MY_TODO_LIST("MY TODO LIST"),
	DASH("-"),
	LIST_PROJECTS("List Projects"), 
	ADD_PROJECT("Add Project"),
	UPDATE_PROJECT("Update Project"),
	DELETE_PROJECT("Delete Project"),
	LIST_TASKS("List Tasks"),
	DELETE_TASK("Delete Task"),
	CODE_REVIEW("Code Review"),
	ADD_TASK("Add Task"); 

	private String label;

	BotLabels(String enumLabel) {
		this.label = enumLabel;
	}

	public String getLabel() {
		return label;
	}

}