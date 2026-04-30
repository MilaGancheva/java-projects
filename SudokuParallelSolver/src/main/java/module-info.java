module com.sudokusolver.sudokuparallel {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;

    opens com.sudokusolver.sudokuparallel to javafx.fxml;
    exports com.sudokusolver.sudokuparallel;
}
