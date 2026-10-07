/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

/**
 *
 * @author mgssr
 */
public class LoggerUtil {

    private static final String LOG_FOLDER = "logs";
    private static final String LOG_FILE = LOG_FOLDER + File.separator + "logger.log";
    private static final Level LOG_LEVEL = Level.CONFIG;

    static {
        try {
            File dir = new File(LOG_FOLDER);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            LogManager.getLogManager().reset();

            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setLevel(LOG_LEVEL);
            fileHandler.setFormatter(new SimpleFormatter());

            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(LOG_LEVEL);

            Logger logger = Logger.getLogger("");
            logger.setLevel(LOG_LEVEL);
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
        } catch (IOException | SecurityException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public static Logger getLogger(Class<?> c) {
        return Logger.getLogger(c.getName());
    }

}
