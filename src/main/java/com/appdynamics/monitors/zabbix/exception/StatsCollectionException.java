/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.zabbix.exception;


public class StatsCollectionException extends RuntimeException {

    public StatsCollectionException(String message) {
        super(message);
    }

    public StatsCollectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
