import java.util.Scanner;

/**
 * ManuscriptSorter - Master runner for all search algorithms.
 */
public class ManuscriptSorter {

    public static void main(String[] args) throws Exception {
        final String inputFile = args.length > 0 ? args[0] : "inputfile/input.txt";

        System.out.println("$".repeat(60));
        System.out.println("$  MANUSCRIPT SORTING PROBLEM - COMPLETE ANALYSIS");
        System.out.println("$".repeat(60)+"\n");

        final Scanner inputScanner = new Scanner(System.in);
        System.out.print("Print intermediate steps? (yes/no): ");
        final String logTrace = inputScanner.nextLine().trim().toLowerCase();

        // Execute each algorithm's main method
        System.out.println("*".repeat(60));
        System.out.println("*  SECTION 2A: UNINFORMED SEARCH");
        System.out.println("*".repeat(60));
        System.out.println();
        BFSSearch.main(new String[]{inputFile,logTrace});
        DFSSearch.main(new String[]{inputFile,logTrace});

        System.out.println("*".repeat(60));
        System.out.println("*  SECTION 2B: INFORMED SEARCH");
        System.out.println("*".repeat(60));
        System.out.println();
        GreedyBestFirstSearch.main(new String[]{inputFile,logTrace});
        AStarSearch.main(new String[]{inputFile,logTrace});

        System.out.println("*".repeat(60));
        System.out.println("*  SECTION 2C: MEMORY-BOUNDED & LOCAL SEARCH");
        System.out.println("*".repeat(60));
        System.out.println();
        IDAStarSearch.main(new String[]{inputFile,logTrace});
        SimulatedAnnealingSearch.main(new String[]{inputFile,logTrace});

        System.out.println("*".repeat(60));
        System.out.println("*  SECTION 2D: ADVERSARIAL SEARCH");
        System.out.println("*".repeat(60));
        System.out.println();
        AdversarialSearch.main(new String[]{inputFile,logTrace});

    }
}
