package com.x.server.udpserver;

import com.x.server.event.EventHandleThread;
import com.x.server.event.MsgRecThread;
import com.x.server.msg.MsgSendThread;

public class UdpServer {
	
	public static void main(String args[]){
		//启动服务器
		UdpService.startServer(2333);
		
		//启动消息接收线程
		MsgRecThread recTh = new MsgRecThread();
		recTh.setName("MsgRecThread");
		recTh.start();
		
		//启动事件分发线程
		EventHandleThread eht = new EventHandleThread(10);
		eht.setName("EventHandleThread");
		eht.start();
		
		//启动消息发送线程
		MsgSendThread msgSendTh = new MsgSendThread(200);
		msgSendTh.setName("MsgSendThread");
		msgSendTh.start();
	}
}
