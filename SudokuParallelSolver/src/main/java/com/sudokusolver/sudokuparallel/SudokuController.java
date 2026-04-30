package com.sudokusolver.sudokuparallel;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class SudokuController {
    @FXML
    private GridPane sudokuGrid;
    @FXML
    private Label statusLabel;
    @FXML
    private Button solveBtn;
    @FXML
    private ChoiceBox<String> modeChoiceBox;
    @FXML
    private ChoiceBox<String> algoChoiceBox;
    @FXML
    private Label timerLabel;
    @FXML
    private Label backtrackLabel;
    @FXML
    private Label solutionsLabel;

    private int currentSize = 9;
    private TextField[][] textFields;


    public void initialize() {
        modeChoiceBox.getItems().addAll("4x4 (2x2 blocks)", "9x9 (3x3 blocks)", "16x16 (4x4 blocks)");
        modeChoiceBox.setValue("9x9 (3x3 blocks)");

        algoChoiceBox.getItems().addAll("Multithreaded", "Standard (Single)");
        algoChoiceBox.setValue("Multithreaded");

        modeChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.startsWith("4x4")) currentSize = 4;
            else if (newVal.startsWith("9x9")) currentSize = 9;
            else currentSize = 16;
            setupGrid();
        });

        setupGrid();
        loadExample();
    }
    private String getCellStyle(int r, int c, int blockSize, String fontSize, String textColor, boolean isBold) {
        String top = "1";
        String left = "1";
        String bottom = (r % blockSize == blockSize - 1 && r < currentSize - 1) ? "3" : "1";
        String right = (c % blockSize == blockSize - 1 && c < currentSize - 1) ? "3" : "1";

        return String.format(
                "-fx-alignment: center; " +
                        "-fx-font-size: %s; " +
                        "-fx-text-fill: %s; " +
                        "-fx-font-weight: %s; " +
                        "-fx-border-color: #7f8c8d; " +
                        "-fx-border-width: %s %s %s %s; " +
                        "-fx-background-radius: 0; " +
                        "-fx-background-color: white; " +
                        "-fx-padding: 0;",
                fontSize, textColor, (isBold ? "bold" : "normal"), top, right, bottom, left
        );
    }

    private void setupGrid() {
        sudokuGrid.getChildren().clear();
        sudokuGrid.setHgap(0);
        sudokuGrid.setVgap(0);

        textFields = new TextField[currentSize][currentSize];
        int blockSize = (int) Math.sqrt(currentSize);

        // Динамични размери
        double cellSize;
        String fontSize;
        if (currentSize == 4) {
            cellSize = 80;
            fontSize = "24px";
        } else if (currentSize == 16) {
            cellSize = 40;
            fontSize = "14px";
        } else {
            cellSize = 50;
            fontSize = "16px";
        }

        for (int r = 0; r < currentSize; r++) {
            for (int c = 0; c < currentSize; c++) {
                TextField tf = new TextField();
                tf.setPrefSize(cellSize, cellSize);
                tf.setMinSize(cellSize, cellSize);
                tf.setMaxSize(cellSize, cellSize);

                tf.setStyle(getCellStyle(r, c, blockSize, fontSize, "#2c3e50", false));

                addValidation(tf);
                textFields[r][c] = tf;
                sudokuGrid.add(tf, c, r);
            }
        }
    }


    private void loadExample() {
        int[][] example;
        String status;

        switch (currentSize) {
            case 4:
                example = SudokuUtils.EXAMPLE_4x4;
                status = "Loaded: 4x4 Junior Sudoku";
                break;
            case 16:
                example = SudokuUtils.EXAMPLE_16x16;
                status = "Loaded: 16x16 Example Hexadoku";
                break;
            case 9:
            default:
                example = SudokuUtils.HARDEST_SUDOKU;
                status = "Loaded: Inkala's 'Hardest Sudoku'";
                break;
        }

        statusLabel.setText(status);

        for (int r = 0; r < currentSize; r++) {
            for (int c = 0; c < currentSize; c++) {
                int val = example[r][c];
                if (val != 0) {
                    textFields[r][c].setText(String.valueOf(val));
                    textFields[r][c].setStyle(textFields[r][c].getStyle() +
                            "-fx-font-weight: bold; -fx-text-fill: #e74c3c;");
                } else {
                    textFields[r][c].setText("");
                }
            }
        }
    }

    @FXML
    private void handleGenerate() {
        handleClear(); // Първо изчистваме всичко

        int[][] newPuzzle = SudokuGenerator.generate(currentSize);

        for (int r = 0; r < currentSize; r++) {
            for (int c = 0; c < currentSize; c++) {
                if (newPuzzle[r][c] != 0) {
                    textFields[r][c].setText(String.valueOf(newPuzzle[r][c]));
                    // Маркираме генерираните числа с различен цвят
                    textFields[r][c].setStyle(textFields[r][c].getStyle() + "-fx-text-fill: #2c3e50; -fx-font-weight: bold;");
                } else {
                    textFields[r][c].setText("");
                }
            }
        }
        statusLabel.setText("New " + currentSize + "x" + currentSize + " puzzle generated!");
    }

    @FXML
    private void handleSolve() {
        int[][] inputBoard = getBoardFromUI();
        if (!SudokuUtils.isValidBoard(inputBoard)) {
            statusLabel.setText("Invalid initial board!");
            return;
        }

        solveBtn.setDisable(true);
        SudokuUtils.resetStats();
        String selectedAlgo = algoChoiceBox.getValue();
        statusLabel.setText("Analyzing all solutions...");

        Task<int[][]> solveTask = new Task<>() {
            @Override
            protected int[][] call() {
                long startTime = System.nanoTime();

                if ("Multithreaded".equals(selectedAlgo)) {
                    java.util.concurrent.ForkJoinPool pool = java.util.concurrent.ForkJoinPool.commonPool();
                    pool.invoke(new ParallelSolverTask(inputBoard, 0));
                } else {
                    SudokuUtils.solveAllStandard(SudokuUtils.copyGrid(inputBoard));
                }

                long endTime = System.nanoTime();
                double duration = (endTime - startTime) / 1_000_000_000.0;

                updateMessage(String.format("%.4f", duration) + "|" +
                        SudokuUtils.backtrackCounter.sum() + "|" +
                        SudokuUtils.solutionsCount.get());

                return SudokuUtils.firstSolution.get();
            }
        };

        solveTask.setOnSucceeded(e -> {
            int[][] firstResult = solveTask.getValue();
            String[] res = solveTask.getMessage().split("\\|");

            timerLabel.setText("Time: " + res[0] + " s");
            backtrackLabel.setText("Backtracks: " + res[1]);
            solutionsLabel.setText("Solutions: " + res[2]);

            if (firstResult != null) {
                displaySolution(firstResult);
                statusLabel.setText("Analysis finished!");
            } else {
                statusLabel.setText("No solution exists.");
            }
            solveBtn.setDisable(false);
        });

        new Thread(solveTask).start();
    }


    private int[][] getBoardFromUI() {
        int[][] b = new int[currentSize][currentSize];
        for (int r = 0; r < currentSize; r++) {
            for (int c = 0; c < currentSize; c++) {
                String val = textFields[r][c].getText().trim();
                if (val.isEmpty()) {
                    b[r][c] = 0;
                } else {
                    try {
                        int num = Integer.parseInt(val);
                        if (num >= 1 && num <= currentSize) {
                            b[r][c] = num;
                        } else {
                            b[r][c] = 0;
                        }
                    } catch (NumberFormatException e) {
                        b[r][c] = 0;
                    }
                }
            }
        }
        return b;
    }

    private void displaySolution(int[][] grid) {
        String fontSize = (currentSize == 16) ? "14px" : "16px";
        for (int r = 0; r < currentSize; r++) {
            for (int c = 0; c < currentSize; c++) {
                if (textFields[r][c].getText().isEmpty()) {
                    textFields[r][c].setText(String.valueOf(grid[r][c]));
                    textFields[r][c].setStyle(textFields[r][c].getStyle() +
                            "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: " + fontSize + ";");
                }
            }
        }
    }

    @FXML
    private void handleClear() {
        for (int r = 0; r < currentSize; r++) {
            for (int c = 0; c < currentSize; c++) {
                textFields[r][c].setText("");
                int blockSize = (int) Math.sqrt(currentSize);
                int right = (c % blockSize == blockSize - 1 && c < currentSize - 1) ? 3 : 1;
                int bottom = (r % blockSize == blockSize - 1 && r < currentSize - 1) ? 3 : 1;
                textFields[r][c].setStyle("-fx-alignment: center; -fx-font-size: 16px; -fx-border-color: #bdc3c7; " +
                        "-fx-border-width: 1 " + right + " " + bottom + " 1;");
            }
        }

        SudokuUtils.resetStats();
        statusLabel.setText("Grid cleared. Ready for new input.");
        timerLabel.setText("Time: 0.0000 s");
        backtrackLabel.setText("Backtracks: 0");
        solutionsLabel.setText("Solutions: 0");
    }

    private void addValidation(TextField tf) {
        tf.textProperty().addListener((observable, oldValue, newValue) -> {

            if (!newValue.matches("\\d*")) {
                tf.setText(newValue.replaceAll("[^\\d]", ""));
                return;
            }

            int maxLen = currentSize == 16 ? 2 : 1;
            if (tf.getText().length() > maxLen) {
                tf.setText(tf.getText().substring(0, maxLen));
                return;
            }

            if (!tf.getText().isEmpty()) {
                int val = Integer.parseInt(tf.getText());
                if (val == 0 || val > currentSize) {
                    tf.setText("");
                }
            }
        });
    }
}
