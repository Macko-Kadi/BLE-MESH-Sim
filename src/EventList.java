import java.util.ArrayList;
/**
 * <pre>
 * The class represents a list of events. The list is sorted* and the first event from the list is evaluated.
 * The evaluated event may trigger another events, that are added to the list. Repeat. 
 * 
 * *Actually we never sort the list, but when we add events to the list, at first we look for proper position in the list, taking into account the event time.
 * </pre>
 */
class EventList {
	ArrayList<Event> theList= new ArrayList<Event>();
	/**
	 * We don't initialize nothing
	 */
	EventList(){}
	/**
	 * Adds event to the list. 
	 */
	void addEvent(Event e){
		int place=binarySearch(e.startTime);	//find proper position
		theList.add(place,e);
	}
	void addEventsFromList(ArrayList<Event> list){
		for (Event e : list) addEvent(e);
	}
	/**
	 * Evaluates first event from the list, then removes it from the list.
	 * @return new triggered events
	 */
	ArrayList<Event> evaluateFirstEvent(){
		ArrayList<Event> e=theList.get(0).evaluateEvent();
		theList.remove(0);	
		return e;
	}
	/**
	 * The function is helpful for Engine object. First event time is current simulation time. 
	 */
	double getFirstEventStartTime(){
		return theList.get(0).startTime;
	}
	/**
	 * <pre>
	 * Removes events that are related with a node (the node probably gets discharged or goes sleep so may not perform any actions)
	 * 
	 * REMARK: The function does not remove the 0 element from the list - it will be removed by performFirstEvent()
	 * If the function would remove the 0 element, then next event would become zero element and then would be removed as well (because of performFirstEvent(), theList.remove(0))	
	 * </pre>
	 * @param ID The node ID
	 */
	void removeEventsOfNode(byte ID){
		for (int i=theList.size()-1; i>0;i--){
			if (theList.get(i).metadata.equals(Byte.toString(ID)))	theList.remove(i);			
		}	
	}
	/**
	 * Prints the current event list 
	 */
	void printEvents(){
		System.out.println("---Current event list:-----");
		for(int i=0; i<theList.size();i++)	System.out.println("EVENT :  "+theList.get(i).startTime+ "\t" +theList.get(i).type + "\t ID:"+theList.get(i).metadata);
	}	
	/**
	 * Simple binary search taking into account event times
	 * 
	 * @return position in the event list where to place the event 
	 */
	 int binarySearch(double startTime){
		if (theList.size()==0) return 0; //When there is nothing in the list, the place is equal to 0;
		//When something in the list:	 
		int firstIndex=0;
		int lastIndex=theList.size()-1;			
		while (true){ 										//Repeat until 2 consecutive indexes are founded (index, index+1) or (index, index)
			int midIndex=(firstIndex+lastIndex)/2;			//get the midIndex of your current indexes range
			if (theList.get(midIndex).startTime>startTime){		//if the value at the midIndex is greater than your value, your value position will be not greater than the midIndex
				lastIndex=midIndex;								
			}
			if (theList.get(midIndex).startTime<=startTime){	//if the value at the midIndex is not greater than your value, your value position will be not smaller than the midIndex
				firstIndex=midIndex;
			}
			if (lastIndex<=firstIndex+1){						//if you have 2 consecutive indexes
				if (startTime>theList.get(lastIndex).startTime){	//if your value is greater than lastIndex 
					//(it's only possible when the value should take the last place at the list - so below error just for sure)
					if (lastIndex!=theList.size()-1) System.out.println("ERROR: binary search - you shouldn't see that. It should be only possible when lastIndex == theList.size()-1 ");
					//put the value at the last place of the list
					return lastIndex+1;
				}
				else if (startTime<theList.get(firstIndex).startTime){ 	//if your value is smaller than firstIndex
					//(it's only possible when the value should take the first place at the list - so below error just for sure)
					if (firstIndex!=0) System.out.println("ERROR: binary search - you shouldn't see that. It should be only possible when firstIndex == 0 ");
					//put the value at the first place of the list
					return 0;		
				}
				else
					//In general (except 2 defined above cases) value at firstIndex should be smaller/equal than your value and the value at lastIndex should be equal/greater than your value
					//e.g: value=5.6, list(firstIndex)=3.4, list(lastIndex)=6.2 -> put at lastIndex
					//e.g: value=5.6, list(firstIndex)=5.6, list(lastIndex)=5.6 -> put at lastIndex
					return lastIndex;
			}
		}					
	}

}
