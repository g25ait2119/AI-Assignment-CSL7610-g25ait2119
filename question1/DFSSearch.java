import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Depth-First Search (DFS) for the Manuscript Sorting Problem.
 */
public class DFSSearch {

    private static final int MAX_DEPTH = 50;

    public static void main(final String[] args) {
        final String inputFile = args.length > 0 ? args[0] : "resource/input.txt";
        final List<int[][]> puzzles = PuzzleState.readInputMultipleLines(inputFile);

        if (puzzles.isEmpty()) return;

        final boolean printTrace= args.length > 1 && ("y".equalsIgnoreCase(args[1]) || "yes".equalsIgnoreCase(args[1]));
        System.out.println("============================================\n Depth-First Search (DFS) \n============================================");

        AtomicInteger i= new AtomicInteger(1);
        puzzles.forEach(puzzle -> {
            System.out.println("######################### Start of Test Case - "+i +" #########################");
            i.getAndIncrement();
            solveAndPrint(puzzle, printTrace);
            System.out.println("######################### END #########################");
        });
    }

    /**
     * Solve a single puzzle using depth-limited DFS and print the results.
     */
    private static void solveAndPrint(final int[][] puzzle, final boolean printTrace) {
        final int[] initial = puzzle[0];
        final int[] goal = puzzle[1];

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));

        final long startTime = System.currentTimeMillis();
        int nodesExplored = 0;
        boolean solved = false;
        List<int[]> solutionPath = null;

        final Deque<int[]> frontier = new ArrayDeque<>();
        final Deque<Integer> depthTracker = new ArrayDeque<>();
        final Map<String, String> parentOf = new HashMap<>();
        final Map<String, int[]> stateByKey = new HashMap<>();
        final Set<String> seen = new HashSet<>();

        final String initialKey = Arrays.toString(initial);
        frontier.push(initial);
        depthTracker.push(0);
        parentOf.put(initialKey, null);
        stateByKey.put(initialKey, initial);

        while (!frontier.isEmpty()) {
            final int[] current = frontier.pop();
            final int currentDepth = depthTracker.pop();
            final String currentKey = Arrays.toString(current);

            if (seen.contains(currentKey)) continue;
            seen.add(currentKey);
            nodesExplored++;

            if (Arrays.equals(current, goal)) {
                solved = true;
                solutionPath = PuzzleState.reconstructPath(parentOf, stateByKey, current);
                break;
            }

            if (currentDepth >= MAX_DEPTH) continue;

            for (final int[] successor : PuzzleState.getNeighbors(current)) {
                final String successorKey = Arrays.toString(successor);
                if (!seen.contains(successorKey)) {
                    parentOf.put(successorKey, currentKey);
                    stateByKey.put(successorKey, successor);
                    frontier.push(successor);
                    depthTracker.push(currentDepth + 1);
                }
            }
        }

        final long timeTakenInMs = System.currentTimeMillis() - startTime;

        PuzzleState.printResult(
                "Depth Limit = " + MAX_DEPTH,
                solved, solutionPath, nodesExplored, timeTakenInMs, printTrace);
    }
}