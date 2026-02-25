import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;

/**
 * A* Search for the Manuscript Sorting Problem.
 */
public class AStarSearch {

    /**
     * Represents a search node in the A* frontier.
     */
    static final class SearchNode implements Comparable<SearchNode> {
        final int[] state;
        final int pathCost;
        final int heuristicValue;
        final int totalEstimatedCost;
        final String stateKey;

        SearchNode(final int[] state, final int pathCost, final int heuristicValue) {
            this.state = state;
            this.pathCost = pathCost;
            this.heuristicValue = heuristicValue;
            this.totalEstimatedCost = pathCost + heuristicValue;
            this.stateKey = Arrays.toString(state);
        }

        @Override
        public int compareTo(final SearchNode other) {
            final int costComparison = Integer.compare(this.totalEstimatedCost, other.totalEstimatedCost);
            return costComparison != 0
                    ? costComparison
                    : Integer.compare(this.heuristicValue, other.heuristicValue);
        }
    }

    /**
     * Compute heuristic value for a state using the selected strategy.
     */
    private static int computeHeuristic(final int[] state, final int[] goal,
                                        final int[][] goalPositions, final boolean useMisplacedTiles) {
        return useMisplacedTiles
                ? PuzzleState.h1(state, goal)
                : PuzzleState.h2(state, goalPositions);
    }

    /**
     * Solve the puzzle using A* search with the specified heuristic.
     *
     * @param useMisplacedTiles true = h1 (misplaced tiles), false = h2 (Manhattan distance)
     */
    static void solve(final int[] initial, final int[] goal, final int[][] goalPositions,
                      final boolean useMisplacedTiles, final boolean printTrace) {
        final String heuristicName = useMisplacedTiles ? "h1 - Misplaced Tiles" : "h2 - Manhattan Distance";
        final long startTime = System.currentTimeMillis();
        int nodesExplored = 0;
        boolean solved = false;
        List<int[]> solutionPath = null;

        final PriorityQueue<SearchNode> frontier = new PriorityQueue<>();
        final Map<String, Integer> lowestCostTo = new HashMap<>();
        final Map<String, String> parentOf = new HashMap<>();
        final Map<String, int[]> stateByKey = new HashMap<>();

        final String initialKey = Arrays.toString(initial);
        final int initialHeuristic = computeHeuristic(initial, goal, goalPositions, useMisplacedTiles);
        frontier.add(new SearchNode(initial, 0, initialHeuristic));
        lowestCostTo.put(initialKey, 0);
        parentOf.put(initialKey, null);
        stateByKey.put(initialKey, initial);

        while (!frontier.isEmpty()) {
            final SearchNode current = frontier.poll();
            nodesExplored++;

            if (PuzzleState.isGoal(current.state, goal)) {
                solved = true;
                solutionPath = PuzzleState.reconstructPath(parentOf, stateByKey, current.state);
                break;
            }

            // Skip if a cheaper path to this state was already found
            if (current.pathCost > lowestCostTo.getOrDefault(current.stateKey, Integer.MAX_VALUE)) {
                continue;
            }

            for (final int[] successor : PuzzleState.getNeighbors(current.state)) {
                final String successorKey = Arrays.toString(successor);
                final int newCost = current.pathCost + 1;

                if (newCost < lowestCostTo.getOrDefault(successorKey, Integer.MAX_VALUE)) {
                    lowestCostTo.put(successorKey, newCost);
                    parentOf.put(successorKey, current.stateKey);
                    stateByKey.put(successorKey, successor);
                    final int successorHeuristic = computeHeuristic(successor, goal, goalPositions, useMisplacedTiles);
                    frontier.add(new SearchNode(successor, newCost, successorHeuristic));
                }
            }
        }

        final long timeTakenInMs = System.currentTimeMillis() - startTime;
        PuzzleState.printResult(heuristicName, solved, solutionPath, nodesExplored, timeTakenInMs, printTrace);
    }

    /**
     * Solve a single puzzle with both heuristics and print results.
     */
    private static void solveWithBothHeuristics(final int[][] puzzle, final boolean printTrace) {
        final int[] initial = puzzle[0];
        final int[] goal = puzzle[1];
        final int[][] goalPositions = PuzzleState.goalPosition(goal);

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));

        solve(initial, goal, goalPositions, true, printTrace);
        solve(initial, goal, goalPositions, false, printTrace);
    }

    public static void main(final String[] args) {
        final String inputFile = args.length > 0 ? args[0] : "resource/input.txt";
        final List<int[][]> puzzles = PuzzleState.readInputMultipleLines(inputFile);

        final boolean printTrace= args.length > 1 && ("y".equalsIgnoreCase(args[1]) || "yes".equalsIgnoreCase(args[1]));
        if (puzzles.isEmpty()) return;


        System.out.println("============================================\n A* Search \n============================================");
        AtomicInteger i= new AtomicInteger(1);

        puzzles.forEach(puzzle -> {
            System.out.println("######################### Start of Test Case - "+i +" #########################");
            i.getAndIncrement();
            solveWithBothHeuristics(puzzle, printTrace);
            System.out.println("######################### END #########################");
        });
    }
}