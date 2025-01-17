package com.sonyericsson.jenkins.plugins.bfa;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

import com.sonyericsson.jenkins.plugins.bfa.model.FailureCause;
import com.sonyericsson.jenkins.plugins.bfa.model.IFailureCauseMetricData;
import com.sonyericsson.jenkins.plugins.bfa.model.indication.BuildLogIndication;
import com.sonyericsson.jenkins.plugins.bfa.model.indication.Indication;
import jenkins.metrics.api.Metrics;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.sonyericsson.jenkins.plugins.bfa.MetricsManager.addMetric;
import static com.sonyericsson.jenkins.plugins.bfa.MetricsManager.incCounters;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * Tests for {@link MetricsManager}.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({MetricRegistry.class, Metrics.class})
public class MetricsManagerTest {
    @Mock
    private MetricRegistry metricRegistry;
    @Mock
    private Counter counter;

    private FailureCause mockedCause;
    private List<? extends IFailureCauseMetricData> mockedCauseList;

    /**
     * Common stuff to set up for the tests.
     */
    @Before
    public void setUp() {
        List<Indication> indications = new LinkedList<>();
        Indication indication = new BuildLogIndication("something");
        indications.add(indication);
        mockedCause = new FailureCause("id", "myFailureCause", "description", "comment", new Date(),
                "category", indications, null);
        mockedCauseList = new ArrayList<>(Arrays.asList(mockedCause, mockedCause));

        PowerMockito.mockStatic(Metrics.class);
        PowerMockito.when(Metrics.metricRegistry()).thenReturn(metricRegistry);
        PowerMockito.when(metricRegistry.counter(Mockito.anyString())).thenReturn(counter);
    }

    /**
     * Test that the case and category counters are created from a FailureCause.
     */
    @Test
    public void testAddMetric() {
        addMetric(mockedCause);

        verify(metricRegistry, times(1)).counter("jenkins_bfa.cause.myFailureCause");
        verify(metricRegistry, times(1)).counter("jenkins_bfa.category.category");
    }

    /**
     * Test that the cause and category counters are incremented twice for a FailureCause when not using squashing.
     */
    @Test
    public void testIncCountersWithSquashingDisabled() {
        incCounters(mockedCauseList, false);

        verify(counter, times(mockedCauseList.size() * 2)).inc();
    }

    /**
     * Test that the cause and category counters are incremented once for a FailureCause when using squashing.
     */
    @Test
    public void testIncCountersWithSquashingEnabled() {
        incCounters(mockedCauseList, true);

        verify(counter, times(mockedCauseList.size())).inc();
    }

}
