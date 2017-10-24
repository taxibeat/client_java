package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.assertEquals;

public class VersionInfoExportsTest {
    private static final String appIdName = "app_id";
    private static final String appNameName = "application_name";
    private static final String appIdValue = DefaultExports.getAppId();
    private static final String appNameValue = DefaultExports.getAppName();

    private CollectorRegistry registry = new CollectorRegistry();

    @Before
    public void setUp() {
        new VersionInfoExports().register(registry);
    }

    @Test
    public void testVersionInfo() {
        assertEquals(
                1L,
                registry.getSampleValue(
                        "jvm_info", new String[]{appIdName, appNameName, "version", "vendor"},
                        new String[]{appIdValue, appNameValue,
                                System.getProperty("java.runtime.version", "unknown"),
                                System.getProperty("java.vm.vendor", "unknown")}),
                .0000001);
    }
}
