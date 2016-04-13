package p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import p2p.utility.P2PMessage;
import p2p.utility.PeerLog;
import p2p.utility.PeerTable;

public class Peer {

	private PeerTable peerTable;
	
	private Integer peerPort;
	private PeerTableUpdateThread updateThread;
	private PeerListenThread listenThread;
	
	
	public Peer(Integer peerPort){
		this.peerTable = new PeerTable();
		this.peerPort = peerPort;
		
		this.updateThread = new PeerTableUpdateThread(this);
		this.updateThread.start();
		
		this.listenThread = new PeerListenThread(this, this.peerPort);
		this.listenThread.start();
		
		PeerLog.logMessage(getLogName(), "Peer created on port "+this.peerPort);
	}
	
	
	protected synchronized void handleConnectedNode(Socket otherNode){
		PeerLog.logMessage(getLogName(), "Node connected. Waiting for message..");
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(otherNode.getInputStream());
			
			
			try {
				// receive other peer table
				PeerTable otherTable = (PeerTable) in.readObject();
				receivedPeerTable(otherNode, otherTable);
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				
				try {
					P2PMessage msg = (P2PMessage) in.readObject();
					receivedMessage(otherNode, msg);
				} catch (ClassNotFoundException e1) {
					e1.printStackTrace();
				}
			}
					
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void receivedPeerTable(Socket receivedFrom, PeerTable otherTable){
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(receivedFrom.getOutputStream());
			
			
			otherTable.removeEntry(getMyPeerTableEntry());
			
			// send my peer table
			PeerTable tableToSend = this.getPeerTable();
			tableToSend.addEntry(getMyPeerTableEntry());
			out.writeObject(tableToSend);
			
			// merge the peer tables
			this.getPeerTable().mergeWithTable(otherTable);
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if (out != null){
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	private void receivedMessage(Socket receivedFrom, P2PMessage msg){
		
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
	
	protected synchronized void sendReceivePeerTableToNode(Socket connectedPeer, PeerTable localTable){
		PeerLog.logMessage(getLogName(), "Exchanging PeerTable with connected Node..");
		
		ObjectOutputStream out;
		ObjectInputStream in;
		try {
			in = new ObjectInputStream(connectedPeer.getInputStream());
			out = new ObjectOutputStream(connectedPeer.getOutputStream());
			
			try {
				//send my peer table
				PeerTable tableToSend = this.getPeerTable();
				tableToSend.addEntry(getMyPeerTableEntry());
				out.writeObject(tableToSend);
				
				PeerLog.logMessage(getLogName(), "PeerTable sent, waiting for response..");
				
				// receive other peer table
				PeerTable otherTable = (PeerTable) in.readObject();
				otherTable.removeEntry(getMyPeerTableEntry());
				
				//merge
				this.getPeerTable().mergeWithTable(otherTable);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
					
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void closePeer(){
		if (this.listenThread != null){
			this.listenThread.stopListenThread();
		}
		
		if (this.updateThread != null){
			updateThread.stopUpdateThread();
		}
	}
	
	protected String getLogName(){
		return "Peer("+this.peerPort+")";
	}
	
	protected PeerTable.TableEntry getMyPeerTableEntry(){
		return new PeerTable.TableEntry(this.listenThread.getServerSocketIP(), this.listenThread.getServerSocketPort());
	}
	
	
}
