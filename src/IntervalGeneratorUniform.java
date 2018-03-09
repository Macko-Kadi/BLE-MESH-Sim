/**
 * The class generates values according to uniform distribution with parameters a and b.
 * a and b are the smallest and the highest possible value.
 * In our context, the values means time intervals between consecutive packet generations. 
 */
class IntervalGeneratorUniform extends IntervalGenerator{
	double a;
	double b;
	IntervalGeneratorUniform(int seed, double a_, double b_){
		super(seed);
		a=a_;
		b=b_;
	}
	/**
	 * @return time interval 
	 */
	float calculateInterval() {
		double randValue=randomIntervalGenerator.nextDouble();
		//uniform from a to b
	    double interval = a+(b-a)*randValue;	    
	    return (float)(Helper.round(interval, 6));
	}	
}
