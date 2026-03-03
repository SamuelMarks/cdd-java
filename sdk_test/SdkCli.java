package cli;

import java.util.Arrays;

/**
 * Generated SDK CLI.
 */
public class SdkCli {
    /** Default constructor. */
    public SdkCli() {}

    /**
     * CLI Entrypoint.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if (args.length == 0 || args[0].equals("--help") || args[0].equals("-h")) {
            printHelp();
            return;
        }

        String command = args[0];
        if (command.equals("listPets")) {
            System.out.println("Executing listPets");
            return;
        }
        if (command.equals("createPets")) {
            System.out.println("Executing createPets");
            return;
        }
        if (command.equals("showPetById")) {
            System.out.println("Executing showPetById");
            return;
        }
        System.err.println("Unknown command: " + command);
        printHelp();
    }

    /**
     * Prints help.
     */
    private static void printHelp() {
        System.out.println("SDK CLI");
        System.out.println("Commands:");
        System.out.println("  listPets - List all pets");
        System.out.println("  createPets - Create a pet");
        System.out.println("  showPetById - Info for a specific pet");
    }
}
