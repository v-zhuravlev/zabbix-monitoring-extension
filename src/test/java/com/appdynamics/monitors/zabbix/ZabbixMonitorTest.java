package com.appdynamics.monitors.zabbix;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import com.appdynamics.extensions.yml.YmlReader;
import com.appdynamics.monitors.zabbix.config.Configuration;
import com.appdynamics.monitors.zabbix.statsCollector.HistoryStatsCollector;
import com.appdynamics.monitors.zabbix.statsCollector.ITServiceStatsCollector;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.zabbix4j.ZabbixApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Satish Muddam
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ZabbixMonitor.class, YmlReader.class})
public class ZabbixMonitorTest {

    @Mock
    private ITServiceStatsCollector itServiceStatsCollector;

    @Mock
    private HistoryStatsCollector historyStatsCollector;

    @Mock
    private ZabbixApi zabbixApi;

    @Mock
    private MetricWriter metricWriter;

    private ZabbixMonitor monitor = new ZabbixMonitor();


    @Test
    public void testExecuteWithCommaInMetricName() throws Exception {

        Map<String, String> itServiceStats = new HashMap<String, String>();

        itServiceStats.put("ITMetric1|Have,IN Name", "20");

        Mockito.when(itServiceStatsCollector.collect(Matchers.any(ZabbixApi.class), Matchers.any(Configuration.class))).thenReturn(itServiceStats);

        Map<String, String> historyServiceStats = new HashMap<String, String>();

        historyServiceStats.put("HistoryMetric1|Have,IN Name", "30");

        Mockito.when(historyStatsCollector.collect(Matchers.any(ZabbixApi.class), Matchers.any(Configuration.class))).thenReturn(historyServiceStats);

        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put("config-file", "src/test/resources/config/config.yml");

        whenNew(ZabbixApi.class)
                .withArguments(Matchers.anyString()).thenReturn(zabbixApi);

        whenNew(ITServiceStatsCollector.class)
                .withNoArguments().thenReturn(itServiceStatsCollector);
        whenNew(HistoryStatsCollector.class)
                .withNoArguments().thenReturn(historyStatsCollector);

        ZabbixMonitor monitor = new ZabbixMonitor();

        ZabbixMonitor monitorSpy = Mockito.spy(monitor);

        Mockito.doReturn(zabbixApi).when(monitorSpy).createZabbixAPI(Matchers.any(Configuration.class));

        Mockito.doReturn(metricWriter).when(monitorSpy).getMetricWriter(Matchers.anyString(), Matchers.anyString(), Matchers.anyString(), Matchers.anyString());

        Mockito.doNothing().when(zabbixApi).login(Matchers.anyString(), Matchers.anyString());
        Mockito.doNothing().when(metricWriter).printMetric(Matchers.anyString());

        whenNew(MetricWriter.class).withArguments(any(AManagedMonitor.class),
                anyString()).thenReturn(metricWriter);


        monitorSpy.execute(taskArgs, null);


        Mockito.verify(monitorSpy).getMetricWriter(Mockito.eq("Custom Metrics|Zabbix|ITMetric1|Have;IN Name"), anyString(), anyString(), anyString());
        Mockito.verify(monitorSpy).getMetricWriter(Mockito.eq("Custom Metrics|Zabbix|HistoryMetric1|Have;IN Name"), anyString(), anyString(), anyString());

    }


}
