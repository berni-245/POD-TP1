package ar.edu.itba.pod.client.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationClient {

    private static final Logger logger = LoggerFactory.getLogger(NotificationClient.class);

    public static void main(String[] args) {

        logger.info("Notification Client Starting ...");

        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");
        final String platform = System.getProperty("platform");

        if (serverAddress == null || action == null) {
            logger.error("Invalid arguments");
            // Exception?
            return;
        }

        logger.info("serverAddress: {}, action: {}, platform: {}", serverAddress, action, platform);

        /*try {
        } finally {
        }*/
    }
}
