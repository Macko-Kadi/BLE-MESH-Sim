import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

class Event {
	float startTime;
	/**
	 * <pre>
	 * Type of the event. For now there is a number of defined events:
	 * PACKET_GENERATION
	 * TRY_TO_START_TRANSMISSION
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
	Event(float startTime_, String type_, String metadata_){
		startTime=startTime_;
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
		if (Helper.DEBUG_EVENTS) System.out.println("--evaluate Event--time: " +Engine.simTime+"----");
		byte nodeID; 										//ID of the note that is related with the event - it will be obtained from event metadata field.
		ArrayList<Event> newEvents = new ArrayList<Event>();	//while an event is evaluated it may trigger another events -> we will return the new events as a list
		switch (type){							
			case "END_OF_SYNCED_RECEPTION":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: END_OF_SYNCED_RECEPTION,\t ID: "+metadata);
				nodeID=Byte.valueOf(metadata);
				newEvents.add(Engine.LIST_OF_NODES.get(nodeID).endOfSyncedReception(this.getTransmission()));
				break;
			case "TRY_TO_START_TRANSMISSION":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: TRY_TO_START_TRANSMISSION, ID: "+metadata);
				nodeID=Byte.valueOf(metadata);
				Event e=Engine.LIST_OF_NODES.get(nodeID).tryToStartTransmission();
				newEvents.add(e);
				if (e.type.equals("END_OF_TRANSMISSION")){			//if successfully started <- end of the transmission scheduled					
					for (Node n : Engine.LIST_OF_NODES){				//all other nodes (that are able to receive) start receiving
						if (n.ID!=nodeID && n.isAbleToReceive()) newEvents.add(n.startReceiving(Engine.LIST_OF_NODES.get(nodeID).currentTransmission));
					}
				}				
				break;		
			case "END_OF_TRANSMISSION":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: END_OF_TRANSMISSION,\t ID: "+metadata);
				nodeID=Byte.valueOf(metadata);
				newEvents.add(Engine.LIST_OF_NODES.get(nodeID).endOfTransmission());	
				break;				
			case "PACKET_GENERATION":
				if (Helper.DEBUG_EVENTS) System.out.println("EVENT: PACKET_GENERATION,\t ID: \t"+metadata);
				nodeID=Byte.valueOf(metadata);
				//this Event triggers one or two Events, so we need to use addAll. 
				//generatePacket() returns an ArrayList of Events
				newEvents.addAll(Engine.LIST_OF_NODES.get(nodeID).generatePacket());
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
