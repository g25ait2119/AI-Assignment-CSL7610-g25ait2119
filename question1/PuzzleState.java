import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.*;
import java.io.*;

/**
 * PuzzleState - Shared utility class for the Manuscript Sorting Problem.
 * Provides state representation, neighbor generation, heuristics, and I/O.
 */
public class PuzzleState {

    public static final int SIZE = 3;
    public static final int TILE_COUNT = SIZE * SIZE;
    public static final int BLANK = 0;

    // Direction vectors: Up, Down, Left, Right
    public static final int[] DR = {-1, 1, 0, 0};
    public static final int[] DC = {0, 0, -1, 1};
    public static final String[] DIR_NAMES = {"Up", "Down", "Left", "Right"};

    /**
     * Build a lookup table mapping each tile value to its (row, col) in the goal state.
     */
    public static int[][] goalPosition(final int[] goal) {
        final int[][] positions = new int[TILE_COUNT][2];
        for (int i = 0; i < TILE_COUNT; i++) {
            final int tile = goal[i];
            positions[tile] = new int[]{i / SIZE, i % SIZE};
        }
        return positions;
    }

    /**
     * Parse state string like "123;B46;758" or "123 456 78B" into int array.
     * 'B' or '0' represents the blank.
     */
    public static int[] parseState(final String raw) {
        final String cleaned = raw.replace(";", "").replace("B", "0").replace(" ", "");
        return cleaned.chars()
                .map(ch -> ch - '0')
                .toArray();
    }

    /**
     * Read initial and goal states from input file.
     * Returns int[2][9]: [0] = initial, [1] = goal.
     */
    public static int[][] readInput(final String filepath) throws Exception {
        try (final Scanner reader = new Scanner(new File(filepath))) {
            final String startLine = reader.nextLine().trim();
            final String goalLine = reader.hasNextLine() ? reader.nextLine().trim() : "123 456 78B";
            return new int[][]{parseState(startLine), parseState(goalLine)};
        }
    }

    /**
     * Read multiple puzzle pairs from a file (every 2 non-empty lines form one puzzle).
     * Validates that line count is even.
     */
    public static List<int[][]> readInputMultipleLines(final String filepath) {
        final List<String> lines = readNonEmptyLines(filepath);

        if (lines.isEmpty()) {
            return Collections.emptyList();
        }

        if (lines.size() % 2 != 0) {
            System.out.println("Exception: Input file is incomplete. Please check the goal state or provided input.txt file");
            System.out.println("Total lines found: " + lines.size());
            return Collections.emptyList();
        }

        return IntStream.range(0, lines.size() / 2)
                .mapToObj(i -> new int[][]{
                        parseState(lines.get(i * 2)),
                        parseState(lines.get(i * 2 + 1))
                })
                .collect(Collectors.toList());
    }

    /**
     * Read all non-empty trimmed lines from a file.
     */
    private static List<String> readNonEmptyLines(final String filepath) {
        try (final BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Display state as "1 2 3 / B 4 6 / 7 5 8"
     */
    public static String stateToString(final int[] state) {
        return IntStream.range(0, TILE_COUNT)
                .mapToObj(i -> {
                    final String tile = state[i] == BLANK ? "B" : String.valueOf(state[i]);
                    final String separator = (i % SIZE == 2 && i < TILE_COUNT - 1) ? " / "
                            : (i < TILE_COUNT - 1) ? " " : "";
                    return tile + separator;
                })
                .collect(Collectors.joining());
    }

    /**
     * Display state as a 3x3 grid.
     */
    public static String stateToGrid(final int[] state) {
        return IntStream.range(0, TILE_COUNT)
                .mapToObj(i -> {
                    final String tile = state[i] == BLANK ? "B" : String.valueOf(state[i]);
                    final String separator = (i % SIZE == 2) ? "\n" : " ";
                    return tile + separator;
                })
                .collect(Collectors.joining());
    }

    /**
     * Find the index of the blank (0) in the state array.
     */
    public static int findBlank(final int[] state) {
        return IntStream.range(0, TILE_COUNT)
                .filter(i -> state[i] == BLANK)
                .findFirst()
                .orElse(-1);
    }

    /**
     * Check if state matches the goal.
     */
    public static boolean isGoal(final int[] state, final int[] goal) {
        return Arrays.equals(state, goal);
    }

    /**
     * Create a new state by swapping tiles at two positions.
     */
    public static int[] swap(final int[] state, final int posA, final int posB) {
        final int[] newState = state.clone();
        final int temp = newState[posA];
        newState[posA] = newState[posB];
        newState[posB] = temp;
        return newState;
    }

    /**
     * Generate all valid neighbor states by sliding a tile into the blank position.
     */
    public static List<int[]> getNeighbors(final int[] state) {
        final int blankPos = findBlank(state);
        final int blankRow = blankPos / SIZE;
        final int blankCol = blankPos % SIZE;

        return IntStream.range(0, 4)
                .filter(dir -> {
                    final int newRow = blankRow + DR[dir];
                    final int newCol = blankCol + DC[dir];
                    return newRow >= 0 && newRow < SIZE && newCol >= 0 && newCol < SIZE;
                })
                .mapToObj(dir -> {
                    final int targetPos = (blankRow + DR[dir]) * SIZE + (blankCol + DC[dir]);
                    return swap(state, blankPos, targetPos);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get the action name (Up/Down/Left/Right) that transforms 'from' to 'to'.
     */
    public static String getAction(final int[] from, final int[] to) {
        final int blankBefore = findBlank(from);
        final int blankAfter = findBlank(to);
        final int rowDelta = (blankAfter / SIZE) - (blankBefore / SIZE);
        final int colDelta = (blankAfter % SIZE) - (blankBefore % SIZE);

        return IntStream.range(0, 4)
                .filter(dir -> DR[dir] == rowDelta && DC[dir] == colDelta)
                .mapToObj(dir -> DIR_NAMES[dir])
                .findFirst()
                .orElse("?");
    }

    /**
     * h1: Number of misplaced manuscripts (excluding blank).
     */
    public static int h1(final int[] state, final int[] goal) {
        return (int) IntStream.range(0, TILE_COUNT)
                .filter(i -> state[i] != BLANK && state[i] != goal[i])
                .count();
    }

    /**
     * h2: Total Manhattan Distance of all tiles from goal positions.
     */
    public static int h2(final int[] state, final int[][] goalPositions) {
        return IntStream.range(0, TILE_COUNT)
                .filter(i -> state[i] != BLANK)
                .map(i -> {
                    final int tile = state[i];
                    final int currentRow = i / SIZE;
                    final int currentCol = i % SIZE;
                    return Math.abs(currentRow - goalPositions[tile][0])
                            + Math.abs(currentCol - goalPositions[tile][1]);
                })
                .sum();
    }

    /**
     * Reconstruct path from goal back to start using the parent chain.
     */
    public static List<int[]> reconstructPath(final Map<String, String> parentOf,
                                              final Map<String, int[]> stateByKey,
                                              final int[] goalState) {
        final List<int[]> solutionPath = new ArrayList<>();
        String key = Arrays.toString(goalState);
        while (key != null) {
            solutionPath.add(stateByKey.get(key));
            key = parentOf.get(key);
        }
        Collections.reverse(solutionPath);
        return solutionPath;
    }

    /**
     * Print the search result summary including path and timing.
     */
    public static void printResult(final String heuristic,
                                   final boolean solved, final List<int[]> solutionPath,
                                   final int nodesExplored, final long timeTaken, final boolean printTrace) {

        if(!heuristic.isEmpty()) {
            System.out.println("Heuristic : " + heuristic);
        }
        System.out.println("Reached goal state : " + (solved ? "Yes" : "No"));
        System.out.println("Number of States Explored: " + nodesExplored);
        System.out.println("Time taken to reach goal state : " + timeTaken + " ms");

        if (solved && solutionPath != null && solutionPath.size() > 1) {
            final int moveCount = solutionPath.size() - 1;
            System.out.println("Path Length  : " + moveCount + " moves");

            final String pathDescription = IntStream.range(1, solutionPath.size())
                    .mapToObj(i -> getAction(solutionPath.get(i - 1), solutionPath.get(i)))
                    .collect(Collectors.joining(" -> "));
            System.out.println("Path trace: " + pathDescription +"\n");
            System.out.println("Initial State:\n"+stateToGrid(solutionPath.get(0)));
            System.out.println("Goal State:\n" + stateToGrid(solutionPath.get(solutionPath.size() - 1)));

            if(printTrace) {
                System.out.println("State Transition Trace:");
                AtomicInteger currentStep = new AtomicInteger();
                solutionPath.forEach(sp -> {
                    System.out.println("Step-" + currentStep + "\n" + stateToGrid(sp));
                    currentStep.getAndIncrement();
                });
            }
        }
    }
}