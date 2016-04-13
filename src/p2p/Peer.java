package p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import p2p.utility.PeerLog;
import p2p.utility.PeerTable;

public class Peer {

	private PeerTable peerTable;
	
	private ServerSocket serverSocket;
	private Integer peerPort;
	private PeerTableUpdateThread updateThread;
	
	public Peer(Integer peerPort){
		this.peerTable = new PeerTable();
		this.peerPort = peerPort;
		try {
			this.serverSocket = new ServerSocket(this.peerPort);
		} catch (IOException e) {
			PeerLog.logMessage(getLogName(), "Unable to create server socket.");
			e.printStackTrace();
		}
		this.updateThread = new PeerTableUpdateThread(this);
		this.updateThread.start();
		PeerLog.logMessage(getLogName(), "Peer created on port "+this.peerPort);
	}
	
	
	
	public synchronized PeerTable getPeerTable(){
		return this.peerTable;
	}
	
	protected synchronized Socket connectToPeer(PeerTable.TableEntry peer){
		try {
			Socket connectionSocket = new Socket(peer.getIP(), peer.getPort());
			return connectionSocket;
		} catch (IOException e) {
			PeerLog.logMessage(getLogName(), "Could not connect to peer.");
			e.printStackTrace();
			return null;
		}
		
	}
	
	protected synchronized void exchangePeerTableWithNode(Socket connectedPeer, PeerTable localTable){
		PeerLog.logMessage(getLogName(), "Exchanging PeerTable with connected Node..");
	}
	
	
	public void closePeer(){
		if (this.serverSocket != null){
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (this.updateThread != null){
			updateThread.stopUpdateThread();
		}
	}
	
	private String getLogName(){
		return "Peer("+this.peerPort+")";
	}
	
	
}
