package models.builders;

import manager.AskManager;
import manager.ParserManager;
import manager.PrinterManager;
import manager.ScannerManager;
import models.Organization;
import network.TCPClient;

public class OrganizationBuilder extends Builder<Organization> {

    private ScannerManager scannerManager;
    private final PrinterManager printerManager;
    private AskManager askManager;
    private Boolean isWorking = true;

    public OrganizationBuilder(ScannerManager scannerManager, PrinterManager printerManager) {
        this.scannerManager = scannerManager;
        this.printerManager = printerManager;
    }

    public Organization build() {
        askManager = new AskManager(scannerManager, printerManager);
        while (isWorking) {
            printerManager.println("Enter some information about Organization:");
            return new Organization(
                askManager.askArgument(
                    "Annual turnover",
                    "(greater than 0 or empty)",
                    0.0,
                    Double.MAX_VALUE - 1,
                    true,
                    ParserManager.parseDouble
                ),
                askManager.askArgument(
                    "Employees count",
                    "(greater than 0 or empty)",
                    0,
                    Integer.MAX_VALUE - 1,
                    true,
                    ParserManager.parseInteger
                )
            );
        }
        printerManager.println("Building was stopped cause of shutting down the Server!");
        return null;
    }
}
