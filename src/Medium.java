import java.util.List;
import java.util.ArrayList;

/**
 * <pre>
 * The class represents wireless medium.
 * Main components: 
 * - a list of transmissions in the air
 * - noise levels as seen by particular nodes
 * - path losses between each pair of nodes (Transmission power - Path Loss = Receiving power)
 * </pre>
 *
 */
class Medium {
	/**
	 * if there are no transmissions in the air, the noise equals the value.
	 */
	static final float BACKGROUND_NOISE=-80;
	/**
	 * constant time of synchronization phase
	 * TODO: is it realistic ?
	 */
	static final float SYNC_TIME=0.001f;
	/**
	 * Transmissions in the air
	 */
	static ArrayList<Transmission> currentTransmissions=new ArrayList<Transmission>();
	/**
	 * <pre>
	 *List of noise lvls (channel 37) (for all nodes). 
	 *Values in the list are calculated with taking into account, that node synced reception and a transmission that node transmit is NOT a noise from the node perspective.
	 *</pre>
	 */
	static ArrayList<Float> globalNoiseLvl37 = new ArrayList<Float>();
	/**
	 * <pre>
	 *List of noise lvls (channel 38) (for all nodes). 
	 *Values in the list are calculated with taking into account, that node synced reception and a transmission that node transmit is NOT a noise from the node perspective.
	 *</pre>
	 */
	static ArrayList<Float> globalNoiseLvl38 = new ArrayList<Float>();
	/**
	 * <pre>
	 *List of noise lvls (channel 39) (for all nodes). 
	 *Values in the list are calculated with taking into account, that node synced reception and a transmission that node transmit is NOT a noise from the node perspective.
	 *</pre>
	 */
	static ArrayList<Float> globalNoiseLvl39 = new ArrayList<Float>();
	/**
	 * Noises per channel
	 */
	static List<Float>[] globalNoiseLvl = new List[3];
	
	/**
	 *Path loss propagation model - From the document: Yet Another Network Simulator (auth. Lacage, Henderson), chapter 8.1
	 *Pl(d)=Pl(d0)+n*10*log_10(d/d0)
	 *Pl(d0)=Pt*Gt*Gr*lambda^2/(16*pi^2*d0^2*L)
	 *
	 *I will take values like in the document - if you want ot adjust transmission gains and so on - feel free !
	 *for 2.4GHz: lambda=v/f, v=3*10^8 m/s, f=2.4*10^9 Hz, lambda=0.125 m
	 *
	 *That part is constant: 
	 *Pl(d0)/Pt=Gt*Gr*lambda^2/(16*pi^2*d0^2*L)
	 */
	static final float Pld0_Pt=(float)(Math.pow(0.125,2)/(16*Math.pow(3.1415,2)));	
	/**
	 * Matrix of transmission power decrease due to distance between nodes.
	 */
	private static float[][] distancePowerDecreaseMatrix=new float[Topology.NR_OF_NODES][Topology.NR_OF_NODES];

	
	/**
	 *Constructor. Initializes power decrease matrix on the basis of nodes distances, also sets background noise as global noise lvl (for each channel).
	 */
	Medium(){
		fillDistancePowerDecreaseMatrix();
		globalNoiseLvl[0]=globalNoiseLvl37;
		globalNoiseLvl[1]=globalNoiseLvl38;
		globalNoiseLvl[2]=globalNoiseLvl39;
		for (byte i=0;i<Topology.NR_OF_NODES;i++){
			globalNoiseLvl[0].add(BACKGROUND_NOISE);	//initialize noise lvls equal to background noise
			globalNoiseLvl[1].add(BACKGROUND_NOISE);	//initialize noise lvls equal to background noise
			globalNoiseLvl[2].add(BACKGROUND_NOISE);	//initialize noise lvls equal to background noise
		}
	}
//==================================================================================================//
//======================================Medium: METHODS ==============================================//
//==================================================================================================//
	
	
	/**
	 * When a node starts a transmission, the function is called. 
	 * It adds the transmission to the list of current transmissions and updates global noise lvls.
	 * 
	 * @see Node#tryToStartTransmission
	 * @param t transmission
	 */
	static void addCurrentTransmission(Transmission t){
		currentTransmissions.add(t);
		updateGlobalNoiseLvls(t.channel);
	}
	/**
	 * find the transmission t in the list of current transmissions and remove it from the list. Then, update noise lvls.
	 * @param t the transmission
	 */
	static void removeCurrentTransmission(Transmission t){
		for (int i=0;i<currentTransmissions.size();i++)
			if (currentTransmissions.get(i).equals(t))
				currentTransmissions.remove(i);
		updateGlobalNoiseLvls(t.channel);
	}
	/**
	 * Global noise lvl update (for each node).
	 * @see Medium#updateNoiseLvlForAReceiver(Node, ArrayList)
	 */
	static void updateGlobalNoiseLvls(byte channel_){
		ArrayList<Byte> currentlyTransmittingNodes=getListOfcurrentlyTransmittingNodes();
		//for each receiver (I mean - node)
		for (Node n : Engine.LIST_OF_NODES){
			globalNoiseLvl[0].set(n.ID, updateNoiseLvlForAReceiverOnChannel(n, currentlyTransmittingNodes,(byte)0));
			globalNoiseLvl[1].set(n.ID, updateNoiseLvlForAReceiverOnChannel(n, currentlyTransmittingNodes,(byte)1));
			globalNoiseLvl[2].set(n.ID, updateNoiseLvlForAReceiverOnChannel(n, currentlyTransmittingNodes,(byte)2));
		}
	}
	/**
	 * Updates noise lvl for a node.
	 * 
	 * @param receiver
	 * @param transmittersIDs list of transmitting nodes
	 * @return noise lvl as seen by the receiver
	 *
	 */
	static float updateNoiseLvlForAReceiverOnChannel(Node receiver, ArrayList<Byte> transmittersIDs, byte channel){
		ArrayList<Float> powers=new ArrayList<Float>();
		for (byte i : transmittersIDs){			//for all transmitting nodes
			Node n = Engine.LIST_OF_NODES.get(i); //get a transmitting node A
			if ((receiver.ID != n.ID) && channel==n.currentTransmission.channel){	//don't add transmission of node A to the node A noise lvl, and add it only when considered channel is the same as the transmission channel 		
				if(receiver.syncedReception==null)									//if the receiver is not synced to a transmission, treat the transmission as a noise 
					powers.add(getReceptionPower(n.currentTransmission, receiver.ID));				
				else if (receiver.syncedReception.transmission.transmitterID!=n.ID)  //or, if it is synced, ommit the transmission
					powers.add(getReceptionPower(n.currentTransmission, receiver.ID));
			}			
		}	
		powers.add(BACKGROUND_NOISE); //each node has background noise
		return Helper.sumDBm(powers); //sum all powers
	}
	/**
	 * Collects nodes that transmitt
	 * @return
	 */
	static ArrayList<Byte>	getListOfcurrentlyTransmittingNodes(){
		ArrayList<Byte> theList = new ArrayList<Byte>();
		for (Node n : Engine.LIST_OF_NODES){
			if (Helper.DEBUG_NOISE) System.out.println("Medium.getListOfcurrentlyTransmittingNodes(), node: " + n.ID + " nodeState "+n.getNodeState());
			if (n.getNodeState().equals("TRANSMITTING")) theList.add(n.ID);	
		}
		return theList;
	}
	/**
	 * Initializes power decrease matrix. The decrease depends on nodes distance.
	 */
	private void fillDistancePowerDecreaseMatrix(){
	for (byte i=0; i<Topology.NR_OF_NODES;i++)
		for (byte j=0; j<Topology.NR_OF_NODES;j++)
			distancePowerDecreaseMatrix[i][j]=getDistancePowerDecrease(Engine.LIST_OF_NODES.get(i).position, Engine.LIST_OF_NODES.get(j).position);
	}
	/**
	 * <pre>
	 * Path loss propagation model - From the document: Yet Another Network Simulator (auth. Lacage, Henderson), chapter 8.1
	 * Pl(d)=Pl(d0)+n*10*log_10(d/d0)
	 * 
	 * The function calculates this part: n*10*log_10(d/d0)
	 * n=3 - From the document: Yet Another Network Simulator (auth. Lacage, Henderson), chapter 8.1
	 * </pre>
	 */
	private static float getDistancePowerDecrease(Position pos1, Position pos2){
		return (float)(30*Math.log10(Position.getDistance(pos1, pos2)));
	}
	
	/**
	 * Calculates reception power of a transmission. It's based on YANA relations.
	 * @param transmission
	 * @param receiverID
	 * @return
	 * @see Medium#getPowerDecreaseBetweenNodes(byte,byte)
	 * @see Medium#distancePowerDecreaseMatrix 
	 */
	static float getReceptionPower(Transmission transmission, byte receiverID){
		float power=transmission.power-transmission.power*Pld0_Pt-getPowerDecreaseBetweenNodes(transmission.transmitterID,receiverID);	
		if (Helper.DEBUG_NOISE)System.out.println("Node "+ receiverID +" reception Power " + power+" on channel " +transmission.channel);
		return power;
	}
	/**
	 * Returns power decrease between two nodes 
	 * @param i First node ID
	 * @param j Second node ID
	 * @return value at 
	 */
	static float getPowerDecreaseBetweenNodes(byte i, byte j){return distancePowerDecreaseMatrix[i][j];}
	/**
	 * @param nodeID
	 * @return noise lvl seen by node of nodeID ID
	 */
	static float getNoise(byte nodeID, byte channel){	return globalNoiseLvl[channel].get(nodeID);}
}
