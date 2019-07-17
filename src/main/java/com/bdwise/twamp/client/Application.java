package com.bdwise.twamp.client;

import java.io.IOException;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;

import com.bdwise.twamp.client.event.StopClientEvent;

@EnableAsync(proxyTargetClass = true)
@SpringBootApplication
public class Application implements  ApplicationContextAware {
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	
	private ApplicationContext applicationContext;
	public static void main(String[] args) throws Exception{
		SpringApplication application = new SpringApplication(Application.class);
		application.addListeners(new ApplicationPidFileWriter("./bin/app.pid"));
		application.run(args);
//		Thread.sleep(10000);
//		SpringApplication.exit(context, () -> 0);
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
		
		return new ConcurrentHashMap<Long, Packet>();
	}
	
	
	@Bean
	public ReflectHandler reflectHandler(@Qualifier("packetMap") Map<Long, Packet> packets, @Value("${twamp.test.count}") int packetCount ){
		return new ReflectHandler(packets, packetCount);
	}
	
	@Bean
	public UdpChannelManager udpChannelManager(@Qualifier("paddingLength")  int paddingLength, @Qualifier("packetMap") Map<Long, Packet> packets, @Value("${twamp.test.count}") int packetCount, ReflectHandler reflectHandler ) throws SocketException {
		return new UdpChannelManager( paddingLength, packets, packetCount, reflectHandler);
	}
	
	@Bean
	public TickManager tickManager(@Value("${twamp.test.count}") int packetCount) {
		return new TickManager(packetCount);
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
	
	@EventListener
	public void  handleStopClientEvent(StopClientEvent stopClientEvent) throws IOException {
		logger.info("stopping spring context........");
		SpringApplication.exit(applicationContext);
	}
	


}
