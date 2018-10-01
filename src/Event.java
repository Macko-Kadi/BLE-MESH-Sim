import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
	//comment added via website - will it syunchronize with my local repository ?

class Event {
	double startTime;
	/**
	
	 * <pre>
	 * Type of the event. For now there is a number of defined events:
	 * PACKET_GENERATION
	 * TRY_TO_START_TRANSMISSION_PROCEDURE
	 * END_TRANSMISSION_PROCEDURE
	 * START_TRANSMISSION
	 * END_OF_TRANSMISSION
	 * END_OF_SYNCED_RECEPTION
	 * GO_SLEEP
	 * WAKE_UP
	 * BATTERY_CHECK
	 * BATTERY_DISCHARGED
	 * 
	 * </pre>
	 */
	String type;
	/**
	 * For now it is related node ID
	 */
	String metadata;
	Event(double d, String type_, String metadata_){
		startTime=d;
		type=type_;
		metadata=metadata_;
	}
	/**
	 * Only end of transmission event (EventEndTrans object) has a Transmission object
	 * @see EventEndTrans#getTransmission()
	 */
	Transmission getTransmission(){return null;}
	/**
	 * Each event may trigger a set of other events
	 * @return New scheduled events
	 */
	ArrayList<Event> evaluateEvent(){
		if (Helper.DEBUG_EVENTS && (type!="START_ADVERTISING_EVENT" || Helper.DEBUG_ADVERTISING_EVENT) && (type!="TRY_TO_SWITCH_CHANNEL" || Helper.DEBUG_SWITCH_CHANNEL))
			System.out.println("--evaluate Event--time: " +Engine.simTime+"----");
		byte nodeID; 										//ID of the note that is related with the event - it will be obtained from event metadata field.
		ArrayList<Event> newEvents = new ArrayList<Event>();	//while an event is evaluated it may trigger another events -> we will return the new events as a list
		switch (type){							
			case "END_OF_SYNCED_RECEPTION":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: END_OF_SYNCED_RECEPTION,\t ID: "+metadata + ", channel: "+this.getTransmission().channel);
				nodeID=Byte.valueOf(metadata);
				newEvents.addAll(Engine.LIST_OF_NODES.get(nodeID).endOfSyncedReception(this.getTransmission(), Engine.LIST_OF_NODES.get(nodeID)));
				break;
			case "START_ADVERTISING_EVENT":
				if (Helper.DEBUG_EVENTS && (type!="START_ADVERTISING_EVENT" || Helper.DEBUG_ADVERTISING_EVENT))
					System.out.println("EVENT: START_ADVERTISING_EVENT, ID: "+metadata);
				nodeID=Byte.valueOf(metadata);
				newEvents.addAll(Engine.LIST_OF_NODES.get(nodeID).startAdvertisingEvent());
				break;	
			case "END_OF_ADVERTISING_EVENT": //the event does not trigger new events
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: END_OF_ADVERTISING_EVENT,\t ID: "+metadata);
				nodeID=Byte.valueOf(metadata);	
				Engine.LIST_OF_NODES.get(nodeID).endOfAdvertisingEvent();
				break;
			case "START_TRANSMISSION":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: START_TRANSMISSION, ID: "+metadata+ ", channel: "+this.getTransmission().channel);
				nodeID=Byte.valueOf(metadata);				
				Event e=Engine.LIST_OF_NODES.get(nodeID).startTransmission(this.getTransmission());
				newEvents.add(e);
				for (Node n : Engine.LIST_OF_NODES){				//all other nodes start receiving 
					//when a node is idle - tries to sync, when is synced, then updates noise lvl
					if (n.ID!=nodeID) newEvents.add(n.startReceiving(Engine.LIST_OF_NODES.get(nodeID).currentTransmission));
				}				
				break;		
			case "END_OF_TRANSMISSION":  //the event does not create any new events
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: END_OF_TRANSMISSION,\t ID: "+metadata);
				nodeID=Byte.valueOf(metadata);
				Engine.LIST_OF_NODES.get(nodeID).endOfTransmission();	
				break;	
			case "TRY_TO_SWITCH_CHANNEL":  
				if (Helper.DEBUG_EVENTS && (type!="TRY_TO_SWITCH_CHANNEL" || Helper.DEBUG_SWITCH_CHANNEL)) 
					System.out.println("EVENT: TRY_TO_SWITCH_CHANNEL,\t ID: "+metadata);
				nodeID=Byte.valueOf(metadata);
				Event e1=Engine.LIST_OF_NODES.get(nodeID).tryToSwitchChannel();	
				newEvents.add(e1);
				break;
			case "PACKET_GENERATION":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: PACKET_GENERATION,\t ID: \t"+metadata);
				nodeID=Byte.valueOf(metadata);
				//this Event triggers one or two Events, so we need to use addAll. 
				//generatePacket() returns an ArrayList of Events
				newEvents.add(Engine.LIST_OF_NODES.get(nodeID).generatePacket());
				break;
				/**
				 * TODO: Some logic needed. For now, node goes to sleep for 305s. The value should be taken from a packet with this request. 
				 */
			case "GO_SLEEP":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: GO_SLEEP \t"+metadata);
				nodeID=Byte.valueOf(metadata);
				newEvents.add(Engine.LIST_OF_NODES.get(nodeID).goSleep(305f));
				/*
				 * When a node goes sleep - remove all its events 
				 * 
				 * REMARK: when a node goes down when it is transmitting - END_OF_TRANSMISSION event will be removed from the event list
				 * However, all nodes synced to the transmission will evaluate END_OF_SYNCED_RECEPTION event. It's actually a bug: 
				 * a node can successfully receive a packet that was partially transmitted, but probability of the situation is neglible small and I don't care.
				 * 
				 * It may influence some statistics (eg. node A may receive 100 packets of node B, when node B transmited only 99 packets)
				 */
				Engine.eventList.removeEventsOfNode(nodeID);
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: All events of " +nodeID+" removed from the event list.");
				if (Helper.DEBUG_EVENTS) Engine.eventList.printEvents();
				break;
			case "WAKE_UP":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: WAKE_UP \t"+metadata);
				nodeID=Byte.valueOf(metadata);
				newEvents.addAll(Engine.LIST_OF_NODES.get(nodeID).wakeUp());
				break;
			case "BATTERY_CHECK":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: BATTERY_CHECK \t"+metadata);
				newEvents.addAll(Engine.checkGlobalBatteryLevels());//Battery levels for all nodes updates eg. once a 5 minutes
				break;
			case "BATTERY_DISCHARGED":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: BATTERY_DISCHARGED,\t ID: "+metadata);
				nodeID=Byte.valueOf(metadata);
				Engine.LIST_OF_NODES.get(nodeID).switchOff();	//when a battery is discharged -> turn off the node
				/*
				 * When a node goes down - remove all its events 
				 * 
				 * REMARK: when a node goes down when it is transmitting - END_OF_TRANSMISSION event will be removed from the event list.
				 * However, all nodes synced to the transmission will evaluate END_OF_SYNCED_RECEPTION event. It's actually a bug: 
				 * a node can successfully receive a packet that was partially transmitted, but probability of the situation is neglible small (?).
				 * 
				 * It may influence some statistics (eg. node A may receive 100 packets of node B, when node B transmited only 99 packets)
				 */
				Engine.eventList.removeEventsOfNode(nodeID);
				break;	
		/*
		 *Since there are no actions related with END_OF_NON_SYNCED_RECEPTION I've decided to remove the event
		 *Thanks to that, the Engine.eventList is shorter (so, it's easier to sort and so on) 
		 * 
		 * 	case "END_OF_NON_SYNCED_RECEPTION":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: END_OF_NON_SYNCED_RECEPTION,\t ID: "+metadata);
				nodeID=Byte.valueOf(metadata);
				Engine.listOfNodes.get(nodeID).endOfNonSyncedReception(this.t);
				break;
			*/
		}	
		newEvents.removeAll(Collections.singleton(null));   //remove all "null" events
		return newEvents;
	}
}
/**
 * End of transmission event. It inherits from Event (Transmission object added).
 * After end of transmission the transmitted packet can be obtained from the transmission object.
 */
class EventEndTrans extends Event{
		Transmission transmission;
		public EventEndTrans(double d, String type_, String metadata_, Transmission trans_) {
			super(d, type_, metadata_);
			transmission=trans_;
		}
		/**
		 * Only transmission events (EventStartTrans/EventEndTrans object) has a Transmission object
		 */
		Transmission getTransmission(){return transmission;} 	
}

/**
 * End of transmission event. It inherits from Event (Transmission object added).
 * After end of transmission the transmitted packet can be obtained from the transmission object.
 */
class EventStartTrans extends Event{
		Transmission transmission;
		public EventStartTrans(double d, String type_, String metadata_, Transmission trans_) {
			super(d, type_, metadata_);
			transmission=trans_;
		}
		/**
		 * Only transmission events (EventStartTrans/EventEndTrans object) has a Transmission object
		 */
		Transmission getTransmission(){return transmission;} 	
}
