import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Greedy Best-First Search for the Manuscript Sorting Problem.
 */
public class GreedyBestFirstSearch {

    /**
     * Represents a search node ranked solely by heuristic value.
     */
    static final class SearchNode implements Comparable<SearchNode> {
        final int[] state;
        final int heuristicValue;
        final String stateKey;

        SearchNode(final int[] state, final int heuristicValue) {
            this.state = state;
            this.heuristicValue = heuristicValue;
            this.stateKey = Arrays.toString(state);
        }

        @Override
        public int compareTo(final SearchNode other) {
            return Integer.compare(this.heuristicValue, other.heuristicValue);
        }
    }

    public static void main(final String[] args) {
        final String inputFile = args.length > 0 ? args[0] : "resource/input.txt";
        final List<int[][]> puzzles = PuzzleState.readInputMultipleLines(inputFile);

        if (puzzles.isEmpty()) return;

        final boolean printTrace= args.length > 1 && ("y".equalsIgnoreCase(args[1]) || "yes".equalsIgnoreCase(args[1]));

        System.out.println("============================================\n Greedy Best-First Search \n============================================");
        AtomicInteger i= new AtomicInteger(1);
        puzzles.forEach(puzzle -> {
            System.out.println("######################### Start of Test Case - "+i +" #########################");
            i.getAndIncrement();
            solveAndPrint(puzzle, printTrace);
            System.out.println("######################### END #########################");
        });
    }

    /**
     * Solve a single puzzle using Greedy Best-First Search and print the results.
     */
    private static void solveAndPrint(final int[][] puzzle,  final boolean printTrace) {
        final int[] initial = puzzle[0];
        final int[] goal = puzzle[1];
        final int[][] goalPositions = PuzzleState.goalPosition(goal);

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));

        final long startTime = System.currentTimeMillis();
        int nodesExplored = 0;
        boolean solved = false;
        List<int[]> solutionPath = null;

        final PriorityQueue<SearchNode> frontier = new PriorityQueue<>();
        final Map<String, String> parentOf = new HashMap<>();
        final Map<String, int[]> stateByKey = new HashMap<>();
        final Set<String> seen = new HashSet<>();

        final String initialKey = Arrays.toString(initial);
        frontier.add(new SearchNode(initial, PuzzleState.h2(initial, goalPositions)));
        parentOf.put(initialKey, null);
        stateByKey.put(initialKey, initial);

        while (!frontier.isEmpty()) {
            final SearchNode current = frontier.poll();

            if (seen.contains(current.stateKey)) continue;
            seen.add(current.stateKey);
            nodesExplored++;

            if (PuzzleState.isGoal(current.state, goal)) {
                solved = true;
                solutionPath = PuzzleState.reconstructPath(parentOf, stateByKey, current.state);
                break;
            }

            for (final int[] successor : PuzzleState.getNeighbors(current.state)) {
                final String successorKey = Arrays.toString(successor);
                if (!seen.contains(successorKey)) {
                    if (!parentOf.containsKey(successorKey)) {
                        parentOf.put(successorKey, current.stateKey);
                        stateByKey.put(successorKey, successor);
                    }
                    frontier.add(new SearchNode(successor, PuzzleState.h2(successor, goalPositions)));
                }
            }
        }


        final long timeTakenInMS = System.currentTimeMillis() - startTime;

        PuzzleState.printResult(
                "h2 - Manhattan Distance",
                solved, solutionPath, nodesExplored, timeTakenInMS, printTrace);
    }
}