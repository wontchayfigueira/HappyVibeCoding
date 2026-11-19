package com.happyvibe.ui;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

@Component
public class MainView {

    public void start(Stage stage) {

        Label welcomeLabel = new Label("Welcome to HappyVibeCoding!");

        StackPane root = new StackPane(welcomeLabel);

        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("HappyVibeCoding - Kanban");
        stage.setScene(scene);
        stage.show();
    }
}
