import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;

/**
 * Simulated Annealing for the Manuscript Sorting Problem.
 */
public class SimulatedAnnealingSearch {

    private static final double STARTING_TEMPERATURE = 1000.0;
    private static final double COOLING_FACTOR = 0.9995;
    private static final double TEMPERATURE_FLOOR = 0.001;
    private static final int ITERATION_LIMIT = 500000;
    private static final int PROGRESS_INTERVAL = 100000;
    private static final long RANDOM_SEED = 42;

    public static void main(final String[] args) {
        final String inputFile = args.length > 0 ? args[0] : "resource/input.txt";
        final List<int[][]> puzzles = PuzzleState.readInputMultipleLines(inputFile);

        if (puzzles.isEmpty()) return;
        System.out.println("============================================\n Simulated Annealing \n============================================");
        final boolean printTrace= args.length > 1 && ("y".equalsIgnoreCase(args[1]) || "yes".equalsIgnoreCase(args[1]));

        AtomicInteger i= new AtomicInteger(1);
        puzzles.forEach(puzzle -> {
            System.out.println("######################### Start of Test Case - "+i +" #########################");
            i.getAndIncrement();
            solveAndPrint(puzzle, printTrace);
            System.out.println("######################### END #########################");
        });
    }

    /**
     * Solve a single puzzle using Simulated Annealing and print the results.
     */
    private static void solveAndPrint(final int[][] puzzle, final boolean printTrace) {
        final int[] initial = puzzle[0];
        final int[] goal = puzzle[1];
        final int[][] goalPositions = PuzzleState.goalPosition(goal);

        System.out.println("Start State: " + PuzzleState.stateToString(initial));
        System.out.println("Goal  State: " + PuzzleState.stateToString(goal));

        printCoolingSchedule();

        final long startTime = System.currentTimeMillis();
        final Random rng = new Random(RANDOM_SEED);
        int nodesExplored = 0;
        boolean solved = false;

        double temperature = STARTING_TEMPERATURE;
        int[] current = initial.clone();
        int currentDistance = PuzzleState.h2(current, goalPositions);

        int[] closestState = current.clone();
        int closestDistance = currentDistance;

        final List<int[]> solutionPath = new ArrayList<>();
        solutionPath.add(initial.clone());

        for (int iteration = 0; iteration < ITERATION_LIMIT && temperature > TEMPERATURE_FLOOR; iteration++) {
            nodesExplored++;

            if (currentDistance == 0) {
                solved = true;
                break;
            }

            final List<int[]> successors = PuzzleState.getNeighbors(current);
            final int[] candidate = successors.get(rng.nextInt(successors.size()));
            final int candidateDistance = PuzzleState.h2(candidate, goalPositions);
            final int costDifference = candidateDistance - currentDistance;

            if (shouldAccept(costDifference, temperature, rng)) {
                current = candidate;
                currentDistance = candidateDistance;
                solutionPath.add(current.clone());

                if (currentDistance < closestDistance) {
                    closestDistance = currentDistance;
                    closestState = current.clone();
                }
            }

            temperature *= COOLING_FACTOR;

            if ((iteration + 1) % PROGRESS_INTERVAL == 0) {
                System.out.printf("  Iteration %d: T=%.4f, current h2=%d, best h2=%d%n",
                        iteration + 1, temperature, currentDistance, closestDistance);
            }
        }

        final long timeTakenInMS = System.currentTimeMillis() - startTime;

        System.out.println();
        PuzzleState.printResult("h2 - Manhattan Distance",
                solved, solved ? solutionPath : null, nodesExplored, timeTakenInMS, printTrace);

        System.out.printf("Final Temperature: %.6f%n", temperature);
        System.out.println("Best h2 achieved : " + closestDistance);
        if (!solved) {
            System.out.println("Best state found (not goal):");
            System.out.print(PuzzleState.stateToGrid(closestState));
        }
        System.out.println();
    }

    /**
     * Decide whether to accept a candidate state based on the Metropolis criterion.
     * Always accepts improvements; accepts worse states with probability e^(-delta/T).
     */
    private static boolean shouldAccept(final int costDifference, final double temperature,
                                        final Random rng) {
        return costDifference < 0 || rng.nextDouble() < Math.exp(-costDifference / temperature);
    }

    /**
     * Print the cooling schedule parameters.
     */
    private static void printCoolingSchedule() {
        System.out.println("Cooling Schedule:");
        System.out.println("  T0           = " + STARTING_TEMPERATURE);
        System.out.println("  Cooling Rate = " + COOLING_FACTOR);
        System.out.println("  Temperature minimum       = " + TEMPERATURE_FLOOR);
        System.out.println("  Iteration limit     = " + ITERATION_LIMIT);
    }
}