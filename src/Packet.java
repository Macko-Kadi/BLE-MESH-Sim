/**
 * 
 * TODO: Improve packet structure ! Fields, flags ? 
 * 
 */
class Packet {
	ControlData controlData; 	//a set of control commands, e.g. "turn on energy saving mode", "start acting as a relay", etc.
	String otherData; 			//some data that packet can have (eg. temperature)
	final Header header;
	
	/**
	 * 	
	 * @param n_ source node
	 * @param destinationID_ destination ID - node or group
	 * @param packetSize_ packet size (in bytes)
	 * @param TTL_	Time to live
	 */
	Packet(Node n_, byte destinationID_, int packetNumber_, int packetSize_, byte TTL_){
		header=new Header(n_.ID, destinationID_,packetNumber_, packetSize_, TTL_);
		/**
		 * TODO: what we (who?) should controll ?
		 */
		controlData=new ControlData();
	}
	Packet(Packet p)
	{
		header = new Header(p.header);
		controlData=new ControlData();
	}
	
	/**
	 * Packet header structure
	 */
	class Header{
		byte TTL;
		int packetSize;			//number of bytes - it will determine packet transmission time
		byte sourceID;			//ID of a node that generated the packet
		byte destinationID;		//ID of a node (or a group) that is the packet destination
		/**
		 * packetID is stored in a node cache - if a node successfully receipt a packet that is in cache, ignores it.
		 */
		final String packetID;		//I assume, that packetID has a form "sourceID:destinationID:packetNumber" (e.g. "4:1:456") 	
		Header(byte sourceID_, byte destinationID_, int packetNumber_, int packetSize_, byte TTL_){		
			sourceID=sourceID_;
			destinationID=destinationID_;
			packetSize=packetSize_;
			TTL=TTL_;
			packetID=""+sourceID+":"+destinationID_+":"+packetNumber_;
		}
		Header(Header h){
			sourceID=h.sourceID;
			destinationID=h.destinationID;
			packetSize=h.packetSize;
			TTL=h.TTL;
			packetID=h.packetID;
		}
		
	}
	/**
	 * Set of control fields, flags 
	 * 
	 * TODO: Enhance the model - what we may controll ? Then, the data must be processed somehow. It would be easy to implement, but someone needs to do that :)
	 * @see Node#performPacketActions
	 *
	 */
	class ControlData {	
		boolean turnOnEnergySavingMode;	
		boolean turnOffEnergySavingMode;	
		boolean turnOnRelayMode;	
		boolean turnOffRelayMode;	
	}
}
