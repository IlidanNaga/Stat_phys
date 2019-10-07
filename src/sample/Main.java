package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {

    // scene item

    private Pane root = new Pane();

    // spawn position for new balls

    private int ball_start_x  = 150;
    private int ball_start_y = 150;
    private int ball_num = 0;
    private double init_speed = 3;

    // flag for pause/unpause
    private boolean pause = false;

    // adding globally used sliders and labels
    private Label pair_am_lab = new Label();
    private Slider pair_am_sl = new Slider();

    private Slider init_speed_sl = new Slider();
    private Label init_speed_lab = new Label();

    private Slider curve_rad_sl = new Slider();
    private Label curve_rad_lab = new Label();

    private Slider wall_speed_sl = new Slider();
    private Label wall_speed_lab = new Label();

    // adding globally used buttons (a.k.a. exit button)

    private Button button_exit = new Button("Выход");
    private Button button_pause = new Button("Пауза");
    private Button button_start = new Button("Перезапуск");
    private Button button_back = new Button("Назад");

    // variables of active zone size
    private double base_l_lim = 100;
    private double l_lim = 100;
    private double r_lim = 700;
    private double t_lim = 100;
    private double b_lim = 500;
    private double rad_x = 100;

    // united ball radius
    private int ball_rad = 10;

    // speed of left wall
    private double wall_speed = 1;

    // Y position and Y rad of arc
    private double cent_y = (b_lim + t_lim) / 2;
    private double rad_y = (b_lim - t_lim) / 2;

    // startX, startY, endX, endY
    private Line line1 = new Line(l_lim, t_lim, l_lim, b_lim); // left
    private Line line2 = new Line(l_lim, t_lim, r_lim, t_lim); // top
    private Line line3 = new Line(l_lim, b_lim, r_lim, b_lim); // bot

    // pre-set for arc
    private MoveTo start = new MoveTo();
    private ArcTo finish = new ArcTo();
    private Path arc = new Path();

    // somewhy it creates with coordinates like 600x425
    // so our area is within 50, 50 - 350, 250

    // creates init scene
    private Parent createContent() {
        // colors of borders
        line1.setStroke(Color.BLUE);
        line2.setStroke(Color.BLUE);
        line3.setStroke(Color.BLUE);
        arc.setStroke(Color.BLUE);

        // setting borders

        root.getChildren().add(line1);
        root.getChildren().add(line2);
        root.getChildren().add(line3);

        if (rad_x >= 0) { // right-oriented shape
            start.setX(r_lim);
            start.setY(b_lim);

            finish.setRadiusX(rad_x);
            finish.setRadiusY(rad_y);
            finish.setXAxisRotation(0);
            finish.setX(r_lim);
            finish.setY(t_lim);
            finish.setLargeArcFlag(false);
            finish.setSweepFlag(false);
        } else { // left-oriented shape
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

        // borders

        root.getChildren().add(new Line(25, 75, 1025, 75));
        root.getChildren().add(new Line(25, 75, 25, 525));
        root.getChildren().add(new Line(25, 525, 1025, 525));
        root.getChildren().add(new Line(1025, 525, 1025, 75));

        // adding first pair
        addPair();

        // buttons section
        // pause
        button_pause.setTranslateX(1235);
        button_pause.setTranslateY(350);
        button_pause.setMinWidth(130);
        button_pause.setMinHeight(20);

        button_pause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                buttonPauseAction();
            }
        });

        root.getChildren().add(button_pause);

        button_start.setTranslateX(1100);
        button_start.setTranslateY(350);
        button_start.setMinWidth(130);
        button_start.setMinHeight(20);

        button_start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                buttonStartAction();
            }
        });

        root.getChildren().add(button_start);

        // exit
        button_exit.setTranslateX(1100);
        button_exit.setTranslateY(380);
        button_exit.setMinWidth(130);
        button_exit.setMinHeight(20);

        root.getChildren().add(button_exit);

        //pushing back from this frame
        button_back.setTranslateX(1235);
        button_back.setTranslateY(380);
        button_back.setMinWidth(130);
        button_back.setMinHeight(20);

        root.getChildren().add(button_back);

        // sliders section


        // balls_amount slider
        pair_am_lab.setLabelFor(pair_am_sl);
        pair_am_lab.setTranslateX(1150);
        pair_am_lab.setTranslateY(75);
        pair_am_lab.setFont(new Font(22));

        pair_am_sl.setMin(1);
        pair_am_sl.setMax(100);
        pair_am_sl.setValue(1);
        pair_am_sl.setShowTickLabels(true);
        pair_am_sl.setMajorTickUnit(10);

        pair_am_sl.setTranslateX(1100);
        pair_am_sl.setTranslateY(100);
        pair_am_sl.setMinWidth(275);
        pair_am_sl.setMinHeight(40);

        root.getChildren().add(pair_am_sl);
        root.getChildren().add(pair_am_lab);

        //init x speed boost
        init_speed_sl.setMin(1);
        init_speed_sl.setMax(20);
        init_speed_sl.setValue(3);
        init_speed_sl.setShowTickLabels(true);
        init_speed_sl.setMajorTickUnit(5);

        init_speed_sl.setTranslateX(1100);
        init_speed_sl.setTranslateY(170);
        init_speed_sl.setMinWidth(275);
        init_speed_sl.setMinHeight(40);

        init_speed_lab.setLabelFor(init_speed_sl);
        init_speed_lab.setTranslateX(1125);
        init_speed_lab.setTranslateY(145);
        init_speed_lab.setFont(new Font(22));

        root.getChildren().add(init_speed_sl);
        root.getChildren().add(init_speed_lab);

        // curve_radius slider
        curve_rad_sl.setMin(-300);
        curve_rad_sl.setMax(300);
        curve_rad_sl.setValue(100);
        curve_rad_sl.setShowTickLabels(true);

        curve_rad_sl.setTranslateX(1100);
        curve_rad_sl.setTranslateY(240);
        curve_rad_sl.setMinWidth(275);
        curve_rad_sl.setMaxWidth(275);
        curve_rad_sl.setMinHeight(40);

        curve_rad_lab.setLabelFor(curve_rad_sl);
        curve_rad_lab.setTranslateX(1125);
        curve_rad_lab.setTranslateY(215);
        curve_rad_lab.setFont(new Font(22));

        root.getChildren().add(curve_rad_sl);
        root.getChildren().add(curve_rad_lab);

        // wall_speed slider
        wall_speed_sl.setMin(0);
        wall_speed_sl.setMax(10);
        wall_speed_sl.setValue(1);
        wall_speed_sl.setShowTickLabels(true);
        wall_speed_sl.setMajorTickUnit(5);

        wall_speed_sl.setTranslateX(1100);
        wall_speed_sl.setTranslateY(310);
        wall_speed_sl.setMinWidth(275);
        wall_speed_sl.setMinHeight(40);

        wall_speed_lab.setLabelFor(wall_speed_sl);
        wall_speed_lab.setTranslateX(1125);
        wall_speed_lab.setTranslateY(285);
        wall_speed_lab.setFont(new Font(22));

        root.getChildren().add(wall_speed_sl);
        root.getChildren().add(wall_speed_lab);


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };

        timer.start();

        return root;
    }

    private void buttonStartAction() {

        pause = false;
        button_pause.setText("Пауза");

        l_lim = base_l_lim;

        double pair_am = Math.round(pair_am_sl.getValue());
        wall_speed = Math.round(wall_speed_sl.getValue());
        double new_rad_x = Math.round(curve_rad_sl.getValue());
        init_speed = Math.round(init_speed_sl.getValue());

        all_balls.forEach(s -> {
            root.getChildren().remove(s[0]);
            root.getChildren().remove(s[1]);
        });

        all_links.forEach(s -> {
            root.getChildren().remove(s);
        });

        all_balls.clear();
        all_links.clear();

        line1.setStartX(l_lim);
        line1.setEndX(l_lim);

        line2.setStartX(l_lim);
        line3.setStartX(l_lim);

        if (new_rad_x * rad_x < 0) {
            double lim_1 = start.getY();
            double lim_2 = finish.getY();
            start.setY(lim_2);
            finish.setY(lim_1);
        }
        finish.setRadiusX(Math.abs(new_rad_x));
        rad_x = new_rad_x;

        ball_num = 0;

        while(pair_am > 0) {
            addPair();
            pair_am --;
        }

    }

    private void buttonPauseAction() {
        if (!pause) {
            button_pause.setText("Продолжить");
            pause = true;
        } else {
            button_pause.setText("Пауза");
            pause = false;
        }
    }

    private ArrayList<Ball[]> all_balls = new ArrayList();
    private ArrayList<Line> all_links = new ArrayList();

    // adding pair into the scene
    private void addPair() {
        Random rand = new Random();

        double angle1 = 20 * rand.nextDouble() - 10;
        double angle2 = 20 * rand.nextDouble() - 10;
        double abs_speed1 = rand.nextDouble();

        double x_s_1 = abs_speed1 * Math.cos(Math.toRadians(angle1)) + init_speed;
        double y_s_1 = abs_speed1 * Math.sin(Math.toRadians(angle1));

        double x_s_2 = abs_speed1 * Math.cos(Math.toRadians(angle2)) + init_speed;
        double y_s_2 = abs_speed1 * Math.sin(Math.toRadians(angle2));

        double red = rand.nextDouble(), green = rand.nextDouble(), blue = rand.nextDouble();

        Ball ball1 = new Ball(ball_start_x, ball_start_y, ball_rad, x_s_1, y_s_1, Color.color(red, green, blue), ball_num);
        ball_num ++;
        Ball ball2 = new Ball(ball_start_x, ball_start_y, ball_rad, x_s_2, y_s_2, Color.color(red, green, blue), ball_num);
        ball_num ++;

        root.getChildren().add(ball1);
        root.getChildren().add(ball2);

        Ball[] new_pair = new Ball[2];

        new_pair[0] = ball1;
        new_pair[1] = ball2;

        all_balls.add(new_pair);

        Line linker = new Line(ball_start_x, ball_start_y, ball_start_x, ball_start_y);
        linker.setStroke(Color.color(red, green, blue));

        all_links.add(linker);

        root.getChildren().add(linker);

    }

    // loop function
    private void update() {

        if (!pause) {
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
            all_balls.forEach(pair -> {

                boolean collision_this_frame = false;

                int pos = all_balls.indexOf(pair);

                double[] x_coords = new double[2];
                double[] y_coords = new double[2];

                for (int i = 0; i < 2; i++) {

                    Ball s = pair[i];

                    Bounds boundsInScene = s.localToScene(s.getBoundsInLocal());

                    double min_x = boundsInScene.getMinX();
                    double min_y = boundsInScene.getMinY();
                    double max_x = boundsInScene.getMaxX();
                    double max_y = boundsInScene.getMaxY();

                    double x = (min_x + max_x) / 2;
                    double y = (min_y + max_y) / 2;

                    if (s.getBoundsInParent().intersects(line1.getBoundsInParent())) {
                        s.Bump_left(wall_speed);
                        collision_this_frame = true;
                    }

                    if (s.getBoundsInParent().intersects(line2.getBoundsInParent())) {
                        s.Bump_top_bot();
                        collision_this_frame = true;
                    }

                    if (s.getBoundsInParent().intersects(line3.getBoundsInParent())) {
                        s.Bump_top_bot();
                        collision_this_frame = true;
                    }


                    // it must work another way, but for now it'd be fine
                    if (rad_x >= 0) {
                        // external shape
                        if (max_x >= r_lim) {
                            if (ellipsis(x, y) && s.timer < 1) {
                                s.timer = 2;
                                s.bumpArc(x, y, r_lim, cent_y, rad_x, rad_y); // в тесте соотношение координат работает как-то так
                                collision_this_frame = true;
                            }

                        }
                    } else {
                        // internal shape
                        if (max_x >= r_lim + rad_x - ball_rad) {
                            if (!ellipsis(x, y) && s.timer < 1) {
                                s.timer = 2;
                                s.inner_bumpArc(x, y, r_lim, cent_y, rad_x, rad_y);
                                collision_this_frame = true;
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

                    Bounds bound2;

                    boolean flag = true;

                    while (flag) {
                        bound2 = s.localToScene(s.getBoundsInLocal());

                        min_x = bound2.getMinX();
                        min_y = bound2.getMinY();
                        max_x = bound2.getMaxX();
                        max_y = bound2.getMaxY();
                        if (min_x >= l_lim - ball_rad && min_y >= t_lim - 1 && max_y <= b_lim + 1)
                            flag = false;

                        if (min_x < l_lim - ball_rad) {
                            s.shiftRight();

                        }
                        if (min_y < t_lim - 1)
                            s.shiftBot();

                        if (max_y > b_lim + 1)
                            s.shiftTop();


                    }


                    bound2 = s.localToScene(s.getBoundsInLocal());

                    x_coords[s.number % 2] = (bound2.getMaxX() + bound2.getMinX()) / 2;
                    y_coords[s.number % 2] = (bound2.getMaxY() + bound2.getMinY()) / 2;

                }

                all_links.get(pos).setStartX(x_coords[0]);
                all_links.get(pos).setEndX(x_coords[1]);
                all_links.get(pos).setStartY(y_coords[0]);
                all_links.get(pos).setEndY(y_coords[1]);


            });
        }

        pair_am_lab.setText("Число пар: " + Math.round(pair_am_sl.getValue()));
        init_speed_lab.setText("Начальная скорость: " + Math.round(init_speed_sl.getValue()));
        curve_rad_lab.setText("Радиус дуги: " + Math.round(curve_rad_sl.getValue()));
        wall_speed_lab.setText("Скорость стенки: " + Math.round(wall_speed_sl.getValue()));
    }

    // just supportive function
    private boolean ellipsis(double x, double y) {
        double value = Math.pow((x - r_lim), 2)/Math.pow(rad_x - ball_rad, 2) + Math.pow((y - cent_y), 2)/Math.pow(rad_y - ball_rad, 2);
        return value >= 1;
    }

    // ball class, contains collision logic
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
            double abs_sp = absSpeed();
            if (abs_sp > 30) {
                this.speed_x *= 30/abs_sp;
                this.speed_y *= 30/abs_sp;
            }
            moveX();
            moveY();
        }

        void Bump_left(double wall_speed) {
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

        void shiftTop() {
            setTranslateY(getTranslateY() - 1);
        }
        void shiftBot() {
            setTranslateY(getTranslateY() + 1);
        }
        void shiftLeft() {
            setTranslateX(getTranslateX() - 1);
        }
        void shiftRight() {
            setTranslateX(getTranslateX() + this.radius);
        }

    }

    @Override
    public void start(Stage stage) throws Exception{
        Scene scene = new Scene(createContent());

        button_exit.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
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

    public static void main(String[] args) {
        launch(args);
    }

}
