import java.util.*;

/**
 * Adversarial Search for the Manuscript Sorting Problem.
 * Implements both Minimax and Alpha-Beta Pruning.
 */
public class AdversarialSearch {

    private static int minimaxNodeCount = 0;
    private static int alphaBetaNodeCount = 0;

    /**
     * Utility = negative Manhattan distance.
     * MAX wants this high (close to 0 = near goal).
     * MIN wants this low (far from goal).
     */
    static int computeUtility(final int[] state, final int[][] goalPositions) {
        return -PuzzleState.h2(state, goalPositions);
    }

    /**
     * Evaluate state using plain Minimax search.
     *
     * @param state          current game state
     * @param remainingDepth remaining depth to search
     * @param isMaxTurn      true if MAX's turn, false if MIN's turn
     * @param visitedStates  visited states to avoid cycles
     * @return minimax value
     */
    static int evaluateWithMinimax(final int[] state, final int[] goal, final int[][] goalPositions,
                                   final int remainingDepth, final boolean isMaxTurn,
                                   final Set<String> visitedStates) {
        minimaxNodeCount++;

        if (remainingDepth == 0 || PuzzleState.isGoal(state, goal)) {
            return computeUtility(state, goalPositions);
        }

        final List<int[]> successors = PuzzleState.getNeighbors(state);
        final int fallbackValue = computeUtility(state, goalPositions);

        if (isMaxTurn) {
            return successors.stream()
                    .map(Arrays::toString)
                    .filter(key -> !visitedStates.contains(key))
                    .mapToInt(key -> {
                        final int[] next = parseState(key);
                        visitedStates.add(key);
                        final int value = evaluateWithMinimax(next, goal, goalPositions,
                                remainingDepth - 1, false, visitedStates);
                        visitedStates.remove(key);
                        return value;
                    })
                    .max()
                    .orElse(fallbackValue);
        } else {
            return successors.stream()
                    .map(Arrays::toString)
                    .filter(key -> !visitedStates.contains(key))
                    .mapToInt(key -> {
                        final int[] next = parseState(key);
                        visitedStates.add(key);
                        final int value = evaluateWithMinimax(next, goal, goalPositions,
                                remainingDepth - 1, true, visitedStates);
                        visitedStates.remove(key);
                        return value;
                    })
                    .min()
                    .orElse(fallbackValue);
        }
    }

    /**
     * Parse a state array from its string representation.
     */
    private static int[] parseState(final String key) {
        return Arrays.stream(key.replaceAll("[\\[\\] ]", "").split(","))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    /**
     * Evaluate state using Minimax with Alpha-Beta pruning.
     *
     * @param alpha best value MAX can guarantee
     * @param beta  best value MIN can guarantee
     * Prunes when beta <= alpha
     */
    static int evaluateWithAlphaBeta(final int[] state, final int[] goal, final int[][] goalPositions,
                                     final int remainingDepth, int alpha, int beta,
                                     final boolean isMaxTurn, final Set<String> visitedStates) {
        alphaBetaNodeCount++;

        if (remainingDepth == 0 || PuzzleState.isGoal(state, goal)) {
            return computeUtility(state, goalPositions);
        }

        final List<int[]> successors = PuzzleState.getNeighbors(state);
        final int fallbackValue = computeUtility(state, goalPositions);

        if (isMaxTurn) {
            int bestScore = Integer.MIN_VALUE;
            for (final int[] next : successors) {
                final String key = Arrays.toString(next);
                if (visitedStates.contains(key)) continue;

                visitedStates.add(key);
                final int value = evaluateWithAlphaBeta(next, goal, goalPositions,
                        remainingDepth - 1, alpha, beta, false, visitedStates);
                visitedStates.remove(key);

                bestScore = Math.max(bestScore, value);
                alpha = Math.max(alpha, bestScore);
                if (beta <= alpha) break;
            }
            return bestScore == Integer.MIN_VALUE ? fallbackValue : bestScore;
        } else {
            int worstScore = Integer.MAX_VALUE;
            for (final int[] next : successors) {
                final String key = Arrays.toString(next);
                if (visitedStates.contains(key)) continue;

                visitedStates.add(key);
                final int value = evaluateWithAlphaBeta(next, goal, goalPositions,
                        remainingDepth - 1, alpha, beta, true, visitedStates);
                visitedStates.remove(key);

                worstScore = Math.min(worstScore, value);
                beta = Math.min(beta, worstScore);
                if (beta <= alpha) break;
            }
            return worstScore == Integer.MAX_VALUE ? fallbackValue : worstScore;
        }
    }

    /**
     * Find the best move for MAX using plain Minimax.
     */
    static int[] findBestMoveMinimax(final int[] initial, final int[] goal,
                                     final int[][] goalPositions, final int searchDepth) {
        minimaxNodeCount = 0;
        final Set<String> visitedStates = new HashSet<>();
        visitedStates.add(Arrays.toString(initial));

        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        String bestAction = "";

        for (final int[] next : PuzzleState.getNeighbors(initial)) {
            final String key = Arrays.toString(next);
            visitedStates.add(key);
            final int value = evaluateWithMinimax(next, goal, goalPositions,
                    searchDepth - 1, false, visitedStates);
            visitedStates.remove(key);

            if (value > bestScore) {
                bestScore = value;
                bestMove = next;
                bestAction = PuzzleState.getAction(initial, next);
            }
        }

        System.out.println("Best move: " + bestAction + " (utility=" + bestScore + ")");
        return bestMove;
    }

    /**
     * Find the best move for MAX using Alpha-Beta pruning.
     */
    static int[] findBestMoveAlphaBeta(final int[] initial, final int[] goal,
                                       final int[][] goalPositions, final int searchDepth) {
        alphaBetaNodeCount = 0;
        final Set<String> visitedStates = new HashSet<>();
        visitedStates.add(Arrays.toString(initial));

        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        String bestAction = "";

        for (final int[] next : PuzzleState.getNeighbors(initial)) {
            final String key = Arrays.toString(next);
            visitedStates.add(key);
            final int value = evaluateWithAlphaBeta(next, goal, goalPositions,
                    searchDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE,
                    false, visitedStates);
            visitedStates.remove(key);

            if (value > bestScore) {
                bestScore = value;
                bestMove = next;
                bestAction = PuzzleState.getAction(initial, next);
            }
        }

        System.out.println("  Best move: " + bestAction + " (utility=" + bestScore + ")");
        return bestMove;
    }

    /**
     * Run both search strategies on a single puzzle and print comparison.
     */
    private static void solveAndCompare(final int[][] input) {
        final int[] initial = input[0];
        final int[] goal = input[1];
        final int[][] goalPositions = PuzzleState.goalPosition(goal);
        final int searchDepth = 6;

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));
        System.out.println("Start Grid:\n" + PuzzleState.stateToGrid(initial));
        System.out.println("Adversarial Search Depth: " + searchDepth);
        System.out.println("Utility function: u(s) = -ManhattanDistance(s)\n");

        System.out.println("--- Plain MINI - MAX ---");
        final long minimaxStart = System.currentTimeMillis();
        final int[] minimaxMove = findBestMoveMinimax(initial, goal, goalPositions, searchDepth);
        final long minimaxElapsed = System.currentTimeMillis() - minimaxStart;
        final int minimaxNodes = minimaxNodeCount;

        System.out.println("  States evaluated: " + minimaxNodes);
        System.out.println("  Time: " + minimaxElapsed + " ms");
        Optional.ofNullable(minimaxMove)
                .ifPresent(move -> {
                    System.out.println("  Resulting state:");
                    System.out.print(PuzzleState.stateToGrid(move));
                });
        System.out.println();

        System.out.println("--- Alpha-Beta Pruning ---");
        final long alphaBetaStart = System.currentTimeMillis();
        final int[] alphaBetaMove = findBestMoveAlphaBeta(initial, goal, goalPositions, searchDepth);
        final long alphaBetaElapsed = System.currentTimeMillis() - alphaBetaStart;

        System.out.println("  States evaluated: " + alphaBetaNodeCount);
        System.out.println("  Time: " + alphaBetaElapsed + " ms");
        Optional.ofNullable(alphaBetaMove)
                .ifPresent(move -> {
                    System.out.println("  Resulting state:");
                    System.out.print(PuzzleState.stateToGrid(move));
                });
        System.out.println();

        printComparison(searchDepth, minimaxNodes, minimaxElapsed,
                alphaBetaNodeCount, alphaBetaElapsed, minimaxMove, alphaBetaMove);
    }

    /**
     * Print side-by-side comparison of Minimax vs Alpha-Beta results.
     */
    private static void printComparison(final int searchDepth,
                                        final int minimaxNodes, final long minimaxElapsed,
                                        final int alphaBetaNodes, final long alphaBetaElapsed,
                                        final int[] minimaxMove, final int[] alphaBetaMove) {
        System.out.println("=".repeat(60));
        System.out.println("Minimax vs Alpha-Beta (depth=" + searchDepth + ")");
        System.out.println("=".repeat(60));
        System.out.println("                     Minimax      Alpha-Beta");
        System.out.println("States evaluated:    " + String.format("%-11d%d", minimaxNodes, alphaBetaNodes));
        System.out.println("Execution time (ms): " + String.format("%-11d%d", minimaxElapsed, alphaBetaElapsed));
        System.out.println("Same best move?     "
                + (Arrays.equals(minimaxMove, alphaBetaMove) ? "YES - pruning is lossless" : "NO -unexpected"));

        if (minimaxNodes > 0) {
            final double savingsPercent = (1.0 - (double) alphaBetaNodes / minimaxNodes) * 100;
            System.out.printf("Pruning saved:      %.1f%% of state evaluations%n", savingsPercent);
        }
        System.out.println("#".repeat(60));
    }

    public static void main(final String[] args) {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";
        final List<int[][]> puzzles = PuzzleState.readInputMultipleLines(inputFile);

        if (puzzles.isEmpty()) return;
        puzzles.forEach(AdversarialSearch::solveAndCompare);
    }
}