/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.monitors.zabbix.statsCollector;

import com.appdynamics.monitors.zabbix.config.Configuration;
import com.appdynamics.monitors.zabbix.exception.StatsCollectionException;
import com.zabbix4j.ZabbixApi;
import com.zabbix4j.ZabbixApiException;
import com.zabbix4j.itservice.ITServiceGetRequest;
import com.zabbix4j.itservice.ITServiceGetResponse;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ITServiceStatsCollector implements StatsCollector {
    private static final Logger logger = Logger.getLogger(ITServiceStatsCollector.class);
    private static final String IT_SERVICE_PATH_PREFIX = "ITService" + METRIC_SEPARATOR;

    public Map<String, String> collect(ZabbixApi zabbixApi, Configuration configuration) {
        Map<String, String> itServiceStats = new HashMap<String, String>();
        try {
            ITServiceGetRequest request = new ITServiceGetRequest();
            ITServiceGetResponse itServiceGetResponse = zabbixApi.itservice().get(request);
            List<ITServiceGetResponse.Result> results = itServiceGetResponse.getResult();
            if (results != null) {
                for (ITServiceGetResponse.Result result : results) {
                    String name = result.getName();
                    Integer status = result.getStatus();
                    itServiceStats.put(IT_SERVICE_PATH_PREFIX + name, status+"");
                }
            } else {
                logger.info("No result found for IT Services");
            }

        } catch (ZabbixApiException e) {
            logger.error("Error while requesting IT Service stats", e);
            throw new StatsCollectionException("Error while requesting IT Service stats", e);
        }
        return itServiceStats;
    }
}
