package com.springboot.MyTodoList.controller;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.springboot.MyTodoList.model.Proyecto;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.service.ProyectoService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {

	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	private TareaService TareaService;
	private ToDoItemService toDoItemService;
	private ProyectoService ProyectoService;
	private String botName;

	public ToDoItemBotController(String botToken, String botName,ToDoItemService toDoItemService, TareaService tareaService, ProyectoService proyectoService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.toDoItemService = toDoItemService;
		this.TareaService = tareaService;
		this.ProyectoService = proyectoService;
		this.botName = botName;
	}

	@Override
	public void onUpdateReceived(Update update) {
		if (update.hasMessage() && update.getMessage().hasText()) {
			String messageTextFromTelegram = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();

			if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
				|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
					mostrarMenu(chatId);
				}

			else if (messageTextFromTelegram.indexOf(BotLabels.DONE.getLabel()) != -1) {
				marcarTareaCompletada(chatId, messageTextFromTelegram);
			}

			else if (messageTextFromTelegram.indexOf(BotLabels.LIST_TASKS.getLabel()) != -1) {
				mostrarTareas(chatId);
			}

			else if (messageTextFromTelegram.equals(BotLabels.ADD_TASK.getLabel()) || messageTextFromTelegram.contains(":")) {
				crearTarea(chatId, messageTextFromTelegram);
			}

			else if (messageTextFromTelegram.indexOf(BotLabels.UNDO.getLabel()) != -1) {

				String undo = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(undo);

				try {

					ToDoItem item = getToDoItemById(id).getBody();
					item.setDone(false);
					updateToDoItem(item, id);
					BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_UNDONE.getMessage(), this);

				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.indexOf(BotLabels.DELETE.getLabel()) != -1) {

				String delete = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(delete);

				try {

					deleteToDoItem(id).getBody();
					BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DELETED.getMessage(), this);

				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.equals(BotCommands.HIDE_COMMAND.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.HIDE_MAIN_SCREEN.getLabel())) {

				BotHelper.sendMessageToTelegram(chatId, BotMessages.BYE.getMessage(), this);

			} else if (messageTextFromTelegram.equals(BotCommands.TODO_LIST.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.LIST_ALL_ITEMS.getLabel())
					|| messageTextFromTelegram.equals(BotLabels.MY_TODO_LIST.getLabel())) {

				List<ToDoItem> allItems = getAllToDoItems();
				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboard = new ArrayList<>();

				// command back to main screen
				KeyboardRow mainScreenRowTop = new KeyboardRow();
				mainScreenRowTop.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
				keyboard.add(mainScreenRowTop);

				KeyboardRow firstRow = new KeyboardRow();
				firstRow.add(BotLabels.ADD_NEW_ITEM.getLabel());
				keyboard.add(firstRow);

				KeyboardRow myTodoListTitleRow = new KeyboardRow();
				myTodoListTitleRow.add(BotLabels.MY_TODO_LIST.getLabel());
				keyboard.add(myTodoListTitleRow);

				List<ToDoItem> activeItems = allItems.stream().filter(item -> item.isDone() == false)
						.collect(Collectors.toList());

				for (ToDoItem item : activeItems) {

					KeyboardRow currentRow = new KeyboardRow();
					currentRow.add(item.getDescription());
					currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
					keyboard.add(currentRow);
				}

				List<ToDoItem> doneItems = allItems.stream().filter(item -> item.isDone() == true)
						.collect(Collectors.toList());

				for (ToDoItem item : doneItems) {
					KeyboardRow currentRow = new KeyboardRow();
					currentRow.add(item.getDescription());
					currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
					currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
					keyboard.add(currentRow);
				}

				// command back to main screen
				KeyboardRow mainScreenRowBottom = new KeyboardRow();
				mainScreenRowBottom.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
				keyboard.add(mainScreenRowBottom);

				keyboardMarkup.setKeyboard(keyboard);

				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(BotLabels.MY_TODO_LIST.getLabel());
				messageToTelegram.setReplyMarkup(keyboardMarkup);

				try {
					execute(messageToTelegram);
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.equals(BotCommands.ADD_ITEM.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.ADD_NEW_ITEM.getLabel())) {
				try {
					SendMessage messageToTelegram = new SendMessage();
					messageToTelegram.setChatId(chatId);
					messageToTelegram.setText(BotMessages.TYPE_NEW_TODO_ITEM.getMessage());
					// hide keyboard
					ReplyKeyboardRemove keyboardMarkup = new ReplyKeyboardRemove(true);
					messageToTelegram.setReplyMarkup(keyboardMarkup);

					// send message
					execute(messageToTelegram);

				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
			}
		}
	}

	private void mostrarMenu(long chatId) {
		SendMessage messageToTelegram = new SendMessage();
		messageToTelegram.setChatId(chatId);
		messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		// first row
		KeyboardRow row = new KeyboardRow();
		row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
		row.add(BotLabels.LIST_TASKS.getLabel());

		//second row
		KeyboardRow row2 = new KeyboardRow();
        row2.add(BotLabels.ADD_TASK.getLabel());
        keyboard.add(row2);


		// Add the first row to the keyboard
		keyboard.add(row);
		keyboardMarkup.setKeyboard(keyboard);

		// Add the keyboard markup
		messageToTelegram.setReplyMarkup(keyboardMarkup);

		try {
			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	private void marcarTareaCompletada(long chatId, String messageTextFromTelegram) {
		String done = messageTextFromTelegram.substring(0,messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
		Long id = Long.valueOf(done);
			try {
				Tarea tarea = TareaService.obtenerTareaPorId(id);
				tarea.setEstatus(done);
				TareaService.actualizarTarea(id, tarea);
				BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DONE.getMessage(), this);
				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}
	}

	private void mostrarTareas(long chatId) {
		try {
			List<Proyecto> proyectos  = ProyectoService.findAll();
			List<Tarea> todasLasTareas = TareaService.findAll();

			StringBuilder msj = new StringBuilder();
			msj.append("Lista de tareas por proyectos:\n\n");

			for (Proyecto proyecto : proyectos) {
				msj.append(proyecto.getNombre()).append("\n");

				List<Tarea> tareasProyecto = todasLasTareas.stream()
					.filter(tarea -> tarea.getProyecto().getId().equals(proyecto.getId()))
					.collect(Collectors.toList());

				if (tareasProyecto.isEmpty()) {
					msj.append("No hay tareas");
				} else {
					for (Tarea tarea : tareasProyecto) {
						msj.append("- ").append(tarea.getDescripcion());
						msj.append(" [").append(tarea.getEstatus()).append("]");
						msj.append("\n");
					}
				}
				msj.append("\n");
			}

			SendMessage messageToTelegram = new SendMessage();
			messageToTelegram.setChatId(chatId);
			messageToTelegram.setText(msj.toString());

			execute(messageToTelegram);
			logger.info("Lista de tareas enviada");
		} catch (Exception e) {
			logger.error("Error al mostrar tareas: " + e.getMessage(), e);
		}
	}

	private void crearTarea(long chatId, String messageTextFromTelegram) {
		if (messageTextFromTelegram.equals(BotLabels.ADD_TASK.getLabel())) {
			mostrarProyectosDisponibles(chatId);
			return;
		}

		String[] partes = messageTextFromTelegram.split(":", 2);
		Long proyectoId = Long.parseLong(partes[0].trim());
		String descripcion = partes[1].trim();

		ResponseEntity<Proyecto> proyectoResponse = ProyectoService.obtenerProyectoPorId(proyectoId);

		Tarea nuevaTarea = new Tarea();
		nuevaTarea.setDescripcion(descripcion);
		nuevaTarea.setEstatus("In Progress");
		nuevaTarea.setTiempoEstimado(0.0f);
		nuevaTarea.setTiempoReal(0.0f);
		nuevaTarea.setPuntuacionCalidad(0);
		nuevaTarea.setEficienciaTarea(0.0f);
		nuevaTarea.setProductividadTarea(0.0f);

		TareaService.crearTarea(proyectoId, nuevaTarea);
		mostrarMenu(chatId);
	}

	private void mostrarProyectosDisponibles(long chatId) {
		List<Proyecto> proyectos = ProyectoService.findAll();
		StringBuilder mensaje = new StringBuilder();
		mensaje.append("Proyectos disponibles:\n\n");

		for (Proyecto proyecto : proyectos) {
			mensaje.append(String.format("ID: %d - %s (%s)\n",
			proyecto.getId(),
			proyecto.getNombre(),
			proyecto.getEstatus()));
		}

		mensaje.append("\nPara crear una tarea, escriba:\n");
		mensaje.append("ID_PROYECTO:DESCRIPCION_TAREA\n");

		BotHelper.sendMessageToTelegram(chatId, mensaje.toString(), this);
	}

	@Override
	public String getBotUsername() {
		return botName;
	}

	// GET /todolist
	public List<ToDoItem> getAllToDoItems() { 
		return toDoItemService.findAll();
	}

	// GET BY ID /todolist/{id}
	public ResponseEntity<ToDoItem> getToDoItemById(@PathVariable int id) {
		try {
			ResponseEntity<ToDoItem> responseEntity = toDoItemService.getItemById(id);
			return new ResponseEntity<ToDoItem>(responseEntity.getBody(), HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
	}

	// PUT /todolist
	public ResponseEntity addToDoItem(@RequestBody ToDoItem todoItem) throws Exception {
		ToDoItem td = toDoItemService.addToDoItem(todoItem);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.set("location", "" + td.getID());
		responseHeaders.set("Access-Control-Expose-Headers", "location");
		// URI location = URI.create(""+td.getID())

		return ResponseEntity.ok().headers(responseHeaders).build();
	}

	// UPDATE /todolist/{id}
	public ResponseEntity updateToDoItem(@RequestBody ToDoItem toDoItem, @PathVariable int id) {
		try {
			ToDoItem toDoItem1 = toDoItemService.updateToDoItem(id, toDoItem);
			System.out.println(toDoItem1.toString());
			return new ResponseEntity<>(toDoItem1, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
	}

	// DELETE todolist/{id}
	public ResponseEntity<Boolean> deleteToDoItem(@PathVariable("id") int id) {
		Boolean flag = false;
		try {
			flag = toDoItemService.deleteToDoItem(id);
			return new ResponseEntity<>(flag, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
			return new ResponseEntity<>(flag, HttpStatus.NOT_FOUND);
		}
	}
}