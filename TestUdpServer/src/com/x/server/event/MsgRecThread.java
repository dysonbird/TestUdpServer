package com.x.server.event;

import java.net.DatagramPacket;

import tutorial.Basemessage.BaseMessage;

import com.x.server.common.World;
import com.x.server.udpserver.UdpService;

public class MsgRecThread extends Thread {
	private World world = World.getInstance();

	public void run() {
		while (true) {
			try {
				byte[] buffer = new byte[1024 * 64]; // 缓冲区
				DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
				UdpService.receive(packet);
				
				byte[] data = new byte[packet.getLength()];  
	            System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());  
				
				// 获得事件实例
	            BaseMessage bm = (BaseMessage) BaseMessage.parseFrom(data);
	            System.out.println("消息接收: " + bm.getStrValue(0));
				ServerEvent event = world.getServerEvent();
				event.setPacket(packet);
				event.setbMsg(bm);
				boolean rs = world.putEvent(event);
				System.out.println("事件入列结果: " + rs);
			} catch (Exception e) {
//				e.printStackTrace();
			}
			// Thread.sleep(1 * 1000);
		}
	}
}
