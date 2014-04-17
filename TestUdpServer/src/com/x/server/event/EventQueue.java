package com.x.server.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.x.server.common.World;

public class EventQueue {
	private static World world = World.getInstance();
	
	private static EventQueue queue = null;
	
	private static int QUEUE_MAX_SIZE = 100;//用户队列最大请求数
	
	private int readPoint = 0;
	private HashMap<Integer,LinkedList<ServerEvent>> usersQueue = new HashMap<Integer,LinkedList<ServerEvent>>();
	private List<Integer> players = new ArrayList<Integer>();
	
	private EventQueue(){}
	
	public static synchronized EventQueue getInstance(){
		if(queue==null){
			queue = new EventQueue();
		}
		return queue;
	}
	
	public synchronized ServerEvent getEvent() {
		if(players.size()==0){
			return null;
		}
		ServerEvent event = null;
		int count = players.size();
		while(true){
			count--;
			if(readPoint>=players.size()){//队列越界了，从头起
				readPoint=0;
			}
			Integer player = players.get(readPoint);
			if(usersQueue.containsKey(player)){
				event = usersQueue.get(player).peekFirst();
				if(event!=null){
					//检查锁
					AtomicBoolean lock = world.getActorEventGetLock(event.getPlayerId());
					if(!lock.get()){//没有被锁住，赶快拿
						readPoint++;//获取点移动到下一个队列
						event = usersQueue.get(player).poll();//对象出列
						break;
					}else{
						event = null;//清除掉原纪录
					}
				}
			}else{//出现异常，补全
				LinkedList<ServerEvent> events = new LinkedList<ServerEvent>();
				usersQueue.put(player, events);
//				logger.error("从玩家队列获取数据出错，找不到玩家队列，player="+player);
			}
			if(count==0){//轮询了所有的队列了，找不到就走吧
				break;
			}
			readPoint++;//找下一个队列
		}		
		return event;
	}

	public synchronized boolean putEvent(ServerEvent event) {
		Integer player = event.getPlayerId();
		if(usersQueue.containsKey(player)){
			LinkedList<ServerEvent> queue = usersQueue.get(player);
			if(player!=0 && queue.size()>QUEUE_MAX_SIZE){
//				logger.error("玩家playerid="+player+"的消息队列已经大于"+QUEUE_MAX_SIZE+"了,当前值="+queue.size());
				return false;
			}
			queue.offer(event);
		}else{
			LinkedList<ServerEvent> events = new LinkedList<ServerEvent>();
			events.offer(event);
			usersQueue.put(player, events);
			players.add(player);
		}
		return true;
	}
	
	public synchronized boolean actorLeave(long player){
		players.remove(Long.valueOf(player));
		usersQueue.remove(Long.valueOf(player));
		return true;
	}
	
	
}
