import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class mRT {

	Multimap<String, String> multiMap = ArrayListMultimap.create();

	static Map<Integer, Integer> vertexList = new HashMap<Integer, Integer>();
	public static Map<Integer, Integer> mrtList = new HashMap<Integer, Integer>();
	static Multimap<Integer, Map<Integer, Integer>> edgesMatrix = ArrayListMultimap.create();
//	static ArrayList<Integer> removedList = new ArrayList<Integer>();
	static Map<Integer, Integer> removedList = new HashMap<Integer, Integer>();
//	Map<Integer, Map<Integer, Integer>> removedList = new HashMap<Integer, Map<Integer, Integer>>();

	int numberVertex;
	int VertexID;
	static Integer mrtID2=0;
	static PrintWriter writer = null;

	static void initmRT() {
		
//		try {
//			String path = "C:\\Users\\Dominika.Zawislak\\Downloads\\BLE-MESH-Sim-master\\simresults\\";
//			String fileName=("mRTResults_"+Topology.topologyType+"_"+Topology.NR_OF_NODES+".txt");
//			writer = new PrintWriter(path+fileName, "UTF-8");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		createList();
		findMRT();

	}

	public static void createList() {

		byte startVertex, endVertex;
		int start = 0;
		int end;
		int w = 0;

		ArrayList<Integer> EndV = new ArrayList<Integer>();

		System.out.println("\n");
		System.out.println("SNR levels:");
		for (startVertex = 0; startVertex < Topology.NR_OF_NODES; startVertex++) {
			w = 0;
			for (endVertex = 0; endVertex < Topology.NR_OF_NODES; endVertex++) {
				float powerRecv = Node.MAX_TRANSMISSION_POWER - Node.MAX_TRANSMISSION_POWER * Medium.Pld0_Pt
						- Medium.getPowerDecreaseBetweenNodes(startVertex, endVertex);
				float SNR = powerRecv - Medium.BACKGROUND_NOISE;
				start = (int) startVertex;
				end = (int) endVertex;
				System.out.println("NodeIDs: " + start + "-" + end + " powerRecv: " + Medium.getPowerDecreaseBetweenNodes(startVertex, endVertex));
				System.out.println("NodeIDs: " + start + "-" + end + " SNR: " + SNR);
				if (SNR > 25.00) {
					w = w + 1;
					EndV.add(end);
				}

			}

			vertexList.put(start, w);

			Map<Integer, Integer> ss = new HashMap<Integer, Integer>();
			ss.put(start, w);

			for (int ee = 0; ee < EndV.size(); ee++) {
				edgesMatrix.put(EndV.get(ee), ss);
			}

			ss.remove(ss);
			EndV.removeAll(EndV);

		}

	}

	public static Map<Integer, Integer> findMRT() {

		// calculate minimum Relay Tree

		// clean mrtList
		mrtList.clear();

		// create toAddList with all nodes
		Map<Integer, Integer> toAddList = new HashMap<Integer, Integer>();
		Map tmp = new HashMap(vertexList);
		tmp.keySet().removeAll(toAddList.keySet());
		toAddList.putAll(tmp);

		
		
		// TODO: dodac warunek konca jesli graf jest niespojny tj. jesli w danych cyklu
		// nie udalo sie nic usunac z toAddList
		 while ( !toAddList.isEmpty() ) {
			System.out.println("List of nodes: ");
			for (Map.Entry<Integer, Integer> entry : toAddList.entrySet()) {
				System.out.println(" Node " + entry.getKey() + " degree " + entry.getValue());
//				writer.println(" Node " + entry.getKey() + " degree " + entry.getValue());
			}

			// 1. select newRelay
			int VertexID = 0;
			int Degree = 0;
			// Pair newRelay = new Pair(VertexID, Degree);

			if (mrtList.isEmpty()) {
				// initially set newRelay as the highest degree node from the toAddList
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
//				writer.println(" First Relay: " + VertexID + " covered nodes " + Degree + "\n");
			} else {

//				for (Map.Entry<Integer, Map<Integer, Integer>> neighbour : edgesMatrix.entries()) {
//
//					if (neighbour.getKey().equals(mrtID2)) {
//					
//
//						Map<Integer, Integer> toT1 = new HashMap<Integer, Integer>();
//						Map<Integer, Integer> temp1 = new HashMap<Integer, Integer>();
//						toT1.clear();
//						temp1.clear();
//						temp1 = new HashMap(neighbour.getValue());
//						temp1.keySet().removeAll(toT1.keySet());
//						toT1.putAll(temp1);

						
//						int newDegree=0;
//						for (Map.Entry<Integer, Integer> t1 : toT1.entrySet()) {
						for (Map.Entry<Integer, Integer> t1 : removedList.entrySet()) {
							
							int newDegree=0;
							
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
							if(ad.getKey().equals(t2.getKey())) {
							newDegree=newDegree+1;}
							}
							}
						}
						}
							if (Degree < newDegree) {
								VertexID = (mrtNeighbourID);
								Degree = (newDegree);
							}
						}
//						}
//
//					
//					}
//				}
				System.out.println(" New Relay: " + VertexID + " covered nodes " + Degree + "\n");
//				writer.println(" New Relay: " + VertexID + " covered nodes " + Degree + "\n");
			}

			// 2. add newRelay to mrtList
			 mrtID2=VertexID;
			mrtList.put(VertexID, Degree);
			// 3. remove newRelay and its neighbours from toAddList
			toAddList.remove(VertexID);
			for (Map.Entry<Integer, Map<Integer, Integer>> a : edgesMatrix.entries()) {
				Map<Integer, Integer> toT = new HashMap<Integer, Integer>();
				Map temp = new HashMap(a.getValue());
				temp.keySet().removeAll(toT.keySet());
				toT.putAll(temp);
				for (Map.Entry<Integer, Integer> t : toT.entrySet()) {
					// System.out.println(" nodeT "+ t.getKey() + " degreeT " + t.getValue());
					if (t.getKey().equals(VertexID)) {
						toAddList.remove(a.getKey());
					//add removed neighbours to list of removed nodes
						removedList.put(a.getKey(),0);
					}
				}
			}

		}

		for (Map.Entry<Integer, Integer> e : mrtList.entrySet()) {
			System.out.println(" nodemrt " + e.getKey() + " degree " + e.getValue() + "\n");
//			writer.println(" nodemrt " + e.getKey() + " degree " + e.getValue() + "\n");
		}

		System.out.println(" Number_of_Relays: " + mrtList.size()+ " from "+ vertexList.size()+ " nodes.");
//		writer.println(" Number_of_Relays: " + mrtList.size()+ " from "+ vertexList.size()+ " nodes.");

		return (mrtList);

	}

}
