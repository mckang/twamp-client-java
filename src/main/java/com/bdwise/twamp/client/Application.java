package com.bdwise.twamp.client;

import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;

import com.bdwise.twamp.client.event.ControlSessionClosedEvent;
import com.bdwise.twamp.client.event.DatagramSessionClosedEvent;

@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
public class Application implements  ApplicationContextAware {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	private ApplicationContext applicationContext;
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Bean
	public HandshakeHandler inboundHandler(@Qualifier("paddingLength")  int paddingLength){
		return new HandshakeHandler(paddingLength);
	}
	
	@Bean
	public HandshakeManager handshakeManager(HandshakeHandler handler){
		return new HandshakeManager(handler);
	}
	
	@Bean
	@Qualifier("packetMap")
	public Map<Long, Packet> packetMap(){
		
		return new HashMap<Long, Packet>();
	}
	
	
	@Bean
	public TestManager testManager(DatagramSocketManager datagramSocketManager, @Qualifier("paddingLength")  int paddingLength, @Qualifier("packetMap") Map<Long, Packet> packets) {
		return new TestManager(datagramSocketManager.getDatagramSocket(), paddingLength, packets);
	}
	
	@Bean
	public ReflectManager reflectManager(DatagramSocketManager datagramSocketManager, @Qualifier("packetMap") Map<Long, Packet> packets) {
		return new ReflectManager(datagramSocketManager.getDatagramSocket(), packets);
	}
	
	@Bean
	public DatagramSocketManager datagramSocketManager() throws SocketException {
		return new DatagramSocketManager();
	}
	
	@Bean
	@Qualifier("paddingLength")
	public int paddingLength(@Value("${twamp.test.packet.size}") int packetSize) {
		int paddingLength = 0;
		if(packetSize > 14) {
			paddingLength = packetSize - 14;
		} else {
			paddingLength = 0;
		}
		return paddingLength;
	}

	
	private boolean isControlSessionStopped = false;
	private boolean isDatagramSessionStopped = false;
	
	@EventListener
	public void  handleControlSessionStopEvent(ControlSessionClosedEvent controlSessionClosedEvent) throws IOException {
		logger.info("handleControlSessionStopEvent : preparing stopping spring context........");
		isControlSessionStopped = true;
		doStop();
	}
	
	@EventListener
	public void handleDatagramSessionStopEvent(DatagramSessionClosedEvent datagramSessionClosedEvent) throws IOException {
		logger.info("handleDatagramSessionStopEvent : preparing stopping  spring context........");
		isDatagramSessionStopped = true;
		doStop();
	}
	
	private synchronized void doStop() {
		if(isControlSessionStopped && isDatagramSessionStopped) {
			logger.info("stopping spring context........");
			SpringApplication.exit(applicationContext);

		}
	}


}
