/**
 * End of transmission event. It inherits from Event (Transmission object added).
 * After end of transmission the transmitted packet can be obtained from the transmission object.
 */
class EventEndTrans extends Event{
		Transmission transmission;
		public EventEndTrans(float startTime_, String type_, String metadata_, Transmission trans_) {
			super(startTime_, type_, metadata_);
			transmission=trans_;
		}
		/**
		 * Only end of transmission event (EventEndTrans objact) has a Transmission object
		 */
		Transmission getTransmission(){return transmission;} 	
}