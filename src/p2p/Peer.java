package p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import p2p.utility.P2PMessageOneToAll;
import p2p.utility.PeerLog;
import p2p.utility.PeerTable;
import p2p.utility.PeerTable.TableEntry;

/**
 * The main peer class.
 * This holds 2 Threads:
 * 	- A server listen thread, which listens on a given ServerSocket for incoming connections of other peers.
 * 	- A update thread, which periodically connects to a random Node (chosen from Peertable) and 
 * 		synchronizes the PeerTables with it.
 * 
 * 
 * 
 * @author dAmihl
 *
 */


public class Peer {

	private PeerTable peerTable;
	
	private String peerName;
	
	private Integer peerPort;
	private PeerTableUpdateThread updateThread;
	private PeerListenThread listenThread;
	
	private Integer sizeTable;
	
	
	public Peer(Integer peerPort, Integer sizeTable){
		this.peerTable = new PeerTable();
		this.peerPort = peerPort;
		this.peerName = "UNNAMED";
		this.sizeTable = sizeTable;
		
		this.updateThread = new PeerTableUpdateThread(this);
		this.updateThread.start();
		
		this.listenThread = new PeerListenThread(this, this.peerPort);
		this.listenThread.start();
		
		addShutdownHook();
		
		PeerLog.logMessage(getLogName(), "Peer created on port "+this.peerPort);
	}
	
	public Peer(Integer peerPort, String peerName, Integer sizeTable){
		this.peerTable = new PeerTable();
		this.peerPort = peerPort;
		this.peerName = peerName;
		this.sizeTable = sizeTable;
		
		this.updateThread = new PeerTableUpdateThread(this);
		this.updateThread.start();
		
		this.listenThread = new PeerListenThread(this, this.peerPort);
		this.listenThread.start();
		
		addShutdownHook();
		
		PeerLog.logMessage(getLogName(), "Peer created on port "+this.peerPort);
	}
	
	private void addShutdownHook(){
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			
			@Override
			public void run() {
				closePeer();
			}
		}));
	}
	
	
	/**
	 * Handles a connected node, called by ListenThread (ServerSocket)
	 * Responds to incoming messages, which can be
	 * 	1. A PeerTable, which indicates a synchronization of the tables or
	 * 	2. A message, which has to be forwarded to this peers known nodes.
	 * @param otherNode
	 */
	protected synchronized void handleConnectedNode(Socket otherNode){
		PeerLog.logMessage(getLogName(), "Node connected. Waiting for message..");
		ObjectInputStream in = null;
		Object readObject = null;
		try {
			in = new ObjectInputStream(otherNode.getInputStream());
			
			
			try {
				// receive other peer table
				readObject = in.readObject();
				PeerTable otherTable = (PeerTable) readObject;
				receivedPeerTable(otherNode, otherTable);
				
			} catch (ClassCastException e) {
				PeerLog.logMessage(getLogName(), "Not a table, must be message..");
				P2PMessageOneToAll msg = (P2PMessageOneToAll) readObject;
				receivedMessage(otherNode, msg);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
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
	
	
	/**
	 * Called when the handler received a PeerTable by another node.
	 * Merges the peer tables and responds with it's own peer table.
	 * @param receivedFrom
	 * @param otherTable
	 */
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
			this.getPeerTable().removeEntry(getMyPeerTableEntry());
			this.getPeerTable().takeSubsetOfN(sizeTable);
			PeerLog.logMessage(getLogName(), "Peer Tables exchanged.");
			PeerLog.logMessage(getLogName(), this.getPeerTable().toString());
			receivedFrom.close();
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
	
	
	/**
	 * Called when a Message object is received by the handler.
	 * Reads the message and forwards it to it's known nodes. (PeerTable)
	 * @param receivedFrom
	 * @param msg
	 */
	private void receivedMessage(Socket receivedFrom, P2PMessageOneToAll msg){
		PeerLog.logMessage(getLogName(), "Received Message: '"+msg.getMessage()+"'");
		msg.peerVisited(getMyPeerTableEntry());
		
		// send message further
		for (PeerTable.TableEntry e: this.getPeerTable().getTable()){
			if (!msg.isPeerAlreadyVisited(e)){
				sendMessageToPeer(e, msg);
			}
		}
	}
	
	/**
	 * Returns the peer table object.
	 * @return
	 */
	public synchronized PeerTable getPeerTable(){
		return this.peerTable;
	}
	
	/**
	 * Trys to connect to a peer node.
	 * @param peer
	 * @return
	 */
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
	
	/**
	 * Called by the update thread to send it's peer table to a connected peer
	 * and wait for response. Then merges the received table and picks randomly N entries.
	 * @param connectedPeer
	 * @param localTable
	 */
	protected synchronized void sendReceivePeerTableToNode(Socket connectedPeer, PeerTable localTable){
		PeerLog.logMessage(getLogName(), "Exchanging PeerTable with connected Node..!");
		
		ObjectOutputStream out;
		ObjectInputStream in;
		try {
			out = new ObjectOutputStream(connectedPeer.getOutputStream());
			try {
				//send my peer table
				PeerTable tableToSend = this.getPeerTable();
				PeerLog.logMessage(getLogName(), tableToSend.toString());
				tableToSend.addEntry(getMyPeerTableEntry());
				out.writeObject(tableToSend);
				out.flush();
								
				in = new ObjectInputStream(connectedPeer.getInputStream());
				// receive other peer table
				PeerTable otherTable = (PeerTable) in.readObject();
				otherTable.removeEntry(getMyPeerTableEntry());
				
				//merge
				this.getPeerTable().mergeWithTable(otherTable);
				this.getPeerTable().removeEntry(getMyPeerTableEntry());
				this.getPeerTable().takeSubsetOfN(sizeTable);
				PeerLog.logMessage(getLogName(), "Peer Tables exchanged.");
				PeerLog.logMessage(getLogName(), this.getPeerTable().toString());
				connectedPeer.close();
				
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
					
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		
		try {
			connectedPeer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Closes a peer and all its threads.
	 */
	public void closePeer(){
		PeerLog.logMessage(getLogName(), "Closing peer!");
		if (this.listenThread != null){
			this.listenThread.stopListenThread();
		}
		
		if (this.updateThread != null){
			updateThread.stopUpdateThread();
		}
		
		
	}
	
	/**
	 * Returns the log name. used for PeerLog static log class.
	 * @return
	 */
	protected String getLogName(){
		return this.peerName+"@Peer("+this.peerPort+")";
	}
	
	/**
	 * Generates a PeerTable Entry from this node. e.g. this will be added when sending it's own peer
	 * table to another node.
	 * @return
	 */
	protected PeerTable.TableEntry getMyPeerTableEntry(){
		return new PeerTable.TableEntry(this.listenThread.getServerSocketIP(), this.listenThread.getServerSocketPort());
	}
	
	/**
	 * Adds a node-node connection to the peer table.
	 * @param ip
	 * @param port
	 */
	public void addConnection(InetAddress ip, Integer port){
		this.getPeerTable().addEntry(new TableEntry(ip, port));
	}
	
	/**
	 * Adds a node-node connection to the peer table. easier to use since ip address can be given
	 * as String.
	 * @param ipStr
	 * @param port
	 */
	public void addConnection(String ipStr, Integer port){
		try {
			this.getPeerTable().addEntry(new TableEntry(InetAddress.getByName(ipStr), port));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends a message to all known (PeerTable) nodes.
	 * @param message
	 */
	public void sendOneToAllMessage(String message){
		P2PMessageOneToAll msg = new P2PMessageOneToAll(message, getMyPeerTableEntry());
		for (PeerTable.TableEntry e: this.getPeerTable().getTable()){
			sendMessageToPeer(e, msg);
		}
	}
	
	/**
	 * Helper method to send a message to a single node.
	 * @param peer
	 * @param msg
	 */
	private void sendMessageToPeer(PeerTable.TableEntry peer, P2PMessageOneToAll msg){
		PeerLog.logMessage(getLogName(), "Sending message to "+peer+".");
		Socket peerSocket = connectToPeer(peer);
		ObjectOutputStream out = null;
		if (peerSocket != null){
			
			try {
				out = new ObjectOutputStream(peerSocket.getOutputStream());
				out.writeObject(msg);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
				try {
					peerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
	}
	
	
}
