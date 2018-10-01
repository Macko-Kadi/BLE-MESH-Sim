	/**
	 * Transmission in the air on one channel!
	 * According to https://devzone.nordicsemi.com/power/ 
	 * Chip: nRF52832QFAAB0 
	 * a node sends a packet 3 times in different channels, it needs some set upos before, so new class TransmissionProcedure is implemented
	 * 
	 */
	class Transmission {
		float duration; //how long the transmission will last  
		float power;	//power of the transmission
		byte transmitterID;
		byte channel;
		Packet packet;
		
		Transmission(Node transmitter, Packet p, byte channel_){
			power=transmitter.getCurrentTransmissionPower();
			transmitterID=transmitter.ID;		
			packet=p;
			duration=calculateTransmissionTime();
			channel=channel_;
		}
		/**
		 * Returns transmission time of a packet
		 * According to https://devzone.nordicsemi.com/power/ 
		 * Chip: nRF52832QFAAB0 
		 * Payload of size:
		 * 1B:   0.2ms   0.0002s;
		 * 16B:  0.32ms  0.00032
		 * 31B:  0.44ms; 0.00044
		 * so approximated function: 0.0002+0.0000075*size [B]
		 * 
		 * 
		 * @return transmission time
		 */
		private float calculateTransmissionTime(){
			//return (Medium.SYNC_TIME+(float)(packet.header.packetSize*8/interfaceSpeed));
			return((float)(0.0002+0.0000075*(packet.header.packetSize)));
		}
	}