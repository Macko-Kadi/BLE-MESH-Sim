
	/**
	 * Transmission in the air
	 */
	class Transmission {
		float duration; //how long the transmission will last  
		float power;	//power of the transmission
		byte transmitterID;
		Packet packet;
		
		Transmission(Node transmitter, Packet p){
			power=transmitter.getCurrentTransmissionPower();
			transmitterID=transmitter.ID;		
			packet=p;
			duration=calculateTransmissionTime(Node.INTERFACE_SPEED);
		}
		/**
		 * Returns transmission time of a packet 
		 * It's a sum of a constant synchronization time (Medium.SYNC_TIME) and time of transmission via node interface 
		 * 
		 * TODO: Is the formula OK ?
		 * 
		 * @param interfaceSpeed Node interface speed
		 * @return transmission time
		 */
		private float calculateTransmissionTime(float interfaceSpeed){
			return (Medium.SYNC_TIME+(float)(packet.header.packetSize*8/interfaceSpeed));
		}
	}