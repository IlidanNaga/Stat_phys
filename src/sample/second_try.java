package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.List;

public class second_try extends Application {

    private int width = 4;

    private Pane root = new Pane();
    private Line wall1 = new Line();
    private Line wall2 = new Line();
    private Line wall3 = new Line();

    private Path arc = new Path();

    private Ball ball1 = new Ball(200, 100, 10, 2, 4, Color.GREEN);
    private Ball ball2 = new Ball(100, 100, 12, 0, -1, Color.YELLOW);

    private Parent createContext() {

        root.setPrefSize(1200, 900);

        // setting 3 normal walls
        wall1.setStartX(100);
        wall1.setStartY(100);
        wall1.setEndX(700);
        wall1.setEndY(100);

        wall2.setStartX(100);
        wall2.setStartY(100);
        wall2.setEndX(100);
        wall2.setEndY(500);

        wall3.setStartX(100);
        wall3.setStartY(500);
        wall3.setEndX(700);
        wall3.setEndY(500);

        wall1.setStrokeWidth(width);
        wall2.setStrokeWidth(width);
        wall3.setStrokeWidth(width);

        wall1.setStroke(Color.BLUE);
        wall2.setStroke(Color.BLUE);
        wall3.setStroke(Color.BLUE);

        root.getChildren().add(wall1);
        root.getChildren().add(wall2);
        root.getChildren().add(wall3);


        //setting arc wall
        MoveTo start = new MoveTo(700, 500);
        ArcTo finish = new ArcTo(100, 200, 0, 700, 100, false, false);

        arc.getElements().add(start);
        arc.getElements().add(finish);
        arc.setStroke(Color.BLUE);
        arc.setStrokeWidth(width);

        root.getChildren().add(arc);

        //setting balls
        root.getChildren().add(ball1);
        root.getChildren().add(ball2);

        AnimationTimer timer = new AnimationTimer() {

            @Override
            public void handle(long now){
                update();
            }

        };

        timer.start();

        return root;
    }

    private List<Ball> all_balls = List.of(ball1, ball2);

    private void update() {

        all_balls.forEach(s -> {

            Bounds boundsInScene = s.localToScene(s.getBoundsInParent());

            double min_x = boundsInScene.getMinX();
            double min_y = boundsInScene.getMinY();
            double max_x = boundsInScene.getMaxX();
            double max_y = boundsInScene.getMaxY();

            double x = (min_x + max_x) / 2;
            double y = (min_y + max_y) / 2;

            if (s.getBoundsInParent().intersects(wall1.getBoundsInParent())) {
                s.Bump_left();
            }

            if (s.getBoundsInParent().intersects(wall2.getBoundsInParent())) {
                s.Bump_top_bot();
            }

            if (s.getBoundsInParent().intersects(wall3.getBoundsInParent())) {
                s.Bump_top_bot();
            }
            s.move();
        });
    }

    private static class Ball extends Circle {
        double speed_x, speed_y;

        Ball(double x, double y, int r, double s_x, double s_y, Color color) {
            super(x, y, r, color);

            this.speed_x = s_x;
            this.speed_y = s_y;

            setTranslateX(x);
            setTranslateY(y);
        }

        void moveX() {
            setTranslateX(getTranslateX() + this.speed_x);
        }

        void moveY() {
            setTranslateY(getTranslateY() + this.speed_y);
        }

        void move() {
            moveX();
            moveY();
        }

        void Bump_left() {
            this.speed_x *= -1;
        }

        void Bump_top_bot() {
            this.speed_y *= -1;
        }

        void Bump_arc() {
            this.speed_x *= -1;
            this.speed_y *= -1;
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        Scene scene = new Scene(createContext(), 1200, 900);

        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
