
package com.capstone.server.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

/**
 * Context initializer that loads the API key from a {@value #PATH} file located
 * in the classpath (typically under {@code WEB-INF/classes}).
 */
public class ApiKeyInitializer implements ServletContextListener {

    public static final String ATTRIBUTE_ACCESS_KEY = "apiKey";

    private static final String PATH = "/api.key";

    private final static Logger sLogger = Logger.getLogger(ApiKeyInitializer.class);

    public void contextInitialized(ServletContextEvent event) {
        sLogger.info("Reading " + PATH + " from resources (probably from " + "WEB-INF/classes");
        String key = getKey();
        event.getServletContext().setAttribute(ATTRIBUTE_ACCESS_KEY, key);
    }

    /**
     * Gets the access key.
     */
    protected String getKey() {
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH);
        if (stream == null) {
            throw new IllegalStateException("Could not find file " + PATH + " on web resources)");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        try {
            String key = reader.readLine();
            return key;
        } catch (IOException e) {
            throw new RuntimeException("Could not read file " + PATH, e);
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                sLogger.warn("Exception closing " + PATH, e);
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
    }
}
