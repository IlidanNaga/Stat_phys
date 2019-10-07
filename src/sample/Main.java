package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

    private Pane root = new Pane();

    private double l_lim = 100;
    private double r_lim = 700;
    private double t_lim = 100;
    private double b_lim = 500;
    private double rad_x = 100;
    private int ball_rad = 10;

    private double wall_speed = 1;

    private double cent_y = (b_lim + t_lim) / 2;
    private double rad_y = (b_lim - t_lim) / 2;

    // startX, startY, endX, endY
    private Line line1 = new Line(l_lim, t_lim, l_lim, b_lim); // left
    private Line line2 = new Line(l_lim, t_lim, r_lim, t_lim); // top
    private Line line3 = new Line(l_lim, b_lim, r_lim, b_lim); // bot

    private MoveTo start = new MoveTo();
    private ArcTo finish = new ArcTo();
    private Path arc = new Path();

    // somewhy it creates with coordinates like 600x425
    // so our area is within 50, 50 - 350, 250
    private Ball ball1 = new Ball(150, 100, ball_rad, 3, 0, Color.GREEN, 0);

    private Ball ball2 = new Ball( 200, 150, ball_rad, -2, 4, Color.GREEN, 1);

    private Ball ball3 = new Ball(60, 150, ball_rad, 0.5, 0, Color.RED, 2);

    private Line line_link = new Line();



    private Parent createContent() {
        root.setPrefSize(1200, 900);

        line1.setStroke(Color.BLUE);
        line2.setStroke(Color.BLUE);
        line3.setStroke(Color.BLUE);
        arc.setStroke(Color.BLUE);

        root.getChildren().add(line1);
        root.getChildren().add(line2);
        root.getChildren().add(line3);

        if (rad_x >= 0) {
            start.setX(r_lim);
            start.setY(b_lim);

            finish.setRadiusX(rad_x);
            finish.setRadiusY(rad_y);
            finish.setXAxisRotation(0);
            finish.setX(r_lim);
            finish.setY(t_lim);
            finish.setLargeArcFlag(false);
            finish.setSweepFlag(false);
        } else {
            start.setX(r_lim);
            start.setY(t_lim);

            finish.setRadiusX(rad_x);
            finish.setRadiusY(rad_y);
            finish.setXAxisRotation(0);
            finish.setX(r_lim);
            finish.setY(b_lim);
            finish.setLargeArcFlag(false);
            finish.setSweepFlag(false);
        }
        arc.getElements().add(start);
        arc.getElements().add(finish);

        root.getChildren().add(arc);

        root.getChildren().add(ball1);
        root.getChildren().add(ball2);

        root.getChildren().add(ball3);

        if (rad_x >= 0) {
            root.getChildren().add(new Line(r_lim, t_lim, r_lim, b_lim));
            root.getChildren().add(new Line(r_lim + rad_x, t_lim, r_lim + rad_x, b_lim));

        }
        else {
            root.getChildren().add(new Line(r_lim + rad_x - ball_rad, t_lim, r_lim + rad_x - ball_rad, b_lim));
        }
        line_link.setStroke(Color.GREEN);

        root.getChildren().add(line_link);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };

        timer.start();

        return root;
    }

    private List<Ball> all_balls = List.of(ball1, ball2, ball3);

    private ArrayList distantion = new ArrayList();

    private void update() {

        double[] x_coords = new double[2];
        double[] y_coords = new double[2];

        //moving the walls
        // setting bot limit of x to 50, top limit to 150

        if (l_lim < 50 || l_lim > 150) {
            wall_speed *= -1;
        }
        l_lim += wall_speed;

        line1.setStartX(l_lim);
        line1.setEndX(l_lim);

        line2.setStartX(l_lim);
        line3.setStartX(l_lim);



        //collision of ball:
        all_balls.forEach(s -> {

            Bounds boundsInScene = s.localToScene(s.getBoundsInLocal());

            double min_x = boundsInScene.getMinX();
            double min_y = boundsInScene.getMinY();
            double max_x = boundsInScene.getMaxX();
            double max_y = boundsInScene.getMaxY();

            double x = (min_x + max_x) / 2;
            double y = (min_y + max_y) / 2;

            if (s.getBoundsInParent().intersects(line1.getBoundsInParent())) {
                s.Bump_left(wall_speed);
            }

            if (s.getBoundsInParent().intersects(line2.getBoundsInParent())) {
                s.Bump_top_bot();
            }

            if (s.getBoundsInParent().intersects(line3.getBoundsInParent())) {
                s.Bump_top_bot();
            }


            // it must work another way, but for now it'd be fine
            if (rad_x >= 0) {
                // external shape
                if (max_x >= r_lim) {
                    if (ellipsis(x, y) && s.timer < 1) {
                        s.timer = 2;
                        s.bumpArc(x, y, r_lim, cent_y, rad_x, rad_y); // в тесте соотношение координат работает как-то так
                    }

                }
            } else {
                // internal shape
                if (max_x >= r_lim + rad_x - ball_rad) {
                    if (!ellipsis(x, y) && s.timer < 1) {
                        s.timer = 2;
                        s.inner_bumpArc(x, y, r_lim, cent_y, rad_x, rad_y);
                    }
                }
            }
            s.timer -= 1;

            if (y < t_lim + ball_rad && s.speed_y < 0) {
                s.speed_y *= -1;
            }

            if (y > b_lim - ball_rad && s.speed_y > 0) {
                s.speed_y *= -1;
            }

            s.move();

            Bounds bound2 = s.localToScene(s.getBoundsInLocal());

            if (s.number < 2) {
                x_coords[s.number] = (bound2.getMaxX() + bound2.getMinX()) / 2;
                y_coords[s.number] = (bound2.getMaxY() + bound2.getMinY()) / 2;
            }

        });

        line_link.setStartX(x_coords[0]);
        line_link.setEndX(x_coords[1]);
        line_link.setStartY(y_coords[0]);
        line_link.setEndY(y_coords[1]);

        double dist = Math.sqrt(Math.pow(x_coords[1] - x_coords[0], 2) + Math.pow(y_coords[1] - y_coords[0], 2));
        distantion.add(dist);
    }

    private boolean ellipsis(double x, double y) {
        double value = Math.pow((x - r_lim), 2)/Math.pow(rad_x - ball_rad, 2) + Math.pow((y - cent_y), 2)/Math.pow(rad_y - ball_rad, 2);
        return value >= 1;
    }

    private static class Ball extends Circle {
        double speed_x, speed_y;
        int timer;
        int number;
        int radius;

        Ball(int x, int y, int r, double s_x, double s_y, Color color, int numb) {
            super(x, y, r, color);


            setTranslateX(x);
            setTranslateY(y);

            this.timer = 0;
            this.speed_x = s_x;
            this.speed_y = s_y;
            this.number = numb;
            this.radius = r;

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

        void Bump_left(double wall_speed) {
            /*
            System.out.print(speed_x);
            System.out.print(' ');
            System.out.print(wall_speed);
            System.out.print('\n');

             */
            if (this.speed_x > 0) {
                this.speed_x += wall_speed;
            } else {
                this.speed_x = -1 * this.speed_x + wall_speed;
            }

        }

        void Bump_top_bot() {
            this.speed_y *= -1;
        }

        double absSpeed() {
            return Math.sqrt(Math.pow(this.speed_x, 2) + Math.pow(this.speed_y, 2));
        }

        void bumpArc(double x_pos, double y_pos, double r_lim, double cent_y, double rad_x, double rad_y) {
            // inputs are absolute coordinates of ball's center, we need to get:
            // coordinates of point where ball bumps
            double local_x_pos = x_pos - r_lim;
            double local_y_pos = y_pos - cent_y;

            // trying to approximately find a position of collision
            double x_c_el = r_lim;
            double y_c_el = cent_y;
            double a = rad_x;
            double b = rad_y;
            double xe, ye, t, dt;
            t = -1 * Math.PI / 2;
            dt = 0.00001;

            double tmp, saved = 0;
            double x_arc_pos = 0, y_arc_pos = 0, count = 0;

            while (t < Math.PI / 2) {

                xe = x_c_el + a * Math.cos(t);
                ye = y_c_el + b * Math.sin(t);

                tmp = Math.pow(xe - x_pos, 2) + Math.pow(ye - y_pos, 2) - this.radius * this.radius;

                if (tmp * saved < 0) {
                    x_arc_pos += xe;
                    y_arc_pos += ye;
                    count++;

                }

                saved = tmp;

                t += dt;
            }

            if (count != 0) {
                x_arc_pos /= count;
                y_arc_pos /= count;
            } else {
                x_arc_pos = x_pos;
                y_arc_pos = y_pos;
            }
            // x_arc_pos and y_arc_pos - coordinates of required dot

            double[] normal_vect = tangent(x_arc_pos, y_arc_pos, r_lim, cent_y, rad_x, rad_y);
            double dir = direction(normal_vect[0], normal_vect[1]);

            // for ball now
            double abs_sp = this.absSpeed();
            double part_speed_x = this.speed_x / abs_sp;
            double part_speed_y = this.speed_y / abs_sp;

            double speed_angle;
            if (part_speed_y >= 0) {
                speed_angle = Math.toDegrees(Math.acos(part_speed_x));
            } else {
                speed_angle = 360 - Math.toDegrees(Math.acos(part_speed_x));
            }

            // fixing bug, but it's a crunch
            /*
            if (dir > 70 && dir < 85) {
                System.out.print('h');
                dir += 25;
            }
            if (dir > 270 && dir < 285) {
                System.out.print('e');
                dir += 25;
            }
            
             */


            double new_angle = (180 + 2 * dir - speed_angle) % 360;
            this.speed_x = abs_sp * Math.cos(Math.toRadians(new_angle));
            this.speed_y = abs_sp * Math.sin(Math.toRadians(new_angle));
/*
            if (y_pos > cent_y + rad_y - this.radius && this.speed_y > 0) {
                this.speed_y *= -1;
            }
            if (y_pos < cent_y - rad_y + this.radius && this.speed_y < 0) {
                this.speed_y *= -1;
            }

 */
/*
            System.out.print(normal_vect[0]);
            System.out.print(' ');
            System.out.print(normal_vect[1]);
            System.out.print(' ');
            System.out.print(dir);
            System.out.print(' ');
            System.out.print(speed_angle);
            System.out.print(' ');
            System.out.print(new_angle);
            System.out.print('\n');

 */


        }

        double direction(double x_part, double y_part) {
            // as input takes parts of normal vector, returns it's direction in circle
            // for ball it-d be 0-360, for wall, rn, 270-360, 0-90
            if (y_part >= 0) {
                return Math.toDegrees(Math.acos(x_part));
            } else {
                return 360 - Math.toDegrees(Math.acos(x_part));
            }
        }

        double[] tangent(double x_pos, double y_pos, double r_lim, double cent_y, double rad_x, double rad_y) {
            // input data = coordinate on arc, we think we already have arc's specifications as global variables
            // will return normal vector to tangent line

            double x_part = (x_pos - r_lim) / rad_x / rad_x;
            double y_part = (y_pos - cent_y) / rad_y / rad_y;
            // this is parts of normal vector to tangent... but not normalised, so their sum isn't 1
            // so we count their sum and multiply them to 1 / it, so we get them normalised
            double sum = 1 / Math.sqrt(x_part * x_part + y_part * y_part);
            x_part *= sum;
            y_part *= sum;

            double res[] = new double[2];
            res[0] = x_part;
            res[1] = y_part;

            return res;
        }

        void inner_bumpArc(double x_pos, double y_pos, double r_lim, double cent_y, double rad_x, double rad_y) {

            double local_x_pos = x_pos - r_lim;
            double local_y_pos = y_pos - cent_y;

            // trying to approximately find a position of collision
            double x_c_el = r_lim;
            double y_c_el = cent_y;
            double a = -1 * rad_x;
            double b = rad_y;
            double xe, ye, t, dt;
            t = Math.PI / 2;
            dt = 0.00001;

            double tmp, saved = 0;
            double x_arc_pos = 0, y_arc_pos = 0, count = 0;

            while (t < Math.PI * 3 / 2) {

                xe = x_c_el + a * Math.cos(t);
                ye = y_c_el + b * Math.sin(t);

                tmp = Math.pow(xe - x_pos, 2) + Math.pow(ye - y_pos, 2) - this.radius * this.radius;

                if (tmp * saved < 0) {
                    x_arc_pos += xe;
                    y_arc_pos += ye;
                    count++;

                }

                saved = tmp;

                t += dt;
            }

            if (count != 0) {
                x_arc_pos /= count;
                y_arc_pos /= count;
            } else {
                x_arc_pos = x_pos;
                y_arc_pos = y_pos;
            }
            // x_arc_pos and y_arc_pos - coordinates of required dot

            double[] normal_vect = tangent(x_arc_pos, y_arc_pos, r_lim, cent_y, rad_x, rad_y);
            double dir = direction(normal_vect[0], normal_vect[1]);

            // for ball now
            double abs_sp = this.absSpeed();
            double part_speed_x = this.speed_x / abs_sp;
            double part_speed_y = this.speed_y / abs_sp;

            double speed_angle;
            if (part_speed_y >= 0) {
                speed_angle = Math.toDegrees(Math.acos(part_speed_x));
            } else {
                speed_angle = 360 - Math.toDegrees(Math.acos(part_speed_x));
            }

            double new_angle = (180 + 2 * dir - speed_angle) % 360;
            this.speed_x = abs_sp * Math.cos(Math.toRadians(new_angle));
            this.speed_y = abs_sp * Math.sin(Math.toRadians(new_angle));

            System.out.print(normal_vect[0]);
            System.out.print(' ');
            System.out.print(normal_vect[1]);
            System.out.print(' ');
            System.out.print(dir);
            System.out.print(' ');
            System.out.print(speed_angle);
            System.out.print(' ');
            System.out.print(new_angle);
            System.out.print('\n');



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
