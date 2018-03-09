import java.util.ArrayList;
import java.util.Collections;
/**
 * MS-eclipse
 * <pre>
 * Main class of the simulator. Everything starts and ends here.
 * You create an environment here 
 * </pre>
 * @author macso
 *
 */
class Engine {
	public static float simTime=0; 				//current simulation time - in seconds
	public static final int NR_OF_NODES=10; 
	public static final ArrayList<Node> LIST_OF_NODES=new ArrayList<Node>();
	public static final float BATTERY_CHECK_INTERVAL=300f; //how often to update battery statuses (and therefore -> nodes transmission powers)
	static EventList eventList=new EventList();
//	static final float MAX_SIM_TIME=3600*24*10; //10 days in the simulated world 
	static final float MAX_SIM_TIME=320f; //6 minuts in the simulated world

//=============================================MAIN FUNCTION=========================================================
	public static void main(String[] args) {
		long simStartTime=System.currentTimeMillis(); //needed for simulaton time calculation (printed at the end of simulation)
				
//==========================================JUST AN EXEMPLARY ENVIRONMENT====================================================
		 
		for(byte i=0; i<NR_OF_NODES; i++){
			float[] position_i={20*i,0.0f};	//all nodes are in line with 20m in between
			float[] sourcePar={1f/60f}; 	//each node has Poisson source (type 0) with lambda=1/60 (once per minute)
			int cacheSize=100;
			//Initialize nodes: ID=i, position_i, batteryType=3 (tiny test battery), packetsSource=0, lambda=1/60, casheSize=100
			LIST_OF_NODES.add(new Node(i, position_i,(byte)(3),(byte)(0),sourcePar,cacheSize));	
			/*
			 * Next packet generation is a consequnence of the first, so if you want a node to generate packets -> you must trigger the first event manually. 
			 * All packets triggered in the example:
			 */
			eventList.addEventsFromList(LIST_OF_NODES.get(i).generatePacket());
		}	
		final Medium medium = new Medium();	// we use only static methods, so ignore the warning. However, we need to instantiate the object in order to have proper path loss matrix and so on.
		//same as with packet generation - first BATTERY_CHECK event must be triggered manually
		eventList.addEventsFromList(checkGlobalBatteryLevels());
		//there is no logic behind energy_saving_mode so, for the example purposes -> all nodes go sleep at 0.017088s (for 305s - the value is defined in the Node.goSleep() function).
		//Why 305 ? because simulation time is set to 320 (why ? why not!) so for almost all time the node 3,4,5 sleep
		eventList.addEvent(new Event(0.017088f,"GO_SLEEP",Byte.toString((byte)3)));
		eventList.addEvent(new Event(0.017088f,"GO_SLEEP",Byte.toString((byte)4)));
		eventList.addEvent(new Event(0.017088f,"GO_SLEEP",Byte.toString((byte)5)));
		if (Helper.DEBUG_EVENTS)eventList.printEvents();
		
//==========================================EDN OF: JUST AN EXEMPLARY ENVIRONMENT============================================		
		
		/**
		 * after each event will increase +1  (will goes 0, when equal to iterationTreshold
		 */
		int iterations=0;
		/**
		 * number of iterations after that the current progress of simulation is shown
		 */
		int iterationTreshold=(int)MAX_SIM_TIME/36;	
		/*
		 * The while block evaluates events one by one
		 */
		while (simTime<MAX_SIM_TIME && !(eventList.theList.size()==1)){ //simTime must not exceed range, and there must be an event to evaluate. Why ==1, not 0? Because there is always BATTERY_CHECK event scheduled.
			simTime=eventList.getFirstEventStartTime();
			if (Helper.DEBUG_EVENTS)eventList.printEvents();
			eventList.addEventsFromList(eventList.evaluateFirstEvent());
			if(iterations==iterationTreshold){
				System.out.println(Helper.round(100*simTime/MAX_SIM_TIME,1)+"%");	// Progress of the simulation - just to let you estimate how long the sim will last
				iterations=0;
			}
		}
		//some results are printed
		System.out.println("============================================");
		System.out.println("In this scenario, nodes 0 and 9 generate packets to node 1 (so node 1 does not schedule transmission of the received packet)");		
		if (eventList.theList.size()==1) System.out.println("No event to evaluate - The network stopped working after " + simTime + " seconds ["+simTime/3600+"h] ["+simTime/3600/24+" days]");
		else System.out.println("Simulation finished after requested period of time: " + simTime + " seconds ["+simTime/3600+"h] ["+simTime/3600/24+" days]");
		summarizeBatteryLevel();
		long simEndTime=System.currentTimeMillis();
		System.out.println("\nSimulation evaluated in: "+ (simEndTime-simStartTime)+"ms");
	}
	
//==================================================================================================//
//==================================Engine: METHODS ================================================//
//==================================================================================================//
	/**
	 * Checks all nodes batteries levels. If one is discharged, schedule BATTERY_DISCHARGED event. Then schedule next BATTERY_CHECK event.
	 * @return list of events
	 */
	static ArrayList<Event> checkGlobalBatteryLevels(){
		ArrayList<Event> list=new ArrayList<Event>();
		for (Node n : LIST_OF_NODES)	{
			if (n.batteryPowered) list.add(checkNodeBatteryLevel(n));
		}
		list.removeAll(Collections.singleton(null));
		list.add(new Event(Engine.simTime+BATTERY_CHECK_INTERVAL,"BATTERY_CHECK"," it's global thing"));
		return list;
	}
	/**
	 * If battery level of a node goes below 3%, it's discharged
	 * @param n Node
	 * @return	BATTERY_DISCHARGED event or null
	 */
	static Event checkNodeBatteryLevel(Node n){
		n.updateTransmissionPower(); //Just in case - update battery lvl if the state does not change during simulation (eg. in tests when a node is set to sleep for the whole time)
		if (n.getCurrentBatteryLevel()<3 && !n.getNodeState().equals("TURNED_OFF")){ //if battery lvl lower than 3% and node status is not discharged
			return new Event(simTime,"BATTERY_DISCHARGED",Byte.toString(n.ID));			//it's discharged.
		}
		return null;
	}
	/**
	 * Prints batteries levels of all nodes at the end of simulation
	 */
	private static void summarizeBatteryLevel(){
		System.out.println("=============BATTERY LEVELS===========");
		for (Node n : LIST_OF_NODES){
			if (n.batteryPowered){
				n.drainBattery();					
				System.out.println("Node ID "+n.ID+ " : " +Helper.round(n.getCurrentBatteryLevel(),2)+"%");
			}
			else System.out.println("Node ID "+n.ID+ " is power supplied!");
		}
	}
}
