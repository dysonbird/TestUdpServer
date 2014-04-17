package com.x.server.event;

import java.net.DatagramPacket;

import tutorial.Basemessage.BaseMessage;

public class ServerEvent {
	private DatagramPacket packet;
	private Integer playerId;
	private BaseMessage bMsg;
	
	private boolean isInHandleQueue = false;
	
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
	public BaseMessage getbMsg() {
		return bMsg;
	}
	public void setbMsg(BaseMessage bMsg) {
		this.bMsg = bMsg;
		if(bMsg != null){
			this.setPlayerId(this.bMsg.getIntValue(0));
		}
	}
	public boolean isInHandleQueue() {
		return isInHandleQueue;
	}
	public void setInHandleQueue(boolean isInHandleQueue) {
		this.isInHandleQueue = isInHandleQueue;
	}
}
