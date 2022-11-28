package com.migratorydata.jwt;

/**
 * This class represents a status notification to be sent to the client.
 */
public class StatusNotification {
    private final String status;
    private final String info;

    /**
     * Constructs a new status notification.
     *
     * @param status a string indicating the type of the status notification
     * @param info a string providing the detailed information of the status notification
     */
    public StatusNotification(String status, String info) {
        this.status = status;
        this.info = info;
    }

    /**
     * Returns the type of the status notification.
     *
     * @return the type of the status notification
     */
    public String getStatus() {
        return status;
    }

    /**
     * Returns the detailed information of the status notification.
     *
     * @return the detailed information of the status notification
     */
    public String getInfo() {
        return info;
    }
}