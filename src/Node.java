import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 * The class provides a BLE MESH node
 * 
 * TODO: Role of the node (Roles may change during simulation - so keep it in mind before you use inheritance):
 * 				1. Provisioner that set up roles of other nodes (and request them to go sleep and so on (?))
 * 				2. Relay - a node that forwards received packets (THE implementation is like that)
 * 				3. MeasuringNode (?) - sends only data it generates (temperature ?) and piggybacks metadata
 * 				4. Gateway - a node that collects data
 * 				5. Friend/LittleFriend (?) scheme - Friend keeps packets destinated for LittleFriend (eg. when it sleeps) and when requested - Friend send it 
 * 				6. ??????
 * 
 * TODO: Profiles - different type of packets 
 * 				1. metadata about a node state - battery level, noise level, received power level (?) 
 * 				2. communication with and without acknowledgments (NO ACK scheme implemented) 
 * TODO: Statistics: 
 * 				1. How many packets send, how many received ? (for each relation)
 * 				2. How many times a packet was received by a node ? (redundancy) 
 * 				3. Number of successfull/unsuccessfult transmisions
 * TODO: 	????
 * 
 * Some functions are based on the document: Yet Another Network Simulator (auth. Lacage, Henderson), chapter 8.1.
 * I will use YANS abbrevation when refering to the document.
 * </pre>
 */
class Node {
//==================================================================================================//
//==================================Node: CLASS VARIABLES ==========================================//
//==================================================================================================//
	/**
	 * Interface speed - 1Mbit/s 
	 */
	static final float INTERFACE_SPEED=1000000f; 
	/**
	 * <pre>
	 * We will use dBm as a power unit. Typically (?), for BLE 1Mbit/s, 10mW=10dBm is used.
	 * TODO: confirm the value
	 * </pre>
	 */
	public static final float MAX_TRANSMISSION_POWER=10;	
	/**
	 * If noise power is greater than the level - phyState=3 (CCA_BUSY) - a node can't start transmission
	 */
	private static final float ENERGY_DETECTION_THRESHOLD=-30;
	/**
	 * <pre>
	 * ATTENTION: It is important to distinguish synchronization and data reception phases. 
	 * 
	 * We should prevent situation when a node synchronizes to a transmission that has no chance to be successfully received.
	 * It is important, because, when a node is synchronized to one transmission (phyState=="SYNC"), all other transmissions cannot be synchronized (thus successfully received).
	 *  
	 * Minimum SNR level required for non zero probability of successfull synchronization.
	 * If the level is lower than the value a node has no chance to sync to the transmission.
	 * 
	 * If the value should be different for different nodes, remove 'static'.
	 * 
	 * TODO: Is the value realistic ? I used these http://www.wireless-nets.com/resources/tutorials/define_SNR_values.html
	 * </pre>
	 */
	private static final float MIN_SNR_SYNC_P0=25; 
	/**
	 * <pre>
	 * ATTENTION: It is important to distinguish synchronization and data reception phases. 
	 * 
	 * We should prevent situation when a node synchronizes to a transmission that has no chance to be successfully received.
	 * It is important, because, when a node is synchronized to one transmission (phyState=="SYNC"), all other transmissions cannot be synchronized (thus successfully received).
	 *  
	 * Minimum SNR level required for certainty (P=1) of successful synchronization (so a node changes PhyState to SYNC).
	 * 
	 * If the value should be different for different nodes, remove 'static'.
	 * 
	 * TODO: Is the value realistic ? I used these http://www.wireless-nets.com/resources/tutorials/define_SNR_values.html
	 *  </pre>
	 */
	private static final float MIN_SNR_SYNC_P1=40; 
	/**
	 * Minimum SNR level required for non-zero probability of successful transmission reception.
	 * It allows to assign failure to a reception at its beginning (so there is no need to calculate how noise level changes during the reception and so on)
	 * 
	 * If the value should be different for different nodes, remove 'static'.
	 * 
	 * TODO: Is the value realistic ?  
	 */
	private static final float MIN_SNR_RCVD_P0=15; 
	/**
	 * Minimum SNR level required for certainty (P=1) of successful transmission reception.
	 * If SNR is greater than the value, transmission is always successfully received.
	 * 
	 * If the value should be different for different nodes, remove 'static'.
	 * 
	 * TODO: Is the value realistic ?  
	 */
	private static final float MIN_SNR_RCVD_P1=40; 
	 /**
	 * <pre>
	 * I've assumed that during transmission a node consumes maxTransmissionPower, no matter of currentTransmissionPower.
	 * It means that when battery is discharged, a node transmitt with e.g. 80% of maxTransmissionPower but it still needs maxTransmissionPower to perform the transmission. 
	 * 
	 * If you want to change maxTransmissionPower into currentTransmissionPower - you will need to periodically update the inStateEnergyConsumption array (since float is not mutable)
	 * 
	 * States: [turned off , transmitting, idle, sleep, deep sleep, ?]
	 * 
	 * TODO: Check the values and define required dependencies (if there are any (?)).
	 * 
	 * </pre>
	 */
	private static final float[] IN_STATE_ENERGY_CONSUMPTION={0,MAX_TRANSMISSION_POWER+0.1f,0.1f, 0.001f, 0.0001f, 0.0f};	//in mW	
	/**
	 * Maximum backoff time that may be generated 
	 * 
	 * TODO: Is the value realistic ?
	 * TODO: Should we use a BackoffCounter object instead ?
	 */
	private static final float MAX_BACKOFF_TIME=0.1f; //seconds
	 
//==================================================================================================//
//================================Node: INSTANCE VARIABLES =========================================//
//==================================================================================================//
	
	/**
	 * The value decreases when battery level goes down (and does not change when power-supplied)
	 * Initial value = MAX_TRANSMISSION_POWER
	 */
	private float currentTransmissionPower=MAX_TRANSMISSION_POWER;	
	/**
	 * Each node has an ID.
	 * If the network will consist of more than 127 nodes you should rebuild the ID scheme (byte range -128:127, 0-127 are for nodes IDs, -128:-1 are for groups IDs)
	 */
	byte ID;
	/**
	 * Each node may be a member of a number of groups
	 * 
	 * TODO: implement group related functions (join, leave, and so on)!
	 */
	ArrayList<Byte> groupIDs=new ArrayList<Byte>();
	/**
	 * Each node has an (x, y) position. The position is needed to calculate distance between nodes, and the distance is needed for path loss (power decrease)  
	 */
	Position position;
	/**
	 * Queue of packets to send
	 * 
	 * TODO: Is there always only one queue ?
	 */
	ArrayList<Packet> queue=new ArrayList<Packet>();	
	/**
	 * <pre>
	 * A node can be in various states:
	 * 
	 * TURNED_OFF: 		discharged - does not do anything 
	 * TRANSMITTING:	can't synchronize to new transmission
	 * IDLE:			can do anything
	 * SLEEP:			same as TURNED_OFF but a node goes sleep for a specified time, then goes IDLE
	 * DEEP_SLEEP:		sleep with no accurate clock - same as sleep, but sleeps for +- specified time
	 * ???
	 * 
	 * The node state is needed for energy consumption calcualtions, moreover depending on current state it may/may not perform certain actions.
	 * Please note, that a node has also phyState and the node state should correspond to the phyState (nodeState=="TRANSMITTING" = phyState=="TX"). 
	 * 
	 * TODO: Define possible states and their energy consumption !
	 * 
	 * @see Node#IN_STATE_ENERGY_CONSUMPTION inStateEnergyConsumption
	 * </pre>
	 */	
	private String nodeState="IDLE";
	/**
	 * <pre>
	 * Physical layer can be in 5 states:
	 * 
	 * From YANS
	 * TX: 			the PHY is currently transmitting a signal on behalf of its associated MAC
	 * SYNC: 		the PHY is synchronized on a signal and is waiting until it has received its last bit to forward it to the MAC
	 * IDLE: 	 	the PHY is not in the TX or SYNC states and the energy measured on the medium is LOWER than Energy Detection Threshold (ENERGY_DETECTION_THRESHOLD)
	 * CCA_BUSY: 	the PHY is not in the TX or SYNC states but the energy measured on the medium is HIGHER than Energy Detection Threshold (ENERGY_DETECTION_THRESHOLD)
	 * NONE:		node is turned off/sleep	
	 * 
	 * Or in another words:
	 * TX: 			sending
	 * SYNC: 		receiving
	 * IDLE:		idle, can start transmission - noise level is low (no other transmissions or transmissions far away)
	 * CCA_BUSY:	idle, can't start transmission - noise level is high (sum of other transmissions power is too high to start transmission)
	 * NONE			node is turned off/sleep	
	 * </pre>
	 * @see Node#ENERGY_DETECTION_THRESHOLD
	 */
	private String phyState="IDLE";
	/**
	 * The object represents currently synced transmission and their parameters (encapsulated in Reception object)
	 * It is null if there is no currently synced reception
	 */
	Reception syncedReception=null;
	/**
	 * The object represents currently transmitted transmission
	 * It is null if node is not transmitting
	 */
	Transmission currentTransmission=null;
	/**
	 * Each node has a battery or is power supplied
	 * When power supplied - battery = null;
	 */
	Battery battery=null;
	boolean batteryPowered=false;
	/**
	 * A cache that contains a list of received packets. 
	 * Size of the cache is set when a Node object is being created
	 */
	private Cache cache;
	/**
	 * Source that generates packets
	 */
	private PacketsSource packetsSource;
	
	public static int packetCount=0;
	public static int packetReceivedCount=0;
	public static int retransmit=0;
	public static ArrayList<String> duplicateList = new ArrayList<String>();
	public static int duplicateCounter;
	public static int collisionCounter;
	public static int noCollisionCounter;
	public static Map<String, Float> timeOfPacketGeneration = new HashMap<String, Float>();
	public static Map<String,Float> timeOfPacketReception=new HashMap<String, Float>();

//	I've decided to make it very simple so I don't use the BackoffCounter object at the moment
//	/**
//	 * Counter for backoff time
//	 */
//	private BackoffCounter backoffCounter;
	/**
	 * Node constructor
	 * 
	 * @param id_ ID (number)
	 * @param position_ (x,y) coordinates
	 * @param batteryType_ batteryType (if -1, no battery - power supplied)
	 * @param packetsSourceType_ (
	 * @param packetsSourceParameters_
	 * @param cacheSize_
	 */
	Node(byte id_, float[] position_, byte batteryType_, byte packetsSourceType_, float[] packetsSourceParameters_, int cacheSize_){
		ID=id_;
		putInBattery(batteryType_);	
		position=new Position(position_[0],position_[1]);
		packetsSource=new PacketsSource(packetsSourceType_, id_, packetsSourceParameters_);
		cache=new Cache(cacheSize_);
	}		
//==================================================================================================//
//======================================Node: METHODS ==============================================//
//==================================================================================================//
	/**
	 * Tries to start a transmission. 
	 * @return If success schedules END_OF_TRANSMISSION, otherwise starts another backoff procedure (so schedules TRY_TO_START_TRANSMISSION)
	 */
	Event tryToStartTransmission(){			
		if(Helper.DEBUG_TRANS) System.out.println("Node "+ID+" queue size: "+ queue.size());
		if(isAbleToTransmit()){						//if the node is able to transmit  (checks its phyState, nodeState, noise lvl, queueOccupancy)
			if (Helper.DEBUG_TRANS) System.out.println("Node "+ID+" is ableToTransmit");
			currentTransmission=new Transmission(this,getFirstPacketFromQueue());	//transmit first packet from the queue
			phyState="TX";   
			nodeStateChange("TRANSMITTING");							
			Medium.addCurrentTransmission(currentTransmission);					//give transmission to the Medium 
			return new Event(Engine.simTime+currentTransmission.duration,"END_OF_TRANSMISSION", Byte.toString(ID));	//schedule the end of transmission 
		}
		else{ //if can't start transmission - start backoff again
			return startBackoffProcedure();
			
		}
	}
	/**
	 * End of transmission.
	 * When transmission has ended, if there is something in queue, start backoff procedure
	 * @return may return scheduled TRY_TO_START_TRANSMISSION event or null
	 */
	Event endOfTransmission(){	
		phyState="IDLE";   
		nodeStateChange("IDLE"); 
		Medium.removeCurrentTransmission(currentTransmission);
		currentTransmission=null;
		if (isQueueEmpty()) return null;
		else{
			retransmit++;
			return startBackoffProcedure();
		}
	}
	/**
	 * <pre>
	 * Packet generation may trigger two Events: 
	 * 1) It always generate NEXT_PACKET_GEN Event, and
	 * 2) addPacketToQueue, if queue was empty, Start backoff procedure, TRY_TO_START_TRANSMISSION
	 * 
	 * ArrayList of Events is used instead of EventList - I don't need to sort events in this list - they need to be sorted anyway later.
	 * </pre>
	 */
	static ArrayList<Packet> packetList = new ArrayList<Packet>();
	ArrayList<Event> generatePacket(){
		ArrayList<Event> list = new ArrayList<Event>();
		Packet p=packetsSource.createPacket(this);
		if(Helper.DEBUG_TRANS) System.out.println("Generated packet ID: " +p.header.packetID);	
		packetList.add(p);
		list.add(new Event (packetsSource.timeOfNextGen,"PACKET_GENERATION", Byte.toString(ID)));		
		addPacketToCache(p);							//all generated packets need to be cached 
		list.add(addPacketToQueue(p));					//add packet to the queue (it may trigger event)
		list.removeAll(Collections.singleton(null)); 	//remove all nulls from the list
		packetCount++;
		timeOfPacketGeneration.put(p.header.packetID,Engine.simTime);
		return list;
	}
	/**
	 * <pre>
	 * Adds packet to the queue. If the queue was empty and the node is not transmitting, start backoff procedure, ohterwise do nothing.
	 * Why need to check phyState!=TX? You add a packet to the queue after its successful reception OR after its generation - it may be generated during transmission.
	 * </pre>
	 * @param p
	 * @return scheduled TRY_TO_START_TRANSMISSION event or null
	 */
	Event addPacketToQueue(Packet p){	
		queue.add(p);
		if(queue.size()==1 && !phyState.equals("TX"))return startBackoffProcedure();
		else return null;
	}
	/**
	 * <pre>
	 * I've decided to make it very simple at the moment so I don't use BackoffCounter object and any logic.
	 * Actually it schedules TRY_TO_START_TRANSMISSION event. That's it.
	 * </pre>
	 * @return scheduled TRY_TO_START_TRANSMISSION event;
	 */
	Event startBackoffProcedure(){
	/* 
	 *	backoffCounter=new BackoffCounter();
	 *	return new Event(backoffCounter.getTimeOfProcedureEnd(),"TRY_TO_START_TRANSMISSION",Byte.toString(ID));
	 */	
		return new Event(Engine.simTime+Helper.generator.nextFloat()*MAX_BACKOFF_TIME,"TRY_TO_START_TRANSMISSION",Byte.toString(ID));
	}	
	/**
	 * Node goes to sleep for a given time
	 * 
	 * @param duration time of sleep
	 * @return scheduled WAKE_UP event
	 */
	Event goSleep(float duration){
		if(Helper.DEBUG_STATE) System.out.println("Node "+ID +" goes to sleep for " + duration +" seconds");
		phyState="NONE";
		nodeStateChange("SLEEP");
		return (new Event(Engine.simTime+duration,"WAKE_UP",Byte.toString(ID))); 
	}
	/**
	 * TODO: Specify, how a node behaves after waking up. I've assumed that it generates a packet - it will start a chain of other events (transmission and generation of another packet)
	 * 
	 * @return list of events generated by generatePacket() function
	 * @see Node#generatePacket()
	 */
	ArrayList<Event> wakeUp(){
		if(Helper.DEBUG_STATE) System.out.println("Node "+ID +" wakes up");
		phyState="IDLE";
		nodeStateChange("IDLE");
		return generatePacket();			
	}
	/**
	 * If packet already cached - do nothing. Otherwise:
	 * add packet to cache, check whether the node supposed to performPacketActions and whether the packet should be sent further
	 * 
	 * @param p 
	 */
	private Event processPacket(Packet p, Node n){	
		if(!cache.isThePacketInCache(p)){  					//if it is the first time when the node received the packet 
			p.header.TTL = (byte) (p.header.TTL-1);
			System.out.println("TTL: "+ p.header.TTL);
			addPacketToCache(p);								//add the packet to the cache, and
			if (isNodeDestination(p)){							//1) if the node is packet destination
				performPacketActions(p); 							//perform actions defined in packet, 
				return null;										//then do nothing	
			}
			else if (doesNodeBelongToDestinationGroup(p)){		//2) if the node belongs to a destination group 
				performPacketActions(p);							//perform actions defined in packet 
				if (Provisioner.isNodeRelay(n)) {
				return addPacketToQueue(p);	}						//and then send* it to other nodes - the packet must reach all the group members. *(I meant, add it to the queue, of course)			
			}
			else 
				if (Provisioner.isNodeRelay(n)) {
					return addPacketToQueue(p);}					//3) if not 1) or 2) -> send* it to other nodes. *(I meant, add it to the queue, of course)
		}
		else if(Helper.DEBUG_CACHE) 
			{System.out.println("The packet " + p.header.packetID+ " was cached already !");
			if (isNodeDestination(p)){
				duplicateList.add(p.header.packetID);
				duplicateCounter++;
			}
			}
		return null; 										//if the packet was cached already - do nothing (
	}	
	/* <pre>
	 * When a transmission start other nodes will start receiving it (the transmission is hidden in the reception object).
	 * They may SYNC to the transmission (and the reception becomes a syncedReception), or the transmission will be treated as a part of noise (in Medium class)
	 * </pre>
	 * @param T transmission a node starts to receive 
	 */
	Event startReceiving(Transmission t){	
		if (phyState.equals("SYNC")){				//if the node is currently synced to another transmission 
			if(Helper.DEBUG_RCV) System.out.println("Track noise lvl for reception of packet: "+syncedReception.transmission.packet.header.packetID);
			syncedReception.trackNoiseLvl(0f);		//update noise lvl of the synced receiving transmission
		}		
		else if (isAbleToSync()){		 			//if the not is not currently synced and the node can sync to the receiving transmission	
			if(Helper.DEBUG_RCV) System.out.println("Node "+ID +" is able to sync, phyState" + phyState);		
			Reception reception=new Reception(t);	//Encapsulate the transmission in the Reception object		
			if (reception.isTheSyncSuccessfull()){				//if successfully synced
				phyState="SYNC";					//change phyState to SYNC
				if (Helper.DEBUG_RCV) System.out.println("Node "+ID +" synced, phyState: " + phyState);
				syncedReception=reception;			//set that the reception is the synced one
				return new EventEndTrans(Engine.simTime+reception.transmission.duration,"END_OF_SYNCED_RECEPTION", Byte.toString(ID),t);
			}
		}		
		/*
		 *Since there are no actions related with END_OF_NON_SYNCED_RECEPTION I've decided to remove the event
		 *Thanks to that, the Engine.eventList is shorter (so, it's easier to sort and so on) 
		 *if you want to consider this kind of event - change the "return null" into:
		 *return new EndTransEvent(Engine.simTime+t.duration,"END_OF_NON_SYNCED_RECEPTION", Byte.toString(ID),t );
		 */
		return null;
	}
	/**
	 * <pre>
	 * Actions related with the end of synced reception. Determine whether the packet was successfully received and process the packet.
	 * Details in the source code comments.
	 *</pre>
	 */
	Event endOfSyncedReception(Transmission t, Node n){
		phyState="IDLE"; 													//phyState changes from SYNC to IDLE
		if(syncedReception.isTheReceptionSuccessfull()){					//if the transmission is successfully received
			Event e=processPacket(syncedReception.transmission.packet, n);			//process the packet obtained from the received transmission (it generates an event)
			if (Helper.DEBUG_RCV) System.out.println("Node " + ID+" SUCCESSFULL RECEPTION ! Process Packet");//													|
			noCollisionCounter++;
			syncedReception=null;												//there is no synced reception at the moment									|									
			return e;															//return the event <-------------------------------------------------------------
		}
		else {																//if the transmission is NOT successfully received
			collisionCounter++;
			syncedReception=null;												//there is no synced reception at the moment
			return null;														//do nothing
		}			
	}
	/**
	 * TODO: Actions related with packet performing eg. nodeStateChange (the ProvisionerNode (?) may send a goToSleep request)
	 * @param p
	 */
	private void performPacketActions(Packet p){
		if(Helper.DEBUG_RCV) System.out.println("Packet "+p.header.packetID+" successfully received and processed in node: " +ID);
		packetReceivedCount++;
		timeOfPacketReception.put(p.header.packetID, Engine.simTime);


		/**
		 * TODO: all logic needed !
		 */
	}
	/**
	 * <pre>
	 * Updates node currentTransmissionPower based on node state (battery level and (???) so on)
	 * I've assumed that transmission power corresponds to battery level (in %), however I guess it is not truth - big battery discharged to 30% has still much more energy than fully charged small battery 
	 * e.g:
	 * Pcurr=Min(Pcurr,Pcurr-6*(0.5-batteryLevel/100)) - the curve is nice, but don't ask me if it corresponds to the reality. 
	 * Properties:
	 * ----------------
	 * battery 	Pcurr
	 * ----------------
	 * 100-50%	Pmax
	 * 50-0% 	linear degradation (linear in dBms!)
	 * 0%		Pmax-3
	 * remember that we use dBms - when battery goes to 0, signal is 2 times weaker
	 * 
	 * TODO: REALISTIC formula describing relation between node state and transmission power.
	 * </pre>
	 */
	void updateTransmissionPower(){
		if (batteryPowered) drainBattery();		
		currentTransmissionPower=(float)(Math.min(MAX_TRANSMISSION_POWER, MAX_TRANSMISSION_POWER-6*(0.5-battery.energyLevel/100)));
	}
	/**
	 * <pre>
	 * Changes the nodeState.
	 * If battery powered, drain the battery.
	 * </pre>
	 * @param nodeState_
	 */
	private void nodeStateChange(String nodeState_){
		if (batteryPowered) drainBattery();	
		nodeState=nodeState_;	
	}
	/**
	 * switches Node into the TURNED_OFF state.
	 */
	void switchOff(){
		System.out.println("Node "+ID+" discharged ! Turning off...");
		nodeStateChange("TURNED_OFF");
		phyState=("NONE");
	}
	/**
	 * Whether packet is destinated for the node
	 */
	private boolean isNodeDestination(Packet p){return (p.header.destinationID==ID);}
	/**
	 * Whether packet is destinated for the group the node belongs to
	 */
	private boolean doesNodeBelongToDestinationGroup(Packet p){	return (groupIDs.contains(p.header.destinationID));}
	private boolean isQueueEmpty(){return (queue.size()==0);}
	/**
	 * The node can receive a transmission only when it is turned on (thus nodeState == IDLE or TRANSMITTING) 
	 */
	boolean isAbleToReceive() {
		return (nodeState.equals("IDLE") || nodeState.equals("TRANSMITTING"));
	}
	/**
	 * Checks if a node is able to sync, node is idle and (phyState is IDLE or CCA)	
	 */
	private boolean isAbleToSync() {
		return (nodeState.equals("IDLE") && (phyState.equals("IDLE") || phyState.equals("CCA_BUSY")));
	}
	/**
	 * Checks if a node is able to transmit: node is IDLE, phyState is IDLE, there is something to send  	
	 */
	private boolean isAbleToTransmit(){			
		determinePhyState(); 				//updates phyState. Actually determine whether it is IDLE or CCA_BUSY state
		if(Helper.DEBUG_TRANS) System.out.println("Node "+ID+" phyState "+ phyState);
		return (nodeState.equals("IDLE") && phyState.equals("IDLE") && queue.size()>0); 	
	}
	/**
	 * If phyState == IDLE or CCA_BUSY - compares noise lvl with ENERGY_DETECTION_THRESHOLD and sets phyState to IDLE or CCA_BUSY 
	 */
	private void determinePhyState(){
		//if TX, SYNC, NONE - do nothing
		if (phyState.equals("TX")) return;
		if (phyState.equals("NONE")) return;
		if (phyState.equals("SYNC")) return;		
		float sumOfNoise=Medium.getNoise(ID); 			//get noise seen by this node
		if(Helper.DEBUG_TRANS) System.out.println("Node "+ID+" isPhyStateIdleOrCCA_BUSY() - sumOfNoise "+ sumOfNoise);
		if (sumOfNoise<ENERGY_DETECTION_THRESHOLD) phyState="IDLE";
		else phyState="CCA_BUSY";		
	}
	
	/*
	 *Since there are no actions related with END_OF_NON_SYNCED_RECEPTION I've decided to remove the event, so also the function:
	 *
	 *void endOfNonSyncedReception(Transmission t){
	 *	//it doesn't trigger nothing (at least for now)
	 *}
	 */
	
//==================================================================================================//
//==========================Node: SETTERS, GETTERS, INITIALIZATORS==================================//
//==================================================================================================//
	/**
	 * Initializes battery object
	 * 
	 * @param batteryType_ - different types of batteries are allowed
	 * @see Battery
	 */
	private void putInBattery(byte batteryType_){
		//type==-1 means that a node is power-supplied
		if(batteryType_!=-1){
			batteryPowered=true;
			battery=new Battery(batteryType_);
		}
	}
	/**
	 * Drains battery with certain power
	 * @see Battery#drainBattery(float)
	 */
	void drainBattery(){battery.drainBattery(IN_STATE_ENERGY_CONSUMPTION[getNodeStateNr(nodeState)]);} 
	
	/**
	 * Points at a proper position in inStateEnergyConsumption.
	 * 
	 * @return position related with given state name
	 * @see Node#IN_STATE_ENERGY_CONSUMPTION
	 * @see Node#nodeState
	 * @see Battery#drainBattery(float)
	 */
	private static byte getNodeStateNr(String nodeState_){
		switch (nodeState_){
			case ("TURNED_OFF"): return (byte)0;
			case ("TRANSMITTING"): return (byte)1;
			case ("IDLE"): return (byte)2;
			case ("SLEEP"): return (byte)3;
			case ("DEEP_SLEEP"): return (byte)4;
			default : {
				System.out.println("WARNING: getNodeStateNr() - not defined nodeState ! Returned -1 ");
				return (byte)(-1);
			}
		}
	}	
	/**
	 * @see Cache#addPacketToCache(Packet)
	 * @param p
	 */
	private void addPacketToCache(Packet p){
		cache.addPacketToCache(p);
	}
	/**
	 * Get the first packet from the queue and remove it from the queue (only one queue per node is assumed)
	 * @return First packet from the queue
	 */
	private Packet getFirstPacketFromQueue(){		
		Packet p = queue.get(0);
		queue.remove(0);
		return p;
	}
	String getPhyState(){return phyState;}
	String getNodeState(){return nodeState;}
	float getCurrentTransmissionPower(){return currentTransmissionPower;}
	/**
	 * @return Returns battery level (in %)
	 */
	double getCurrentBatteryLevel(){return battery.energyLevel;}
	
	double getCurrentEnergyLevel(){return battery.currentEnergytmp;}
	double getInitialEnergyLevel(){return battery.initialEnergy;}	
	double getUsedEnergyLevel(){return battery.usedEnergy1;}

	
	
//==================================================================================================//
//=================================== Node.Cache: CLASS ============================================//
//==================================================================================================//	
	/**
	 * The class provides a cache object. It contains an array list of CachedPackets, has maximum size
	 * 
	 *  @see CachedPacket
	 * 
	 */
	class Cache{
		int maxSize;
		private ArrayList<CachedPacket> listOfPackets = new ArrayList<CachedPacket>();		
		Cache(int size_){
			maxSize=size_;
		}
		
		private boolean isThePacketInCache(Packet thePacket){
			if (Helper.DEBUG_CACHE)	System.out.print("Node.isThePacketInCache(), node: "+ID+" - received packet ID: " +thePacket.header.packetID+"\nCache state: ");
			boolean response=false;
			for (CachedPacket cp : listOfPackets){
				if (cp.packetID.equals(thePacket.header.packetID)) response=true;
			}
			if (Helper.DEBUG_CACHE) System.out.println(response);
			return response;
		}
		/**
		 * Add packet to the list of cached packets.
		 * If the list is of maxSize, remove the oldest one cached packet at first.
		 *  
		 * @param p
		 */
		private void addPacketToCache(Packet p){
			listOfPackets.add(new CachedPacket(p));
			if (listOfPackets.size()==maxSize){
				listOfPackets.remove(0);		
			}
		}	
		
//=============================================================================================//
//============================ Node.Cache.CachedPacket: CLASS =================================//
//=============================================================================================//	
		/**
		 * Simple structure representing cached packet - contains packet ID and moment of cache
		 */
		class CachedPacket{
			/*
			 * moment when a packet was cached.
			 * at the moment it is not used, and may never be -> a cache has a capacity and packets are stored in cache until it fills up.
			 * If so, instead of the structure, you may have ArrayList<String> of packetIDs.
			 * However, I will leave it as it is, because the startCachingTime may be used for some statistics
			 * 
			 */
			private float startCachingTime; 
			private String packetID;
			CachedPacket(Packet p){
				startCachingTime=Engine.simTime;
				packetID=p.header.packetID; 
			}
		}	
	}	
//=======================================================================================//
//============================ Node.Battery: CLASS ======================================//
//=======================================================================================//
	/**
	 * <pre>
	 * The class provides a battery. Different types of batteries may be used. 
	 * Each battery has a type that corresponds to its initial capacity/energy.
	 * Battery lost energy during time.
	 * It also summarize current energyLevel (charge lvl in %).
	 * </pre>
	 */
	class Battery {
		private float initialCapacity; 		//mAh
		private float nominalVoltage;		//V
		private double initialEnergy;		//mWh
		private double currentEnergy;		//mWh
		private double currentEnergytmp;		//mWh
		private double energyLevel=100.0f; 	//in percents - 100.0 fully charged, 0.0 discharged
		private float timeOfLastUpdate;		//s
		private double usedEnergy1;
		/**
		 * <pre>
		 * Initiates a battery parameters.
		 * Diffrent types of batteries are specified:
		 * 0 	- small battery
		 * 1 	- big battery
		 * 2 	- very big battery
		 * 3	- tiny battery (for testing purposes in short time)
		 * 
		 * -1 	- power-supplied (infinite battery)
		 * </pre>
		 * @param type Type of battery
		 */
		Battery(int type){	
			switch(type){
				case 0:	
					/**
					 * VARTA BAT-V371 1.55V
					 * this battery allows for 54mWh/10mW=5.4h of constant transmission
					 * if a node transmitts for +- 1ms every minute it can transmit for 324000h=13500days=37years
					 * however, a node consumes some power constantly even in the most energy-saving mode
					 */
					initialCapacity=35f;					
					nominalVoltage=1.55f;
					initialEnergy=initialCapacity*nominalVoltage;	//54mWh
					break;
				case 1:
					/**
					 * Exemplary big battery (?)
					 */
					initialCapacity=1250f;					
					nominalVoltage=3f;
					initialEnergy=initialCapacity*nominalVoltage; // 3750mWh
					break;
				case 2:
					/**
					 * Exemplary very big battery (?)
					 */
					initialCapacity=2500f;					
					nominalVoltage=3f;
					initialEnergy=initialCapacity*nominalVoltage; // 7500mWh
					break;
				case 3:
					/**
					 * tiny battery (for tests) 
					 */
					initialCapacity=0.2f;					
					nominalVoltage=1.55f;
					initialEnergy=initialCapacity*nominalVoltage;
					break;
				default:
					initialCapacity=35f;					
					nominalVoltage=1.55f;
					initialEnergy=initialCapacity*nominalVoltage;
					System.out.println("WARNING: You've chosen not supported type of battery - type 0: small battery is set");
					break;		
			}
			currentEnergy=initialEnergy;
		}		
		/**
		 * Subtract amount of energy consumed since the last update. Calculates current energy level (in %)
		 * @param currentPowerConsumption
		 */
		private void drainBattery(float currentPowerConsumption){
			double usedEnergy=currentPowerConsumption*(Engine.simTime-timeOfLastUpdate)/3600; //time is in seconds, but energy in mWh -> 1mWs = 1/3600 mWh 		
			currentEnergy=currentEnergy-usedEnergy;	
			currentEnergytmp=currentEnergy-usedEnergy;
			energyLevel=currentEnergy/initialEnergy*100; //current energy lvl (in %)
			timeOfLastUpdate=Engine.simTime;
			usedEnergy1=initialEnergy-currentEnergytmp;
		}
	}	
	
//=======================================================================================//
//============================ Node.PacketsSource: CLASS ================================//
//=======================================================================================//
	/**
	 * Source that generates packets. Interval between packets is a random variable with defined distribution. 
	 */
	class PacketsSource {
		IntervalGenerator intervalGenerator; 	//different distributions may be applied 
		/**
		 * How many packets were generated so far by the source 
		 */
		int packetNumber=0;	
		/**
		 * It will be initialized when type of generator will be set. 
		 */
		float timeOfNextGen;						
		/**
		 * PacketsSource constructor.
		 * 
		 * @param typeOfDistribution 0 - poisson, 1-uniform, 2- ?
		 * @param seed	Random generator seed
		 * @param parameters	random variable distribution parameters
		 */
		PacketsSource(byte typeOfDistribution, byte seed, float[] parameters){	
			initializeIntervalGenerator(typeOfDistribution, seed, parameters);	
			timeOfNextGen=intervalGenerator.calculateInterval();
		}		
		/**
		 * Initializes random generator for time intervals generation
		 * 
		 * @param typeOfDistribution 0 - Poisson, 1 - Uniform, 2 - ?
		 * @param seed			Random generator seed
		 * @param parameters	andom variable distribution parameters
		 */
		private void initializeIntervalGenerator(int typeOfDistribution, int seed, float[] parameters){		
			switch (typeOfDistribution) {
			case 0:		intervalGenerator=new IntervalGeneratorExponential(seed, parameters[0]);	
						if (parameters.length>1){
							System.out.println("WARNING: You've provided more then one parameter while one was expectected");
						}										
						break;
			case 1:		intervalGenerator=new IntervalGeneratorUniform(seed, parameters[0], parameters[1]);
						if (parameters.length>2){
							System.out.println("WARNING: You've provided more then two parameters while two was expectected");
						}
						break;
		//	case 2: ... and so on
			default: 	intervalGenerator=new IntervalGeneratorExponential(seed, parameters[0]);
						System.out.println("WARNING: You've chosen not supported type of distribution - Poisson is set");
						break;
			}
		}	
		private Packet createPacket(Node n){
			/**
			 * TODO: the packet size may vary depends on what's inside
			 */
			int packetSize =300;
			/**
			 * TODO: differ packet receivers
			 * For now: All packets destination -> nodeID = 1 
			 */
			byte destID=1;
			byte TTL=20;	//time to live, TODO: it is not checked anywhere...
			Packet P = new Packet(n, destID, packetNumber, packetSize, TTL);  		//create packet
			float tempT=intervalGenerator.calculateInterval();
			timeOfNextGen=Engine.simTime+tempT;		//update the time of next generation:
			packetNumber++;
			return P;
		}		
	}

//=======================================================================================//
//============================== Node.Reception: CLASS ==================================//
//=======================================================================================//
	/**
	 * <pre>
	 * Act of receiving of a transmission. 
	 * Even a transmission that can't be successfully received (e.g. when the node phyState is TX (0) or SYNC (1)) is receiving (and influences the synced reception).
	 * </pre>
	 */
	class Reception {
		/**
		 * Receiving transmission
		 */
		final Transmission transmission;
		/**
		 * <pre>
		 * Transmission power decreased by path loss...
		 * TODO: ... and obstacles !
		 * </pre>
		 */
		private float receptionPower;
		/**
		 * <pre>
		 * Defines how noise level changes during the transmission - consists of arraylist of Noise (startTime, currentNoiseLvl) objects eg.
		 * 0.0	24
		 * 0.3	26
		 * 0.6	24
		 * 0.8	21
		 * It means that in time 0.0-0.3 noise level = 24, 0.3-0.6, noise level = 26, and so on
		 * 
		 * It is collected as in YANS
		 * 
		 * ATTENTION: At the moment I'm going to calculate one SNR for whole reception period taking into account the highest noise lvl.
		 * </pre>
		 * TODO: Enhance SNR / BER / Ploss relations
		 */
		private ArrayList<Noise> noiseDuringTransmission=new ArrayList<Noise>();		
		/**
		 * Constructor
		 * @param transmission_
		 */
		Reception(Transmission transmission_){		
			transmission=transmission_;
			receptionPower=Medium.getReceptionPower(transmission, ID); //Calculate reception power of the transmission, and 
			//the initial noise lvl:
			if (syncedReception==null){ 				//if the node is not synced, you should not treat the transmission as noise - the node tries to sync to this transmission.
				trackNoiseLvl(receptionPower);			 
			}
			/*
			 *TODO: I'm not sure if it is necessary to track noise lvl of not synced transmission. 
			 *		Well, actually it is definitely not necessary, but need to take a look to be sure - I don't want to remove it without testing 
			 */
			else{
				trackNoiseLvl(0f);
			}
		}
		/**
		 * <pre>
		 * Updates noise lvl seen by the node. It is a sum of power of all receiving transmissions except the synced one.
		 * Actually, when syncing process is in progress (so there is no synced transmission), the syncing transmission power should not be counted.
		 * The easiest way to workaround it, is to get noise level from Medium and subtract the syncing transmission power. (* see source code below)
		 * Maybe it's not elegant, but otherwise it would need some extra logic - e.g. creation of syncingReception object for the time of syncing.
		 * If it hurts your eyes, feel free to reimplement. 
		 * </pre>
		 * 
		 * @param syncingTransmissionPower
		 * @see Helper#subDBm(float, float)	
		 * @see Medium#getNoise(byte)				
		 * @see Medium#updateNoiseLvlForAReceiver													
		 * 																									
		 *///																									   *|
		private void trackNoiseLvl(float syncingTransmissionPower){//												| It seems to be easier to get sum of all transmissions powers
			if(syncingTransmissionPower==0f){//																		| and subtract the power of the currently syncing transmission.
				if (Helper.DEBUG_NOISE) System.out.println("Node ID "+ID+" trackNoiseLvl "+Medium.getNoise(ID));//	| 
				noiseDuringTransmission.add(new Noise(Engine.simTime,Medium.getNoise(ID)));//						|
			}//																								------------------------------
			else{//																							|				|			  |
				if (Helper.DEBUG_NOISE) System.out.println("Node ID "+ID+" trackNoiseLvl (Syncing) "+Helper.subDBm(Medium.getNoise(ID),receptionPower));
				noiseDuringTransmission.add(new Noise(Engine.simTime,Helper.subDBm(Medium.getNoise(ID),receptionPower)));							
			}
		}		 
		/**
		 * @return SNR of the Reception
		 */
		private float getSNR(){
			 if (Helper.DEBUG_NOISE) System.out.println("getSNR() -> reception power " + receptionPower +" getMaxNoise: "+getMaxNoise());
			 return receptionPower-getMaxNoise();
		}
		/**
		 * The function is helpful for naive approach of SNR calculation. It provides the worst case scenario - maximum noise level that occures during reception
		 * @return The highest noise lvl occured during receiving period
		 */
		private float getMaxNoise(){
			float maxNoise=Medium.BACKGROUND_NOISE;		//maxNoise can't be lower than background noise
			for (Noise noise : noiseDuringTransmission){
				maxNoise=Math.max(maxNoise, noise.currentNoiseLvl);
			}
			return maxNoise;
		}
		/**
		  * Try to synchronize to the receiving transmission
		  * @return success or fail of synchronization
		  */
		 private boolean isTheSyncSuccessfull(){return (getProbabilityOfSuccessfullSync()>Helper.generator.nextFloat()); }
		 /**
		  * Is the reception successfull?
		  * 
		  * @return success or fail of the reception
		  */
		private boolean isTheReceptionSuccessfull(){return (getProbabilityOfSuccessfullReception()>Helper.generator.nextFloat()); }		
		/**
		  * <pre>
		  * I assumed, that the probability depends directly on SNR 
		  * If SNR higher than MIN_SNR_SYNC_P1 - P=1, if SNR smaller than MIN_SNR_SYNC_P0 - P=0
		  * in between - P proportional to the value
		  * </pre>
		  * TODO: How to calculate probability of successfull synchronization ?
		  */
		 private float getProbabilityOfSuccessfullSync(){
			 float SNR=getSNR();
			 if (Helper.DEBUG_NOISE) System.out.println("Node.Transmission.getProbabilityOfSuccessfullSync(), SNR: "+SNR);
			 if (SNR<MIN_SNR_SYNC_P0) return 0;
			 else return 1;
//			 else{	 
//				 //relative position in span between minSNRsync(P0) and minSNRsync(P1) level	 
//				 float relativeSNR=(SNR-MIN_SNR_SYNC_P0)/(MIN_SNR_SYNC_P1-MIN_SNR_SYNC_P0); //value between 0-1 
//				 //TODO: realistic function
//				 //I've assumed that the probability is equal to relativeSNR^(1/2) 
//				 float procSNR=(float)(Math.pow(relativeSNR,0.5));
//				 return procSNR;		 
//			 }
		 }
		 /**
		  * TODO: How to calculate probability of successfull reception ?
		  * 
		  * I assume, that the probability depends directly on SNR (but you should calculate BER, include packet size, used bandwidth and so on - same as in YANS)
		  * If SNR higher than MIN_SNR_RCVD_P1 - P=1, if SNR smaller than MIN_SNR_RCVD_P0 - P=0
		  * in between - P proportional to the value
		  * 
		  */
		private float getProbabilityOfSuccessfullReception(){
			 float SNR=getSNR();
			 if (Helper.DEBUG_NOISE)  System.out.println("Node.Transmission.getProbabilityOfSuccessfullReception(), SNR: "+SNR);
			 if (SNR<MIN_SNR_RCVD_P0) return 0;
			 else return 1;
//			 else{		 
//				 //get relative position in span between minSNR(P0) and minSNR(P1) level	 
//				 float relativeSNR=(SNR-MIN_SNR_RCVD_P0)/(MIN_SNR_RCVD_P1-MIN_SNR_RCVD_P0); //value between 0-1 
//				 //TODO: realistic function
//				 //I've assumed that the probability is equal to relativeSNR^(1/3) 
//				 return (float)(Math.pow(relativeSNR,1/3));
//			 }
		 }
	}
		 
//=======================================================================================//
//======================== Node.Reception.Noise: CLASS ==================================//
//=======================================================================================//
		/**
		 * Simple structure that represents current noise lvl (as seen by the node) and the time when the noise started.
		 * @see Reception#noiseDuringTransmission
		 */
		class Noise {
			private float startTime;		//at the moment, since getMaxNoise() is used to obtain SNR - durations of particular noise periods don't matter
			private float currentNoiseLvl;
			Noise(float startTime_, float currentNoiseLvl_){
				startTime=startTime_;
				currentNoiseLvl=currentNoiseLvl_;
			}	
		}
		
		
		
		
	
//	/**
//	 * 
//	 * I've decided to use very simple counting scheme - the object is not needed at the moment - if needed -> toggle comment (CTRL+7)
//	 * 
//	 * TODO: Should be extended, eg. MAX_BACKOFF_TIME may change, the counter may be paused...
//	 */
//	class BackoffCounter{
//		/**
//		 * <pre>
//		 * Backoff value is generated from 0 to MAX_BACKOFF_TIME (in seconds)
//		 * Unslotted version (non synchronized network)
//		 * 
//		 * TODO: I assumed that the backoff value DOES NOT stop when CCA_BUSY. 
//		 * Just counting down counter, when 0 -> check noise level -> if low enough -> start transmission
//		 * 														   -> if not -> start another backoff procedure				
//		 * 
//		 * TODO: (?) Also, the MAX_BACKOFF_TIME does not increase after unsuccessful start of the transmission	
//		 * </pre>																		
//		 */
//		private final float MAX_BACKOFF_TIME=0.1f; //100ms - is this value realistic ?
//		private float timer;
//		private final float timeOfStart;
//		private float timeOfEnd;
//
//		BackoffCounter(){
//			timeOfStart=Engine.simTime;
//			timer=getInitialBackoffTime();
//			timeOfEnd=timeOfStart+timer;
//		}
//		/**
//		 * Well, I've decided not to complicate the simulator at this stage so the functions are not used anywere
//		 *
//		 *
//		private float pauseTime;
//		private float unpauseTime;
//		private boolean paused=false;
//		void pause(){
//			if (!paused){
//				paused=true;
//				pauseTime=Engine.simTime;
//			}
//		}
//		void unpause(){
//			if (paused){
//				unpauseTime=Engine.simTime;
//				//moment of the procedure end is increased by the pause period
//				timeOfEnd=timeOfEnd+unpauseTime-pauseTime;
//				paused=false;
//			}
//		}
//		float getTimeOfProcedureEnd(){
//			//if backoff counter is paused - time of the end is unknown.
//			if (paused)	return Float.MAX_VALUE;
//			else		return timeOfEnd;
//		}*/
//		float getTimeOfProcedureEnd(){
//			return timeOfEnd;
//		}
//		float getInitialBackoffTime(){
//			return Helper.generator.nextFloat()*MAX_BACKOFF_TIME;
//		}
//	}
}
