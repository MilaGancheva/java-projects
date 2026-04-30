package com.sudokusolver.sudokuparallel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class ParallelSolverTask extends RecursiveAction {
    private final int[][] board;
    private final int depth;

    public ParallelSolverTask(int[][] board, int depth) {
        this.board = board;
        this.depth = depth;
    }

    @Override
    protected void compute() {

        if (SudokuUtils.solutionsCount.get() >= 5000) return;

        if (depth > 0) {
            SudokuUtils.solveAllStandardNoLimit(board);
            return;
        }

        int[] cell = SudokuUtils.findBestEmptyCell(board);
        if (cell == null) {
            SudokuUtils.processSolution(board);
            return;
        }

        int r = cell[0], c = cell[1];
        int size = board.length;
        int mask = SudokuUtils.getUsedMask(board, r, c);

        int allChoices = ((1 << size) - 1) & ~mask;

        List<ParallelSolverTask> subTasks = new ArrayList<>();

        for (int num = 1; num <= size; num++) {
            if ((allChoices & (1 << (num - 1))) != 0) {
                int[][] nextGrid = SudokuUtils.copyGrid(board);
                nextGrid[r][c] = num;
                subTasks.add(new ParallelSolverTask(nextGrid, depth + 1));
            }
        }

        if (!subTasks.isEmpty()) {
            invokeAll(subTasks);
        }
    }
}
