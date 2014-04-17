package com.x.server.event;

import org.apache.commons.pool.PoolableObjectFactory;


/**
 * Class name:ServerEventFactory
 * Description:用于生产服务器事件的工厂类，提供给对象池使用
 */
public class ServerEventFactory implements PoolableObjectFactory {
	
	public ServerEventFactory(){
		super();
	}

	public void activateObject(Object arg0) throws Exception {
		
	}

	public void destroyObject(Object arg0) throws Exception {
		ServerEvent event = (ServerEvent)arg0;
//		event.setConData(null);
		event.setPacket(null);
		event.setbMsg(null);
		event.setPlayerId(0);
		event.setInHandleQueue(false);
		event = null;
	}

	public Object makeObject() throws Exception {
		ServerEvent event = new ServerEvent();
		return event;
	}

	public void passivateObject(Object arg0) throws Exception {
		ServerEvent event = (ServerEvent)arg0;
		event.setPacket(null);
		event.setbMsg(null);
		event.setPlayerId(0);
		event.setInHandleQueue(false);
//		event.reset();
//		event.setChannelHandlerContent(null);
//		event.setCommand(0);
//		event.setActorid(0);
//		event.setSessionId(0);
//		event.setTimestamp(0);
//		event.setConnectType(ServerEvent.CONNECT_TYPE_SOCKET);
//		event.setEventtype(ServerEvent.EVENT_TYPE_CLIENT);
//		event.setMultiRequest(false);
	}

	public boolean validateObject(Object arg0) {
		return true;
	}
	
	

}
