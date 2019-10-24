package sample;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Menu extends Application {

    private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    private double screenwidth = primaryScreenBounds.getWidth();
    private double screenheight = primaryScreenBounds.getHeight();

    private double width_upscale = screenwidth / 1440;
    private double height_upscale = screenheight / 900;

    private Pane root = new Pane();

    private Button button_launch = new Button("Launch");

    private Parent createContent() {

        button_launch.setTranslateX(600 * width_upscale);
        button_launch.setTranslateY(400 * height_upscale);
        button_launch.setMinWidth(300 * width_upscale);
        button_launch.setMinHeight(200 * height_upscale);

        root.getChildren().add(button_launch);

        return root;
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(createContent());

        button_launch.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                Application.launch(Experiment.class, arguments);
                stage.close();
            }
        });

        Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

        stage.setX(primaryScreenBounds.getMinX());
        stage.setY(primaryScreenBounds.getMinY());
        stage.setWidth(primaryScreenBounds.getWidth());
        stage.setHeight(primaryScreenBounds.getHeight());

        stage.setResizable(false);

        stage.setScene(scene);
        stage.show();
    }
    private String[] arguments;
    public static void main(String[] args) {
        launch(args);
    }
}
