package ar.edu.itba.pod.client.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportClient {

    private static final Logger logger = LoggerFactory.getLogger(ReportClient.class);

    public static void main(String[] args) {

        logger.info("Report Client Starting ...");

        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");
        final String outPath = System.getProperty("outPath");
        final String platform = System.getProperty("platform");

        if (serverAddress == null || action == null) {
            logger.error("Invalid arguments");
            // Exception?
            return;
        }

        logger.info("serverAddress: {}, action: {}, outPath: {}, platform: {}", serverAddress, action, outPath, platform);

        /*try {
        } finally {
        }*/
    }
}
