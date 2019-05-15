/************************************************************************
Description : Weighted Prim Algorithm implementation
Created by : Dominika Zawislak
************************************************************************/
  
import java.util.*;
import java.util.Map.Entry; 

class WeightedPrim 
{ 
    // Number of vertices in the graph 
	private static final int V=Topology.NR_OF_NODES;
	public static Map<Integer, Integer> wpRelayList = new HashMap<Integer, Integer>();
  
    // A utility function to find the vertex with minimum key 
    // value, from the set of vertices not yet included in MST 
    int minKey(int key[], Boolean mstSet[]) 
    { 
        // Initialize min value 
        int min = Integer.MAX_VALUE, min_index=-1; 
  
        for (int v = 0; v < V; v++) 
            if (mstSet[v] == false && key[v] < min) 
            { 
                min = key[v]; 
                min_index = v; 
            } 
  
        return min_index; 
    } 
  
    // A utility function to print the constructed MST stored in 
    // parent[] 
	public static void printRelayList() {
		for (Map.Entry<Integer, Integer> entry : wpRelayList.entrySet()) {
			System.out.println(" nodemrt " + entry.getKey() + " degree " + entry.getValue() + "\n");
		}
		System.out.println(" Number_of_Relays: " + wpRelayList.size() + " from " + V + " nodes.");
	}
  
    // Function to construct and print MST for a graph represented 
    // using adjacency matrix representation 
    void primMST(int graph[][]) 
    { 
        // Array to store constructed MST 
        int parent[] = new int[V]; 
  
        // Key values used to pick minimum weight edge in cut 
        int key[] = new int [V]; 
  
        // To represent set of vertices not yet included in MST 
        Boolean mstSet[] = new Boolean[V]; 
  
        // Initialize all keys as INFINITE 
        for (int i = 0; i < V; i++) 
        { 
            key[i] = Integer.MAX_VALUE; 
            mstSet[i] = false; 
        } 
  
        // Always include first 1st vertex in MST. 
        key[0] = 0;     // Make key 0 so that this vertex is 
                        // picked as first vertex 
        parent[0] = -1; // First node is always root of MST 
  
        // The MST will have V vertices 
        for (int count = 0; count < V-1; count++) 
        { 
            // Pick thd minimum key vertex from the set of vertices 
            // not yet included in MST 
            int u = minKey(key, mstSet); 
  
            // Add the picked vertex to the MST Set 
            mstSet[u] = true; 
  
            // Update key value and parent index of the adjacent 
            // vertices of the picked vertex. Consider only those 
            // vertices which are not yet included in MST 
            for (int v = 0; v < V; v++) 
  
                // graph[u][v] is non zero only for adjacent vertices of m 
                // mstSet[v] is false for vertices not yet included in MST 
                // Update the key only if graph[u][v] is smaller than key[v] 
                if (graph[u][v]!=0 && mstSet[v] == false && 
                    graph[u][v] < key[v]) 
                { 
                    parent[v] = u; 
                    key[v] = graph[u][v]; 
                } 
        } 
  
        printMST(parent, V, graph); 
		// add relays to list
		addRelays(parent, V, graph); 
        printRelayList(); 
    } 
    
    void printMST(int parent[], int n, int graph[][]) 
    { 
        System.out.println("Edge \tWeight"); 
        for (int i = 1; i < V; i++) 
            System.out.println(parent[i]+" - "+ i+"\t"+ 
                            graph[i][parent[i]]); 
    }
  
	public static int[][] constructGraph() {
		int[][] graph= new int[V][V];
		
		byte row, column;
		int rowInt = 0;
		int columnInt = 0;
		
		for (row = 0; row < Topology.NR_OF_NODES; row++) {
			for (column = 0; column < Topology.NR_OF_NODES; column++) {
				float powerRecv = Node.MAX_TRANSMISSION_POWER - Node.MAX_TRANSMISSION_POWER * Medium.Pld0_Pt
						- Medium.getPowerDecreaseBetweenNodes(row, column);
				float SNR = powerRecv - Medium.BACKGROUND_NOISE;
				rowInt = (int) row;
				columnInt = (int) column;
				if (SNR > 25.00){
					if(rowInt==columnInt)
						graph[rowInt][columnInt]=0;
					else
						graph[rowInt][columnInt]=(int) (100/SNR);
				}
				else {
					graph[rowInt][columnInt]=0;
				}

			}
		}
		 return graph;
	}
	
	public static void addRelays(int parent[], int n, int graph[][]) {

		ArrayList<Integer> ListOfParents = new ArrayList<Integer>();
		ArrayList<Integer> ListOfChildren = new ArrayList<Integer>();
		Map<Integer, Integer> ListOfDegrees = new HashMap<Integer, Integer>();

		for (int i = 1; i < V; i++) {
			ListOfParents.add(parent[i]);
			ListOfChildren.add(i);
		}

		for (int j = 0; j < V; j++) {
			int degree = 0;

			for (int k = 0; k < ListOfParents.size(); k++) {
				if (ListOfParents.get(k) == j) {
					degree++;
				}
			}

			for (int l = 0; l < ListOfChildren.size(); l++) {
				if (ListOfChildren.get(l) == j) {
					degree++;
				}
			}

			ListOfDegrees.put(j, degree);
		}

		for (Entry<Integer, Integer> entrylist : ListOfDegrees.entrySet()) {
			if (entrylist.getValue() > 1) {
				wpRelayList.put(entrylist.getKey(), entrylist.getValue());
			}
		}

//		return (relayList);
	}
	
	public static void initWeightedPrim() {
		WeightedPrim t = new WeightedPrim();
		int graph[][] = constructGraph();
		t.primMST(graph);
	}
	
//    public static void main (String[] args) 
//    { 
//        /* Let us create the following graph 
//        2 3 
//        (0)--(1)--(2) 
//        | / \ | 
//        6| 8/ \5 |7 
//        | /     \ | 
//        (3)-------(4) 
//            9         */
//        PrimTest t = new PrimTest(); 
//        int graph[][] = constructGraph();
////        int graph[][] = new int[][] {{0, 2, 0, 6, 0}, 
////                                    {2, 0, 3, 8, 5}, 
////                                    {0, 3, 0, 0, 7}, 
////                                    {6, 8, 0, 0, 9}, 
////                                    {0, 5, 7, 9, 0}}; 
//  
//        // Print the solution 
//        t.primMST(graph); 
//    } 
} 