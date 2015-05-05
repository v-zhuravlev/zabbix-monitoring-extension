package com.appdynamics.monitors.zabbix.exception;


public class StatsCollectionException extends RuntimeException {

    public StatsCollectionException(String message) {
        super(message);
    }

    public StatsCollectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
