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
	/** 角色消息发送获取锁 */
	public HashMap<Integer, AtomicBoolean> actorSendMessageGetLocks = new HashMap<Integer, AtomicBoolean>();
	/** 角色事件获取锁 */
	public HashMap<Integer, AtomicBoolean> actorEventGetLocks = new HashMap<Integer, AtomicBoolean>();
	/** 世界中的所有会话信息 */
	public ConcurrentHashMap<Long, Object> allSessions = new ConcurrentHashMap<Long, Object>();
	
	public static EventQueue queueManage = EventQueue.getInstance();// 玩家事件队列
	public static SendMsgQueue sendMessageQueueManage = SendMsgQueue.getInstance();
	
	private static StackObjectPool sendpool = ObjectPoolManage.getPool(SendMessageFactory.class, 50000);
	private static StackObjectPool serverEventPool = ObjectPoolManage.getPool(ServerEventFactory.class, 10000);
	
	private static World world;//世界实例
	
	private World() {
		System.out.println("***************world 实例化***************");
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
	 * Function name:getActorSendMessageGetLock Description: 获取角色发送消息获取锁
	 * 
	 * @param player：账号id
	 * @return：返回发送消息锁对象
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
	 * Function name:getSendMessage Description: 从对象池获得发送消息体
	 * @param level：消息大小级别
	 * @param command：命令码
	 * @param actor: 角色实例
	 * @return：发送消息体
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
//			logger.error("获取SendMessage对象出错");
			return null;
		}
	}
	
	/**
	 * Function name:returnSendMessage Description: 返回消息体到对象池
	 * @param msg：消息体
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
//			logger.error("返回SendMessage到对象池出错");
		}
	}
	
	/**
	 * Function name:putEvent Description: 把事件插入到事件队列，以供系统处理
	 * @param event：事件
	 * @return：返回结果
	 */
	public boolean putEvent(ServerEvent event) {
		if (event.isInHandleQueue() == true) {
//			logger.error("一个ServerEvent被多次 使用，command=" + event.getCommand());
			return false;
		}
		event.setInHandleQueue(true);
//		event.reset();
		boolean rs = queueManage.putEvent(event);
		if (rs == false) {// 入列失败，直接返回对象池
			returnServerEvent(event);
		}
		return rs;
	}
	
	/**
	 * Function name:returnServerEvent Description: 把服务器事件对象返回给对象池
	 * @param event：服务器事件对象
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
//				logger.error("返回ServerEvent到对象池出错");
			}
		}
	}
	
	/**
	 * Function name:getServerEvent Description: 通过对象池申请一个服务器事件对象
	 * @return：服务器事件对象
	 */
	public ServerEvent getServerEvent() {
		synchronized (serverEventPool) {
			try {
				ServerEvent event = (ServerEvent) serverEventPool.borrowObject();
//				serverEventNum++;
				return event;
			} catch (Exception e) {
//				logger.error("获取ServerEvent对象出错");
				return new ServerEvent();
			}
		}
	}
	
	/**
	 * Function name:getActorEventGetLock Description: 获取角色事件获取锁
	 * @param player：账号id
	 * @return：返回事件锁对象
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
	 * Function name:replyMessage Description: 回复消息
	 * @param sm：消息实体
	 * @param event：事件(null的时候不取队列，直接回复)
	 */
	public void replyMessage(SendMessage sm, ServerEvent event) {
		if (sm.isSending() == true) {
//			logger.error("有一个SendMessage被两次使用了,command=" + sm.getCommand());
			return;
		}
		sm.setSending(true);
		boolean rs = sendMessageQueueManage.putMessage(sm);
		if (rs == false) {// 入列失败了，需要手动的回收
			returnSendMessage(sm);
		}
	}
}
