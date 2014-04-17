package com.x.server.msg;

import java.net.DatagramPacket;

import tutorial.Basemessage.BaseMessage;

import com.x.server.udpserver.UdpService;

public class SendMessage {
	private DatagramPacket packet;
	private Integer playerId;
	
	private boolean isSending = false;
	
	public void clean(){
		packet = null;
		playerId = 0;
		isSending = false;
	}
	
	public void send(){
		try {
			System.out.println("ÏûÏ¢·¢ËÍ: " + playerId);
			UdpService.datagramSocket.send(packet);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
	}
	
	public DatagramPacket getPacket() {
		return packet;
	}
	public void setPacket(DatagramPacket packet) {
		this.packet = packet;
	}
	public Integer getPlayerId() {
		return playerId;
	}
	public void setPlayerId(Integer playerId) {
		this.playerId = playerId;
	}
	
	public boolean isSending() {
		return isSending;
	}
	public void setSending(boolean isSending) {
		this.isSending = isSending;
	}
	public void setMsg(BaseMessage msg){
		packet.setData(msg.toByteArray());
	}
}
