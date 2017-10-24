package io.prometheus.client.hotspot;

import io.prometheus.client.CollectorRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.management.ThreadMXBean;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ThreadExportsTest {

  private ThreadMXBean mockThreadsBean = Mockito.mock(ThreadMXBean.class);
  private CollectorRegistry registry = new CollectorRegistry();
  private ThreadExports collectorUnderTest;

  private static final String[] LABEL_NAMES = {"app_id", "application_name"};
  private static final String[] LABEL_VALUES = {DefaultExports.getAppId(), DefaultExports.getAppName()};

  @Before
  public void setUp() {
    when(mockThreadsBean.getThreadCount()).thenReturn(300);
    when(mockThreadsBean.getDaemonThreadCount()).thenReturn(200);
    when(mockThreadsBean.getPeakThreadCount()).thenReturn(301);
    when(mockThreadsBean.getTotalStartedThreadCount()).thenReturn(503L);
    when(mockThreadsBean.findDeadlockedThreads()).thenReturn(new long[]{1L,2L,3L});
    when(mockThreadsBean.findMonitorDeadlockedThreads()).thenReturn(new long[]{2L,3L,4L});
    collectorUnderTest = new ThreadExports(mockThreadsBean).register(registry);
  }

  @Test
  public void testThreadPools() {
    Double sample = registry.getSampleValue(
            "jvm_threads_current", LABEL_NAMES, LABEL_VALUES);
    assertEquals(
            300L,
            registry.getSampleValue(
                    "jvm_threads_current", LABEL_NAMES, LABEL_VALUES),
            .0000001);
    assertEquals(
            200L,
            registry.getSampleValue(
                    "jvm_threads_daemon", LABEL_NAMES, LABEL_VALUES),
            .0000001);
    assertEquals(
            301L,
            registry.getSampleValue(
                    "jvm_threads_peak", LABEL_NAMES, LABEL_VALUES),
            .0000001);
    assertEquals(
            503L,
            registry.getSampleValue(
                    "jvm_threads_started_total", LABEL_NAMES, LABEL_VALUES),
            .0000001);
    assertEquals(
        3L,
            registry.getSampleValue(
            "jvm_threads_deadlocked", LABEL_NAMES, LABEL_VALUES),
        .0000001);
    assertEquals(
            3L,
            registry.getSampleValue(
            "jvm_threads_deadlocked_monitor", LABEL_NAMES, LABEL_VALUES),
            .0000001);
  }
}
