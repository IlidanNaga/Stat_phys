package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.NumberBinding;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.*;



public class Experiment extends Application {

    // upscale from 1440x900 to real size
    private Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();

    private double screenwidth = primaryScreenBounds.getWidth();
    private double screenheight = primaryScreenBounds.getHeight();

    private double width_upscale = screenwidth / 1440;
    private double height_upscale = screenheight / 900;
    private double total_upscale = (width_upscale + height_upscale) / 2;

    // time value

    private Date time = new Date();
    private long start_time;

    // scene item

    private Pane root = new Pane();

    // LineChart - диаграмма зависимости расстояния от времени

    private NumberAxis time_axis = new NumberAxis();
    private NumberAxis dist_axis = new NumberAxis();
    private LineChart<Number, Number> lineChart =
            new LineChart<Number, Number>(time_axis, dist_axis);

    private XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();

    // BarChart - гистограмма, показывающая для различных расстояний, сколько времени элементы были на этом расстоянии

    private CategoryAxis Cat_dist_axis = new CategoryAxis();
    private NumberAxis amount_axis = new NumberAxis();
    private BarChart<String, Number> barChart =
            new BarChart<String, Number>(Cat_dist_axis, amount_axis);

    private CategoryAxis Cat_x_axis = new CategoryAxis();
    private NumberAxis amount_x = new NumberAxis();
    private BarChart<String, Number> bar_x =
            new BarChart<String, Number>(Cat_x_axis, amount_x);

    private CategoryAxis Cat_y_axis = new CategoryAxis();
    private NumberAxis amount_y = new NumberAxis();
    private BarChart<String, Number> bar_y =
            new BarChart<String, Number>(Cat_y_axis, amount_y);

    private XYChart.Series<String, Number> series1 = new XYChart.Series<String, Number>();
    private XYChart.Series<String, Number> series_x = new XYChart.Series<String, Number>();
    private XYChart.Series<String, Number> series_y = new XYChart.Series<String, Number>();

    // spawn position for new balls

    private double ball_start_x  = 150 * width_upscale;
    private double ball_start_y = 125 * height_upscale;
    private int ball_num = 0;
    private double init_speed = 3;
    private long pair_am = 1;

    private float this_turn_dist;
    private float this_turn_x;
    private float this_turn_y;


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
/*
    private Slider ball_rad_sl = new Slider();
    private Label ball_rad_lab = new Label();


 */

    // adding checkboxes

    private CheckBox box_hist_1 = new CheckBox("Распределение расстояний");
    private CheckBox box_hist_2 = new CheckBox("Распределение по x");
    private CheckBox box_hist_3 = new CheckBox("Распределение по y");

    // adding globally used buttons (a.k.a. exit button)

    private Button button_pause = new Button("Пауза");
    private Button button_start = new Button("Перезапуск");
    private Button button_back = new Button("Назад");
    private Button button_exit = new Button("Выход");

    // variables of active zone size
    private double base_l_lim = 100 * width_upscale;
    private double l_lim = 100 * width_upscale;
    private double r_lim = 700 * width_upscale;
    private double t_lim = 50 * height_upscale;
    private double b_lim = 450 * height_upscale;
    private double rad_x = 100 * height_upscale;

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
    private Line line4 = new Line(r_lim, t_lim, r_lim, b_lim);

    // pre-set for arc
    private MoveTo start = new MoveTo();
    private ArcTo finish = new ArcTo();
    private Path arc = new Path();

    // somewhy it creates with coordinates like 600x425
    // so our area is within 50, 50 - 350, 250

    private double time_passed = 0;
    private int lines_amount = 0;
    private void modifyLineChart(float dist) {

        if (lines_amount > 6000) {
            series.getData().remove(0);
        }
        series.getData().add(new XYChart.Data<Number, Number>(time_passed, dist));
        lines_amount ++;
    }

    private double[] statistics = new double[20];
    private void modifyBarChart(float dist, double delta) {
        if (dist > 0 && dist < 1000) {
            statistics[Math.round(dist /50)] += delta;
        }
    }

    private double[] stat_x = new double[20];
    private void modifyBarX(float dist, double delta) {
        if (dist > 0 && dist < 600) {
            stat_x[Math.round(dist / 50)] += delta;
        }
    }

    private double[] stat_y = new double[20];
    private void modifyBarY(float dist, double delta) {
        if (dist > 0 && dist < 400) {
            stat_y[Math.round(dist / 50)] += delta;
        }
    }

    // creates init scene
    private Parent createContent() {

        // working with checkbox

        box_hist_1.setTranslateX(1100 * width_upscale);
        box_hist_1.setTranslateY(400 * height_upscale);
        box_hist_1.setSelected(true);
        box_hist_1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (box_hist_1.isSelected()) {
                    box_hist_2.setSelected(false);
                    box_hist_3.setSelected(false);
                } else {
                    box_hist_1.setSelected(true);
                }

            }
        });

        box_hist_2.setTranslateX(1100 * width_upscale);
        box_hist_2.setTranslateY(420 * height_upscale);
        box_hist_2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (box_hist_2.isSelected()) {
                    box_hist_1.setSelected(false);
                    box_hist_3.setSelected(false);
                } else {
                    box_hist_2.setSelected(true);
                }
            }
        });

        box_hist_3.setTranslateX(1100 * width_upscale);
        box_hist_3.setTranslateY(440 * height_upscale);
        box_hist_3.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (box_hist_3.isSelected()) {
                    box_hist_1.setSelected(false);
                    box_hist_2.setSelected(false);
                } else {
                    box_hist_3.setSelected(true);
                }
            }
        });

        root.getChildren().add(box_hist_1);
        root.getChildren().add(box_hist_2);
        root.getChildren().add(box_hist_3);



        // working with date item
        start_time = time.getTime();
        System.out.print(start_time);

        // working with barChart item

        for (int i = 0; i < 20; i++) {
            statistics[i] = 0;
            stat_x[i] = 0;
            stat_y[i] = 0;
        }

        barChart.setTranslateY(480 * height_upscale);
        barChart.setTranslateX(675 * width_upscale);
        barChart.setMinHeight(370 * height_upscale);
        barChart.setMaxHeight(370 * height_upscale);
        barChart.setMinWidth(750 * width_upscale);
        barChart.setMaxWidth(750 * width_upscale);

        bar_x.setTranslateY(480 * height_upscale);
        bar_x.setTranslateX(675 * width_upscale);
        bar_x.setMinHeight(370 * height_upscale);
        bar_x.setMaxHeight(370 * height_upscale);
        bar_x.setMinWidth(750 * width_upscale);
        bar_x.setMaxWidth(750 * width_upscale);

        Cat_dist_axis.setLabel("Расстояние между объектами");
        amount_axis.setLabel("Время на данном расстоянии");
        Cat_dist_axis.setVisible(true);
        Cat_dist_axis.setTickLabelsVisible(true);

        amount_axis.setForceZeroInRange(true);

        // adding columns
        series1.getData().add(new XYChart.Data<String, Number>("0-50", statistics[0]));
        series1.getData().add(new XYChart.Data<String, Number>("50-100", statistics[1]));
        series1.getData().add(new XYChart.Data<String, Number>("100-150", statistics[2]));
        series1.getData().add(new XYChart.Data<String, Number>("150-200", statistics[3]));
        series1.getData().add(new XYChart.Data<String, Number>("200-250", statistics[4]));
        series1.getData().add(new XYChart.Data<String, Number>("250-300", statistics[5]));
        series1.getData().add(new XYChart.Data<String, Number>("300-350", statistics[6]));
        series1.getData().add(new XYChart.Data<String, Number>("350-400", statistics[7]));
        series1.getData().add(new XYChart.Data<String, Number>("400-450", statistics[8]));
        series1.getData().add(new XYChart.Data<String, Number>("450-500", statistics[9]));
        series1.getData().add(new XYChart.Data<String, Number>("500-550", statistics[10]));
        series1.getData().add(new XYChart.Data<String, Number>("550-600", statistics[11]));
        //series1.getData().add(new XYChart.Data<String, Number>("600-650", statistics[12]));
        //series1.getData().add(new XYChart.Data<String, Number>("650-700", statistics[13]));
        //series1.getData().add(new XYChart.Data<String, Number>("700-750", statistics[14]));
        //series1.getData().add(new XYChart.Data<String, Number>("750-800", statistics[15]));
        //series1.getData().add(new XYChart.Data<String, Number>("800-850", statistics[16]));
        //series1.getData().add(new XYChart.Data<String, Number>("850-900", statistics[17]));
        //series1.getData().add(new XYChart.Data<String, Number>("900-950", statistics[18]));
        //series1.getData().add(new XYChart.Data<String, Number>("950-1000", statistics[19]));

        barChart.getData().add(series1);

        barChart.setLegendVisible(false);
        barChart.setAnimated(false);

        root.getChildren().add(barChart);

        //adding columns
        series_x.getData().add(new XYChart.Data<String, Number>("0-50", stat_x[0]));
        series_x.getData().add(new XYChart.Data<String, Number>("50-100", stat_x[1]));
        series_x.getData().add(new XYChart.Data<String, Number>("100-150", stat_x[2]));
        series_x.getData().add(new XYChart.Data<String, Number>("150-200", stat_x[3]));
        series_x.getData().add(new XYChart.Data<String, Number>("200-250", stat_x[4]));
        series_x.getData().add(new XYChart.Data<String, Number>("250-300", stat_x[5]));
        series_x.getData().add(new XYChart.Data<String, Number>("300-350", stat_x[6]));
        series_x.getData().add(new XYChart.Data<String, Number>("350-400", stat_x[7]));
        series_x.getData().add(new XYChart.Data<String, Number>("400-450", stat_x[8]));
        series_x.getData().add(new XYChart.Data<String, Number>("450-500", stat_x[9]));
        series_x.getData().add(new XYChart.Data<String, Number>("500-550", stat_x[10]));
        series_x.getData().add(new XYChart.Data<String, Number>("550-600", stat_x[11]));

        bar_x.getData().add(series_x);
        bar_x.setVisible(false);
        bar_x.setAnimated(false);
        Cat_x_axis.setLabel("Расстояние по оси x");
        amount_x.setLabel("Время на этом расстоянии");
        Cat_x_axis.setVisible(true);
        amount_x.setVisible(true);

        bar_x.setLegendVisible(false);
        amount_x.setForceZeroInRange(true);

        root.getChildren().add(bar_x);

        bar_y.setTranslateY(480 * height_upscale);
        bar_y.setTranslateX(675 * width_upscale);
        bar_y.setMinHeight(370 * height_upscale);
        bar_y.setMaxHeight(370 * height_upscale);
        bar_y.setMinWidth(750 * width_upscale);
        bar_y.setMaxWidth(750 * width_upscale);

        Cat_y_axis.setLabel("Расстояние по оси y");
        amount_y.setLabel("Время на данном расстоянии");
        Cat_y_axis.setVisible(true);
        Cat_y_axis.setTickLabelsVisible(true);

        amount_y.setForceZeroInRange(true);

        series_y.getData().add(new XYChart.Data<String, Number>("0-50", stat_y[0]));
        series_y.getData().add(new XYChart.Data<String, Number>("50-100", stat_y[1]));
        series_y.getData().add(new XYChart.Data<String, Number>("100-150", stat_y[2]));
        series_y.getData().add(new XYChart.Data<String, Number>("150-200", stat_y[3]));
        series_y.getData().add(new XYChart.Data<String, Number>("200-250", stat_y[4]));
        series_y.getData().add(new XYChart.Data<String, Number>("250-300", stat_y[5]));
        series_y.getData().add(new XYChart.Data<String, Number>("300-350", stat_y[6]));
        series_y.getData().add(new XYChart.Data<String, Number>("350-400", stat_y[7]));

        bar_y.getData().add(series_y);

        bar_y.setLegendVisible(false);
        bar_y.setAnimated(false);

        root.getChildren().add(bar_y);

        // working with lineChart item

        lineChart.setTranslateY(480 * height_upscale);
        lineChart.setMaxHeight(370 * height_upscale);
        lineChart.setMinHeight(370 * height_upscale);
        lineChart.setMinWidth(700 * width_upscale);
        lineChart.setMaxWidth(700 * width_upscale);

        //lineChart.getYAxis().setTickLabelsVisible(false);

        dist_axis.setLabel("Расстояние между объектами");
        time_axis.setLabel("Прошедшее время");

        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

        lineChart.getData().add(series);
        lineChart.setAnimated(false);

        time_axis.setForceZeroInRange(false);

        root.getChildren().add(lineChart);


        // colors of borders
        line1.setStroke(Color.BLUE);
        line2.setStroke(Color.BLUE);
        line3.setStroke(Color.BLUE);
        line4.setStroke(Color.BLUE);
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

        root.getChildren().add(new Line(25 * width_upscale, 25 * height_upscale, 1025 * width_upscale, 25 * height_upscale));
        root.getChildren().add(new Line(25 * width_upscale, 25 * height_upscale, 25 * width_upscale, 475 * height_upscale));
        root.getChildren().add(new Line(25 * width_upscale, 475 * height_upscale, 1025 * width_upscale, 475 * height_upscale));
        root.getChildren().add(new Line(1025 * width_upscale, 475 * height_upscale, 1025 * width_upscale, 25 * height_upscale));

        // adding first pair
        addPair();

        // buttons section
        // pause
        button_pause.setTranslateX(1100 * width_upscale);
        button_pause.setTranslateY(310 * height_upscale);
        button_pause.setMinWidth(133 * width_upscale);
        button_pause.setMinHeight(25 * height_upscale);

        button_pause.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                buttonPauseAction();
            }
        });

        root.getChildren().add(button_pause);

        button_start.setTranslateX(1237 * width_upscale);
        button_start.setTranslateY(310 * height_upscale);
        button_start.setMinWidth(133 * width_upscale);
        button_start.setMinHeight(25 * height_upscale);

        button_start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                buttonStartAction();
            }
        });

        root.getChildren().add(button_start);

        //pushing back from this frame
        button_back.setTranslateX(1100 * width_upscale);
        button_back.setTranslateY(340 * height_upscale);
        button_back.setMinWidth(133 * width_upscale);
        button_back.setMinHeight(25 * height_upscale);

        root.getChildren().add(button_back);

        //exit button
        button_exit.setTranslateX(1237 * width_upscale);
        button_exit.setTranslateY(340 * height_upscale);
        button_exit.setMinWidth(133 * width_upscale);
        button_exit.setMinHeight(25 * height_upscale);

        root.getChildren().add(button_exit);

        // sliders section


        // balls_amount slider
        pair_am_lab.setLabelFor(pair_am_sl);
        pair_am_lab.setTranslateX(1150 * width_upscale);
        pair_am_lab.setTranslateY(25 * height_upscale);
        pair_am_lab.setFont(new Font(22));

        pair_am_sl.setMin(1);
        pair_am_sl.setMax(100);
        pair_am_sl.setValue(1);
        pair_am_sl.setShowTickLabels(true);
        pair_am_sl.setMajorTickUnit(10);

        pair_am_sl.setTranslateX(1100 * width_upscale);
        pair_am_sl.setTranslateY(50 * height_upscale);
        pair_am_sl.setMinWidth(275 * width_upscale);
        pair_am_sl.setMinHeight(40 * height_upscale);

        root.getChildren().add(pair_am_sl);
        root.getChildren().add(pair_am_lab);

        //init x speed boost
        init_speed_sl.setMin(1);
        init_speed_sl.setMax(20);
        init_speed_sl.setValue(3);
        init_speed_sl.setShowTickLabels(true);
        init_speed_sl.setMajorTickUnit(5);

        init_speed_sl.setTranslateX(1100 * width_upscale);
        init_speed_sl.setTranslateY(120 * height_upscale);
        init_speed_sl.setMinWidth(275 * width_upscale);
        init_speed_sl.setMinHeight(40 * height_upscale);

        init_speed_lab.setLabelFor(init_speed_sl);
        init_speed_lab.setTranslateX(1125 * width_upscale);
        init_speed_lab.setTranslateY(95 * height_upscale);
        init_speed_lab.setFont(new Font(22));

        root.getChildren().add(init_speed_sl);
        root.getChildren().add(init_speed_lab);

        // curve_radius slider
        curve_rad_sl.setMin(-300);
        curve_rad_sl.setMax(300);
        curve_rad_sl.setValue(100);
        curve_rad_sl.setShowTickLabels(true);

        curve_rad_sl.setTranslateX(1100 * width_upscale);
        curve_rad_sl.setTranslateY(190 * height_upscale);
        curve_rad_sl.setMinWidth(275 * width_upscale);
        curve_rad_sl.setMaxWidth(275 * width_upscale);
        curve_rad_sl.setMinHeight(40 * height_upscale);

        curve_rad_lab.setLabelFor(curve_rad_sl);
        curve_rad_lab.setTranslateX(1125 * width_upscale);
        curve_rad_lab.setTranslateY(165 * height_upscale);
        curve_rad_lab.setFont(new Font(22));

        root.getChildren().add(curve_rad_sl);
        root.getChildren().add(curve_rad_lab);

        // wall_speed slider
        wall_speed_sl.setMin(0);
        wall_speed_sl.setMax(10);
        wall_speed_sl.setValue(1);
        wall_speed_sl.setShowTickLabels(true);
        wall_speed_sl.setMajorTickUnit(5);

        wall_speed_sl.setTranslateX(1100 * width_upscale);
        wall_speed_sl.setTranslateY(260 * height_upscale);
        wall_speed_sl.setMinWidth(275 * width_upscale);
        wall_speed_sl.setMinHeight(40 * height_upscale);

        wall_speed_lab.setLabelFor(wall_speed_sl);
        wall_speed_lab.setTranslateX(1125 * width_upscale);
        wall_speed_lab.setTranslateY(235 * height_upscale);
        wall_speed_lab.setFont(new Font(22));

        root.getChildren().add(wall_speed_sl);
        root.getChildren().add(wall_speed_lab);

        //ball_rad slider
/*
        ball_rad_sl.setMin(5);
        ball_rad_sl.setMax(30);
        ball_rad_sl.setValue(10);
        ball_rad_sl.setShowTickLabels(true);
        ball_rad_sl.setMajorTickUnit(5);

        ball_rad_sl.setTranslateX(1100);
        ball_rad_sl.setTranslateY(380);
        ball_rad_sl.setMinWidth(275);
        ball_rad_sl.setMinHeight(40);

        ball_rad_lab.setLabelFor(ball_rad_sl);
        ball_rad_lab.setTranslateX(1125);
        ball_rad_lab.setTranslateY(355);
        ball_rad_lab.setFont(new Font(22));

        root.getChildren().add(ball_rad_sl);
        root.getChildren().add(ball_rad_lab);


 */


        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
            }
        };

        timer.start();

        return root;
    }

    // button actions
    private void buttonStartAction() {

        lines_amount = 0;

        pause = false;
        button_pause.setText("Пауза");


        l_lim = base_l_lim;

        //ball_rad = Math.round((float)ball_rad_sl.getValue());

        // renewing lineChart

        time_passed = 0;
        time = new Date();
        start_time = time.getTime();
        root.getChildren().remove(lineChart);

        lineChart = null;
        series = null;

        lineChart = new LineChart<Number, Number>(time_axis, dist_axis);
        series = new XYChart.Series<Number, Number>();

        lineChart.setTranslateY(480 * height_upscale);
        lineChart.setMaxHeight(370 * height_upscale);
        lineChart.setMinHeight(370 * height_upscale);
        lineChart.setMinWidth(700 * width_upscale);
        lineChart.setMaxWidth(700 * width_upscale);

        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

        lineChart.getData().add(series);
        lineChart.setAnimated(false);

        root.getChildren().add(lineChart);

        // renewing barChart

        for (int ii = 0; ii < 20; ii++) {
            statistics[ii] = 0;
            stat_x[ii] = 0;
            stat_y[ii] = 0;
        }
        int j = 0;

        for (XYChart.Data<String, Number> data : series1.getData()) {
            data.setYValue(statistics[j]);
            j++;
        }

        // renewing everything else

        pair_am = Math.round(pair_am_sl.getValue());
        wall_speed = Math.round(wall_speed_sl.getValue());
        double new_rad_x = Math.round(curve_rad_sl.getValue()) * width_upscale;
        init_speed = Math.round(init_speed_sl.getValue()) * total_upscale;

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

        if (rad_x != 0) {
            root.getChildren().remove(arc);
        } else
            root.getChildren().remove(line4);

        if (new_rad_x * finish.getRadiusX() < 0) {
            double lim_1 = start.getY();
            double lim_2 = finish.getY();
            start.setY(lim_2);
            finish.setY(lim_1);
            finish.setRadiusX(new_rad_x);
        } else if (new_rad_x * finish.getRadiusX() > 0) {
            finish.setRadiusX(new_rad_x);
        }

        rad_x = new_rad_x;

        if (rad_x != 0) {
            root.getChildren().add(arc);
        } else {
            root.getChildren().add(line4);
        }


        ball_num = 0;

        for (int i = 0; i < pair_am; i++) {
            addPair();
        }

    }
    private void buttonPauseAction() {
        if (!pause) {
            button_pause.setText("Продолжить");
            pause = true;
        } else {
            button_pause.setText("Пауза");
            pause = false;
            time = new Date();
            start_time = time.getTime();
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

        if (box_hist_1.isSelected())
            barChart.setVisible(true);
        else
            barChart.setVisible(false);

        if (box_hist_2.isSelected())
            bar_x.setVisible(true);
        else
            bar_x.setVisible(false);

        if (box_hist_3.isSelected())
            bar_y.setVisible(true);
        else
            bar_y.setVisible(false);

        if (!pause) {

            int i = 0;

            this_turn_dist = 0;
            this_turn_x = 0;
            this_turn_y = 0;
            //moving the walls
            // setting bot limit of x to 50, top limit to 150
            if (l_lim < 50 * width_upscale || l_lim > 150 * width_upscale) {
                wall_speed *= -1;
            }
            l_lim += wall_speed;

            line1.setStartX(l_lim);
            line1.setEndX(l_lim);

            line2.setStartX(l_lim);
            line3.setStartX(l_lim);

            //collision of ball:
            all_balls.forEach(pair -> {

                int pos = all_balls.indexOf(pair);

                double[] x_coords = new double[2];
                double[] y_coords = new double[2];

                for (int ii = 0; ii < 2; ii++) {

                    Ball s = pair[ii];

                    Bounds boundsInScene = s.localToScene(s.getBoundsInLocal());

                    double min_x = boundsInScene.getMinX();
                    double min_y = boundsInScene.getMinY();
                    double max_x = boundsInScene.getMaxX();
                    double max_y = boundsInScene.getMaxY();

                    double x = (min_x + max_x) / 2;
                    double y = (min_y + max_y) / 2;

                    boolean positive_curve_flag = false;
                    boolean curved = true;

                    if (s.getBoundsInParent().intersects(line1.getBoundsInParent())) {
                        s.Bump_left(wall_speed);
                    }

                    if (rad_x > 0) {

                        positive_curve_flag = true;

                        if (s.getBoundsInParent().intersects(line2.getBoundsInParent())) {
                            s.Bump_top_bot();
                        }

                        if (s.getBoundsInParent().intersects(line3.getBoundsInParent())) {
                            s.Bump_top_bot();
                        }

                        // external shape
                        if (max_x >= r_lim) {
                            if (ellipsis(x, y) && s.timer < 1) {
                                s.timer = 2;
                                s.bumpArc(x, y, r_lim, cent_y, rad_x, rad_y); // в тесте соотношение координат работает как-то так
                            }

                        }
                    } else if (rad_x < 0){

                        double maxX = maximalX();
                        positive_curve_flag = false;

                        if (s.getBoundsInParent().intersects(line2.getBoundsInParent()) && max_x < maxX) {
                            s.Bump_top_bot();
                        }

                        if (s.getBoundsInParent().intersects(line3.getBoundsInParent()) && max_x < maxX) {
                            s.Bump_top_bot();
                        }

                        if (max_x >= maxX) {
                            s.speedRevert();
                        } else {

                            // internal shape
                            if (max_x >= r_lim + rad_x - ball_rad) {
                                if (neg_curved_ellipsis(x, y) && s.timer < 1) {
                                    s.timer = 2;
                                    s.inner_bumpArc(x, y, r_lim, cent_y, rad_x, rad_y);
                                }
                            }
                        }
                    } else {
                        curved = false;
                        if (max_x >= r_lim) {
                            s.Bump_right();
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

                    boolean flag = true;

                    // обработчик левой стенки
                    if (s.speed_x < 0) {

                        if (bound2.getMinX() <= l_lim + wall_speed) {
                            // значит на следующем шаге он столкнется, надо этому не помешать
                            if (wall_speed >= 0) {
                                // позиция шарика по x до следующего просчета столкновений не изменится, так что можем его сдвинуть
                                if (l_lim > bound2.getMinX())
                                    s.shiftSpecRight(l_lim - bound2.getMinX());
                            } else {
                                double lim = l_lim + wall_speed - bound2.getMinX() - 1;
                                s.shiftSpecRight(lim);
                            }
                        } else {
                            // значит на следующем шаге он не столкнется

                        }
                    }

                    //  обработчик коллизии верхней\нижней стенок
                    bound2 = s.localToScene(s.getBoundsInLocal());
                    min_y = bound2.getMinY();
                    max_y = bound2.getMaxY();

                    if (min_y < t_lim - 1) {
                        s.shiftSpecBot(t_lim - 1 - min_y);
                    }
                    if (max_y > b_lim + 1) {
                        s.shiftSpecTop(max_y - b_lim - 1);
                    }
                    /*
                    while (flag) {
                        bound2 = s.localToScene(s.getBoundsInLocal());

                        min_x = bound2.getMinX();
                        min_y = bound2.getMinY();
                        max_x = bound2.getMaxX();
                        max_y = bound2.getMaxY();

                        if (min_y >= t_lim - 1 && max_y <= b_lim + 1)
                            flag = false;

                        if (min_y < t_lim - 1)
                            s.shiftBot();

                        if (max_y > b_lim + 1)
                            s.shiftTop();

                    }
                    
                     */

                    // обработчик коллизии с дугой
                    if (curved) {
                        if (positive_curve_flag)
                            // c выгнутой дугой
                            while (positive_curve_flag) {
                                bound2 = s.localToScene(s.getBoundsInLocal());

                                min_x = bound2.getMinX();
                                min_y = bound2.getMinY();
                                max_x = bound2.getMaxX();
                                max_y = bound2.getMaxY();

                                if (max_x < r_lim + 2)
                                    break;

                                // coordinates of ball centre
                                x = (min_x + max_x) / 2;
                                y = (min_y + max_y) / 2;

                                double t = 0;
                                double dt = Math.PI / 4;
                                double point_x, point_y;
                                boolean intersects = false;

                                while (t <= 2 * Math.PI) {

                                    point_x = x + ball_rad * Math.cos(t);
                                    point_y = y + ball_rad * Math.sin(t);

                                    intersects = intersects || arc_collision(point_x, point_y);

                                    t += dt;
                                }

                                if (intersects) {
                                    s.shiftLeft();
                                    if (y > cent_y) {
                                        s.shiftTop();
                                    }
                                    if (y < cent_y) {
                                        s.shiftBot();
                                    }
                                } else {
                                    positive_curve_flag = false;
                                }

                            }
                        else {
                            // с вогнутой дугой
                            positive_curve_flag = true;
                            while (positive_curve_flag) {
                                bound2 = s.localToScene(s.getBoundsInLocal());

                                min_x = bound2.getMinX();
                                min_y = bound2.getMinY();
                                max_x = bound2.getMaxX();
                                max_y = bound2.getMaxY();

                                if (max_x < r_lim + rad_x)
                                    break;

                                // coordinates of ball centre
                                x = (min_x + max_x) / 2;
                                y = (min_y + max_y) / 2;

                                double t = 0;
                                double dt = Math.PI / 4;
                                double point_x, point_y;
                                boolean intersects = false;

                                while (t <= 2 * Math.PI) {

                                    point_x = x + ball_rad * Math.cos(t);
                                    point_y = y + ball_rad * Math.sin(t);

                                    intersects = intersects || neg_arc_collision(point_x, point_y);

                                    t += dt;
                                }

                                if (intersects) {
                                    s.shiftLeft();
                                    s.shiftLeft();
                                    if (y > cent_y) {
                                        s.shiftBot();
                                    }
                                    if (y < cent_y) {
                                        s.shiftTop();
                                    }
                                } else {
                                    positive_curve_flag = false;
                                }

                            }
                        }
                    } else {
                        while (true) {
                            bound2 = s.localToScene(s.getBoundsInLocal());

                            max_x = bound2.getMaxX();

                            if (max_x > r_lim + 1) {
                                s.shiftLeft();
                            } else
                                break;
                        }
                    }

                    bound2 = s.localToScene(s.getBoundsInLocal());

                    if (bound2.getMaxX() > r_lim + Math.abs(rad_x) + 100 + ball_rad * 2)
                        s.speed_x *= -1;

                    if (bound2.getMinX() < l_lim - 100 - ball_rad * 2)
                        s.speed_x *= -1;

                    if (bound2.getMaxY() > b_lim + 100 + ball_rad * 2)
                        s.speed_y *= -1;

                    if (bound2.getMinY() < t_lim - 100 - ball_rad * 2)
                        s.speed_y *= -1;

                    x_coords[s.number % 2] = (bound2.getMaxX() + bound2.getMinX()) / 2;
                    y_coords[s.number % 2] = (bound2.getMaxY() + bound2.getMinY()) / 2;


                }

                all_links.get(pos).setStartX(x_coords[0]);
                all_links.get(pos).setEndX(x_coords[1]);
                all_links.get(pos).setStartY(y_coords[0]);
                all_links.get(pos).setEndY(y_coords[1]);

                this_turn_x += Math.abs(x_coords[0] - x_coords[1]) / (float)width_upscale;
                this_turn_y += Math.abs(y_coords[1] - y_coords[0]) / (float)height_upscale;
                this_turn_dist = (float)Math.sqrt(Math.pow((float)this_turn_x, 2) + Math.pow((float)this_turn_y, 2));
            });

            this_turn_dist /= pair_am;
            this_turn_x /= pair_am;
            this_turn_y /= pair_am;

            time = new Date();

            double delta_time = (time.getTime() - start_time) * 0.001;
            start_time = time.getTime();

            modifyLineChart(this_turn_dist);
            time_passed += delta_time;

            modifyBarChart(this_turn_dist, delta_time);
            modifyBarX(this_turn_x, delta_time);
            modifyBarY(this_turn_y, delta_time);

            int j = 0;

            for (XYChart.Data<String, Number> data : series1.getData()) {
                data.setYValue(statistics[j]);
                j++;
            }

            j = 0;
            for (XYChart.Data<String, Number> data: series_x.getData()) {
                data.setYValue(stat_x[j]);
                j++;
            }

            j = 0;
            for (XYChart.Data<String, Number> data: series_y.getData()) {
                data.setYValue(stat_y[j]);
                j++;
            }

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
    private boolean neg_curved_ellipsis(double x, double y) { // x, y - coordinates of ball centre
        double value = Math.pow((r_lim - x), 2)/Math.pow(rad_x - ball_rad, 2) + Math.pow((y - cent_y), 2)/Math.pow(rad_y + ball_rad, 2);
        return value <= 1;
    }
    private boolean arc_collision(double x, double y) {
        double value;
        if (rad_x != 0) {
            value = Math.pow(x - r_lim, 2) / Math.pow(rad_x, 2) + Math.pow(y - cent_y, 2) / Math.pow(rad_y, 2);
            return value > 1.1;
        } else {
            return x > r_lim || y < t_lim || y > b_lim;
        }
    }
    private boolean neg_arc_collision(double x, double y) {
        double value = Math.pow(x - r_lim, 2) / Math.pow(rad_x, 2) + Math.pow(y - cent_y, 2)/Math.pow(rad_y, 2);
        return value < 0.95;
    }
    private double maximalX() {
        double angle = Math.asin(1 - ball_rad/rad_y); // in radians
        return r_lim - Math.cos(angle);
    }

    // ball class, contains collision logic
    private static class Ball extends Circle {
        double speed_x, speed_y;
        int timer;
        int number;
        int radius;

        Ball(double x, double y, int r, double s_x, double s_y, Color color, int numb) {
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
            if (abs_sp > 22) {
                this.speed_x *= 22/abs_sp;
                this.speed_y *= 22/abs_sp;
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
        void Bump_right() {
            this.speed_x *= -1;
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
            dt = 0.001;

            double tmp, saved = 0;
            double x_arc_pos = 0, y_arc_pos = 0, count = 0;

            while (t < Math.PI / 2) {

                xe = x_c_el + a * Math.cos(t);
                ye = y_c_el + b * Math.sin(t);

                tmp = Math.pow(xe - x_pos, 2) + Math.pow(ye - y_pos, 2) - this.radius * this.radius;

                if (tmp * saved < 0 && x_arc_pos > r_lim) {

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

            double local_x_pos = r_lim - x_pos;
            double local_y_pos = y_pos - cent_y;

            // trying to approximately find a position of collision

            if (Math.abs(local_y_pos) > rad_y - this.radius / 2) {
                this.speed_x *= -1;
                this.speed_y *= -1;
                return;
            }


            double x_c_el = r_lim;
            double y_c_el = cent_y;
            double a = -1 * rad_x;
            double b = rad_y;
            double xe, ye, t, dt;
            t = Math.PI / 2;
            dt = 0.001;

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
        void shift(double x, double y) { // it'd distract the value
            setTranslateX(getTranslateX() - x);
            setTranslateY(getTranslateY() - y);
        }
        void speedRevert() {
            this.speed_x *= -1;
            this.speed_y *= -1;
        }

        void shiftSpecRight(double x) {
            setTranslateX(getTranslateX() + x);
        }
        void shiftSpecBot(double y) {
            setTranslateY(getTranslateY() + y);
        }
        void shiftSpecTop(double y) {
            setTranslateY(getTranslateY() - y);
        }

    }

    // run function
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

    // launch from __main__
    public static void main(String[] args) {
        launch(args);
    }

}
