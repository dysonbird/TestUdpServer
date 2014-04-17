package com.x.server.common;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.pool.impl.StackObjectPool;

import com.x.server.event.EventQueue;
import com.x.server.event.ServerEvent;
import com.x.server.event.ServerEventFactory;
import com.x.server.msg.SendMessage;
import com.x.server.msg.SendMessageFactory;
import com.x.server.msg.SendMsgQueue;

public class World {
	/** ��ɫ��Ϣ���ͻ�ȡ�� */
	public HashMap<Integer, AtomicBoolean> actorSendMessageGetLocks = new HashMap<Integer, AtomicBoolean>();
	/** ��ɫ�¼���ȡ�� */
	public HashMap<Integer, AtomicBoolean> actorEventGetLocks = new HashMap<Integer, AtomicBoolean>();
	/** �����е����лỰ��Ϣ */
	public ConcurrentHashMap<Long, Object> allSessions = new ConcurrentHashMap<Long, Object>();
	
	public static EventQueue queueManage = EventQueue.getInstance();// ����¼�����
	public static SendMsgQueue sendMessageQueueManage = SendMsgQueue.getInstance();
	
	private static StackObjectPool sendpool = ObjectPoolManage.getPool(SendMessageFactory.class, 50000);
	private static StackObjectPool serverEventPool = ObjectPoolManage.getPool(ServerEventFactory.class, 10000);
	
	private static World world;//����ʵ��
	
	private World() {
		System.out.println("***************world ʵ����***************");
	}

	public synchronized static World getInstance() {
		if (world == null) {
			world = new World();
			world.init();
		}
		return world;
	}
	
	public void init() {

	}
	
	/**
	 * 
	 * Function name:getActorSendMessageGetLock Description: ��ȡ��ɫ������Ϣ��ȡ��
	 * 
	 * @param player���˺�id
	 * @return�����ط�����Ϣ������
	 */
	public AtomicBoolean getActorSendMessageGetLock(Integer player) {
		if (actorSendMessageGetLocks.containsKey(player)) {
			return actorSendMessageGetLocks.get(player);
		} else {
			AtomicBoolean ab = new AtomicBoolean(false);
			actorSendMessageGetLocks.put(player, ab);
			return ab;
		}
	}
	
	public Object getSessionByAccoutid(Integer accountid) {
		return new Object();
//		return allSessions.get(accountid);
	}
	
	/**
	 * Function name:getSendMessage Description: �Ӷ���ػ�÷�����Ϣ��
	 * @param level����Ϣ��С����
	 * @param command��������
	 * @param actor: ��ɫʵ��
	 * @return��������Ϣ��
	 */
	public SendMessage getSendMessage(ServerEvent event) {
		try {
			synchronized (sendpool) {
				SendMessage sm = (SendMessage) sendpool.borrowObject();
				sm.setPacket(event.getPacket());
				sm.setPlayerId(event.getPlayerId());
				return sm;
			}
		} catch (Exception e) {
			e.printStackTrace();
//			logger.error("��ȡSendMessage�������");
			return null;
		}
	}
	
	/**
	 * Function name:returnSendMessage Description: ������Ϣ�嵽�����
	 * @param msg����Ϣ��
	 */
	public void returnSendMessage(SendMessage msg) {
		if (msg == null) {
			return;
		}
		try {
			msg.setSending(false);
			msg.clean();
			synchronized (sendpool) {
//				minSendMessageNum--;
				sendpool.returnObject(msg);
			}
		} catch (Exception e) {
//			logger.error("����SendMessage������س���");
		}
	}
	
	/**
	 * Function name:putEvent Description: ���¼����뵽�¼����У��Թ�ϵͳ����
	 * @param event���¼�
	 * @return�����ؽ��
	 */
	public boolean putEvent(ServerEvent event) {
		if (event.isInHandleQueue() == true) {
//			logger.error("һ��ServerEvent����� ʹ�ã�command=" + event.getCommand());
			return false;
		}
		event.setInHandleQueue(true);
//		event.reset();
		boolean rs = queueManage.putEvent(event);
		if (rs == false) {// ����ʧ�ܣ�ֱ�ӷ��ض����
			returnServerEvent(event);
		}
		return rs;
	}
	
	/**
	 * Function name:returnServerEvent Description: �ѷ������¼����󷵻ظ������
	 * @param event���������¼�����
	 */
	public void returnServerEvent(ServerEvent event) {
		// System.out.println("returnServerEvent:"+event.toString()+":"+event.getCommand());
		synchronized (serverEventPool) {
			try {
				event.setInHandleQueue(false);
//				event.reset();
//				serverEventNum--;
				serverEventPool.returnObject(event);
			} catch (Exception e) {
//				logger.error("����ServerEvent������س���");
			}
		}
	}
	
	/**
	 * Function name:getServerEvent Description: ͨ�����������һ���������¼�����
	 * @return���������¼�����
	 */
	public ServerEvent getServerEvent() {
		synchronized (serverEventPool) {
			try {
				ServerEvent event = (ServerEvent) serverEventPool.borrowObject();
//				serverEventNum++;
				return event;
			} catch (Exception e) {
//				logger.error("��ȡServerEvent�������");
				return new ServerEvent();
			}
		}
	}
	
	/**
	 * Function name:getActorEventGetLock Description: ��ȡ��ɫ�¼���ȡ��
	 * @param player���˺�id
	 * @return�������¼�������
	 */
	public AtomicBoolean getActorEventGetLock(Integer player) {
		if (actorEventGetLocks.containsKey(player)) {
			return actorEventGetLocks.get(player);
		} else {
			AtomicBoolean ab = new AtomicBoolean(false);
			actorEventGetLocks.put(player, ab);
			return ab;
		}
	}
	
	/**
	 * Function name:replyMessage Description: �ظ���Ϣ
	 * @param sm����Ϣʵ��
	 * @param event���¼�(null��ʱ��ȡ���У�ֱ�ӻظ�)
	 */
	public void replyMessage(SendMessage sm, ServerEvent event) {
		if (sm.isSending() == true) {
//			logger.error("��һ��SendMessage������ʹ����,command=" + sm.getCommand());
			return;
		}
		sm.setSending(true);
		boolean rs = sendMessageQueueManage.putMessage(sm);
		if (rs == false) {// ����ʧ���ˣ���Ҫ�ֶ��Ļ���
			returnSendMessage(sm);
		}
	}
}
