package p2p;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import p2p.utility.PeerLog;

public class PeerListenThread extends Thread {

	private Peer owningPeer;
	private boolean isRunning = true;
	private ServerSocket serverSocket;
	private Integer port;


	
	public PeerListenThread(Peer peer, Integer port){
		this.owningPeer = peer;
		this.port = port;
		
		try {
			this.serverSocket = new ServerSocket(this.port);
		} catch (IOException e) {
			PeerLog.logMessage(getLogName(), "Unable to create server socket.");
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		super.run();
		
		startListeningOnSocket();
		closeAll();
	}
	
	private void startListeningOnSocket(){
		while (isRunning){
			try {
				Socket connectedNode = this.serverSocket.accept();
				this.owningPeer.handleConnectedNode(connectedNode);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String getLogName(){
		return owningPeer.getLogName()+".PeerListenThread";
	}
	
	protected void stopListenThread(){
		this.isRunning = false;
	}
	
	private void closeAll(){
		if (this.serverSocket != null){
			try {
				this.serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected InetAddress getServerSocketIP(){
		try {
			return InetAddress.getByName(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	protected Integer getServerSocketPort(){
		return this.serverSocket.getLocalPort();
	}
}
