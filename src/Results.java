/************************************************************************
Description : Class for gathering simulation results in a .txt file
Created by : Dominika ZawiÅ›lak
************************************************************************/

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Results {

	static PrintWriter writer = null;
	static ArrayList<Double> Energy = new ArrayList<Double>();
	static ArrayList<Double> Hops = new ArrayList<Double>();
	static ArrayList<Double> Delay = new ArrayList<Double>();
	static double mean;
	static double confidenceInterval;

	public static void simulationResults() {

		try {
			String path = "D:\\GoogleDrive\\_PRACA\\eclipse-workspace\\BLE_MESH_SIM\\results\\";
			String fileName = (Engine.algorithm + "_" + Topology.topologyType + "_" + Topology.NR_OF_NODES + ".txt");
			writer = new PrintWriter(path + fileName, "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		writer.println(
				"Simulation with " + Topology.NR_OF_NODES + " nodes in " + Topology.topologyType + " topology. \n");
		writer.println("Algorithm used: " + Engine.algorithm + "\n");
		writer.println("========================================================================= \n");

		energyUsage();
		meanEnergy();

		packetDelay();
		meanPacketDelay();
		
		packetLoss();
		
		hopsCounter();
		meanHops();
		
		duplicateCounter();
		
		collisionCounter();

		writer.close();
	}

	private static void energyUsage() {
		Energy = new ArrayList<Double>();
		Map<Byte, Double> energyMap = new HashMap<Byte, Double>();
		double minEnergy = 0;
		double maxEnergy = 0;
		double totalEnergy = 0;
		String minNodes = "zero";
		String maxNodes = "zero";

		for (Node n : Engine.LIST_OF_NODES) {
			if (n.batteryPowered) {
				n.drainBattery();

				writer.println("Node ID " + n.ID + "\n");
				writer.println("Initial energy level : " + n.getInitialEnergyLevel()*1000 + "uWh \n");
				writer.println("Current energy level : " + n.getCurrentEnergyLevel()*1000 + "uWh \n");
				writer.println("Used energy : " + n.getUsedEnergyLevel()*1000 + "uWh \n");
				writer.println("Battery level : " + Helper.round(n.getCurrentBatteryLevel(), 2) + "% \n");
				writer.println("\n");

				Energy.add(n.getUsedEnergyLevel());
				energyMap.put(n.ID, n.getUsedEnergyLevel());
				totalEnergy = totalEnergy + n.getUsedEnergyLevel();
			} else {
				writer.println("Node ID " + n.ID + " is power supplied!");
			}
		}

		minEnergy = Energy.stream().min(Comparator.comparing(i -> i)).get();
		for (Entry<Byte, Double> entry : energyMap.entrySet()) {
			if (entry.getValue().equals(minEnergy)) {
				if (minNodes.equals("zero"))
					minNodes = String.valueOf(entry.getKey());
				else
					minNodes = (minNodes + ", " + String.valueOf(entry.getKey()));
			}
		}

		maxEnergy = Energy.stream().max(Comparator.comparing(i -> i)).get();
		for (Entry<Byte, Double> entry : energyMap.entrySet()) {
			if (entry.getValue().equals(maxEnergy)) {
				if (maxNodes.equals("zero"))
					maxNodes = String.valueOf(entry.getKey());
				else
					minNodes = (maxNodes + ", " + String.valueOf(entry.getKey()));
			}
		}
		writer.println("Smallest amount of used energy " + minEnergy*1000 + " uWh, by node(s) " + minNodes + " \n");
		;
		writer.println("Biggest amount of used energy " + maxEnergy*1000 + " uWh, by node(s) " + maxNodes + " \n");
		writer.println("Total energy used in simulation: " + totalEnergy*1000 + "uWh \n");
		writer.println("========================================================================= \n");

	}
	
	private static void meanEnergy() {
	calculateConfidenceInterval(Energy);
	
	writer.println("Average amount of energy used in simulation: " + mean*1000 +" ï¿½ " + confidenceInterval*1000+ " uWh \n");
	writer.println("========================================================================= \n");
	}

	
	private static void packetDelay() {
		writer.println("Delay (period of time between packet generation and packet being received): "+"\n");
		
		for (Entry<String, Double> startPacket : Node.timeOfPacketGeneration.entrySet()) {
			String tmppacketID = startPacket.getKey();
			Double tmppacketStart = startPacket.getValue();
		//	System.out.println("Results.packetDelay() - tmppacketStart "+ tmppacketStart);
			for (Entry<String, Double> receivePacket: Node.timeOfPacketReception.entrySet()) {
				if(receivePacket.getKey().equals(tmppacketID)) {
					Double packetDelay = (double) (receivePacket.getValue() - tmppacketStart);
				//	writer.println("Delay: "+packetDelay+" \n");
			//	System.out.println("Results.packetDelay(): "+packetDelay);
					Delay.add(packetDelay);
				}		
			}
		}
		writer.println("========================================================================= \n");
	}
	
	private static void meanPacketDelay() {
	calculateConfidenceInterval(Delay);
	
	writer.println("Average packet delay in simulation: " + mean +" ï¿½ " + confidenceInterval+ " \n");
	writer.println("========================================================================= \n");
	}

	/**
	 * The function return packets that are still in queues of nodes (we should consider the number to approximate Ploss when system utilisation is high)
	 * @return
	 */
	private static int getNumberOfPacketsInTheSystem(){
		int number=0;
		for(Node n : Engine.LIST_OF_NODES){
			System.out.println("Node "+n.ID + " queue size: " + n.queue.size());
			number+=n.queue.size();
		}
		return number;
	}
	//MACIEK coœ zle
	private static void packetLoss() {
		writer.println("Packet loss:  \n");
		writer.println("Number of generated messages: " + (Node.generatedPacketCount) + "\n");
		writer.println("Number of received messages: " + Node.packetReceivedCount + "\n");
		int numberOfPacketsInTheSystem=getNumberOfPacketsInTheSystem();
		System.out.println("nr in system: "+ numberOfPacketsInTheSystem);
		writer.println("Number of packets in the system: " + numberOfPacketsInTheSystem+"\n");
		writer.println("Number of backoff procedures: " + Node.retransmit + "\n");
		float IPLRmax=100*(float)(Node.generatedPacketCount-Node.packetReceivedCount)/Node.generatedPacketCount;								 //MACIEK poprawka
		float IPLRmin=100*(float)(Node.generatedPacketCount-Node.packetReceivedCount-numberOfPacketsInTheSystem)/Node.generatedPacketCount; //MACIEK poprawka
		writer.println("IPLR is approximately: " + Helper.round(IPLRmin, 2) + "-"+Helper.round(IPLRmax, 2)+" % \n");
		writer.println("========================================================================= \n");
	}

	private static void hopsCounter() {
		writer.println("iloï¿½ï¿½ przeskokï¿½w ? "+"\n");
		for (Packet p : Node.packetList) {
//			writer.println("Packet: "+p.header.packetID+"	 TTL: "+p.header.TTL+"\n");
		//	System.out.println("Packet: "+p.header.packetID+"	 TTL: "+p.header.TTL+"\n");
			double hops = 20-(double) p.header.TTL;
	//		System.out.println(hops);
			Hops.add(hops);
		}
		writer.println("========================================================================= \n");
	}
	
	private static void meanHops() {
	calculateConfidenceInterval(Hops);
	
	writer.println("Average amount of hops made by a packet: " + mean +" ï¿½ " + confidenceInterval+ " \n");
	writer.println("========================================================================= \n");
	}

	private static void duplicateCounter() {
		writer.println("Number of packets that reached destination more than once: " + Node.duplicateCounter+"\n");
		for (int d = 0; d < Node.duplicateList.size(); d++) {
			writer.println("Packet ID: " + Node.duplicateList.get(d)+"\n");
		}

		writer.println("========================================================================= \n");

	}
	
	private static void collisionCounter() {
		writer.println("Number of collisions during simulation: " + Node.collisionCounter+"\n");
		writer.println("========================================================================= \n");

	}
	
	private static double[] calculateConfidenceInterval(ArrayList<Double> energy2) {

	    // calculate the mean value (= average)
	    double sum = 0.0;
	    for (Double num : energy2) {
	        sum += num;
	    }
	    mean = sum / energy2.size();

	    // calculate standard deviation
	    double squaredDifferenceSum = 0.0;
	    for (Double num : energy2) {
	        squaredDifferenceSum += (num - mean) * (num - mean);
	    }
	    double variance = squaredDifferenceSum / (energy2.size()-1);
	    double standardDeviation = Math.sqrt(variance);

	    // value for 95% confidence interval, source: https://en.wikipedia.org/wiki/Confidence_interval#Basic_Steps
	    double confidenceLevel = 1.96;
	    confidenceInterval = confidenceLevel * standardDeviation / Math.sqrt(energy2.size());
	    return new double[]{mean - confidenceInterval, mean + confidenceInterval};
	}

}
