package com.springboot.MyTodoList.controller;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import java.util.HashMap;
import java.util.Map;

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

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.model.Proyecto;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.ProyectoService;
import com.springboot.MyTodoList.repository.ProyectoRepository;
import com.springboot.MyTodoList.repository.TareaRepository;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {

	private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
	private ToDoItemService toDoItemService;
	private ProyectoService ProyectoService;
	private ProyectoRepository proyectoRepository;
	private TareaRepository tareaRepository;
	private TareaService tareaService;
	private String botName;
    private Long projectId;
	private Map<Long, Boolean> creatingProjectState = new HashMap<>();
	private Map<Long, UpdateProjectState> projectUpdateStates = new HashMap<>();
	private Map<Long, Proyecto> selectedProjects = new HashMap<>();
	private Map<Long, Boolean> viewingProjectState = new HashMap<>();
	private Map<Long, Boolean> deletingProjectState = new HashMap<>();
	private Map<Long, UpdateProjectState> TaskUpdateStates = new HashMap<>();
	private Map<Long, Proyecto> selectedTasks = new HashMap<>();
	private Map<Long, Boolean> viewingTaskState = new HashMap<>();
	private Map<Long, Boolean> deletingTaskState = new HashMap<>();
	private Map<Long, Long> selectedProjectMap = new HashMap<>(); // chatId -> projectId
    private Map<Long, Boolean> creatingTaskState = new HashMap<>(); // chatId -> isCreatingTask


	private enum UpdateProjectState {
		SELECTING_PROJECT,
		ENTERING_NAME,
		SELECTING_STATUS
	}

	public Tarea crearTarea(Long projectId, Tarea tarea) {
		// Buscar el proyecto correspondiente
		Proyecto proyecto = proyectoRepository.findById(projectId)
							.orElseThrow(() -> new EntityNotFoundException("Proyecto no encontrado"));
	
		// Asociar la tarea con el proyecto antes de guardarla
		tarea.setProyecto(proyecto);
		
		// Guardar la tarea asociada con el proyecto
		return tareaRepository.save(tarea);
	}
	
	public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, ProyectoService ProyectoService,TareaService tareaService) {
		super(botToken);
		logger.info("Bot Token: " + botToken);
		logger.info("Bot name: " + botName);
		this.toDoItemService = toDoItemService;
		this.ProyectoService = ProyectoService;
		this.tareaService = tareaService;
		this.botName = botName;
	}

	@Override
	public void onUpdateReceived(Update update) {
	
		if (update.hasMessage() && update.getMessage().hasText()) {
	
			String messageTextFromTelegram = update.getMessage().getText();
			long chatId = update.getMessage().getChatId();

			if (projectUpdateStates.containsKey(chatId)) {
				handleProjectUpdate(chatId, messageTextFromTelegram);
				return;
			}

			else if (deletingProjectState.getOrDefault(chatId, false) && messageTextFromTelegram.startsWith("📋 Proyecto: ")) {
				handleProjectDeletion(chatId, messageTextFromTelegram);
			}


			if (TaskUpdateStates.containsKey(chatId)) {
				handleProjectUpdate(chatId, messageTextFromTelegram);
				return;
			}

			else if (deletingTaskState.getOrDefault(chatId, false) && messageTextFromTelegram.startsWith("📋 Proyecto: ")) {
				handleProjectDeletion(chatId, messageTextFromTelegram);
			}

			if (creatingProjectState.getOrDefault(chatId, false)) {
				// Crear el proyecto con el nombre proporcionado
				Proyecto nuevoProyecto = new Proyecto();
				nuevoProyecto.setNombre(messageTextFromTelegram);
				ProyectoService.crearProyecto(nuevoProyecto);

				// Confirmación al usuario y limpiar el estado
				BotHelper.sendMessageToTelegram(chatId, BotMessages.PROJECT_CREATED.getMessage(), this);
				creatingProjectState.put(chatId, false);
				return;
			}

			if (creatingTaskState.getOrDefault(chatId, false)) {
				// Crear la tarea
				Tarea newTask = new Tarea();
				newTask.setDescripcion(messageTextFromTelegram);
		
				Long projectId = selectedProjectMap.get(chatId);
				try {
					Tarea createdTask = tareaService.crearTarea(projectId, newTask);
					BotHelper.sendMessageToTelegram(chatId, "Tarea creada con éxito: " + createdTask.getDescripcion(), this);
				} catch (Exception e) {
					BotHelper.sendMessageToTelegram(chatId, "Error al crear la tarea: " + e.getMessage(), this);
				}
		
				// Restablecer el estado de creación de tareas
				creatingTaskState.put(chatId, false);
				selectedProjectMap.remove(chatId); // Limpiar el proyecto seleccionado
			}
			
				
			if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
					|| messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {
	
				SendMessage messageToTelegram = new SendMessage();
				messageToTelegram.setChatId(chatId);
				messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());
	
				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboard = new ArrayList<>();
	
				// Primera fila de botones
				KeyboardRow row = new KeyboardRow();
				row.add(BotLabels.LIST_TASKS.getLabel());
				row.add(BotLabels.ADD_TASK.getLabel());
				keyboard.add(row);
	
				// Segunda fila de botones
				row = new KeyboardRow();
				row.add(BotLabels.UPDATE_TASK.getLabel());
				row.add(BotLabels.DELETE_TASK.getLabel());
				keyboard.add(row);
	
				// Tercera fila para mostrar y agregar proyectos
				row = new KeyboardRow();
				row.add(BotLabels.LIST_PROJECTS.getLabel());
				row.add(BotLabels.ADD_PROJECT.getLabel());
				keyboard.add(row);

				// Cuarta fila para actualizar y eleminar proyectos
				row = new KeyboardRow();
				row.add(BotLabels.UPDATE_PROJECT.getLabel());
				row.add(BotLabels.DELETE_PROJECT.getLabel());
				keyboard.add(row);
	
				// Configuración del teclado
				keyboardMarkup.setKeyboard(keyboard);
				messageToTelegram.setReplyMarkup(keyboardMarkup);
	
				try {
					execute(messageToTelegram);
				} catch (TelegramApiException e) {
					logger.error(e.getLocalizedMessage(), e);
				}
	
			} 
			
			else if (messageTextFromTelegram.equals(BotLabels.ADD_PROJECT.getLabel())) {
				// Cambiar el estado para esperar el nombre del proyecto
				creatingProjectState.put(chatId, true);
				BotHelper.sendMessageToTelegram(chatId, "Por favor, envíame el nombre del nuevo proyecto.", this);

			}
			else if (messageTextFromTelegram.equals(BotLabels.ADD_TASK.getLabel())) {
					// Obtener la lista de proyectos
					List<Proyecto> proyectos = ProyectoService.findAll();
					
					// Construir el teclado con botones
					ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
					List<KeyboardRow> keyboard = new ArrayList<>();
					
					// Botón para volver al menú principal
					KeyboardRow mainMenuRow = new KeyboardRow();
					mainMenuRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
					keyboard.add(mainMenuRow);
					
					// Crear un botón para cada proyecto
					for (Proyecto proyecto : proyectos) {
						KeyboardRow row = new KeyboardRow();
						// Usar un formato especial para identificar que es un botón de proyecto
						row.add("📋 Proyecto: " + proyecto.getId() + " - " + proyecto.getNombre());
						keyboard.add(row);
					}
                    keyboardMarkup.setKeyboard(keyboard);
                    keyboardMarkup.setResizeKeyboard(true);
                    
                    SendMessage messageToTelegram = new SendMessage();
                    messageToTelegram.setChatId(chatId);
                    messageToTelegram.setText("Selecciona un proyecto para crear la tarea:");
                    messageToTelegram.setReplyMarkup(keyboardMarkup);
                    
                    try {
                        execute(messageToTelegram);
                    } catch (TelegramApiException e) {
                        logger.error(e.getLocalizedMessage(), e);
                    }

		// Activar el estado de visualización de proyectos
		viewingProjectState.put(chatId, true);
		
		try {
			execute(messageToTelegram);
		} catch (TelegramApiException e) {
			logger.error(e.getLocalizedMessage(), e);
		}


			}

			else if (messageTextFromTelegram.equals(BotLabels.UPDATE_PROJECT.getLabel())) {
				startProjectUpdate(chatId);
			}

			else if (messageTextFromTelegram.equals(BotLabels.DELETE_PROJECT.getLabel())) {
				startProjectDeletion(chatId);
			}

			else if (messageTextFromTelegram.equals("✅ Confirmar eliminación") && 
         selectedProjects.containsKey(chatId)) {
    Proyecto proyectoAEliminar = selectedProjects.get(chatId);
    try {
        ProyectoService.eliminarProyecto(proyectoAEliminar.getId());
        
        SendMessage successMessage = new SendMessage();
        successMessage.setChatId(chatId);
        successMessage.setText("✅ El proyecto '" + proyectoAEliminar.getNombre() + "' ha sido eliminado exitosamente.");
        
        // Volver al menú principal
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        successMessage.setReplyMarkup(keyboardMarkup);
        
        execute(successMessage);
        
        // Limpiar estados
        deletingProjectState.remove(chatId);
        selectedProjects.remove(chatId);
    } catch (Exception e) {
        logger.error("Error al eliminar el proyecto", e);
        sendErrorMessage(chatId, "Hubo un error al eliminar el proyecto. Por favor, intenta de nuevo.");
    }
}
else if (messageTextFromTelegram.equals("❌ Cancelar") && 
         selectedProjects.containsKey(chatId)) {
    // Cancelar la eliminación
    SendMessage cancelMessage = new SendMessage();
    cancelMessage.setChatId(chatId);
    cancelMessage.setText("Operación cancelada. El proyecto no ha sido eliminado.");
    
    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
    List<KeyboardRow> keyboard = new ArrayList<>();
    KeyboardRow row = new KeyboardRow();
    row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
    keyboard.add(row);
    keyboardMarkup.setKeyboard(keyboard);
    cancelMessage.setReplyMarkup(keyboardMarkup);
    
    try {
        execute(cancelMessage);
        // Limpiar estados
        deletingProjectState.remove(chatId);
        selectedProjects.remove(chatId);
    } catch (TelegramApiException e) {
        logger.error("Error al enviar mensaje de cancelación", e);
    }
}
			
			else if (messageTextFromTelegram.equals(BotLabels.LIST_PROJECTS.getLabel())) {
				// Obtener la lista de proyectos
				List<Proyecto> proyectos = ProyectoService.findAll();
				
				// Construir el teclado con botones
				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboard = new ArrayList<>();
				
				// Botón para volver al menú principal
				KeyboardRow mainMenuRow = new KeyboardRow();
				mainMenuRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
				keyboard.add(mainMenuRow);
				
				// Crear un botón para cada proyecto
				for (Proyecto proyecto : proyectos) {
					KeyboardRow row = new KeyboardRow();
					// Usar un formato especial para identificar que es un botón de proyecto
					row.add("📋 Proyecto: " + proyecto.getId() + " - " + proyecto.getNombre());
					keyboard.add(row);
				}
    
    keyboardMarkup.setKeyboard(keyboard);
    keyboardMarkup.setResizeKeyboard(true);
    
    SendMessage messageToTelegram = new SendMessage();
    messageToTelegram.setChatId(chatId);
    messageToTelegram.setText("Selecciona un proyecto para ver sus detalles:");
    messageToTelegram.setReplyMarkup(keyboardMarkup);
    
    // Activar el estado de visualización de proyectos
    viewingProjectState.put(chatId, true);
    
    try {
        execute(messageToTelegram);
    } catch (TelegramApiException e) {
        logger.error(e.getLocalizedMessage(), e);
    }
}

// Agregar el nuevo else if para manejar la selección de un proyecto
else if (messageTextFromTelegram.startsWith("📋 Proyecto: ") && viewingProjectState.getOrDefault(chatId, false)) {
    // Extraer el ID del proyecto del mensaje
    String projectInfo = messageTextFromTelegram.substring("📋 Proyecto: ".length());
    Long projectId = Long.parseLong(projectInfo.split(" - ")[0]);
    
    // Obtener el proyecto
    ResponseEntity<Proyecto> response = ProyectoService.obtenerProyectoPorId(projectId);
    
    if (response.getStatusCode() == HttpStatus.OK) {
        Proyecto proyecto = response.getBody();
        
        // Construir el mensaje con la información detallada
        StringBuilder infoMessage = new StringBuilder();
        infoMessage.append("📋 *Detalles del Proyecto*\n\n");
        infoMessage.append("🆔 *ID:* ").append(proyecto.getId()).append("\n");
        infoMessage.append("📝 *Nombre:* ").append(proyecto.getNombre()).append("\n");
        infoMessage.append("📊 *Estado:* ").append(proyecto.getEstatus()).append("\n");
        
        // Formatear las fechas si existen
        if (proyecto.getFechaInicio() != null) {
            infoMessage.append("📅 *Fecha Inicio:* ")
                      .append(new SimpleDateFormat("dd/MM/yyyy").format(proyecto.getFechaInicio()))
                      .append("\n");
        }
        
        if (proyecto.getFechaFin() != null) {
            infoMessage.append("🏁 *Fecha Fin:* ")
                      .append(new SimpleDateFormat("dd/MM/yyyy").format(proyecto.getFechaFin()))
                      .append("\n");
        }
        
        // Agregar información sobre las tareas si existen
        if (proyecto.getTareas() != null && !proyecto.getTareas().isEmpty()) {
            infoMessage.append("\n📑 *Tareas asociadas:* ").append(proyecto.getTareas().size());
        }
        
        SendMessage messageToTelegram = new SendMessage();
        messageToTelegram.setChatId(chatId);
        messageToTelegram.setText(infoMessage.toString());
        messageToTelegram.setParseMode("Markdown"); // Habilitar formato Markdown
        
        // Crear teclado con opción para volver
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row1 = new KeyboardRow();
        row1.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
        keyboard.add(row1);
        
        KeyboardRow row2 = new KeyboardRow();
        row2.add(BotLabels.LIST_PROJECTS.getLabel());
        keyboard.add(row2);
        
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        messageToTelegram.setReplyMarkup(keyboardMarkup);
        
        try {
            execute(messageToTelegram);
            // Desactivar el estado de visualización después de mostrar los detalles
            viewingProjectState.remove(chatId);
        } catch (TelegramApiException e) {
            logger.error("Error al enviar mensaje", e);
        }
    } else {
        SendMessage errorMessage = new SendMessage();
        errorMessage.setChatId(chatId);
        errorMessage.setText("❌ No se pudo encontrar el proyecto seleccionado.");
        try {
            execute(errorMessage);
        } catch (TelegramApiException e) {
            logger.error("Error al enviar mensaje de error", e);
        }
    }
	
			} else if (messageTextFromTelegram.indexOf(BotLabels.DONE.getLabel()) != -1) {

				String done = messageTextFromTelegram.substring(0,
						messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
				Integer id = Integer.valueOf(done);

				try {

					ToDoItem item = getToDoItemById(id).getBody();
					item.setDone(true);
					updateToDoItem(item, id);
					BotHelper.sendMessageToTelegram(chatId, BotMessages.ITEM_DONE.getMessage(), this);

				} catch (Exception e) {
					logger.error(e.getLocalizedMessage(), e);
				}

			} else if (messageTextFromTelegram.indexOf(BotLabels.UNDO.getLabel()) != -1) {

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




	private void handleIncomingMessage(long chatId, String messageTextFromTelegram) {
		// Si el usuario está seleccionando un proyecto
		if (messageTextFromTelegram.startsWith("📋 Proyecto:")) {
			handleProjectSelection(chatId, messageTextFromTelegram); // Manejar la selección del proyecto
			return; // Salir del método después de manejar la selección
		}
	
		// Si el usuario está intentando agregar una tarea
		if (creatingTaskState.getOrDefault(chatId, false)) {
			Long projectId = selectedProjectMap.get(chatId);
	
			// Verificar que el proyecto haya sido seleccionado
			if (projectId == null) {
				BotHelper.sendMessageToTelegram(chatId, "Error: No se ha seleccionado un proyecto para esta tarea.", this);
				return;
			}
	
			// Crear la tarea, asignarle una descripción y establecer el estado predeterminado "In Progress"
			Tarea newTask = new Tarea();
			newTask.setDescripcion(messageTextFromTelegram);
			newTask.setEstatus("In Progress"); // Establecer estado por defecto
	
			// Obtener el proyecto para establecerlo en la tarea
			ResponseEntity<Proyecto> responseEntity = ProyectoService.obtenerProyectoPorId(projectId);
	
			// Verificar si la respuesta fue exitosa
			if (responseEntity.getStatusCode() != HttpStatus.OK) {
				BotHelper.sendMessageToTelegram(chatId, "Error: El proyecto seleccionado no existe.", this);
				return;
			}
	
			Proyecto proyecto = responseEntity.getBody(); // Aquí es donde se define la variable proyecto
	
			// Asegúrate de que el proyecto no sea nulo
			if (proyecto == null) {
				BotHelper.sendMessageToTelegram(chatId, "Error: El proyecto seleccionado no existe.", this);
				return;
			}
	
			// Establecer el proyecto en la tarea
			newTask.setProyecto(proyecto); // Aquí se establece el proyecto en la tarea
	
			try {
				// Crear la tarea asociada al proyecto
				Tarea createdTask = tareaService.crearTarea(projectId, newTask);
				// Incluir el ID del proyecto en el mensaje
				BotHelper.sendMessageToTelegram(chatId, "Tarea creada con éxito: " + createdTask.getDescripcion() + 
												  " (Estado: " + createdTask.getEstatus() + 
												  ", ID del Proyecto: " + proyecto.getId() + ")", this);
			} catch (Exception e) {
				BotHelper.sendMessageToTelegram(chatId, "Error al crear la tarea: " + e.getMessage(), this);
			}
	
			// Restablecer el estado de creación de tareas
			creatingTaskState.put(chatId, false);
			selectedProjectMap.remove(chatId); // Limpiar el proyecto seleccionado
		}
	}
	
	
	

	private void startProjectUpdate(long chatId) {
		List<Proyecto> proyectos = ProyectoService.findAll();
		
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Selecciona el proyecto que deseas modificar:");
		
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		
		// Agregar botón para cancelar
		KeyboardRow cancelRow = new KeyboardRow();
		cancelRow.add("Cancelar");
		keyboard.add(cancelRow);
		
		// Crear botones para cada proyecto
		for (Proyecto proyecto : proyectos) {
			KeyboardRow row = new KeyboardRow();
			row.add(proyecto.getId() + " - " + proyecto.getNombre());
			keyboard.add(row);
		}
		
		keyboardMarkup.setKeyboard(keyboard);
		keyboardMarkup.setResizeKeyboard(true);
		message.setReplyMarkup(keyboardMarkup);
		
		projectUpdateStates.put(chatId, UpdateProjectState.SELECTING_PROJECT);
		
		try {
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error al enviar mensaje", e);
		}
	}
	
	// Método para manejar la actualización del proyecto
	private void handleProjectUpdate(long chatId, String messageText) {
		if (messageText.equals("Cancelar")) {
			cancelProjectUpdate(chatId);
			return;
		}
	
		UpdateProjectState currentState = projectUpdateStates.get(chatId);
		
		switch (currentState) {
			case SELECTING_PROJECT:
                System.out.println("Hola estoy seleccionando un proyecto");
				handleProjectSelection(chatId, messageText);
                
				break;
			case ENTERING_NAME:
				handleProjectNameUpdate(chatId, messageText);
				break;
			case SELECTING_STATUS:
				handleProjectStatusUpdate(chatId, messageText);
				break;
		}
	}
	
	
	// Método para manejar la selección del proyecto
	private void handleProjectSelection(long chatId, String messageText) {
        System.out.println("Hola estoy recibiendo este proyecto" + messageText);
			Long projectId = Long.parseLong(messageText);
			
			ResponseEntity<Proyecto> response = ProyectoService.obtenerProyectoPorId(projectId);
			if (response.getStatusCode() == HttpStatus.OK) {
				Proyecto proyecto = response.getBody();
				selectedProjects.put(chatId, proyecto);
				
				SendMessage message = new SendMessage();
				message.setChatId(chatId);
				message.setText("Ingresa el nuevo nombre para el proyecto '" + proyecto.getNombre() + "' (o escribe 'mantener' para conservar el nombre actual):");
				
				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboard = new ArrayList<>();
				
				KeyboardRow row = new KeyboardRow();
				row.add("Cancelar");
				row.add("mantener");
				keyboard.add(row);
				
				keyboardMarkup.setKeyboard(keyboard);
				keyboardMarkup.setResizeKeyboard(true);
				message.setReplyMarkup(keyboardMarkup);
				
				projectUpdateStates.put(chatId, UpdateProjectState.ENTERING_NAME);
				
				try {
					execute(message);
				} catch (TelegramApiException e) {
					logger.error("Error al enviar mensaje", e);
				}
			}
	}
	
	// Método para manejar la actualización del nombre
	private void handleProjectNameUpdate(long chatId, String messageText) {
		Proyecto proyecto = selectedProjects.get(chatId);
		
		if (!messageText.equals("mantener")) {
			proyecto.setNombre(messageText);
		}
		
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Selecciona el nuevo estado del proyecto:");
		
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		
		KeyboardRow row1 = new KeyboardRow();
		row1.add("Cancelar");
		keyboard.add(row1);
		
		KeyboardRow row2 = new KeyboardRow();
		row2.add("Active");
		row2.add("Inactive");
		keyboard.add(row2);
		
		keyboardMarkup.setKeyboard(keyboard);
		keyboardMarkup.setResizeKeyboard(true);
		message.setReplyMarkup(keyboardMarkup);
		
		projectUpdateStates.put(chatId, UpdateProjectState.SELECTING_STATUS);
		
		try {
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error al enviar mensaje", e);
		}
	}
	
	// Método para manejar la actualización del estado
	private void handleProjectStatusUpdate(long chatId, String messageText) {
		Proyecto proyecto = selectedProjects.get(chatId);
		
		// Usar switch tradicional en lugar de switch expression
		String status = null;
		switch (messageText) {
			case "Active":
				status = "Active";
				break;
			case "Inactive":
				status = "Inactive";
				break;
		}
		
		if (status != null) {
			proyecto.setEstatus(status);
			ProyectoService.actualizarProyecto(proyecto.getId(), proyecto);
			
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("Proyecto actualizado exitosamente:\n" +
						   "Nombre: " + proyecto.getNombre() + "\n" +
						   "Estado: " + proyecto.getEstatus());
			
			try {
				execute(message);
			} catch (TelegramApiException e) {
				logger.error("Error al enviar mensaje", e);
			}
			
			// Limpiar estados
			projectUpdateStates.remove(chatId);
			selectedProjects.remove(chatId);
		}
	}
	
	// Método para cancelar la actualización
	private void cancelProjectUpdate(long chatId) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Operación cancelada");
		
		try {
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error al enviar mensaje", e);
		}
		
		// Limpiar estados
		projectUpdateStates.remove(chatId);
		selectedProjects.remove(chatId);
	}

	private Long parseProjectId(String messageText) {
		String[] parts = messageText.split(" - ");
		if (parts.length > 0) {
			try {
				// Extraer el ID del proyecto del mensaje
				return Long.parseLong(parts[0].replace("📋 Proyecto: ", "").trim());
			} catch (NumberFormatException e) {
				return null; // Retorna null si no es un ID válido
			}
		}
		return null; // Retorna null si el formato no es correcto
	}

	private void startProjectDeletion(long chatId) {
		List<Proyecto> proyectos = ProyectoService.findAll();
		
		if (proyectos.isEmpty()) {
			SendMessage message = new SendMessage();
			message.setChatId(chatId);
			message.setText("No hay proyectos disponibles para eliminar.");
			try {
				execute(message);
				return;
			} catch (TelegramApiException e) {
				logger.error("Error al enviar mensaje", e);
			}
		}
		
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("Selecciona el proyecto que deseas eliminar:");
		
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		
		// Botón para cancelar
		KeyboardRow cancelRow = new KeyboardRow();
		cancelRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
		keyboard.add(cancelRow);
		
		// Crear botones para cada proyecto
		for (Proyecto proyecto : proyectos) {
			KeyboardRow row = new KeyboardRow();
			row.add("📋 Proyecto: " + proyecto.getId() + " - " + proyecto.getNombre());
			keyboard.add(row);
		}
		
		keyboardMarkup.setKeyboard(keyboard);
		keyboardMarkup.setResizeKeyboard(true);
		message.setReplyMarkup(keyboardMarkup);
		
		deletingProjectState.put(chatId, true);
		
		try {
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error al enviar mensaje", e);
		}
	}
	
	private void handleProjectDeletion(long chatId, String messageText) {
		try {
			// Extraer el ID del proyecto del mensaje
			String projectInfo = messageText.substring("📋 Proyecto: ".length());
			Long projectId = Long.parseLong(projectInfo.split(" - ")[0]);
			
			// Obtener el proyecto para confirmar
			ResponseEntity<Proyecto> response = ProyectoService.obtenerProyectoPorId(projectId);
			
			if (response.getStatusCode() == HttpStatus.OK) {
				Proyecto proyecto = response.getBody();
				
				// Confirmar con el usuario
				SendMessage confirmMessage = new SendMessage();
				confirmMessage.setChatId(chatId);
				confirmMessage.setText("¿Estás seguro de que deseas eliminar el proyecto '" + 
									 proyecto.getNombre() + "'?\n\n" +
									 "Para confirmar, selecciona una opción:");
				
				ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
				List<KeyboardRow> keyboard = new ArrayList<>();
				
				KeyboardRow row = new KeyboardRow();
				row.add("✅ Confirmar eliminación");
				row.add("❌ Cancelar");
				keyboard.add(row);
				
				keyboardMarkup.setKeyboard(keyboard);
				keyboardMarkup.setResizeKeyboard(true);
				confirmMessage.setReplyMarkup(keyboardMarkup);
				
				// Guardar el ID del proyecto para la confirmación
				selectedProjects.put(chatId, proyecto);
				
				execute(confirmMessage);
			}
		} catch (Exception e) {
			logger.error("Error al procesar la eliminación del proyecto", e);
			sendErrorMessage(chatId, "Hubo un error al procesar tu solicitud. Por favor, intenta de nuevo.");
		}
	}

	private void sendErrorMessage(long chatId, String errorMessage) {
		SendMessage message = new SendMessage();
		message.setChatId(chatId);
		message.setText("❌ " + errorMessage);
		
		ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
		List<KeyboardRow> keyboard = new ArrayList<>();
		KeyboardRow row = new KeyboardRow();
		row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
		keyboard.add(row);
		keyboardMarkup.setKeyboard(keyboard);
		message.setReplyMarkup(keyboardMarkup);
		
		try {
			execute(message);
		} catch (TelegramApiException e) {
			logger.error("Error al enviar mensaje de error", e);
		}
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