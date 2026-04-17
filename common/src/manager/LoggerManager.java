package manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerManager {
    private final Logger logger;

    public LoggerManager(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public void info(String message) {
        logger.info(message);
    }

    public void error(String message) {
        logger.error(message);
    }
}
