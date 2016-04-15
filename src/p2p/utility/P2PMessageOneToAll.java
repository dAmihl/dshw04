package p2p.utility;

import java.io.Serializable;
import java.util.ArrayList;


public class P2PMessageOneToAll implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7277757424645047045L;
	
	private ArrayList<PeerTable.TableEntry> visitedPeers;
	private String message;
	private PeerTable.TableEntry sender;
	
	public P2PMessageOneToAll(String message, PeerTable.TableEntry senderEntry){
		this.message = message;
		this.sender = senderEntry;
		this.visitedPeers = new ArrayList<>();
	}


	public ArrayList<PeerTable.TableEntry> getVisitedPeers() {
		return visitedPeers;
	}

	public String getMessage() {
		return message;
	}

	public PeerTable.TableEntry getSender() {
		return sender;
	}
	
	public void peerVisited(PeerTable.TableEntry peer){
		this.visitedPeers.add(peer);
	}
	
	public boolean isPeerAlreadyVisited(PeerTable.TableEntry peer){
		return this.visitedPeers.contains(peer) || this.sender.equals(peer);
	}
	
	
	
	

}
