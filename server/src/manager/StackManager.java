package manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Stack;

public class StackManager {

    private static Stack<String> fileStack = new Stack<>();

    public static void pushFileToStack(File file) throws FileNotFoundException {
        if (file.exists() && file.isFile()) {
            fileStack.push(file.getAbsolutePath());
        }
    }

    public static boolean isRecursive(File file) {
        return fileStack.contains(file.getAbsolutePath());
    }

    public static void popFileFromStack() {
        fileStack.pop();
    }
}
