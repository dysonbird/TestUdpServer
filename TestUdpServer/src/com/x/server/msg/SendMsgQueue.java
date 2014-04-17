package com.x.server.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.x.server.common.World;

public class SendMsgQueue {
	private final static int USER_QUEUE_MAXSIZE = 200;
	private static World world = World.getInstance();
	private static SendMsgQueue queue = null;
	
	private int readPoint = 0;
	private HashMap<Integer,LinkedList<SendMessage>> usersQueue = new HashMap<Integer,LinkedList<SendMessage>>();
	private List<Integer> players = new ArrayList<Integer>();
	
	private SendMsgQueue(){}
	
	public static synchronized SendMsgQueue getInstance() {
		if (queue == null) {
			queue = new SendMsgQueue();
		}
		return queue;
	}
	
	public SendMessage getMsg() {
		try{
			synchronized(usersQueue){
				if(players.size()==0){
					return null;
				}
				SendMessage sm = null;
				int count = players.size();
				while(true){
					count--;
					if(readPoint>=players.size()){//队列越界了，从头起
						readPoint=0;
					}
					Integer player = players.get(readPoint);
					if(usersQueue.containsKey(player)){
						sm = usersQueue.get(player).peekFirst();
						if(sm!=null){
							//检查锁
							AtomicBoolean lock = world.getActorSendMessageGetLock(sm.getPlayerId());
							if(lock != null && !lock.get()){//没有被锁住，赶快拿
								readPoint++;//获取点移动到下一个队列
								sm = usersQueue.get(player).poll();//对象出列
								break;
							}else{
								sm = null;//清除掉原纪录
							}
						}
					}else{//出现异常，补全
						LinkedList<SendMessage> sms = new LinkedList<SendMessage>();
						usersQueue.put(player, sms);
//						logger.error("从玩家队列获取数据出错，找不到玩家队列，player="+player);
					}
					if(count<=0){//轮询了所有的队列了，找不到就走吧
						break;
					}
					readPoint++;//找下一个队列
				}		
				return sm;
			}
		}catch(Exception ex){
//			logger.error("从socket玩家队列中获取数据出错了",ex);
			return null;
		}
		
	}
	
	public boolean putMessage(SendMessage sm) {
		try{
			Integer player = sm.getPlayerId();
			Object session = world.getSessionByAccoutid(player);
			if(session==null){
//				logger.error("有个玩家没有对应的会话实例，playerid="+player+",command="+sm.getCommand());
				return false;
			}else{
				synchronized(usersQueue){
					if(usersQueue.containsKey(player)){
						LinkedList<SendMessage> userQueue = usersQueue.get(player);
						if(userQueue.size()>=USER_QUEUE_MAXSIZE){//防止队列过大
//							logger.error("usersQueue玩家队列过大了，playerid="+player);
							userQueue.poll();
						}
						userQueue.offer(sm);
					}else{
						LinkedList<SendMessage> sms = new LinkedList<SendMessage>();
						sms.offer(sm);
						usersQueue.put(player, sms);
						players.add(player);
					}
				}
			}
			return true;
		}catch(Exception ex){
//			logger.error("插入消息的时候出错了",ex);
			return false;
		}
	}
	
	public boolean actorLeave(Integer player){
		synchronized(usersQueue){
			players.remove(player);
			usersQueue.remove(player);
		}
		return true;
	}
}
