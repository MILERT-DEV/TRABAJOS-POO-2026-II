module pe.edu.upeu.systurismo {
    requires javafx.controls;
    requires javafx.fxml;

    requires static lombok;
    requires jakarta.validation;

    requires java.sql;
    requires java.naming;
    requires org.slf4j;
    requires com.zaxxer.hikari;

    opens pe.edu.upeu.systurismo to javafx.fxml;
    opens pe.edu.upeu.systurismo.controller to javafx.fxml;
    opens pe.edu.upeu.systurismo.model;
    exports pe.edu.upeu.systurismo;
}
