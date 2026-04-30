package com.sudokusolver.sudokuparallel;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;

public class SudokuGenerator {
    private static final Random random = new Random();

    public static int[][] generate(int size) {
        int[][] board = new int[size][size];

        int blockSize = (int) Math.sqrt(size);
        for (int i = 0; i < size; i += blockSize) {
            fillBlock(board, i, i, blockSize, size);
        }

        // 2. Използваме ForkJoin за намиране на ПЪРВОТО решение

        SudokuUtils.resetStats();
        ForkJoinPool.commonPool().invoke(new ParallelSolverTask(board, 0));

        int[][] fullSolution = SudokuUtils.firstSolution.get();

        if (fullSolution == null) return generate(size);

        int cellsToRemove = (size == 4) ? 8 : 45;
        return removeNumbers(fullSolution, cellsToRemove);
    }

    private static void fillBlock(int[][] board, int row, int col, int blockSize, int size) {
        int num;
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                do {
                    num = random.nextInt(size) + 1;
                } while (!isUnusedInBlock(board, row, col, num, blockSize));
                board[row + i][col + j] = num;
            }
        }
    }

    private static boolean isUnusedInBlock(int[][] board, int row, int col, int num, int blockSize) {
        for (int i = 0; i < blockSize; i++) {
            for (int j = 0; j < blockSize; j++) {
                if (board[row + i][col + j] == num) return false;
            }
        }
        return true;
    }

    private static int[][] removeNumbers(int[][] grid, int count) {
        int size = grid.length;
        int[][] puzzle = SudokuUtils.copyGrid(grid);
        int removed = 0;
        while (removed < count) {
            int r = random.nextInt(size);
            int c = random.nextInt(size);
            if (puzzle[r][c] != 0) {
                puzzle[r][c] = 0;
                removed++;
            }
        }
        return puzzle;
    }
}
