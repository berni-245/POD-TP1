package ar.edu.itba.pod.client.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BoardClient {

    private static final Logger logger = LoggerFactory.getLogger(BoardClient.class);

    public static void main(String[] args) {

        logger.info("Board Client Starting ...");

        final String serverAddress = System.getProperty("serverAddress");
        final String action = System.getProperty("action");

        if (serverAddress == null || action == null) {
            logger.error("Invalid arguments");
            // Exception?
            return;
        }

        logger.info("serverAddress: {}, action: {}", serverAddress, action);
/*
        try {
        } finally {
        }*/
    }
}
