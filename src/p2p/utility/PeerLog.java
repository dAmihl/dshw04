package p2p.utility;

public class PeerLog {

	public synchronized static void logMessage(String name, String msg){
		System.out.println("["+name+"]: "+msg);
	}
	
}
