import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
 * CSP Solver for Security Bot Scheduling Problem
 * Uses Backtracking + MRV heuristic + Forward Checking
 */
public class SecurityBotCSP {

    private final List<String> availableBots;
    private final int numberOfSlots;
    private final boolean enforceNoBackToBack;
    private final boolean enforceMinimumCoverage;
    private final Map<Integer, Set<String>> blockedBotsBySlot;

    private final Map<Integer, List<String>> slotDomains;
    private final Map<Integer, String> assignedSchedule;
    private int totalAssignments = 0;
    private int totalBacktracks = 0;
    private int totalInferences = 0;


    public SecurityBotCSP(final List<String> bots, final int slots,
                          final boolean noBackToBack, final boolean minimumCoverage,
                          final Map<Integer, Set<String>> blocked) {
        this.availableBots = List.copyOf(bots);
        this.numberOfSlots = slots;
        this.enforceNoBackToBack = noBackToBack;
        this.enforceMinimumCoverage = minimumCoverage;
        this.blockedBotsBySlot = Map.copyOf(blocked);
        this.assignedSchedule = new LinkedHashMap<>();

        this.slotDomains = new LinkedHashMap<>();
        IntStream.rangeClosed(1, numberOfSlots)
                .forEach(slotNumber -> slotDomains.put(slotNumber, new ArrayList<>(availableBots)));
    }


    public static SecurityBotCSP loadFromFile(final String filePath) throws IOException {
        final var botList = new ArrayList<String>();
        int slotCount = 0;
        boolean hasNoBackToBack = false;
        boolean hasMinimumCoverage = false;
        final var blockedBotsMap = new HashMap<Integer, Set<String>>();

        try (final var fileReader = new BufferedReader(new FileReader(filePath))) {
            String currentLine;
            while ((currentLine = fileReader.readLine()) != null) {
                final String trimmedLine = currentLine.strip();
                if (trimmedLine.isBlank() || trimmedLine.startsWith("#")) continue;

                if (trimmedLine.startsWith("Bots:")) {
                    Arrays.stream(trimmedLine.substring(5).split(","))
                            .map(String::strip)
                            .filter(token -> !token.isEmpty())
                            .forEach(botList::add);

                } else if (trimmedLine.startsWith("Slots:")) {
                    slotCount = (int) Arrays.stream(trimmedLine.substring(6).split(","))
                            .map(String::strip)
                            .filter(token -> !token.isEmpty())
                            .count();

                } else if (trimmedLine.startsWith("Constraint:")) {
                    final String constraintBody = trimmedLine.substring(11).strip();

                    if (constraintBody.equalsIgnoreCase("NoBackToBack")) {
                        hasNoBackToBack = true;
                    } else if (constraintBody.equalsIgnoreCase("MinimumCoverage")) {
                        hasMinimumCoverage = true;
                    } else if (constraintBody.toLowerCase().startsWith("maintenancebreak")) {
                        final String[] constraintTokens = constraintBody.split("\\s+");
                        if (constraintTokens.length >= 3) {
                            final String blockedBot = constraintTokens[1];
                            final int blockedSlot = Integer.parseInt(constraintTokens[2]);
                            blockedBotsMap.computeIfAbsent(blockedSlot, key -> new HashSet<>())
                                    .add(blockedBot);
                        }
                    }
                }
            }
        }

        if (botList.isEmpty()) throw new IllegalArgumentException("No bots defined in input file!");
        if (slotCount == 0) throw new IllegalArgumentException("No slots defined in input file!");

        return new SecurityBotCSP(botList, slotCount, hasNoBackToBack, hasMinimumCoverage, blockedBotsMap);
    }

    private OptionalInt pickNextSlotByMRV() {
        return IntStream.rangeClosed(1, numberOfSlots)
                .filter(slot -> !assignedSchedule.containsKey(slot))
                .boxed()
                .min(Comparator.comparingInt(slot -> slotDomains.get(slot).size()))
                .map(OptionalInt::of)
                .orElse(OptionalInt.empty());
    }

    private boolean isConsistentAssignment(final int slotNumber, final String botName) {
        final Set<String> blockedSet = blockedBotsBySlot.getOrDefault(slotNumber, Set.of());
        if (blockedSet.contains(botName)) {
            return false;
        }

        if (enforceNoBackToBack) {
            final boolean conflictsWithPrevious = slotNumber > 1
                    && botName.equals(assignedSchedule.get(slotNumber - 1));
            final boolean conflictsWithNext = slotNumber < numberOfSlots
                    && botName.equals(assignedSchedule.get(slotNumber + 1));
            if (conflictsWithPrevious || conflictsWithNext) return false;
        }
        return true;
    }

    private boolean isCoverageStillFeasible() {
        if (!enforceMinimumCoverage) return true;

        final var alreadyAssignedBots = new HashSet<>(assignedSchedule.values());
        for (final String botName : availableBots) {
            if (alreadyAssignedBots.contains(botName)) continue;

            final boolean hasOpenSlot = IntStream.rangeClosed(1, numberOfSlots)
                    .filter(slot -> !assignedSchedule.containsKey(slot))
                    .anyMatch(slot -> slotDomains.get(slot).contains(botName));

            if (!hasOpenSlot) return false;
        }
        return true;
    }

    private boolean isMinimumCoverageSatisfied() {
        final var usedBots = new HashSet<>(assignedSchedule.values());
        return availableBots.stream().allMatch(usedBots::contains);
    }

    private Optional<List<int[]>> applyForwardChecking(final int assignedSlot, final String assignedBot) {
        final var domainChanges = new ArrayList<int[]>();
        if (!enforceNoBackToBack) return Optional.of(domainChanges);

        boolean domainWipeoutDetected = false;
        final int[] neighbourSlots = {assignedSlot + 1, assignedSlot - 1};

        for (final int neighbourSlot : neighbourSlots) {
            if (neighbourSlot < 1 || neighbourSlot > numberOfSlots) continue;
            if (assignedSchedule.containsKey(neighbourSlot)) continue;

            final List<String> neighbourDomain = slotDomains.get(neighbourSlot);
            final int indexToRemove = neighbourDomain.indexOf(assignedBot);

            if (indexToRemove >= 0) {
                neighbourDomain.remove(indexToRemove);
                final int botIndexInMasterList = availableBots.indexOf(assignedBot);
                domainChanges.add(new int[]{neighbourSlot, botIndexInMasterList});
                totalInferences++;

                System.out.printf("    -> Forward Check: pruned '%s' from Slot%d's domain%n",
                        assignedBot, neighbourSlot);

                if (neighbourDomain.isEmpty()) {
                    System.out.printf("    !! Domain wipeout at Slot%d - triggering backtrack%n",
                            neighbourSlot);
                    domainWipeoutDetected = true;
                    break;
                }
            }
        }

        if (domainWipeoutDetected) {
            restorePrunedDomains(domainChanges);
            return Optional.empty();
        }
        return Optional.of(domainChanges);
    }

    private void restorePrunedDomains(final List<int[]> domainChanges) {
        for (int changeIndex = domainChanges.size() - 1; changeIndex >= 0; changeIndex--) {
            final int targetSlot = domainChanges.get(changeIndex)[0];
            final int botMasterIndex = domainChanges.get(changeIndex)[1];
            final String botToRestore = availableBots.get(botMasterIndex);

            final List<String> targetDomain = slotDomains.get(targetSlot);

            int insertionPoint = 0;
            while (insertionPoint < targetDomain.size()
                    && availableBots.indexOf(targetDomain.get(insertionPoint)) < botMasterIndex) {
                insertionPoint++;
            }
            targetDomain.add(insertionPoint, botToRestore);
        }
    }

    private boolean solveByBacktracking() {
        if (!isCoverageStillFeasible()) return false;

        final var nextSlotChoice = pickNextSlotByMRV();
        if (nextSlotChoice.isEmpty()) {
            return !enforceMinimumCoverage || isMinimumCoverageSatisfied();
        }

        final int currentSlot = nextSlotChoice.getAsInt();
        final String domainDisplay = String.join(", ", slotDomains.get(currentSlot));
        System.out.printf("[MRV] Selected Slot%d (remaining values: {%s})%n",
                currentSlot, domainDisplay);

        final var candidateBots = List.copyOf(slotDomains.get(currentSlot));

        for (final String candidateBot : candidateBots) {
            totalAssignments++;
            System.out.printf("  Assign Slot%d = %s ", currentSlot, candidateBot);

            if (!isConsistentAssignment(currentSlot, candidateBot)) {
                System.out.println("=> REJECTED (violates constraint)");
                continue;
            }
            System.out.println("=> OK");

            assignedSchedule.put(currentSlot, candidateBot);

            final var forwardCheckResult = applyForwardChecking(currentSlot, candidateBot);

            if (forwardCheckResult.isPresent()) {
                if (solveByBacktracking()) return true;
                restorePrunedDomains(forwardCheckResult.get());
            }

            assignedSchedule.remove(currentSlot);
            totalBacktracks++;
            System.out.printf("  << Backtrack from Slot%d = %s%n", currentSlot, candidateBot);
        }

        return false;
    }

    private void displayResults(final boolean solutionFound, final double elapsedTimeSeconds) {
        System.out.println();
        System.out.println("=".repeat(45));

        if (solutionFound) {
            System.out.println("  RESULT: Solution Found!");
            System.out.println("=".repeat(45));
            System.out.println();

            System.out.println("  Final Bot Schedule:");
            IntStream.rangeClosed(1, numberOfSlots).forEach(slotNumber ->
                    System.out.printf("    Slot %d  -->  Bot %s%n",
                            slotNumber, assignedSchedule.get(slotNumber))
            );

            System.out.println("\n  Constraint Checks:");
            verifyAndPrintAllConstraints();
        } else {
            System.out.println("  RESULT: No Valid Assignment Exists");
            System.out.println("=".repeat(45));
            System.out.println("  The given set of constraints is unsatisfiable.");
        }

        System.out.println("\n"+"-".repeat(45));
        System.out.println("  Solver Statistics:");
        System.out.println("    Method       : Backtracking + MRV + Forward Checking");
        System.out.printf("    Assignments  : %d%n", totalAssignments);
        System.out.printf("    Backtracks   : %d%n", totalBacktracks);
        System.out.printf("    FC Inferences: %d%n", totalInferences);
        System.out.printf("    Elapsed Time : %.6f sec%n", elapsedTimeSeconds);
        System.out.println("-".repeat(45));
    }

    private void verifyAndPrintAllConstraints() {
        final boolean noBackToBackSatisfied = IntStream.rangeClosed(1, numberOfSlots - 1)
                .noneMatch(slot -> assignedSchedule.get(slot).equals(assignedSchedule.get(slot + 1)));
        System.out.printf("    [%s] No Back-to-Back%n", noBackToBackSatisfied ? "PASS" : "FAIL");
        final boolean maintenanceSatisfied = blockedBotsBySlot.entrySet().stream()
                .noneMatch(entry -> {
                    final int slotNumber = entry.getKey();
                    return slotNumber <= numberOfSlots
                            && entry.getValue().contains(assignedSchedule.get(slotNumber));
                });
        System.out.printf("    [%s] Maintenance Break%n", maintenanceSatisfied ? "PASS" : "FAIL");
        if (enforceMinimumCoverage) {
            final boolean coverageSatisfied = isMinimumCoverageSatisfied();
            System.out.printf("    [%s] Minimum Coverage (all bots used)%n",
                    coverageSatisfied ? "PASS" : "FAIL");
        }
    }


    public static void main(final String[] args) {
        final String inputFilePath = (args.length > 0) ? args[0] : "question22/inputfile/input.txt";

        try {
            final var solver = SecurityBotCSP.loadFromFile(inputFilePath);
            System.out.println("Loaded CSP from: " + inputFilePath);
            System.out.printf("Bots: %s | Slots: %d%n", solver.availableBots, solver.numberOfSlots);
            System.out.println("Starting backtracking search...");
            System.out.println();

            final long startTimeNanos = System.nanoTime();
            final boolean solutionFound = solver.solveByBacktracking();
            final double elapsedSeconds = (System.nanoTime() - startTimeNanos) / 1_000_000_000.0;

            solver.displayResults(solutionFound, elapsedSeconds);

        } catch (IOException exception) {
            System.err.println("Failed to read input file: " + exception.getMessage());
        } catch (IllegalArgumentException exception) {
            System.err.println("Bad input: " + exception.getMessage());
        }
    }
}