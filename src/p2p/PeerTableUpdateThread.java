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
		PeerLog.logMessage("PeerTableUpdateThread", "Update Thread now stopping.");
	}
	
	private void updateRoutine(){
		PeerLog.logMessage("PeerTableUpdateThread", "Update routine started..");
		PeerTable tempPeerTable = getPeerTable();
		
		boolean connectionSuccess = false;
		
		while (!connectionSuccess && !tempPeerTable.isEmpty()){
			PeerTable.TableEntry tmpEntry = tempPeerTable.getRandomEntry();
			Socket socket = this.owningPeer.connectToPeer(tmpEntry);
			
			if (socket != null){
				connectionSuccess = true;
			}else{
				tempPeerTable.removeEntry(tmpEntry);
				PeerLog.logMessage("PeerTableUpdateThread", "Table Entry removed due to connection issues.");
			}
		}
		
		
		
		
	}
	
	public synchronized void stopUpdateThread(){
		isRunning = false;
	}
	
	private PeerTable getPeerTable(){
		return owningPeer.getPeerTable();
	}
	
}
