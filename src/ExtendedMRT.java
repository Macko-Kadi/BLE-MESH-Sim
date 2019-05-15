/************************************************************************
Description : Extended Minimum Relay Tree Algorithm Implementation
Created by : Dominika Zawiœlak
************************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class ExtendedMRT {
	static Map<Integer, Integer> vertexList = new HashMap<Integer, Integer>();

	//run EMRT algorithm
	public static Map<Integer, Integer> initExtendedmRT() {
		Map<Integer, Integer> exmrtList = new HashMap<Integer, Integer>();
		
		//create map of nodes and it's neighbors degrees
		Multimap<Integer, Map<Integer, Integer>> edgesMatrix = createList();
		
		//create list of relay nodes
		exmrtList = findMRT(edgesMatrix);
		
		return exmrtList;
	}

	public static Multimap<Integer, Map<Integer, Integer>> createList() {
		Multimap<Integer, Map<Integer, Integer>> edgesMatrix = ArrayListMultimap.create();
		byte startVertex, endVertex;
		int start = 0;
		int end;
		int w = 0;
		double inf = Double.POSITIVE_INFINITY;
		ArrayList<Integer> EndV = new ArrayList<Integer>();

		for (startVertex = 0; startVertex < Topology.NR_OF_NODES; startVertex++) {
			w = 0;
			for (endVertex = 0; endVertex < Topology.NR_OF_NODES; endVertex++) {
				float powerRecv = Node.MAX_TRANSMISSION_POWER - Node.MAX_TRANSMISSION_POWER * Medium.Pld0_Pt
						- Medium.getPowerDecreaseBetweenNodes(startVertex, endVertex);
				float SNR = powerRecv - Medium.BACKGROUND_NOISE;
				start = (int) startVertex;
				end = (int) endVertex;
			//	System.out.println("NodeIDs: " + start + "-" + end + " powerRecv: "
			//			+ Medium.getPowerDecreaseBetweenNodes(startVertex, endVertex));
			//	System.out.println("NodeIDs: " + start + "-" + end + " SNR: " + SNR);
				if (SNR > 50.00 && SNR!=inf) {
					w = w + 1;
					EndV.add(end);
				}

			}

			vertexList.put(start, w);

			Map<Integer, Integer> tmpMap = new HashMap<Integer, Integer>();
			tmpMap.put(start, w);

			for (int ee = 0; ee < EndV.size(); ee++) {
				edgesMatrix.put(EndV.get(ee), tmpMap);
			}

			tmpMap.remove(tmpMap);
			EndV.removeAll(EndV);

		}

		return edgesMatrix;
	}

	public static Map<Integer, Integer> findMRT(Multimap<Integer, Map<Integer, Integer>> edgesMatrix) {
		//map of covered nodes
		Map<Integer, Integer> removedList = new HashMap<Integer, Integer>();
		//map of relay nodes
		Map<Integer, Integer> exmrtList = new HashMap<Integer, Integer>();		
		//map of nodes and their relays
		Map<Integer, Integer> relaysForNodeCounter = new HashMap<Integer, Integer>();
		Integer mrtID2 = 0;
		
		//1. create toAddList with all nodes and notRelayNodesList with all nodes (later without relays)
		Map<Integer, Integer> toAddList = new HashMap<Integer, Integer>();
		Map<Integer, Integer> notRelayNodesList = new HashMap<Integer, Integer>();
		Map tmp = new HashMap(vertexList);
		tmp.keySet().removeAll(toAddList.keySet());
		toAddList.putAll(tmp);
		notRelayNodesList.putAll(tmp);
		
		//2. check if there are any relays
		while (!toAddList.isEmpty()) {
			System.out.println("List of nodes: ");
			for (Map.Entry<Integer, Integer> entry : toAddList.entrySet()) {
				System.out.println(" Node " + entry.getKey() + " degree " + entry.getValue());
			}

			//3. select newRelay, first node with highest degree
			int VertexID = 0;
			int Degree = 0;

			if (exmrtList.isEmpty()) {
				Map.Entry<Integer, Integer> one = toAddList.entrySet().iterator().next();
				VertexID = one.getKey();
				Degree = one.getValue();

				for (Map.Entry<Integer, Integer> entry : toAddList.entrySet()) {
					if (Degree < entry.getValue()) {
						VertexID = entry.getKey();
						Degree = entry.getValue();
					}
				}
				System.out.println(" First Relay: " + VertexID + " covered nodes " + Degree + "\n");
				
			//4. if there are already relays find next one
			} else {
				for (Map.Entry<Integer, Integer> t1 : notRelayNodesList.entrySet()) {
					int newDegree = 0;
					Integer mrtNeighbourID = t1.getKey();
					
					for (Map.Entry<Integer, Map<Integer, Integer>> neighbour2 : edgesMatrix.entries()) {
						if (neighbour2.getKey().equals(mrtNeighbourID)) {
							Map<Integer, Integer> toT2 = new HashMap<Integer, Integer>();
							Map<Integer, Integer> temp2 = new HashMap<Integer, Integer>();
							toT2.clear();
							temp2.clear();
							temp2 = new HashMap(neighbour2.getValue());
							temp2.keySet().removeAll(toT2.keySet());
							toT2.putAll(temp2);

							for (Map.Entry<Integer, Integer> ad : toAddList.entrySet()) {
								for (Map.Entry<Integer, Integer> t2 : toT2.entrySet()) {
									if (ad.getKey().equals(t2.getKey())) {
										newDegree = newDegree + 1;
									}
								}
							}
						}
					}
					
					if (Degree < newDegree) {
						VertexID = (mrtNeighbourID);
						Degree = (newDegree);
					}
				}
				System.out.println(" New Relay: " + VertexID + " covered nodes " + Degree + "\n");
			}

			// 5. add newRelay to exmrtList
			mrtID2 = VertexID;
			exmrtList.put(VertexID, Degree);
			//6. remove relay from list of all nodes
			notRelayNodesList.remove(VertexID);
			//7. update list of neighbors and its relays
			for (Map.Entry<Integer, Map<Integer, Integer>> a : edgesMatrix.entries()) {
				Map<Integer, Integer> toT = new HashMap<Integer, Integer>();
				Map temp = new HashMap(a.getValue());
				temp.keySet().removeAll(toT.keySet());
				toT.putAll(temp);
				for (Map.Entry<Integer, Integer> t : toT.entrySet()) {
					if (t.getKey().equals(VertexID)) {
						Integer tmpKey = a.getKey();
						if(relaysForNodeCounter.containsKey(a.getKey())) {
							relaysForNodeCounter.remove(a.getKey(), 1);
							relaysForNodeCounter.put(a.getKey(), 2);
						} else {
							relaysForNodeCounter.put(a.getKey(), 1);
						}
						//8. add removed neighbors to list of removed nodes
						removedList.put(a.getKey(), 0);						
					}
				}
			}
			
			//9. if node has two relays remove it from toAddList
			for (Map.Entry<Integer, Integer> relaysforNode : relaysForNodeCounter.entrySet()) {
				if (relaysforNode.getValue()>=2) {
					toAddList.remove(relaysforNode.getKey());
				}
			}
		}

		System.out.println("Number of Relays: " + exmrtList.size() + " from " + vertexList.size() + " nodes.");
		return exmrtList;
	}
}