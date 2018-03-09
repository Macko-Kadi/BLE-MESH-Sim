/**
 * The class generates values according to exponential distribution with parameter lambda.
 * In our context, the values means time intervals between consecutive packet generations. 
 */
class IntervalGeneratorExponential extends IntervalGenerator{
	private double lambda;

	IntervalGeneratorExponential(int seed, double lambda_){
		super(seed);
		lambda=lambda_;
	}
	/**
	 * @return time interval 
	 */
	float calculateInterval() {
		double randValue=randomIntervalGenerator.nextDouble();
	    double interval = (-1) /lambda * Math.log(1-randValue);	    
	    return (float)(Helper.round(interval, 6));
	}	
}