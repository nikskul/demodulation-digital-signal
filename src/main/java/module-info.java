module ru.nikskul.demodulationdigitalsignal {
    requires java.logging;
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;

    requires org.jfree.chart.fx;
    requires org.jfree.jfreechart;
    requires org.jfree.fxgraphics2d;


    opens ru.nikskul.demodulationdigitalsignal to javafx.fxml;
    exports ru.nikskul.demodulationdigitalsignal;
}