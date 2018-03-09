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
import com.zabbix4j.history.HistoryGetRequest;
import com.zabbix4j.history.HistoryGetResponse;
import com.zabbix4j.history.HistoryObject;
import com.zabbix4j.host.HostGetRequest;
import com.zabbix4j.host.HostGetResponse;
import com.zabbix4j.item.ItemGetRequest;
import com.zabbix4j.item.ItemGetResponse;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class HistoryStatsCollector implements StatsCollector {

    private static final Logger logger = Logger.getLogger(HistoryStatsCollector.class);
    private static final String HISTORY_PATH_PREFIX = "History" + METRIC_SEPARATOR + "${HOST}" + METRIC_SEPARATOR + "${ITEM}";
    public static final int MILLIS_PER_MIN = 60000;
    public static final int MILLIS_TO_SEC = 1000;

    public Map<String, String> collect(ZabbixApi zabbixApi, Configuration configuration) {
        Map<String, String> historyStats = new HashMap<String, String>();
        try {
            Map<Integer, String> hostIds = getHostIds(zabbixApi, configuration);

            List<Integer> itemIds = getHistoryItems(zabbixApi, hostIds);

            logger.info("Total number of history items for this minute" + itemIds.size());

            if (itemIds.size() <= 0) {
                logger.info("No history items found for the given hosts for this minute");
                return historyStats;
            }

            ItemGetRequest itemGetRequest = new ItemGetRequest();
            itemGetRequest.getParams().setItemids(itemIds);
            itemGetRequest.getParams().setLimitSelects(100);
            ItemGetResponse itemGetResponse = zabbixApi.item().get(itemGetRequest);
            List<ItemGetResponse.Result> itemResults = itemGetResponse.getResult();

            List<String> excludeItems = configuration.getExcludeItems();
            List<String> includeItems = configuration.getIncludeItems();

            List<Pattern> excludedItemPatterns = buildPatterns(excludeItems, "excludeItems");

            List<Pattern> includeItemPatterns = buildPatterns(includeItems, "includeItems");

            for (ItemGetResponse.Result itemResult : itemResults) {
                String itemName = itemResult.getName();
                if (itemName.contains("$")) {
                    itemName = substituteVariable(itemName, itemResult.getKey_());
                }

                if (includeItemPatterns != null && includeItemPatterns.size() > 0) {
                    if (isMatched(includeItemPatterns, itemName, "item")) {
                        String metricPath = HISTORY_PATH_PREFIX.replace("${HOST}", hostIds.get(itemResult.getHostid()) + "");
                        metricPath = metricPath.replace("${ITEM}", itemName);
                        historyStats.put(metricPath, itemResult.getLastvalue());
                    }
                } else {

                    if (!isMatched(excludedItemPatterns, itemName, "item")) {
                        String metricPath = HISTORY_PATH_PREFIX.replace("${HOST}", hostIds.get(itemResult.getHostid()) + "");
                        metricPath = metricPath.replace("${ITEM}", itemName);
                        historyStats.put(metricPath, itemResult.getLastvalue());
                    }
                }

            }
        } catch (ZabbixApiException e) {
            logger.error("Error while requesting history stats", e);
            throw new StatsCollectionException("Error while requesting history stats", e);
        }
        return historyStats;
    }

    private List<Integer> getHistoryItems(ZabbixApi zabbixApi, Map<Integer, String> hostIds) throws ZabbixApiException {
        List<Integer> hostIdList = new ArrayList<Integer>(hostIds.keySet());
        long prevMin = (System.currentTimeMillis() - (MILLIS_PER_MIN * 20)) / MILLIS_TO_SEC;

        List<HistoryObject> integerHistoryItems = getHistoryItemsForHistoryType(zabbixApi, hostIdList, 3, prevMin);
        List<HistoryObject> floatHistoryItems = getHistoryItemsForHistoryType(zabbixApi, hostIdList, 0, prevMin);


        List<HistoryObject> historyResult = new ArrayList<HistoryObject>();
        historyResult.addAll(integerHistoryItems);
        historyResult.addAll(floatHistoryItems);

        List<Integer> itemIds = new ArrayList<Integer>();
        for (HistoryObject historyObject : historyResult) {
            itemIds.add(historyObject.getItemid());
        }
        return itemIds;
    }

    private List<HistoryObject> getHistoryItemsForHistoryType(ZabbixApi zabbixApi, List<Integer> hostIds, int historyType, long timeFrom) throws ZabbixApiException {

        HistoryGetRequest historyGetRequest = new HistoryGetRequest();
        historyGetRequest.getParams().setHostids(hostIds);
        historyGetRequest.getParams().setOutput("itemid");
        historyGetRequest.getParams().setTime_from(timeFrom);
        historyGetRequest.getParams().setHistory(historyType);

        HistoryGetResponse historyGetResponse = zabbixApi.history().get(historyGetRequest);
        return historyGetResponse.getResult();

    }

    private Map<Integer, String> getHostIds(ZabbixApi zabbixApi, Configuration configuration) throws ZabbixApiException {
        Map<Integer, String> hostWithName = new HashMap<Integer, String>();
        HostGetRequest hostGetRequest = new HostGetRequest();
        HostGetResponse hostGetResponse = zabbixApi.host().get(hostGetRequest);

        List<HostGetResponse.Result> result = hostGetResponse.getResult();


        List<String> includeHosts = configuration.getIncludeHosts();

        if (includeHosts != null && includeHosts.size() > 0) {
            logger.info("includeHosts configured with values " + includeHosts);
            List<Pattern> includePattern = buildPatterns(includeHosts, "includeHosts");
            for (HostGetResponse.Result res : result) {
                if (isMatched(includePattern, res.getName(), "host")) {
                    hostWithName.put(res.getHostid(), res.getName());
                }
            }

            logger.info("Host names to get history from " + hostWithName);
            return hostWithName;
        }


        List<String> excludeHosts = configuration.getExcludeHosts();
        logger.info("excludeHosts configured with values " + excludeHosts);
        List<Pattern> patterns = buildPatterns(excludeHosts, "excludeHosts");
        for (HostGetResponse.Result res : result) {
            if (!isMatched(patterns, res.getName(), "host")) {
                hostWithName.put(res.getHostid(), res.getName());
            }
        }
        logger.info("Host names to get history from " + hostWithName);
        return hostWithName;
    }

    private boolean isMatched(List<Pattern> patterns, String input, String type) {
        boolean isMatched = false;
        for (Pattern pattern : patterns) {
            if (pattern.matcher(input).matches()) {
                logger.debug(" [" + input + "] matched for [" + type + "]");
                isMatched = true;
                break;
            }
        }
        return isMatched;
    }

    private List<Pattern> buildPatterns(List<String> patterns, String excludeParam) {
        if (patterns == null || patterns.size() <= 0) {
            return new ArrayList<Pattern>();
        }

        List<Pattern> excludePatterns = new ArrayList<Pattern>();
        for (String pattern : patterns) {
            try {
                Pattern compile = Pattern.compile(pattern);
                excludePatterns.add(compile);
            } catch (PatternSyntaxException ex) {
                logger.error("Invalid pattern[" + pattern + "] specified in " + excludeParam + ". Ignoring it.");
            }
        }

        return excludePatterns;
    }

    private String substituteVariable(String itemName, String key_) {

        String substring = key_.substring(key_.indexOf('[') + 1, key_.indexOf(']'));
        String[] split = substring.split(",");

        while (itemName.contains("$")) {
            int $Index = itemName.indexOf('$');
            String index = itemName.substring($Index + 1, $Index + 2);

            itemName = itemName.replace("$" + index, split[Integer.parseInt(index) - 1]);
        }

        return itemName;
    }
}
