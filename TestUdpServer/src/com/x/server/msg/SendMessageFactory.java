package com.x.server.msg;

import org.apache.commons.pool.PoolableObjectFactory;

public class SendMessageFactory implements PoolableObjectFactory{
	

	public SendMessageFactory(){
		super();
	}
	
	public void activateObject(Object arg0) throws Exception {
		
	}

	public void destroyObject(Object arg0) throws Exception {
		SendMessage msg = (SendMessage)arg0;
		msg.clean();
		msg = null;
	}

	public Object makeObject() throws Exception {
		SendMessage msg = new SendMessage();
		return msg;
	}

	public void passivateObject(Object arg0) throws Exception {
		SendMessage msg = (SendMessage)arg0;
		msg.clean();
	}

	public boolean validateObject(Object arg0) {
		return true;
	}
	
}
