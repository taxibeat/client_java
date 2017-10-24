package io.prometheus.client.hotspot;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Exports JVM version info.
 * <p>
 * Example usage:
 * <pre>
 * {@code
 *   new VersionInfoExports().register();
 * }
 * </pre>
 * Metrics being exported:
 * <pre>
 *   jvm_info{version="1.8.0_45-b14",vendor="Oracle Corporation"} 1.0
 * </pre>
 */

public class VersionInfoExports extends Collector {


    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
        String applicationId = DefaultExports.getAppId();
        String applicationName = DefaultExports.getAppName();


        GaugeMetricFamily jvmInfo = new GaugeMetricFamily(
                "jvm_info",
                "JVM version info",
                Arrays.asList("app_id", "application_name", "version", "vendor"));
        jvmInfo.addMetric(Arrays.asList(applicationId, applicationName, System.getProperty("java.runtime.version", "unknown"),
                System.getProperty("java.vm.vendor", "unknown")), 1L);
        mfs.add(jvmInfo);

        return mfs;
    }
}
