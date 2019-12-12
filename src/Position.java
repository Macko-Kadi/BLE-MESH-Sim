	/**
	 * Position (x,y)
	 */
	class Position {
		private float x;
		private float y;
		Position(float x_, float y_){
			x=x_;
			y=y_;
		}	
		/**
		 * Returns distance between two position  
		 * 
		 * @param position1 first position
		 * @param position2 another position
		 * 
		 * @return	distance
		 */
		static float getDistance(Position position1, Position position2){
			//simple formula (x1-x2)^2+(y1-y2)^2
			return (float)Math.sqrt(Math.pow(position1.x-position2.x,2)+Math.pow(position1.y-position2.y,2));
		}		
		float getX() {
			return x; 
		}
		float getY() {
			return y; 
		}
	}