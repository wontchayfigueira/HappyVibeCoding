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

import java.util.List;

@Component
public class MainView {

    private final AuthService authService;
    private final BoardRepository boardRepository;
    private final AppSession session;

    private Stage stage;

    public MainView(AuthService authService,
                    BoardRepository boardRepository,
                    AppSession session) {
        this.authService = authService;
        this.boardRepository = boardRepository;
        this.session = session;
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
                "-fx-font-size: 26px;" +
                        "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "Here is an overview of your workspaces."
        );
        subtitle.setStyle("-fx-text-fill: #64748b;");

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
        boardsTitle.setStyle(
                "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        ListView<Board> boardList = new ListView<>();

        List<Board> boards = boardRepository.findForUser(user.id());

        boardList.getItems().setAll(boards);
        boardList.setPrefHeight(350);

        Label emptyLabel = new Label(
                "You don't belong to any boards yet."
        );
        emptyLabel.setStyle("-fx-text-fill: #64748b;");

        boardList.setPlaceholder(emptyLabel);

        VBox content = new VBox(
                15,
                boardsTitle,
                boardList
        );
        content.setPadding(new Insets(25));

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(content);
        root.setStyle("-fx-background-color: #f4f6fb;");

        Scene scene = new Scene(root, 900, 600);

        stage.setScene(scene);
        stage.centerOnScreen();
    }
}
