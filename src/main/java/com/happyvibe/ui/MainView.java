package com.happyvibe.ui;

import com.happyvibe.model.Board;
import com.happyvibe.model.User;
import com.happyvibe.repository.BoardRepository;
import com.happyvibe.service.AppSession;
import com.happyvibe.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import com.happyvibe.model.TaskItem;
import com.happyvibe.model.TaskStatus;
import com.happyvibe.service.TaskService;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

import java.util.List;

@Component
public class MainView {

    private final AuthService authService;
    private final BoardRepository boardRepository;
    private final AppSession session;
    private final TaskService taskService;

    private Stage stage;

    public MainView(AuthService authService,
                    BoardRepository boardRepository,
                    AppSession session,
                    TaskService taskService) {

        this.authService = authService;
        this.boardRepository = boardRepository;
        this.session = session;
        this.taskService = taskService;
    }

    public void start(Stage stage) {
        this.stage = stage;
        stage.setTitle("HappyVibeCoding");
        stage.setMinWidth(750);
        stage.setMinHeight(550);

        showLogin(false, null);
        stage.show();
    }

    // LOGIN AND REGISTER SCREEN

    private void showLogin(boolean registerMode, String errorMessage) {

        Label title = new Label(
                registerMode ? "Create your account" : "Welcome back!"
        );
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label(
                registerMode
                        ? "Join HappyVibeCoding and organize your work."
                        : "Sign in to continue to your workspace."
        );
        subtitle.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setMaxWidth(300);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(300);

        Label errorLabel = new Label(
                errorMessage == null ? "" : errorMessage
        );
        errorLabel.setTextFill(Color.FIREBRICK);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(300);

        Button actionButton = new Button(
                registerMode ? "Create account" : "Sign in"
        );
        actionButton.setMaxWidth(300);
        actionButton.setStyle(
                "-fx-background-color: #5267e8;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-padding: 12px;"
        );

        Button switchButton = new Button(
                registerMode
                        ? "Already have an account? Sign in"
                        : "Don't have an account? Register"
        );
        switchButton.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #5267e8;"
        );

        actionButton.setOnAction(event -> {
            try {
                String username = usernameField.getText();
                String password = passwordField.getText();

                User user;

                if (registerMode) {
                    user = authService.register(username, password);
                } else {
                    user = authService.login(username, password);
                }

                showDashboard();

            } catch (IllegalArgumentException | IllegalStateException ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        switchButton.setOnAction(event ->
                showLogin(!registerMode, null)
        );

        VBox form = new VBox(
                14,
                title,
                subtitle,
                usernameField,
                passwordField,
                errorLabel,
                actionButton,
                switchButton
        );

        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40));
        form.setMaxWidth(400);
        form.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16px;"
        );

        StackPane root = new StackPane(form);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #eef1f8;");

        Scene scene = new Scene(root, 900, 600);

        stage.setScene(scene);
        stage.centerOnScreen();
    }

    // DASHBOARD SCREEN


    private void showDashboard() {

        User user = session.currentUser();

        Label welcomeLabel = new Label(
                "Welcome, " + user.username() + "!"
        );
        welcomeLabel.setStyle(
                "-fx-font-size: 26px; -fx-font-weight: bold;"
        );

        Button logoutButton = new Button("Log out");
        logoutButton.setOnAction(event -> {
            authService.logout();
            showLogin(false, null);
        });

        HBox header = new HBox(
                welcomeLabel,
                new Region(),
                logoutButton
        );

        HBox.setHgrow(header.getChildren().get(1), Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(25));

        Label boardsTitle = new Label("My Boards");
        boardsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ListView<Board> boardList = new ListView<>();

        List<Board> boards = boardRepository.findForUser(user.id());
        boardList.getItems().setAll(boards);
        boardList.setPrefWidth(220);

        Label emptyLabel = new Label(
                "You don't belong to any boards yet."
        );
        boardList.setPlaceholder(emptyLabel);

        VBox kanbanContent = new VBox(15);
        kanbanContent.setPadding(new Insets(20));

        Label instruction = new Label(
                "Select a board to see its tasks."
        );
        instruction.setStyle("-fx-font-size: 16px;");

        kanbanContent.getChildren().add(instruction);

        boardList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldBoard, newBoard) -> {

                    if (newBoard != null) {
                        showKanbanBoard(newBoard, kanbanContent);
                    }
                });

        Button createBoardButton = new Button("+ Create Board");

        createBoardButton.setOnAction(event -> {

            TextInputDialog dialog = new TextInputDialog();

            dialog.setTitle("Create Board");
            dialog.setHeaderText("Create a new Kanban board");
            dialog.setContentText("Board name:");

            dialog.showAndWait().ifPresent(name -> {

                if (!name.isBlank()) {

                    try {
                        long boardId = boardRepository.create(
                                name.trim(),
                                user.id()
                        );

                        boardList.getItems().setAll(
                                boardRepository.findForUser(user.id())
                        );

                        boardList.getItems().stream()
                                .filter(board -> board.id() == boardId)
                                .findFirst()
                                .ifPresent(board ->
                                        boardList.getSelectionModel()
                                                .select(board)
                                );

                    } catch (RuntimeException ex) {
                        new Alert(
                                Alert.AlertType.ERROR,
                                ex.getMessage()
                        ).showAndWait();
                    }
                }
            });
        });

        VBox sidebar = new VBox(
                15,
                boardsTitle,
                createBoardButton,
                boardList
        );

        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(250);

        VBox.setVgrow(boardList, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setLeft(sidebar);
        root.setCenter(kanbanContent);

        root.setStyle("-fx-background-color: #f4f6fb;");

        Scene scene = new Scene(root, 1100, 700);

        stage.setScene(scene);
        stage.centerOnScreen();
    }

    private void showKanbanBoard(Board board, VBox container) {

        container.getChildren().clear();

        Label boardTitle = new Label(board.name());

        boardTitle.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold;"
        );

        Label subtitle = new Label("Manage your tasks and progress.");
        subtitle.setStyle("-fx-text-fill: #64748b;");

        HBox columns = new HBox(15);
        columns.setAlignment(Pos.TOP_CENTER);

        for (TaskStatus status : TaskStatus.values()) {

            VBox column = createKanbanColumn(board.id(), status);

            HBox.setHgrow(column, Priority.ALWAYS);

            columns.getChildren().add(column);
        }

        container.getChildren().addAll(
                boardTitle,
                subtitle,
                columns
        );

        VBox.setVgrow(columns, Priority.ALWAYS);
    }

    private VBox createKanbanColumn(long boardId, TaskStatus status) {

        List<TaskItem> allTasks = taskService.findByBoard(boardId);

        List<TaskItem> filteredTasks = allTasks.stream()
                .filter(task -> task.status() == status)
                .toList();

        Label columnTitle = new Label(
                status.label() + " (" + filteredTasks.size() + ")"
        );

        columnTitle.setStyle(
                "-fx-font-size: 17px; -fx-font-weight: bold;"
        );

        VBox taskContainer = new VBox(10);
        taskContainer.setPadding(new Insets(10));

        for (TaskItem task : filteredTasks) {

            VBox taskCard = new VBox(8);

            Label taskTitle = new Label(task.title());
            taskTitle.setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: bold;"
            );
            taskTitle.setWrapText(true);

            Label course = new Label(task.course());
            course.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #64748b;"
            );

            Label description = new Label(task.description());
            description.setWrapText(true);
            description.setStyle("-fx-font-size: 12px;");

            taskCard.getChildren().addAll(
                    taskTitle,
                    course,
                    description
            );

            taskCard.setPadding(new Insets(12));
            taskCard.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 8px;" +
                            "-fx-border-color: #e2e8f0;" +
                            "-fx-border-radius: 8px;"
            );

            taskContainer.getChildren().add(taskCard);
        }

        if (filteredTasks.isEmpty()) {
            Label empty = new Label("No tasks yet.");
            empty.setStyle("-fx-text-fill: #94a3b8;");
            taskContainer.getChildren().add(empty);
        }

        ScrollPane scrollPane = new ScrollPane(taskContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;"
        );

        VBox column = new VBox(
                12,
                columnTitle,
                scrollPane
        );

        column.setPadding(new Insets(15));
        column.setMinWidth(200);
        column.setStyle(
                "-fx-background-color: #e9edf5;" +
                        "-fx-background-radius: 12px;"
        );

        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return column;
    }
}
