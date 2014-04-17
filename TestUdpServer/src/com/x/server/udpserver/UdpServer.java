package com.x.server.udpserver;

import com.x.server.event.EventHandleThread;
import com.x.server.event.MsgRecThread;
import com.x.server.msg.MsgSendThread;

public class UdpServer {
	
	public static void main(String args[]){
		//����������
		UdpService.startServer(2333);
		
		//������Ϣ�����߳�
		MsgRecThread recTh = new MsgRecThread();
		recTh.setName("MsgRecThread");
		recTh.start();
		
		//�����¼��ַ��߳�
		EventHandleThread eht = new EventHandleThread(10);
		eht.setName("EventHandleThread");
		eht.start();
		
		//������Ϣ�����߳�
		MsgSendThread msgSendTh = new MsgSendThread(200);
		msgSendTh.setName("MsgSendThread");
		msgSendTh.start();
	}
}
