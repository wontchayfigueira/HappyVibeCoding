package com.happyvibe;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import com.happyvibe.ui.MainView;

public class JavaFxApplication extends Application {
    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        context = new SpringApplicationBuilder(HappyVibeApplication.class)
                .web(WebApplicationType.NONE)
                .run(getParameters().getRaw().toArray(String[]::new));
    }

    @Override
    public void start(Stage stage) {
        MainView mainView = context.getBean(MainView.class);
        mainView.start(stage);
    }

    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
    }
}
