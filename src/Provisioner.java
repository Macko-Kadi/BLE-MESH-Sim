/************************************************************************
Description : Provisioner class for managing algorithms
Created by : Dominika Zawiślak
************************************************************************/

import java.util.ArrayList;
import java.util.Map;

public class Provisioner {
	
	static ArrayList<Byte> listOfRelays = new ArrayList<Byte>();

	public static boolean isNodeRelay(Node n) {
		return (listOfRelays.contains(n.ID));
	}

	public static void chooseAlgorithm() {

		if (Engine.algorithm.equals("Minimum Relay Tree"))
			mRTList();
		else if (Engine.algorithm.equals("Prim")) {
			Prim.initPrim();
			primList();
		}
		else if (Engine.algorithm.equals("None"))
			noAlgorithmList();
		else
			System.out.println("No such algorithm");

	}
	
	public static void mRTList() {
		mRT.initmRT();
		
		for (Map.Entry<Integer, Integer> entry : mRT.mrtList.entrySet()) {
		    byte bytemRTID = entry.getKey().byteValue();
			listOfRelays.add(bytemRTID);
		}
	}
	
	public static void primList() {	
		for (Map.Entry<Integer, Integer> entry : Prim.relayList.entrySet()) {
		    byte bytePrimID = entry.getKey().byteValue();
			listOfRelays.add(bytePrimID);
		}
	}
	
	public static void noAlgorithmList() {
		for(byte i=0; i< Topology.NR_OF_NODES; i++) {
		listOfRelays.add(i);
		}
	}
	
}
