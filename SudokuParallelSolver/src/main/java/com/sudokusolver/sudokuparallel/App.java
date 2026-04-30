package com.sudokusolver.sudokuparallel;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/sudokusolver/sudokuparallel/hello-view.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1000, 1000);

        stage.setTitle("Advanced Parallel Sudoku Solver v1.0");
        stage.setResizable(true);
        stage.setMinWidth(500);
        stage.setMinHeight(500);

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}