package pe.edu.upeu.systurismo;

import javafx.application.Application;

/** Punto de entrada para ejecutar desde un jar (evita el error de "JavaFX runtime components are missing"). */
public class App {
    public static void main(String[] args) {
        Application.launch(SysTurismo.class, args);
    }
}
