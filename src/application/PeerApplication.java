package application;

import java.util.ArrayList;

import p2p.Peer;

public class PeerApplication {
	
	
	/**
	 * Main test application
	 * creates 3*N peers and connects them in a "line" by
	 * adding the address of the peer previously created to the new peer. (PeerTable entry)
	 */
	
	private static Integer START_PORT = 1234;
	
 public static void main(String[] args) {
	System.out.println("PeerApplication started.");
	
	/**
	 * Create 4 peers
	 * P2 -> P1
	 * P3 -> P1
	 * P4 -> P2
	 * 
	 * What should do: P2 and P1 exchange addresses: 
	 * 	P1 has now address of P4 and 
	 * 	P2 has address of P3
	 * 
	 * Next update: P2 and P4 exchange tables: P4 has now address of P3
	 * 	and P3 has Address of P4.
	 */
	
	Integer N;
	
	if (args.length == 0){
		N = 3;
	}else{
		N = Integer.parseInt(args[0]);
	}
	
	createNetwork(N);
	
	
 }
 
 
 private static void createNetwork(Integer N){
	 	
	 	System.out.println("Creating network of 3*"+N+" nodes.");
	 
	 	ArrayList<Peer> peers = new ArrayList<>();
		
		for (int i = 0; i < 3*N; i++){
			Peer tmpPeer = new Peer(START_PORT+i, "P"+i, N);
			tmpPeer.addConnection("127.0.0.1", START_PORT+i-1);
			peers.add(tmpPeer);
		}
		
		
		/**
		 * For message demonstration.
		 * after 11 seconds, a peer sends a message to all his entries in peer table.
		 * One update cycle should be already done by 11 seconds.
		 */
		try {
			Thread.sleep(11000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Creating test message");
		peers.get(2).sendOneToAllMessage("Hello!!");
		
 }
 
}
