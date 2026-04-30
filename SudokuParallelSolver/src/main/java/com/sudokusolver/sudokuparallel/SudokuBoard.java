package com.sudokusolver.sudokuparallel;

public class SudokuBoard {
    private final int[][] grid;
    private final int moves;

    public SudokuBoard(int[][] grid, int moves) {
        int size = grid.length;
        this.grid = new int[size][size];
        for (int i = 0; i < size; i++) {
            System.arraycopy(grid[i], 0, this.grid[i], 0, size);
        }
        this.moves = moves;
    }

    public int[][] getGrid() { return grid; }
    public int getMoves() { return moves; }
}
