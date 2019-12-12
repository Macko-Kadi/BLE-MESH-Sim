import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class Engine {
	public static double simTime = 0; // current simulation time - in seconds
//	public static final String algorithm = "None";
//	public static final String algorithm = "Prim";
	public static  String algorithm = "MRT";
//	public static final String algorithm = "Extended Minimum Relay Tree";
//	public static final String algorithm = "Weighted Prim";
//	public static final String algorithm = "OPT";
	public static  int probeNr=1;
	public static  int range=40;
	public static  int nrOfNodes=50;
	public static int lambda_=20;
	public static  String relayPath;
	public static String topo;
	public static  float rangeDb;
	public static String resPath;
	public static String excelPath;
	public static String topologyFilename;
	public static String topologyFilePath;
	public static  ArrayList<Node> LIST_OF_NODES = new ArrayList<Node>();
	public static  double BATTERY_CHECK_INTERVAL = 300; // how often to update battery statuses (and therefore ->
																// nodes transmission powers)
	static EventList eventList = new EventList();
	// static final float MAX_SIM_TIME=3600*24*10; //10 days in the simulated world
	static final float MAX_SIM_TIME = 60*60*1.05f; // 1 hour in the simulated world

	// =============================================MAINFUNCTION=========================================================
	public static void main(String[] args) {
		sim(args);	
	}
	public static void sim(String[] args_) {
		nrOfNodes=Integer.parseInt(args_[0]);
		probeNr=Integer.parseInt(args_[1]);
		range=Integer.parseInt(args_[2]);
		algorithm=args_[3];
		topo=args_[4];
		lambda_=Integer.parseInt(args_[5]);
		rangeDb =10-(float)(30*Math.log10(range));
		resPath=topo+"_"+nrOfNodes+"_"+algorithm+"_"+range+"_"+probeNr+".xls";
		relayPath = topo+"Building_N"+nrOfNodes+"_Nodes"+probeNr+"_D"+range+".000000_optres.txt";
		excelPath="D:\\pulpit\\ble-nowe\\wyniki-"+lambda_+"\\"+topo+"_"+nrOfNodes+"\\"+resPath;
	//	excelPath="D:\\pulpit\\mrt-mesh\\Maciek\\wyniki-L2\\"+topo+"_"+nrOfNodes+"\\demand\\"+resPath;
	//	topologyFilePath="D:\\pulpit\\mrt-mesh\\Maciek\\s-demand\\";
		topologyFilePath="D:\\pulpit\\ble-nowe\\bak\\";
		topologyFilename=(Engine.topo+"Building_N"+Engine.nrOfNodes+"_Nodes"+Engine.probeNr+".txt");
		System.out.println("start!");
		Topology.readFile();
	//	System.out.println("rangeDb: "+rangeDb);
		long simStartTime = System.currentTimeMillis(); // needed for simulaton time calculation (printed at the end of
														// simulation)

	// ==========================================JUST AN EXEMPLARY ENVIRONMENT====================================================
		float lambda = (float)(lambda_)/60f;
		for (Entry<Byte, Map<Float,Float >> entry1 : Topology.nodesMap.entrySet()) {
			Map<Float, Float> tmp = new HashMap<Float, Float>();
			Map<Float, Float> temp1 = new HashMap<Float, Float>();
			tmp.clear();
			temp1.clear();
			temp1 = new HashMap<Float, Float>(entry1.getValue());
			temp1.keySet().removeAll(tmp.keySet());
			tmp.putAll(temp1);
			for (Map.Entry<Float, Float> entry2 : tmp.entrySet()) 	{
				float x_i = entry2.getKey();
				float y_i = entry2.getValue();

				float[] position_i = { x_i, y_i }; // all nodes are in line with 20m in between
				float[] sourcePar = {lambda}; // each node has Poisson source (type 0) with lambda=1/60 (once per minute)
				
				int cacheSize = 1000;
				// Initialize nodes: ID=i, position_i, batteryType=0
				// packetsSource=0, lambda=1/60, casheSize=100
				LIST_OF_NODES.add(new Node(entry1.getKey(), position_i, (byte) (0), (byte) (0), sourcePar, cacheSize));
				

			}
		}		

		final Medium medium = new Medium(); // we use only static methods, so ignore the warning. However, we need to
											// instantiate the object in order to have proper path loss matrix and so
											// on.
		
		Provisioner.chooseAlgorithm();
		
		// first BATTERY_CHECK event must be triggered manually
		eventList.addEventsFromList(checkGlobalBatteryLevels());
		
		// first START_ADVERTISING_EVENT / TRY_TO_SWITCH_CHANNEL event must be triggered manually
		for(Node n : LIST_OF_NODES){
			//if all nodes would start at t=0 system would become synchronized (like slotted aloha)!
			//the performance would be better, we dont have it in real.
			simTime+=Helper.generator.nextFloat()/100; //move simTime forward (0-5ms)	
			eventList.addEvent(n.scheduleAdvertisingEvent());
			eventList.addEvent(n.scheduleTryToSwitchChannelEvent());
		
		} //after all nodes start
		
		 //each packet generation is a consequnence of the first, so if you want a node
		 //to generate packets -> you must trigger the first event manually.
		for(Node n : LIST_OF_NODES){
			if (n.ID!=35) {
					eventList.addEvent(n.schedulePacketGeneration());
				}
		}

		
		// there is no logic behind energy_saving_mode so, for the example purposes ->
		// all nodes go sleep at 0.017088s (for 305s - the value is defined in the
		// Node.goSleep() function).
//		eventList.addEvent(new Event(0.017088f, "GO_SLEEP", Byte.toString((byte) 5)));
		if (Helper.DEBUG_EVENTS) eventList.printEvents();
		eventList.printEvents();
	// ==========================================EDN OF: JUST AN EXEMPLARY ENVIRONMENT============================================
		simTime=0;
		/**
		 * after each event will increase +1 (will goes 0, when equal to
		 * iterationTreshold
		 */
		int iterations = 0;
		/**
		 * number of iterations after that the current progress of simulation is shown
		 */
		int iterationTreshold = 100000;
		/*
		 * The while block evaluates events one by one simTime must not exceed range, 
		 * and there must be an event to evaluate. Why ==1, not 0? Because there is always
		 * BATTERY_CHECK event scheduled.
		 * 
		 * UPDATE: now, there is always a set of START_ADVERTISING_EVENT events scheduled, 
		 * so the second condittion is almost always true
		 */
		boolean pgenRemoved=false;
		while (simTime < MAX_SIM_TIME && !(eventList.theList.size() == 1)) {	
			if (simTime > MAX_SIM_TIME*0.95 && !pgenRemoved) {
				eventList.removeAllPacketGenerations();
				pgenRemoved=true;
				eventList.printEvents();
			}
			simTime = eventList.getFirstEventStartTime();		
			/*Print the debug only when the evaluated event is different than START_ADVERTISING_EVENT 
			*(each node generates this type of event periodically with +-10ms interval - that's a lot!
			* If you realy want to print the event list for all events set
			* Helper.DEBUG_ADVERTISING_EVENT to true
			* 
			* Same for TRY_TO_SWITCH_CHANNEL
			*/
			//if (simTime>2095)	Helper.DEBUG_ADVERTISING_EVENT=true;
			String eventType=eventList.theList.get(0).type;
			if (Helper.DEBUG_EVENTS && (eventType!="START_ADVERTISING_EVENT" || Helper.DEBUG_ADVERTISING_EVENT) && (eventType!="TRY_TO_SWITCH_CHANNEL" || Helper.DEBUG_SWITCH_CHANNEL))
				eventList.printEvents();
		//	if(eventType=="PACKET_GENERATION") {
		//		System.out.println(simTime);
		//		System.out.println(eventType);
		//	}
			eventList.addEventsFromList(eventList.evaluateFirstEvent());
			if (iterations == iterationTreshold) {
				System.out.println(Helper.round(100 * simTime / MAX_SIM_TIME, 1) + "%"); // Progress of the simulation -
																							// just to let you estimate
																							// how long the sim will
																							// last
				iterations = 0;
			}
			iterations++;
		}
		// some results are printed
		System.out.println("============================================");
		System.out.println(
				"In this scenario, nodes 0 and 9 generate packets to node 1 (so node 1 does not schedule transmission of the received packet)");
		if (eventList.theList.size() == 1)
			System.out.println("No event to evaluate - The network stopped working after " + simTime + " seconds ["
					+ simTime / 3600 + "h] [" + simTime / 3600 / 24 + " days]");
		else
			System.out.println("Simulation finished after requested period of time: " + simTime + " seconds ["
					+ simTime / 3600 + "h] [" + simTime / 3600 / 24 + " days]");
		summarizeBatteryLevel();
		long simEndTime = System.currentTimeMillis();
		System.out.println("\nSimulation evaluated in: " + (simEndTime - simStartTime) + "ms");

		//print results
		ResultsExcel.initExcelResults();
		Results.simulationResults();
		resultsTable.simulationResultsTable();

		int relayCounter = 0;
		for (Node n : LIST_OF_NODES) {
			if (Provisioner.isNodeRelay(n)) {
				System.out.println("relay: " + n.ID);
				relayCounter ++;
			}	
		}
		System.out.println("relays: " + relayCounter);
	}

	// ==================================================================================================//
	// ==================================Engine: METHODS================================================//
	// ==================================================================================================//
	/**
	 * Checks all nodes batteries levels. If one is discharged, schedule
	 * BATTERY_DISCHARGED event. Then schedule next BATTERY_CHECK event.
	 * 
	 * @return list of events
	 */
	static ArrayList<Event> checkGlobalBatteryLevels() {
		ArrayList<Event> list = new ArrayList<Event>();
		for (Node n : LIST_OF_NODES) {
			if (n.batteryPowered)
				list.add(checkNodeBatteryLevel(n));
		}
		list.removeAll(Collections.singleton(null));
		list.add(new Event(Engine.simTime + BATTERY_CHECK_INTERVAL, "BATTERY_CHECK", " it's global thing"));
		return list;
	}

	/**
	 * If battery level of a node goes below 3%, it's discharged
	 * 
	 * @param n
	 *            Node
	 * @return BATTERY_DISCHARGED event or null
	 */
	static Event checkNodeBatteryLevel(Node n) {
		if (n.batteryPowered) n.drainBattery();
		n.updateTransmissionPower(); 	
		// if battery lvl lower than 3% and node status is not TURNED_OFF, 
		if (n.getCurrentBatteryLevel() < 3 && !n.getNodeState().equals("TURNED_OFF")) { // it's discharged.
			return new Event(simTime, "BATTERY_DISCHARGED", Byte.toString(n.ID)); 
		}
		return null;
	}

	/**
	 * Prints batteries levels of all nodes at the end of simulation
	 */
	private static void summarizeBatteryLevel() {
		System.out.println("=============BATTERY LEVELS===========");
		for (Node n : LIST_OF_NODES) {
			if (n.batteryPowered) {
				n.drainBattery();
				System.out.println("Node ID " + n.ID + " : " + Helper.round(n.getCurrentBatteryLevel(), 2) + "%");
			} else
				System.out.println("Node ID " + n.ID + " is power supplied!");
		}
	}
}
