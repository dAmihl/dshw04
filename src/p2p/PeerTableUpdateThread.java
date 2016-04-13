package p2p;

import java.net.Socket;

import p2p.utility.PeerLog;
import p2p.utility.PeerTable;

public class PeerTableUpdateThread extends Thread {

	private static final Integer UPDATE_RATE_SECONDS = 5;
	private boolean isRunning = true;
	private Peer owningPeer;
	
	public PeerTableUpdateThread(Peer peer){
		this.owningPeer = peer;
	}
	
	@Override
	public void run() {
		super.run();
		while (isRunning){
			try {
				Thread.sleep(UPDATE_RATE_SECONDS * 1000);
				updateRoutine();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		PeerLog.logMessage(getLogName(), "Update Thread now stopping.");
	}
	
	private void updateRoutine(){
		PeerLog.logMessage(getLogName(), "Update routine started..");
		PeerTable tempPeerTable = getPeerTable();
		
		boolean connectionSuccess = false;
		Socket peerConnectionSocket = null;
		
		while (!connectionSuccess ){
			if (tempPeerTable.isEmpty()){
				PeerLog.logMessage(getLogName(), "PeerTable is empty. Aborting..");
				return;
			}
			PeerTable.TableEntry tmpEntry = tempPeerTable.getRandomEntry();
			peerConnectionSocket = this.owningPeer.connectToPeer(tmpEntry);
			
			
			if (peerConnectionSocket != null){
				connectionSuccess = true;
			}else{
				tempPeerTable.removeEntry(tmpEntry);
				PeerLog.logMessage(getLogName(), "Table Entry removed due to connection issues.");
			}
		}
		
		if (connectionSuccess){
			PeerLog.logMessage(getLogName(), "Successfully connected to node, syncing PeerTables now..");
			owningPeer.sendReceivePeerTableToNode(peerConnectionSocket, tempPeerTable);
		}
		
		
		
	}
	
	public synchronized void stopUpdateThread(){
		isRunning = false;
	}
	
	private PeerTable getPeerTable(){
		return owningPeer.getPeerTable();
	}
	
	private String getLogName(){
		return this.owningPeer.getLogName()+".PeerTableUpdateThread";
	}
	
}
