package ru.nikskul.demodulationdigitalsignal;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.HorizontalAlignment;
import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.fx.FXGraphics2D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Main extends Application {

    static class ChartCanvas extends Canvas {

        final JFreeChart chart;

        private final FXGraphics2D g2;

        public ChartCanvas(JFreeChart chart) {
            this.chart = chart;
            this.g2 = new FXGraphics2D(getGraphicsContext2D());
            // Redraw canvas when size changes.
            widthProperty().addListener(e -> draw());
            heightProperty().addListener(e -> draw());
        }

        private void draw() {
            double width = getWidth();
            double height = getHeight();
            getGraphicsContext2D().clearRect(0, 0, width, height);
            this.chart.draw(this.g2, new Rectangle2D.Double(0, 0, width,
                height));
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) {
            return getWidth();
        }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }
    }

    /**
     * Creates a chart.
     *
     * @param dataset a dataset.
     * @return A chart.
     */
    private static JFreeChart createChart(XYDataset dataset) {

        JFreeChart chart = ChartFactory.createXYLineChart(
            "Модуляция",    // title
            "Время",             // x-axis label
            "Амплитуда",      // y-axis label
            dataset
        );

        String fontName = "Palatino";
        chart.getTitle().setFont(new Font(fontName, Font.BOLD, 18));

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainGridlinePaint(new Color(0, 0, 0));
        plot.setBackgroundPaint(new Color(255, 255, 255));
        plot.setDomainPannable(true);
        plot.setRangePannable(false);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.getDomainAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getDomainAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
//        plot.getRangeAxis().setUpperBound(1);
        plot.getRangeAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getRangeAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
        chart.getLegend().setItemFont(new Font(fontName, Font.PLAIN, 14));
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setHorizontalAlignment(HorizontalAlignment.CENTER);
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer renderer) {
            renderer.setDefaultShapesVisible(false);
            renderer.setDrawSeriesLineAsPath(true);
            // set the default stroke for all series
            renderer.setAutoPopulateSeriesStroke(false);
            renderer.setDefaultStroke(new BasicStroke(3.0f,
                BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL), false);
            renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesPaint(1, new Color(24, 123, 58));
            renderer.setSeriesPaint(2, new Color(149, 201, 136));
            renderer.setSeriesPaint(3, new Color(1, 62, 29));
            renderer.setSeriesPaint(4, new Color(81, 176, 86));
            renderer.setSeriesPaint(5, new Color(0, 55, 122));
            renderer.setSeriesPaint(6, new Color(0, 92, 165));
            renderer.setDefaultLegendTextFont(new Font(fontName, Font.PLAIN, 14));
        }

        return chart;

    }

    /**
     * Creates a dataset, consisting of two series of monthly data.
     *
     * @return the dataset.
     */
    private static XYDataset createDataset() {
        double T = 8;
        double f = 1 / T;
        double c = 3d / 4d;

        Function<Integer, Double> a = (n) -> {
            return (1 / (PI * n)) *
                (cos(PI * n / 4) - cos(3 * PI * n / 4) + cos(6 * PI * n / 4) - cos(7 * PI * n / 4));
        };

        Function<Integer, Double> b = (n) -> {
            return (1 / (PI * n)) *
                (sin(3 * PI * n / 4) - sin(PI * n / 4) + sin(7 * PI * n / 4) - sin(6 * PI * n / 4));
        };

        BiFunction<Double, Integer, Double> sumA = (t, n) -> {
            double result = 0;
            for (int i = 1; i <= n; i++) {
                result += a.apply(i) * sin(2 * PI * i * f * t);
            }
            return result;
        };
        BiFunction<Double, Integer, Double> sumB = (t, n) -> {
            double result = 0;
            for (int i = 1; i <= n; i++) {
                result += b.apply(i) * cos(2 * PI * i * f * t);
            }
            return result;
        };

        // Ряд Фурье
        BiFunction<Double, Integer, Double> g = (t, n) -> {
            return c/2
                + sumA.apply(t, n)
                + sumB.apply(t, n);
        };
        // N-ная гармоника ряда
        BiFunction<Double, Integer, Double> gn = (t, n) -> {
            return c/2
                + a.apply(n) * sin(2 * PI * n * f * t)
                + b.apply(n) * cos(2 * PI * n * f * t);
        };

        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries signal = new XYSeries("Аппроксимированная функция");
        XYSeries nHarmony = new XYSeries("N-ая гармоника");
        int N = 50; // Можно менять число гармоник, чтобы видеть последовательное приближение сигнала
        int end = (int) T;
        double step = 0.01;
        for (double t = 0; t < end; t += step) {
            signal.add(new XYDataItem((Double) t, g.apply(t, N)));
            nHarmony.add(new XYDataItem((Double) t, gn.apply(t, N)));
        }
        dataset.addSeries(signal);
        dataset.addSeries(nHarmony);

        return dataset;
    }

    @Override
    public void start(Stage stage) {
        XYDataset dataset =  createDataset();
        JFreeChart chart = createChart(dataset);
        ChartCanvas canvas = new ChartCanvas(chart);
        StackPane stackPane = new StackPane();
        stackPane.getChildren().add(canvas);
        // Bind canvas size to stack pane size.
        canvas.widthProperty().bind(stackPane.widthProperty());
        canvas.heightProperty().bind(stackPane.heightProperty());
        stage.setScene(new Scene(stackPane));
        stage.setTitle("Модуляция сигнала содержащего последовательность бит (01100010) ");
        stage.setWidth(480);
        stage.setHeight(250);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
