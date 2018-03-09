/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.zabbix.statsCollector;


import com.appdynamics.monitors.zabbix.config.Configuration;
import com.zabbix4j.ZabbixApi;

import java.util.Map;

public interface StatsCollector {

    String METRIC_SEPARATOR = "|";

    Map<String, String> collect(ZabbixApi zabbixApi, Configuration configuration);
}
