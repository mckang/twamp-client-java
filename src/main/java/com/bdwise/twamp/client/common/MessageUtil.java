package com.bdwise.twamp.client.common;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.net.ntp.TimeStamp;

public abstract class MessageUtil {
	public static byte[] fromUnsignedInt(long value) {
		byte[] bytes = new byte[8];
		ByteBuffer.wrap(bytes).putLong(value);

		return Arrays.copyOfRange(bytes, 4, 8);
	}

	public static long toUnsignedInt(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(8).put(new byte[] { 0, 0, 0, 0 }).put(bytes);
		buffer.position(0);

		return buffer.getLong();
	}

	public static byte[] fromUnsignedShort(int value) {
		byte[] bytes = new byte[4];
		ByteBuffer.wrap(bytes).putInt(value);

		return Arrays.copyOfRange(bytes, 2, 4);
	}

	public static long toUnsignedShort(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(4).put(new byte[] { 0, 0 }).put(bytes);
		buffer.position(0);

		return buffer.getInt();
	}

	public static UnixTime getUnixTime() {
		UnixTime twampTime = new UnixTime();
		TimeStamp ts = new TimeStamp(new Date());
		twampTime.setSeconds(ts.getSeconds());
		twampTime.setFraction(ts.getFraction());
		return twampTime;
	}

	
//	int TwampCommon::twampTimeToTimeval (struct twamp_time *time, struct timeval *result)
//	{
//	    result->tv_sec  = (time_t)(time->seconds - TWAMP_BASE_TIME_OFFSET);
//	    result->tv_usec = (time_t)(time->fraction / TWAMP_FLOAT_DENOM);
//	    return 0;
//	}
	
//	private static final long TWAMP_BASE_TIME_OFFSET = 2208988800l;
	private static final double TWAMP_FLOAT_DENOM  = 4294.967296;
	public static double getUnixTimeDiff(UnixTime before, UnixTime after) {
		
		long secs = after.getSeconds() - before.getSeconds();
		double usecs = (after.getFraction() - before.getFraction()) / TWAMP_FLOAT_DENOM;
		if (usecs < 0) {
	        usecs += 1000000;
	        secs--;
	    }
		return (secs * 1000 + (usecs / 1000.0));
//		return after.toTimeValue() - before.toTimeValue();
	}

	public static long ipToLong(String ipAddress) {
		String[] ipAddressInArray = ipAddress.split("\\.");
		long result = 0;
		for (int i = 0; i < ipAddressInArray.length; i++) {

			int power = 3 - i;
			int ip = Integer.parseInt(ipAddressInArray[i]);
			result += ip * Math.pow(256, power);
		}
		return result;
	}
}
