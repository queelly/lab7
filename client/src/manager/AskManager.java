package manager;

import network.TCPClient;

import java.util.function.Function;

/**
 * Class for querying different types of data.
 *
 */
public class AskManager {

    private ScannerManager scannerManager;
    private PrinterManager printerManager;
    private boolean isWorking = true;

    /**
     * Class constructor
     *
     * @param scannerManager object of scanner manager.
     */
    public AskManager(ScannerManager scannerManager, PrinterManager printerManager) {
        this.scannerManager = scannerManager;
        this.printerManager = printerManager;
    }

    /**
     * Any argument asking method
     *
     * @param fieldName required field name
     * @param restrictions restrictions to print
     * @param minValue minimal value (null if not supposed)
     * @param maxValue maximal value (null if not supposed)
     * @param canBeNull true if supposed to be null, else false
     * @param parse parser function
     * @return argument
     * @param <T> type of argument
     */
    public <T extends Comparable<T>> T askArgument(String fieldName,
                                                   String restrictions,
                                                   T minValue,
                                                   T maxValue,
                                                   boolean canBeNull,
                                                   Function<String, T> parse) {
        while (isWorking) {
            printerManager.println("Enter " + fieldName + " " + restrictions + ": ");
            printerManager.print(">>> ");
            String inputLine = scannerManager.readLine().trim();
            if (inputLine.isEmpty()) {
                if (canBeNull) {
                    return null;
                } else {
                    printerManager.printErr("Value can't be null! Please, try again.");
                }
            } else {
                T parsedArgument = parse.apply(inputLine);
                if (parsedArgument == null) {
                    printerManager.printErr("Wrong input format! Please, try again.");
                } else {
                    if (minValue != null && parsedArgument.compareTo(minValue) < 0) {
                        printerManager.printErr("Your value is too small so it was set to " + minValue);
                        return minValue;
                    } else if (maxValue != null && parsedArgument.compareTo(maxValue) > 0) {
                        printerManager.printErr("Your value is too large so it was set to " + maxValue);
                        return maxValue;
                    } else {
                        return parsedArgument;
                    }
                }
            }
        }
        printerManager.println("Asking was stopped cause of shutting down the Server!");
        return null;
    }
}
