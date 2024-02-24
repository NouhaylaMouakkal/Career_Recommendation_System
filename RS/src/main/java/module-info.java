module poo.rs {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires java.sql;
    requires commons.math3;
    requires com.opencsv;
    requires org.apache.opennlp.tools;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    opens poo.rs to javafx.fxml;
    exports poo.rs;
}