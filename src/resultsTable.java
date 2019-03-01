/************************************************************************
Description : 
Created by : Dominika Zawiślak
************************************************************************/

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class resultsTable {

	static PrintWriter writer = null;
	static ArrayList<Double> Energy = new ArrayList<Double>();
	static ArrayList<Double> Hops = new ArrayList<Double>();
	static ArrayList<Double> Delay = new ArrayList<Double>();
	static double mean;
	static double confidenceInterval;

	public static void simulationResultsTable() {

		try {
			String path = "D:\\GoogleDrive\\_PRACA\\eclipse-workspace\\BLE_MESH_SIM\\results\\tables\\";
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
		packetLoss();
		duplicateCounter();	
		hopsCounter();
		meanHops();	
		packetDelay();
		meanPacketDelay();
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
				Energy.add(n.getUsedEnergyLevel());
				energyMap.put(n.ID, n.getUsedEnergyLevel());
				totalEnergy = totalEnergy + n.getUsedEnergyLevel();
			} else {
				writer.println("Node ID " + n.ID + " is power supplied!");
			}
		}

	//	minEnergy = Energy.stream().min(Comparator.comparing(i -> i)).get();
		for (Entry<Byte, Double> entry : energyMap.entrySet()) {
			if (entry.getValue().equals(minEnergy)) {
				if (minNodes.equals("zero"))
					minNodes = String.valueOf(entry.getKey());
				else
					minNodes = (minNodes + ", " + String.valueOf(entry.getKey()));
			}
		}

	//	maxEnergy = Energy.stream().max(Comparator.comparing(i -> i)).get();
		for (Entry<Byte, Double> entry : energyMap.entrySet()) {
			if (entry.getValue().equals(maxEnergy)) {
				if (maxNodes.equals("zero"))
					maxNodes = String.valueOf(entry.getKey());
				else
					minNodes = (maxNodes + ", " + String.valueOf(entry.getKey()));
			}
		}
		writer.println("Total energy used in simulation: " + totalEnergy*1000 + "uWh \n");
		calculateConfidenceInterval(Energy);
		writer.println("Average amount of energy used in simulation: " + mean*1000 +" � " + confidenceInterval*1000+ " uWh \n");
		writer.println("Smallest amount of used energy " + minEnergy*1000 + " uWh, by node(s) " + minNodes + " \n");
		writer.println("Biggest amount of used energy " + maxEnergy*1000 + " uWh, by node(s) " + maxNodes + " \n");
		writer.println("========================================================================= \n");

	}
	
	
	private static void packetDelay() {
//		writer.println("Delay (period of time between packet generation and packet being received): "+"\n");
		
		for (Entry<String, Double> startPacket : Node.timeOfPacketGeneration.entrySet()) {
			String tmppacketID = startPacket.getKey();
			Double tmppacketStart = startPacket.getValue();
			for (Entry<String, Double> receivePacket: Node.timeOfPacketReception.entrySet()) {
				if(receivePacket.getKey().equals(tmppacketID)) {
					Double packetDelay = (double) (receivePacket.getValue() - tmppacketStart);
//					writer.println("Delay: "+packetDelay+" \n");
					Delay.add(packetDelay);
				}		
			}
		}
//		writer.println("========================================================================= \n");
	}
	
	private static void meanPacketDelay() {
	calculateConfidenceInterval(Delay);
	
	writer.println("Average packet delay in simulation: " + mean +" � " + confidenceInterval+ " \n");
	writer.println("========================================================================= \n");
	}


	private static void packetLoss() {
		writer.println("Packet loss:  \n");
		writer.println("Number of generated messages: " + (Node.generatedPacketCount-1) + "\n");
		writer.println("Number of received messages: " + Node.packetReceivedCount + "\n");
		double difference = Node.generatedPacketCount-1 -Node.packetReceivedCount; 
		writer.println("Difference: " + difference + " \n");
		writer.println("Number of backoff procedures: " + Node.retransmit + "\n");
		writer.println("========================================================================= \n");
	}

	private static void hopsCounter() {
	//	writer.println("ilo�� przeskok�w ? "+"\n");
		for (Packet p : Node.packetList) {
	//		writer.println("Packet: "+p.header.packetID+"	 TTL: "+p.header.TTL+"\n");
			double hops = 20-(double) p.header.TTL;
			Hops.add(hops);
		}
	//	writer.println("========================================================================= \n");
	}
	
	private static void meanHops() {
	calculateConfidenceInterval(Hops);
	
	writer.println("Average amount of hops made by a packet: " + mean +" � " + confidenceInterval+ " \n");
	writer.println("========================================================================= \n");
	}

	private static void duplicateCounter() {
		writer.println("Number of packets that reached destination more than once: " + Node.duplicateCounter+"\n");
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
