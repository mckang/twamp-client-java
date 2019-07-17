package com.bdwise.twamp.client.reporter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.bdwise.twamp.client.event.ControlSessionClosedEvent;
import com.bdwise.twamp.client.event.ReceivePacketEvent;
import com.bdwise.twamp.client.event.StopClientEvent;

@Component
public class GraphiteWriter implements InitializingBean {

	@Value("${carbon.server.address}")
	private String carbonServerAddress;

	@Value("${carbon.server.port}")
	private int carbonServerPort;

	@Value("${twamp.server.address}")
	private String server = null;
	
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	private SimpleGraphiteClient graphiteClient;

	private DescriptiveStatistics stats;

	private Timer ticker;

	@Async
	@EventListener
	public void handleEvent(ReceivePacketEvent receivePacketEvent) {
		stats.addValue(receivePacketEvent.getDelay());
		System.out.println(stats.toString());

	}

	@EventListener
	@Async
	public void handleControlSessionStopEvent(ControlSessionClosedEvent controlSessionClosedEvent) throws IOException {
		System.out.println(stats.toString());
		applicationEventPublisher.publishEvent(new StopClientEvent(this));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		graphiteClient = new SimpleGraphiteClient(this.carbonServerAddress, this.carbonServerPort);
		server = server.replaceAll("\\.", "_");
		stats = new DescriptiveStatistics();
		stats.setWindowSize(5);
		ticker = new Timer();
		ticker.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {

				Map<String, Number> allAnswers = new HashMap<String, Number>();

				allAnswers.put(String.format("pSonar.latency.%s.delay.min.%s", "twping-java", server),
						stats.getMin());
				allAnswers.put(String.format("pSonar.latency.%s.delay.max.%s", "twping-java", server),
						stats.getMax());
				allAnswers.put(String.format("pSonar.latency.%s.delay.mean.%s", "twping-java", server),
						stats.getMean());
				allAnswers.put(String.format("pSonar.latency.%s.delay.95.%s", "twping-java", server),
						stats.getPercentile(0.95));

				graphiteClient.sendMetrics(allAnswers);
			}
		}, 1000, 5000);
	}
}
