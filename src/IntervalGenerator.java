import java.util.Random;
/**
 * Abstract class that generates values.
 * In our context, the values means time intervals between consecutive packet generations. 
 */
abstract class IntervalGenerator {
	Random randomIntervalGenerator;
	
	IntervalGenerator(int seed){
		randomIntervalGenerator=new Random(seed);
	}
	/**
	 * @see IntervalGeneratorExponential#calculateInterval()
	 * @see IntervalGeneratorUniform#calculateInterval()
	 */
	abstract float calculateInterval();
}

