package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.util.List;

public class subtraction_attempt extends Application {

    private Pane root = new Pane();

    private Line line1 = new Line(100, 100, 100, 500); // left
    private Line line2 = new Line(100, 100, 700, 100); // top
    private Line line3 = new Line(100, 500, 700, 500); // bot

    MoveTo start = new MoveTo(700, 500);
    ArcTo finish = new ArcTo(100, 200, 0, 700, 100, false, false);
    Path arc = new Path(start, finish);


    MoveTo start2 = new MoveTo(700, 499);
    ArcTo finish2 = new ArcTo(99, 198, 0, 700, 101, false, false);
    Path arc2 = new Path(start2, finish2);

    private Shape right_zone = Shape.subtract(arc, arc2);

    private Ball ball1 = new Ball(340, 100, 10, 2, 0.1, Color.GREEN);
    private Ball ball2 = new Ball( 200, 150, 10, -2, 4, Color.YELLOW);


    private Parent createContent() {

        root.setPrefSize(1200, 900);

        right_zone.setFill(Color.BLUE);
        line1.setStroke(Color.BLUE);
        line2.setStroke(Color.BLUE);
        line3.setStroke(Color.BLUE);

        root.getChildren().add(line1);
        root.getChildren().add(line2);
        root.getChildren().add(line3);
        root.getChildren().add(right_zone);

        root.getChildren().add(ball1);
        root.getChildren().add(ball2);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };

        timer.start();

        return root;
    }

    private List<Ball> all_balls = List.of(ball1, ball2);

    private void update() {

        all_balls.forEach(s -> {

            if (s.getBoundsInParent().intersects(line1.getBoundsInParent())) {
                s.Bump_left();
            }

            if (s.getBoundsInParent().intersects(line2.getBoundsInParent())) {
                s.Bump_top_bot();
            }

            if (s.getBoundsInParent().intersects(line3.getBoundsInParent())) {
                s.Bump_top_bot();
            }

            if (s.getBoundsInParent().intersects(right_zone.getBoundsInParent())) {
                s.Bump_arc();
            }

            s.move();
        });
    }

    private static class Ball extends Circle {
        double speed_x, speed_y;

        Ball(int x, int y, int r, double s_x, double s_y, Color color) {
            super(x, y, r, color);


            setTranslateX(x);
            setTranslateY(y);

            this.speed_x = s_x;
            this.speed_y = s_y;
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
            this.speed_x *= -1.1;
            this.speed_y *= -1;
        }
    }

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new Scene(createContent(), 1200, 900);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }


}