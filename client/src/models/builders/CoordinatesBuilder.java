package models.builders;

import manager.AskManager;
import manager.ParserManager;
import manager.PrinterManager;
import manager.ScannerManager;
import models.Coordinates;
import network.TCPClient;

public class CoordinatesBuilder extends Builder<Coordinates> {

    private ScannerManager scannerManager;
    private final PrinterManager printerManager;
    private Float x;
    private Double y;
    private AskManager askManager;
    private Boolean isWorking = true;
    private TCPClient client;

    public CoordinatesBuilder(ScannerManager scannerManager, PrinterManager printerManager) {
        this.scannerManager = scannerManager;
        this.printerManager = printerManager;
    }

    @Override
    public Coordinates build() {
        askManager = new AskManager(scannerManager, printerManager);
        while (isWorking) {
            printerManager.println("Enter the Coordinates:");
            x = askManager.askArgument(
                "X coordinate",
                "(maximal value is 592, not empty)",
                -Float.MAX_VALUE + 1,
                592.0F,
                false,
                ParserManager.parseFloat
            );
            y = askManager.askArgument(
                "Y coordinate",
                "(maximal value is 846, not empty)",
                -Double.MAX_VALUE + 1,
                846.0,
                false,
                ParserManager.parseDouble
            );
            return new Coordinates(x != null ? x : 0, y);
        }
        printerManager.println("Building was stopped cause of shutting down the Program!");
        return null;
    }
}
