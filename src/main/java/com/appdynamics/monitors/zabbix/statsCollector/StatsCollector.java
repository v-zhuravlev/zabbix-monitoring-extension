package com.appdynamics.monitors.zabbix.statsCollector;


import com.zabbix4j.ZabbixApi;

import java.util.Map;

public interface StatsCollector {

    String METRIC_SEPARATOR = "|";

    Map<String, String> collect(ZabbixApi zabbixApi);
}
