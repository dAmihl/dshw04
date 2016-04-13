package application;

import java.net.InetAddress;
import java.net.UnknownHostException;

import p2p.Peer;
import p2p.utility.PeerTable;

public class PeerApplication {
 public static void main(String[] args) {
	System.out.println("PeerApplication started.");
	
	Peer p1 = new Peer(1234);
	Peer p2 = new Peer(1235);
	try {
		p2.getPeerTable().addEntry(new PeerTable.TableEntry(InetAddress.getByName("127.0.0.1"), 1234));
	} catch (UnknownHostException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
 }
}
