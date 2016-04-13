package application;

import p2p.Peer;

public class PeerApplication {
 public static void main(String[] args) {
	System.out.println("PeerApplication started.");
	
	Peer p1 = new Peer(1234);
	Peer p2 = new Peer(1235);
 }
}
