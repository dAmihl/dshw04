package application;

import java.net.InetAddress;
import java.net.UnknownHostException;

import p2p.Peer;
import p2p.utility.PeerTable;

public class PeerApplication {
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
	int START_IP = 1234;
	Peer p1 = new Peer(START_IP);
	Peer p2 = new Peer(START_IP+1);
	Peer p3 = new Peer(START_IP+2);
	Peer p4 = new Peer(START_IP+3);
	try {
		p2.getPeerTable().addEntry(new PeerTable.TableEntry(InetAddress.getByName("127.0.0.1"), 1234));
		p4.getPeerTable().addEntry(new PeerTable.TableEntry(InetAddress.getByName("127.0.0.1"), 1235));
		p3.getPeerTable().addEntry(new PeerTable.TableEntry(InetAddress.getByName("127.0.0.1"), 1234));
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }
}
