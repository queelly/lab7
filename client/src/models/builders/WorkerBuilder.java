package models.builders;

import manager.AskManager;
import manager.ParserManager;
import manager.PrinterManager;
import manager.ScannerManager;
import models.Position;
import models.Status;
import models.Worker;
import network.TCPClient;
import utility.EnumNames;

import java.time.LocalDateTime;

public class WorkerBuilder extends Builder<Worker> {

    private ScannerManager scannerManager;
    private PrinterManager printerManager;
    private AskManager askManager;
    private Boolean isWorking = true;
    private TCPClient client;

    public WorkerBuilder(ScannerManager scannerManager, PrinterManager printerManager) {
        this.scannerManager = scannerManager;
        this.printerManager = printerManager;
    }

    @Override
    public Worker build() {
        askManager = new AskManager(scannerManager, printerManager);
        while (isWorking) {
            printerManager.println("Enter some information about Worker:");
            return new Worker(
                null,
                askManager.askArgument(
                    "Name",
                    "(not empty)",
                    null,
                    null,
                    false,
                    ParserManager.parseString
                ),
                new CoordinatesBuilder(scannerManager, printerManager).build(),
                null,
                askManager.askArgument(
                    "Salary",
                    "(greater than 0 or empty)",
                    0.0,
                    Double.MAX_VALUE - 1,
                    true,
                    ParserManager.parseDouble
                ),
                askManager.askArgument(
                    "Position",
                    "(enter one of possible values: " +
                        EnumNames.names(Position.class) +
                        ", you may write upper either lower case letters (may be empty))",
                    null,
                    null,
                    true,
                    ParserManager.parseEnum(Position.class)
                ),
                askManager.askArgument(
                    "Status",
                    "(enter one of possible values: " +
                        EnumNames.names(Status.class) +
                        ", you may write upper either lower case letters (not empty))",
                    null,
                    null,
                    false,
                    ParserManager.parseEnum(Status.class)
                ),
                new OrganizationBuilder(scannerManager, printerManager).build()
            );
        }
        printerManager.println("Building was stopped cause of shutting down the Program!");
        return null;
    }
}
