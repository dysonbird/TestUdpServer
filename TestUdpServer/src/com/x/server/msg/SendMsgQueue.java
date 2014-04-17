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
					if(readPoint>=players.size()){//����Խ���ˣ���ͷ��
						readPoint=0;
					}
					Integer player = players.get(readPoint);
					if(usersQueue.containsKey(player)){
						sm = usersQueue.get(player).peekFirst();
						if(sm!=null){
							//�����
							AtomicBoolean lock = world.getActorSendMessageGetLock(sm.getPlayerId());
							if(lock != null && !lock.get()){//û�б���ס���Ͽ���
								readPoint++;//��ȡ���ƶ�����һ������
								sm = usersQueue.get(player).poll();//�������
								break;
							}else{
								sm = null;//�����ԭ��¼
							}
						}
					}else{//�����쳣����ȫ
						LinkedList<SendMessage> sms = new LinkedList<SendMessage>();
						usersQueue.put(player, sms);
//						logger.error("����Ҷ��л�ȡ���ݳ����Ҳ�����Ҷ��У�player="+player);
					}
					if(count<=0){//��ѯ�����еĶ����ˣ��Ҳ������߰�
						break;
					}
					readPoint++;//����һ������
				}		
				return sm;
			}
		}catch(Exception ex){
//			logger.error("��socket��Ҷ����л�ȡ���ݳ�����",ex);
			return null;
		}
		
	}
	
	public boolean putMessage(SendMessage sm) {
		try{
			Integer player = sm.getPlayerId();
			Object session = world.getSessionByAccoutid(player);
			if(session==null){
//				logger.error("�и����û�ж�Ӧ�ĻỰʵ����playerid="+player+",command="+sm.getCommand());
				return false;
			}else{
				synchronized(usersQueue){
					if(usersQueue.containsKey(player)){
						LinkedList<SendMessage> userQueue = usersQueue.get(player);
						if(userQueue.size()>=USER_QUEUE_MAXSIZE){//��ֹ���й���
//							logger.error("usersQueue��Ҷ��й����ˣ�playerid="+player);
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
//			logger.error("������Ϣ��ʱ�������",ex);
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
