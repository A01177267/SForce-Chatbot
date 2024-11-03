package com.springboot.MyTodoList.controller;

import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.springboot.MyTodoList.model.ToDoItem;
import com.springboot.MyTodoList.model.Proyecto;
import com.springboot.MyTodoList.model.Tarea;
import com.springboot.MyTodoList.service.ToDoItemService;
import com.springboot.MyTodoList.service.ProyectoService;
import com.springboot.MyTodoList.service.TareaService;
import com.springboot.MyTodoList.util.BotCommands;
import com.springboot.MyTodoList.util.BotHelper;
import com.springboot.MyTodoList.util.BotLabels;
import com.springboot.MyTodoList.util.BotMessages;

public class ToDoItemBotController extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(ToDoItemBotController.class);
    private ToDoItemService toDoItemService;
    private ProyectoService proyectoService;
    private TareaService tareaService;
    private String botName;

    private Map<Long, Boolean> creatingProjectState = new HashMap<>();
    private Map<Long, UpdateProjectState> projectUpdateStates = new HashMap<>();
    private Map<Long, Proyecto> selectedProjects = new HashMap<>();
    private Map<Long, Boolean> viewingProjectState = new HashMap<>();
    private Map<Long, Boolean> deletingProjectState = new HashMap<>();
    private Map<Long, Boolean> creatingTaskState = new HashMap<>(); 

    private enum UpdateProjectState {
        SELECTING_PROJECT,
        ENTERING_NAME,
        SELECTING_STATUS
    }

    public ToDoItemBotController(String botToken, String botName, ToDoItemService toDoItemService, ProyectoService proyectoService, TareaService tareaService) {
        super(botToken);
        logger.info("Bot Token: " + botToken);
        logger.info("Bot name: " + botName);
        this.toDoItemService = toDoItemService;
        this.proyectoService = proyectoService;
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
            } else if (deletingProjectState.getOrDefault(chatId, false) && messageTextFromTelegram.startsWith("📋 Proyecto: ")) {
                handleProjectDeletion(chatId, messageTextFromTelegram);
            }

            if (creatingProjectState.getOrDefault(chatId, false)) {
                Proyecto nuevoProyecto = new Proyecto();
                nuevoProyecto.setNombre(messageTextFromTelegram);
                proyectoService.crearProyecto(nuevoProyecto);
                BotHelper.sendMessageToTelegram(chatId, BotMessages.PROJECT_CREATED.getMessage(), this);
                creatingProjectState.put(chatId, false);
                return;
            }

            if (creatingTaskState.getOrDefault(chatId, false)) {
                Tarea nuevaTarea = new Tarea();
                nuevaTarea.setDescripcion(messageTextFromTelegram);
                nuevaTarea.setProyecto(selectedProjects.get(chatId));
                tareaService.crearTarea(nuevaTarea.getProyecto().getId(), nuevaTarea); 
                BotHelper.sendMessageToTelegram(chatId, BotMessages.TASK_CREATED.getMessage(), this);
                creatingTaskState.put(chatId, false);
                return;
            }

            if (messageTextFromTelegram.equals(BotCommands.START_COMMAND.getCommand())
                    || messageTextFromTelegram.equals(BotLabels.SHOW_MAIN_SCREEN.getLabel())) {

                SendMessage messageToTelegram = new SendMessage();
                messageToTelegram.setChatId(chatId);
                messageToTelegram.setText(BotMessages.HELLO_MYTODO_BOT.getMessage());

                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                List<KeyboardRow> keyboard = new ArrayList<>();

                KeyboardRow row = new KeyboardRow();
                row.add(BotLabels.LIST_ALL_ITEMS.getLabel());
                row.add(BotLabels.ADD_NEW_ITEM.getLabel());
                keyboard.add(row);

                row = new KeyboardRow();
                row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
                row.add(BotLabels.HIDE_MAIN_SCREEN.getLabel());
                keyboard.add(row);

                row = new KeyboardRow();
                row.add(BotLabels.LIST_PROJECTS.getLabel());
                row.add(BotLabels.ADD_PROJECT.getLabel());
                keyboard.add(row);

                row = new KeyboardRow();
                row.add(BotLabels.LIST_TASKS.getLabel());
                row.add(BotLabels.ADD_TASK.getLabel());
                keyboard.add(row);

                keyboardMarkup.setKeyboard(keyboard);
                messageToTelegram.setReplyMarkup(keyboardMarkup);

                try {
                    execute(messageToTelegram);
                } catch (TelegramApiException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }

            } else if (messageTextFromTelegram.equals(BotLabels.ADD_PROJECT.getLabel())) {
                creatingProjectState.put(chatId, true);
                BotHelper.sendMessageToTelegram(chatId, "Por favor, envíame el nombre del nuevo proyecto.", this);

            } else if (messageTextFromTelegram.equals(BotLabels.ADD_TASK.getLabel())) {
                creatingTaskState.put(chatId, true);
                BotHelper.sendMessageToTelegram(chatId, "Por favor, envíame la descripción de la nueva tarea.", this);
                
            } else if (messageTextFromTelegram.equals(BotLabels.LIST_TASKS.getLabel())) {
                List<Tarea> tareas = tareaService.findAll(); 
                SendMessage messageToTelegram = new SendMessage();
                messageToTelegram.setChatId(chatId);
                StringBuilder tasksMessage = new StringBuilder("📋 Lista de Tareas:\n\n");
                for (Tarea tarea : tareas) {
                    tasksMessage.append(tarea.getId()).append(": ").append(tarea.getDescripcion()).append("\n");
                }
                messageToTelegram.setText(tasksMessage.toString());
                try {
                    execute(messageToTelegram);
                } catch (TelegramApiException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            } else if (messageTextFromTelegram.equals(BotLabels.UPDATE_PROJECT.getLabel())) {
                startProjectUpdate(chatId);
            } else if (messageTextFromTelegram.equals(BotLabels.DELETE_PROJECT.getLabel())) {
                startProjectDeletion(chatId);
            } else if (messageTextFromTelegram.equals("✅ Confirmar eliminación") && selectedProjects.containsKey(chatId)) {
                Proyecto proyectoAEliminar = selectedProjects.get(chatId);
                try {
                    proyectoService.eliminarProyecto(proyectoAEliminar.getId());
                    SendMessage successMessage = new SendMessage();
                    successMessage.setChatId(chatId);
                    successMessage.setText("✅ El proyecto '" + proyectoAEliminar.getNombre() + "' ha sido eliminado exitosamente.");
                    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                    List<KeyboardRow> keyboard = new ArrayList<>();
                    KeyboardRow row = new KeyboardRow();
                    row.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
                    keyboard.add(row);
                    keyboardMarkup.setKeyboard(keyboard);
                    successMessage.setReplyMarkup(keyboardMarkup);
                    execute(successMessage);
                    deletingProjectState.remove(chatId);
                    selectedProjects.remove(chatId);
                } catch (Exception e) {
                    logger.error("Error al eliminar el proyecto", e);
                    sendErrorMessage(chatId, "Hubo un error al eliminar el proyecto. Por favor, intenta de nuevo.");
                }
            } else if (messageTextFromTelegram.equals("❌ Cancelar") && selectedProjects.containsKey(chatId)) {
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
                    deletingProjectState.remove(chatId);
                    selectedProjects.remove(chatId);
                } catch (TelegramApiException e) {
                    logger.error("Error al enviar mensaje de cancelación", e);
                }
            } else if (messageTextFromTelegram.equals(BotLabels.LIST_PROJECTS.getLabel())) {
                List<Proyecto> proyectos = proyectoService.findAll();
                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                List<KeyboardRow> keyboard = new ArrayList<>();
                KeyboardRow mainMenuRow = new KeyboardRow();
                mainMenuRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
                keyboard.add(mainMenuRow);
                for (Proyecto proyecto : proyectos) {
                    KeyboardRow row = new KeyboardRow();
                    row.add("📋 Proyecto: " + proyecto.getId() + " - " + proyecto.getNombre());
                    keyboard.add(row);
                }
                keyboardMarkup.setKeyboard(keyboard);
                keyboardMarkup.setResizeKeyboard(true);
                SendMessage messageToTelegram = new SendMessage();
                messageToTelegram.setChatId(chatId);
                messageToTelegram.setText("Selecciona un proyecto para ver sus detalles:");
                messageToTelegram.setReplyMarkup(keyboardMarkup);
                viewingProjectState.put(chatId, true);
                try {
                    execute(messageToTelegram);
                } catch (TelegramApiException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            } else if (messageTextFromTelegram.startsWith("📋 Proyecto: ") && viewingProjectState.getOrDefault(chatId, false)) {
                String projectInfo = messageTextFromTelegram.substring("📋 Proyecto: ".length());
                Long projectId = Long.parseLong(projectInfo.split(" - ")[0]);
                ResponseEntity<Proyecto> response = proyectoService.obtenerProyectoPorId(projectId);
                if (response.getStatusCode() == HttpStatus.OK) {
                    Proyecto proyecto = response.getBody();
                    StringBuilder infoMessage = new StringBuilder();
                    infoMessage.append("📋 *Detalles del Proyecto*\n\n");
                    infoMessage.append("🆔 *ID:* ").append(proyecto.getId()).append("\n");
                    infoMessage.append("📝 *Nombre:* ").append(proyecto.getNombre()).append("\n");
                    infoMessage.append("📊 *Estado:* ").append(proyecto.getEstatus()).append("\n");
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
                    if (proyecto.getTareas() != null && !proyecto.getTareas().isEmpty()) {
                        infoMessage.append("\n📑 *Tareas asociadas:* ").append(proyecto.getTareas().size());
                    }
                    SendMessage messageToTelegram = new SendMessage();
                    messageToTelegram.setChatId(chatId);
                    messageToTelegram.setText(infoMessage.toString());
                    messageToTelegram.setParseMode("Markdown");
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
                String done = messageTextFromTelegram.substring(0, messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
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
                String undo = messageTextFromTelegram.substring(0, messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
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
                String delete = messageTextFromTelegram.substring(0, messageTextFromTelegram.indexOf(BotLabels.DASH.getLabel()));
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
                KeyboardRow mainScreenRowTop = new KeyboardRow();
                mainScreenRowTop.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
                keyboard.add(mainScreenRowTop);
                KeyboardRow firstRow = new KeyboardRow();
                firstRow.add(BotLabels.ADD_NEW_ITEM.getLabel());
                keyboard.add(firstRow);
                KeyboardRow myTodoListTitleRow = new KeyboardRow();
                myTodoListTitleRow.add(BotLabels.MY_TODO_LIST.getLabel());
                keyboard.add(myTodoListTitleRow);
                List<ToDoItem> activeItems = allItems.stream().filter(item -> !item.isDone()).collect(Collectors.toList());
                for (ToDoItem item : activeItems) {
                    KeyboardRow currentRow = new KeyboardRow();
                    currentRow.add(item.getDescription());
                    currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DONE.getLabel());
                    keyboard.add(currentRow);
                }
                List<ToDoItem> doneItems = allItems.stream().filter(ToDoItem::isDone).collect(Collectors.toList());
                for (ToDoItem item : doneItems) {
                    KeyboardRow currentRow = new KeyboardRow();
                    currentRow.add(item.getDescription());
                    currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.UNDO.getLabel());
                    currentRow.add(item.getID() + BotLabels.DASH.getLabel() + BotLabels.DELETE.getLabel());
                    keyboard.add(currentRow);
                }
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
                    ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                    messageToTelegram.setReplyMarkup(keyboardMarkup);
                    execute(messageToTelegram);
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            } else {
                try {
                    ToDoItem newItem = new ToDoItem();
                    newItem.setDescription(messageTextFromTelegram);
                    newItem.setCreation_ts(OffsetDateTime.now());
                    newItem.setDone(false);
                    ResponseEntity entity = addToDoItem(newItem);
                    SendMessage messageToTelegram = new SendMessage();
                    messageToTelegram.setChatId(chatId);
                    messageToTelegram.setText(BotMessages.NEW_ITEM_ADDED.getMessage());
                    execute(messageToTelegram);
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private void startProjectUpdate(long chatId) {
        List<Proyecto> proyectos = proyectoService.findAll();
        
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Selecciona el proyecto que deseas modificar:");
        
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow cancelRow = new KeyboardRow();
        cancelRow.add("Cancelar");
        keyboard.add(cancelRow);
        
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
    
    private void handleProjectUpdate(long chatId, String messageText) {
        if (messageText.equals("Cancelar")) {
            cancelProjectUpdate(chatId);
            return;
        }
    
        UpdateProjectState currentState = projectUpdateStates.get(chatId);
        
        switch (currentState) {
            case SELECTING_PROJECT:
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
    
    private void handleProjectSelection(long chatId, String messageText) {
        if (messageText.startsWith("📝 ")) {
            String[] parts = messageText.substring(3).split(" - ");
            Long projectId = Long.parseLong(parts[0]);
            
            ResponseEntity<Proyecto> response = proyectoService.obtenerProyectoPorId(projectId);
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
    }
    
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
        row2.add("ACTIVO");
        row2.add("INACTIVO");
        keyboard.add(row2);
        
        KeyboardRow row3 = new KeyboardRow();
        row3.add("COMPLETADO");
        row3.add("PAUSADO");
        keyboard.add(row3);
        
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
    
    private void handleProjectStatusUpdate(long chatId, String messageText) {
        Proyecto proyecto = selectedProjects.get(chatId);
        
        String status = null;
        switch (messageText) {
            case "ACTIVO":
                status = "ACTIVO";
                break;
            case "INACTIVO":
                status = "INACTIVO";
                break;
            case "COMPLETADO":
                status = "COMPLETADO";
                break;
            case "PAUSADO":
                status = "PAUSADO";
                break;
        }
        
        if (status != null) {
            proyecto.setEstatus(status);
            proyectoService.actualizarProyecto(proyecto.getId(), proyecto);
            
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
            
            projectUpdateStates.remove(chatId);
            selectedProjects.remove(chatId);
        }
    }
    
    private void cancelProjectUpdate(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Operación cancelada");
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Error al enviar mensaje", e);
        }
        
        projectUpdateStates.remove(chatId);
        selectedProjects.remove(chatId);
    }

    private void startProjectDeletion(long chatId) {
        List<Proyecto> proyectos = proyectoService.findAll();
        
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
        
        KeyboardRow cancelRow = new KeyboardRow();
        cancelRow.add(BotLabels.SHOW_MAIN_SCREEN.getLabel());
        keyboard.add(cancelRow);
        
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
            String projectInfo = messageText.substring("📋 Proyecto: ".length());
            Long projectId = Long.parseLong(projectInfo.split(" - ")[0]);
            ResponseEntity<Proyecto> response = proyectoService.obtenerProyectoPorId(projectId);
            if (response.getStatusCode() == HttpStatus.OK) {
                Proyecto proyecto = response.getBody();
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
        return ResponseEntity.ok().headers(responseHeaders).build();
    }

    // UPDATE /todolist/{id}
    public ResponseEntity updateToDoItem(@RequestBody ToDoItem toDoItem, @PathVariable int id) {
        try {
            ToDoItem toDoItem1 = toDoItemService.updateToDoItem(id, toDoItem);
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
