package com.appdynamics.monitors.zabbix.statsCollector;


import com.appdynamics.monitors.zabbix.exception.StatsCollectionException;
import com.zabbix4j.ZabbixApi;
import com.zabbix4j.ZabbixApiException;
import com.zabbix4j.history.HistoryGetRequest;
import com.zabbix4j.history.HistoryGetResponse;
import com.zabbix4j.history.HistoryObject;
import com.zabbix4j.item.ItemGetRequest;
import com.zabbix4j.item.ItemGetResponse;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryStatsCollector implements StatsCollector {

    private static final Logger logger = Logger.getLogger(HistoryStatsCollector.class);
    private static final String IT_SERVICE_PATH_PREFIX = "History" + METRIC_SEPARATOR+"${HOST}"+METRIC_SEPARATOR+"${ITEM}";

    public Map<String, String> collect(ZabbixApi zabbixApi) {
        Map<String, String> historyStats = new HashMap<String, String>();
        try {
            HistoryGetRequest historyGetRequest = new HistoryGetRequest();
            HistoryGetResponse historyGetResponse = zabbixApi.history().get(historyGetRequest);
            List<HistoryObject> historyResult = historyGetResponse.getResult();

            ItemGetRequest itemGetRequest = new ItemGetRequest();
            itemGetRequest.getParams().setOutput("extend");
            List<Integer> itemIds = new ArrayList<Integer>();
            for(HistoryObject historyObject : historyResult) {
                itemIds.add(historyObject.getItemid());
            }
            itemGetRequest.getParams().setItemids(itemIds);
            ItemGetResponse itemGetResponse = zabbixApi.item().get(itemGetRequest);
            List<ItemGetResponse.Result> itemResults = itemGetResponse.getResult();
            for(ItemGetResponse.Result itemResult : itemResults) {
                String metricPath = IT_SERVICE_PATH_PREFIX.replace("${HOST}", itemResult.getHostid() + "");
                String itemName = itemResult.getName();
                if(itemName.contains("$")) {
                    itemName = substituteVariable(itemName, itemResult.getKey_());
                }
                metricPath = metricPath.replace("${ITEM}", itemName);
                historyStats.put(metricPath, itemResult.getLastvalue());

            }
        } catch (ZabbixApiException e) {
            logger.error("Error while requesting history stats", e);
            throw new StatsCollectionException("Error while requesting history stats", e);
        }
        return historyStats;
    }

    private String substituteVariable(String itemName, String key_) {

        String substring = key_.substring(key_.indexOf('[')+1, key_.indexOf(']'));
        String[] split = substring.split(",");

        int $Index = itemName.indexOf('$');
        String index = itemName.substring($Index + 1, $Index + 2);

        itemName = itemName.replace("$"+index, split[Integer.parseInt(index)-1]);

        return itemName;
    }
}
