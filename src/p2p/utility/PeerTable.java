package p2p.utility;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class PeerTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1194745469309953800L;

	public static class TableEntry implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 3895097921506278137L;

		public TableEntry(InetAddress ip, Integer port){
			this.IPAddress = ip;
			this.port = port;
		}
		
		public InetAddress IPAddress;
		public Integer port;
		
		public InetAddress getIP(){
			return IPAddress;
		}
		
		public Integer getPort(){
			return this.port;
		}
		
		@Override
		public boolean equals(Object o){
			try{
			return this.IPAddress.equals(((TableEntry)o).getIP()) &&
					this.port.equals(((TableEntry)o).getPort());
			}catch (Exception e){
				return false;
			}
			
		}
		
		@Override
		public String toString() {
			return new String("| "+this.getIP()+" | "+this.getPort()+" |");
		}
	}
	
	private ArrayList<TableEntry> table = new ArrayList<>();
	
	public ArrayList<TableEntry> getTable(){
		return this.table;
	}
	
	public void addEntry(TableEntry entry){
		this.table.add(entry);
	}
	
	public void removeEntry(TableEntry entry){
		this.table.remove(entry);
	}
	
	public boolean hasEntry(TableEntry entry){
		return this.table.contains(entry);
	}
	
	// not so random
	public TableEntry getRandomEntry(){
		return this.table.get(0);
	}
	
	public boolean isEmpty(){
		return this.table.isEmpty();
	}
	
	public void mergeWithTable(PeerTable other){
		for(TableEntry e: other.table){
			if (!this.table.contains(e)){
				this.table.add(e);
			}
		}
	}
	
	@SuppressWarnings("unused")
	private String getLogName(){
		return "PeerTable";
	}
	
	@Override
	public String toString() {
		String result = "\n| IP | PORT |"+"\n";
		for (TableEntry e : table){
			result += e.toString()+"\n";
		}
		return result;
	}
}
