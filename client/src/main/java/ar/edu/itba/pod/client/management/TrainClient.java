package ar.edu.itba.pod.client.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrainClient {

    private static final Logger logger = LoggerFactory.getLogger(TrainClient.class);

    public static void main(String[] args) {

        logger.info("Train Client Starting ...");

        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");
        final String trainId = System.getProperty("id");
        final String size = System.getProperty("size");
        final String platform = System.getProperty("platform");
        final String occupancy = System.getProperty("occupancy");
        final String traction = System.getProperty("traction");

        if (serverAddress == null || action == null) {
            logger.error("Invalid arguments");
            // Exception?
            return;
        }

        logger.info ("serverAddress: {}, action: {}, id: {}, size: {}, platform: {}, occupancy: {}, traction: {}", serverAddress, action, trainId, size, platform, occupancy, traction);

        /*try {
        } finally {
        }*/
    }
}
