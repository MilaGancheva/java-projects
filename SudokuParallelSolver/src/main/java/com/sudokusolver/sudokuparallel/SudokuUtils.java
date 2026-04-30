package com.sudokusolver.sudokuparallel;

import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SudokuUtils {

    public static final LongAdder backtrackCounter = new LongAdder();
    public static final AtomicInteger solutionsCount = new AtomicInteger(0);
    public static final AtomicReference<int[][]> firstSolution = new AtomicReference<>(null);



    public static void resetStats() {

        backtrackCounter.reset();
        solutionsCount.set(0);
        firstSolution.set(null);
    }

// Multithreading algorithm
    public static void solveAllStandardNoLimit(int[][] grid) {

        backtrackCounter.increment();

        int size = grid.length;

        int[] cell = findBestEmptyCell(grid);

        if (cell == null) {

            processSolution(grid);

            return;

        }

        int r = cell[0], c = cell[1];
        int mask = getUsedMask(grid, r, c);

        int allChoices = ((1 << size) - 1) & ~mask;

        for (int num = 1; num <= size; num++) {

            if ((allChoices & (1 << (num - 1))) != 0) {

                grid[r][c] = num;
                solveAllStandardNoLimit(grid);
                grid[r][c] = 0;

                if (solutionsCount.get() >= 20000) return;
            }
        }
    }

// Standard algorithm

    public static void solveAllStandard(int[][] grid) {

        backtrackCounter.increment();
        int size = grid.length;
        int[] cell = findBestEmptyCell(grid);

        if (cell == null) {
            processSolution(grid);
            return;
        }

        int r = cell[0], c = cell[1];
        int mask = getUsedMask(grid, r, c);

        for (int num = 1; num <= size; num++) {

            if ((mask & (1 << (num - 1))) == 0) {

                grid[r][c] = num;

                solveAllStandard(grid);

                grid[r][c] = 0;

                if (solutionsCount.get() >= 20000) return;

            }

        }

    }

    public static void processSolution(int[][] grid) {

        solutionsCount.incrementAndGet();
        if (firstSolution.get() == null) {
            firstSolution.compareAndSet(null, copyGrid(grid));
        }
    }

    public static int[] findBestEmptyCell(int[][] grid) {
        int size = grid.length;
        int[] best = null;
        int minChoices = size + 1;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (grid[r][c] == 0) {
                    int mask = getUsedMask(grid, r, c);
                    int choicesCount = Integer.bitCount(((1 << size) - 1) & ~mask);

                    if (choicesCount < minChoices) {
                        minChoices = choicesCount;
                        best = new int[]{r, c};
                        if (minChoices == 1) return best;

                    }
                }
            }
        }

        return best;
    }


    public static int getUsedMask(int[][] grid, int r, int c) {

        int size = grid.length;
        int blockSize = (int) Math.sqrt(size);
        int mask = 0;

        for (int i = 0; i < size; i++) {
            if (grid[r][i] > 0) mask |= (1 << (grid[r][i] - 1));
            if (grid[i][c] > 0) mask |= (1 << (grid[i][c] - 1));
        }

        int br = r - r % blockSize, bc = c - c % blockSize;

        for (int i = 0; i < blockSize; i++) {

            for (int j = 0; j < blockSize; j++) {

                if (grid[br + i][bc + j] > 0) mask |= (1 << (grid[br + i][bc + j] - 1));
            }
        }

        return mask;
    }

    public static boolean isValidBoard(int[][] grid) {
        int size = grid.length;

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int num = grid[r][c];
                if (num != 0) {
                    grid[r][c] = 0;

                    if (!isSafe(grid, r, c, num)) {
                        grid[r][c] = num;
                        return false;
                    }

                    grid[r][c] = num;
                }
            }
        }
        return true;
    }

    public static boolean isSafe(int[][] grid, int row, int col, int num) {

        int size = grid.length;
        int blockSize = (int) Math.sqrt(size);

        for (int i = 0; i < size; i++) {
            if (grid[row][i] == num || grid[i][col] == num) return false;
        }

        int startRow = row - row % blockSize, startCol = col - col % blockSize;

        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                if (grid[i + startRow][j + startCol] == num) return false;
            }
        }

        return true;
    }

    public static int[][] copyGrid(int[][] original) {
        int size = original.length;
        int[][] copy = new int[size][size];
        for (int i = 0; i < size; i++) System.arraycopy(original[i], 0, copy[i], 0, size);

        return copy;
    }

    public static final int[][] EXAMPLE_4x4 = {

            {1, 0, 0, 4},
            {0, 0, 2, 0},
            {0, 3, 0, 0},
            {2, 0, 0, 1}

    };

    public static final int[][] HARDEST_SUDOKU = {

            {8, 0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 3, 6, 0, 0, 0, 0, 0},
            {0, 7, 0, 0, 9, 0, 2, 0, 0}, {0, 5, 0, 0, 0, 7, 0, 0, 0},
            {0, 0, 0, 0, 4, 5, 7, 0, 0}, {0, 0, 0, 1, 0, 0, 0, 3, 0},
            {0, 0, 1, 0, 0, 0, 0, 6, 8}, {0, 0, 8, 5, 0, 0, 0, 1, 0},
            {0, 9, 0, 0, 0, 0, 4, 0, 0}

    };

    public static final int[][] EXAMPLE_16x16 = {

            {1, 2, 0, 0, 0, 6, 0, 0, 0, 0, 11, 0, 0, 0, 15, 16},
            {0, 0, 7, 0, 0, 0, 0, 12, 13, 0, 0, 0, 0, 2, 0, 0},
            {0, 10, 0, 0, 15, 0, 0, 0, 0, 0, 0, 4, 0, 0, 9, 0},
            {0, 0, 0, 13, 0, 0, 3, 0, 0, 14, 0, 0, 12, 0, 0, 0},

            {0, 0, 14, 0, 0, 0, 12, 0, 0, 3, 0, 0, 0, 1, 0, 0},
            {0, 1, 0, 0, 13, 0, 0, 0, 0, 0, 0, 15, 0, 0, 14, 0},
            {0, 0, 0, 4, 0, 0, 0, 10, 11, 0, 0, 0, 16, 0, 0, 0},
            {15, 0, 0, 0, 0, 2, 0, 0, 0, 0, 5, 0, 0, 0, 0, 3},

            {2, 0, 0, 0, 0, 15, 0, 0, 0, 0, 12, 0, 0, 0, 0, 7},
            {0, 0, 0, 12, 0, 0, 0, 3, 4, 0, 0, 0, 11, 0, 0, 0},
            {0, 6, 0, 0, 1, 0, 0, 0, 0, 0, 0, 10, 0, 0, 5, 0},
            {0, 0, 3, 0, 0, 0, 4, 0, 0, 13, 0, 0, 0, 9, 0, 0},

            {0, 0, 0, 15, 0, 0, 10, 0, 0, 6, 0, 0, 1, 0, 0, 0},
            {0, 11, 0, 0, 2, 0, 0, 0, 0, 0, 0, 14, 0, 0, 13, 0},
            {0, 0, 9, 0, 0, 0, 0, 7, 8, 0, 0, 0, 0, 15, 0, 0},
            {16, 14, 0, 0, 0, 5, 0, 0, 0, 0, 1, 0, 0, 0, 6, 12}
    };
}