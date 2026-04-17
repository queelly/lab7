package manager;

import java.io.PrintStream;

public class PrinterManager {

    private final PrintStream outStream;
    private final PrintStream errStream;

    public void print(Object o) {
        outStream.print(o);
    }

    public void println(Object o) {
        outStream.println(o);
    }

    public void printErr(String message) {
        errStream.println("An Error was occurred: " + message);
    }

    public PrinterManager(PrintStream outStream, PrintStream errStream) {
        this.outStream = outStream;
        this.errStream = errStream;
    }

    public PrinterManager() {
        this.outStream = System.out;
        this.errStream = System.err;
    }
}
