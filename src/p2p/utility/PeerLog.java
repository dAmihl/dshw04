package p2p.utility;

/**
 * Simple log class for thread-safe logging.
 * @author dAmihl
 *
 */
public class PeerLog {

	public synchronized static void logMessage(String name, String msg){
		System.out.println("["+name+"]: "+msg);
	}
	
}
