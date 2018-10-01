

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;


/**
 * Set of handy functions
 */
public final class Helper {
	//you may set what you want to debugg
	public static boolean DEBUG_RCV=false;  
	public static boolean DEBUG_EVENTS=false;  
	public static boolean DEBUG_ADVERTISING_EVENT=false; //it should rather always be false
	public static boolean DEBUG_CACHE=false;
	public static boolean DEBUG_NOISE=false;
	public static boolean DEBUG_TRANS=false;
	public static boolean DEBUG_STATE=false;
	public static boolean DEBUG_SWITCH_CHANNEL=false;

	
	/**
	 * when the flag is set to true - all noises with power below SIMPLE_SNR_TRESHOLD are set as neglible small = -80dBm
	 * also, node transmission power is independent to battery level (removes node.updateTransmissionPower() function)
	 * 
	 */
	public static boolean SIMPLE_SNR=true; 
	public static float SIMPLE_SNR_TRESHOLD=-55f;
	
	public static int ROUND_DEC=3; 		// you may use this parameter whenever you will round a value
	public static Random generator=new Random(2347321); //just a common random number generator
	
	/**
	 * Constructor (well, never used)
	 */
	private Helper(){}
	/**
	 * Sum values of power in dBm. It is logarithmic scale, so you need to change dBms into mW - sum values - change to dBms once again.
	 * 10dBm+10dBm=13dBm - 10mW+10mW=20mW=13dBm
	 * 20dBm=100mW
	 * 
	 * @param dBms
	 * @return
	 */
	static float sumDBm(ArrayList<Float> dBms){
		double sum=0;
		if (dBms.size()<2) return Medium.BACKGROUND_NOISE;
		for (float f : dBms){
			sum+=Math.pow(10, f/10);
		}
		return (float)(10*Math.log10(sum));
	}
	/**
	 *Subtraction of 2 dBm values
	 */
	static float subDBm(float sumdBm, float sub){
		double sum=Math.pow(10, sumdBm/10)-Math.pow(10, sub/10);
		return (float)(10*Math.log10(sum));
	}
	/**
	 * Rounds a Number of any type to desired decimal places 
	 * (all types - float, double...- inherit from Number)
	 * 
	 * @param value
	 * @param decimalPlaces
	 * @return rounded value (double type)
	 */
	public static double round(Number value, int decimalPlaces){
		double result=value.doubleValue()*Math.pow(10, decimalPlaces);
		long tempValue=(long)Math.round(result);
		return tempValue/Math.pow(10, decimalPlaces);	
	}	
//	/**
//	 * Another way of rounding any type of number is to use erasure type. Moreover, the function returns rounded value in the same format as inputed value...
//	 * ... At least it should. It should return double/float but then, e.g. result of the float(?) type can't be casted like this -> (double)result. 
//	 * Resolve it if you know what's happen and give me a feedback.
//	 * 
//	 * @param value
//	 * @param decimalPlaces
//	 * @return Rounded value - float, when initial value was float, and returns double, when initial value was double.
//	 */
//	@SuppressWarnings("unchecked")
//	public static <E> E round(E value, int decimalPlaces){
//		double x = Double.parseDouble(String.valueOf(value));
//		double result=x*Math.pow(10, decimalPlaces);
//		int tempValue=(int)Math.round(result);
//		result=tempValue/Math.pow(10, decimalPlaces);
//		return (value instanceof Float) ? (E) Float.valueOf(String.valueOf(result)) : (E) Double.valueOf(String.valueOf(result));
//	}	
	
	/**
	 * Creates a path to a file
	 * @param filename  path to the file e.g "D:/data/result.txt"
	 */
	public static void createPath(String filename){
		File targetFile = new File(filename);
		File parent = targetFile.getParentFile();
		if(!parent.exists() && !parent.mkdirs()){
		    throw new IllegalStateException("Couldn't create dir: " + parent);
		}
	}	
	/**
	 * Resets an int array
	 * 
	 * @param a an array	
	 * @return new array of size 'a'
	 */
	public int[] resetValues(int[] a){return new int[a.length];}	
	/**
	 * Resets an 2D int array
	 * 
	 * @param a a 2D array	
	 * @return new array of size 'a'
	 */
	public int[][] resetValues(int[][] a){return new int[a.length][a[0].length];}
	/**
	 * Normalizes amounts to probabilities
	 * e.g:
	 * -tab------tabNorm
	 * [0]=2-----[0]=0.1
	 * [1]=4-----[1]=0.2
	 * [2]=6-----[2]=0.3
	 * [3]=8-----[3]=0.4
	 * 
	 * @param tab array with amounts
	 * @return	array with normalized probabilities
	 */
	public static double[] normalizeDistr1D(int[] tab){
		int size=tab.length;
		double[] tabNorm= new double[size];
		int noProbes=0;
		for (int i=0;i<size;i++)
			noProbes=noProbes+tab[i];
		for (int i=0;i<size;i++)
			tabNorm[i]=Helper.round((double)tab[i]/(double)noProbes,ROUND_DEC);
		return tabNorm;
	}
	/**
	 * Normalizes values of amount to probability per each row
	 * ---tab--------tabNorm
	 * [0][0]=2-----[0][0]=0.1
	 * [0][1]=4-----[0][1]=0.2
	 * [0][2]=6-----[0][2]=0.3
	 * [0][3]=8-----[0][3]=0.4
	 * 
	 * @param tab 2D array with amounts 
	 * @return	2D array with normalized probabilities
	 */
	public static double[][] normalizeDistr2D(int[][] tab){
		int size1=tab.length;
		int size2=tab[0].length;
		double[][] tabNorm= new double[size1][size2];
		//per row
		for (int i=0;i<size1;i++){
			tabNorm[i]=normalizeDistr1D(tab[i]);
		}
		return tabNorm;
	}
	/**
	 * Normalizes values of amount to probability for 3D table
	 * ---tab--------tabNorm
	 * [0][0][0]=2-----[0][0][0]=0.1
	 * [0][0][1]=4-----[0][0][1]=0.2
	 * [0][0][2]=6-----[0][0][2]=0.3
	 * [0][0][3]=8-----[0][0][3]=0.4
	 * 
	 * @param tab 2D array with amounts 
	 * @return	2D array with normalized probabilities
	 */
	public static double[][][] normalizeDistr3D(int[][][] tab){
		int size1=tab.length;
		int size2=tab[0].length;
		int size3=tab[0][0].length;
		double[][][] tabNorm= new double[size1][size2][size3];
		//per row
		for (int i=0;i<size1;i++){
				tabNorm[i]=normalizeDistr2D(tab[i]);		
		}
		return tabNorm;
	}
	
	
//	
// Actually there is a lot of redundancy in source code (too many similar functions), when Array is used to collect statistic data. You can't use construct like this (type erasure):
//	
//	public static <E> String print1D(E[] array){
//		String row="[";
//		for (E value : array)
//			row=row+value+ " ";
//		row=row+"]";
//		return row;
//	}
//
// When you will try to use it with array of primitive types eg. ints ([]int) there will be an error.
// So, we need to prepare a set of functions for EACH type of variable in array...
// .. Or just use List object to collect statistical data (and prepare another set of functions). 
//
// If you have an idea how to implement more general function (valid for any type), let me know.
	
	
	/**
	 * Returns a string containing a number of first values from an int array in the form:
	 * [0 2 3 4 5 6 7 3] 
	 * (it's MATLAB format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print1D(int[] array, int howManyValues){
		if (howManyValues>0)
			array=trimArray(array,howManyValues);
		String row="[";
		for (int i : array)
			row=row+i+ " ";
		row=row+"]";
		return row;
	}
	/**
	 * Returns a string containing a number of first values from a double array in the form:
	 * [0.22 2.53 3.13 4.45] 
	 * (it's MATLAB format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print1D(double[] array, int howManyValues){
		if (howManyValues>0)
			array=trimArray(array, howManyValues);
		String row="[";
		for (double d : array)
			row=row+d+ " ";
		row=row+"]";
		return row;
	}
	/**
	 * Returns a string containing a number of first values from a float array in the form:
	 * [0.22 2.53 3.13 4.45] 
	 * (it's MATLAB format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print1D(float[] array, int howManyValues){
		if (howManyValues>0)
			array=trimArray(array, howManyValues);
		String row="[";
		for (double d : array)
			row=row+d+ " ";
		row=row+"]";
		return row;
	}
	/**
	 * Returns a string containing a number of first values from a double array in the form:
	 * 0.22 2.53 3.13 4.45
	 * (it's EXCELL format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print1DExcell(double[] array, int howManyValues){
		if (howManyValues>0)
			array=trimArray(array, howManyValues);
		String row="";
		for (double d : array)
			row=row+d+ " ";
		return row;
	}
	/**
	 * Returns a string containing a number of first values from a float array in the form:
	 * 0.22 2.53 3.13 4.45
	 * (it's EXCELL format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print1DExcell(float[] array, int howManyValues){
		if (howManyValues>0)
			array=trimArray(array, howManyValues);
		String row="";
		for (float d : array)
			row=row+d+ " ";
		return row;
	}
	/**
	 * Returns a string containing a number of first values from a 2D int array in the form:
	 * [0 1 2 3 4; 0 1 2 3 4; 0 1 2 3 4]
	 * (it's MATLAB format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print2D(int[][] array, int howManyValues){
		String row="[";
		for (int j=0;j<array.length;j++){
			row=row+print1D(array[j], howManyValues)+";";
		}
		row=row.substring(0, row.length()-1)+"]";
		return row;
	}
	/**
	 * Returns a string containing a number of first values from a 2D double array in the form:
	 * [0.1 1.2 2.3 3.4 4.5; 0.1 1.2 2.3 3.4 4.5; 0.1 1.2 2.3 3.4 4.5;]
	 * (it's MATLAB format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print2D(double[][] array, int howManyValues){
		String row="[";
		for (int j=0;j<array.length;j++){
			row=row+print1D(array[j], howManyValues)+";";
		}
		row=row.substring(0, row.length()-1)+"]";
		return row;
	}
	/**
	 * Returns a string containing a number of first values from a 2D float array in the form:
	 * [0.1 1.2 2.3 3.4 4.5; 0.1 1.2 2.3 3.4 4.5; 0.1 1.2 2.3 3.4 4.5;]
	 * (it's MATLAB format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print2D(float[][] array, int howManyValues){
		String row="[";
		for (int j=0;j<array.length;j++){
			row=row+print1D(array[j], howManyValues)+";";
		}
		row=row.substring(0, row.length()-1)+"]";
		return row;
	}
	/**
	 * Returns a string containing a number of first values from a 2D double array in the form:
	 * 0.22 2.53 3.13 4.45
	 * 0.12 4.33 3.33 1.45
	 * 0.75 9.53 3.13 2.78
	 * 
	 * (it's EXCELL format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print2DExcell(double[][] array, int howManyValues){
		String row="";
		for (int j=0;j<array.length;j++){
			row=row+print1DExcell(array[j], howManyValues)+"\n";
		}
		row=row.substring(0, row.length()-1);
		return row;
	}
	/**
	 * Returns a string containing a number of first values from a 2D double array in the form:
	 * 0.22 2.53 3.13 4.45
	 * 0.12 4.33 3.33 1.45
	 * 0.75 9.53 3.13 2.78
	 * 
	 * (it's EXCELL format)
	 * 
	 * @param array
	 * @param howManyValues
	 * @return string with array values
	 */
	public static String print2DExcell(float[][] array, int howManyValues){
		String row="";
		for (int j=0;j<array.length;j++){
			row=row+print1DExcell(array[j], howManyValues)+"\n";
		}
		row=row.substring(0, row.length()-1);
		return row;
	}
	
	public static String print3D(double[][][] array, int howManyValues){
		String row="[";
			for (int j=0;j<array.length;j++){
			row=row+print2D(array[j], howManyValues)+";";			
		}
		row=row.substring(0, row.length()-1)+"]";
		return row;
	}
	public static String print3D(int[][][] array, int howManyValues){
		String row="[";
			for (int j=0;j<array.length;j++){
				row=row+print2D(array[j], howManyValues)+";";			
			}
			row=row.substring(0, row.length()-1)+"]";
			return row;
	}
	
	/**
	 * Returns a number of first values of an array
	 * @param tooLongArray
	 * @param newLength
	 * @return trimmed array
	 */
	public static int[] trimArray(int[] tooLongArray, int newLength){
		int[] trimmedArray = new int[newLength];
		System.arraycopy(tooLongArray, 0, trimmedArray, 0, newLength);
		return trimmedArray; 
	}
	/**
	 * Returns a number of first values of an array
	 * @param tooLongArray
	 * @param newLength
	 * @return trimmed array
	 */
	public static double[] trimArray(double[] tooLongArray, int newLength){
		double[] trimmedArray = new double[newLength];
		//copy array into smaller version
		System.arraycopy(tooLongArray, 0, trimmedArray, 0, newLength);
		return trimmedArray; 
	}
	/**
	 * Returns a number of first values of an array
	 * @param tooLongArray
	 * @param newLength
	 * @return trimmed array
	 */
	public static float[] trimArray(float[] tooLongArray, int newLength){
		float[] trimmedArray = new float[newLength];
		//copy array into smaller version
		System.arraycopy(tooLongArray, 0, trimmedArray, 0, newLength);
		return trimmedArray; 
	}

	/**
	 * @return current date (Timezone UTC+1) in specified format
	 */
	public static String getCurrDate(){
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"));
		Date date=new Date();
		SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		//System.out.println(dt.format(date).toString());
		return dt.format(date).toString();
	}
	/**
	 * Writes a string into a text file
	 * 
	 * @param filename
	 * @param data
	 */
	public static void writeToFile(String filename, String data){
		FileWriter fw = null;
		BufferedWriter bw = null;
		PrintWriter out = null;
		try {
		    fw = new FileWriter(filename, true);
		    bw = new BufferedWriter(fw);
		    out = new PrintWriter(bw);
		    out.println(data);
		    out.close();
		} catch (IOException e) {
		    System.out.println("IOException - write to file 1"+ e);
		}
		finally {
		    if(out != null)
			    out.close();
		    try {
		        if(bw != null)
		            bw.close();
		    } catch (IOException e) {
		    	System.out.println("IOException - write to file 2"+ e);
		    }
		    try {
		        if(fw != null)
		            fw.close();
		    } catch (IOException e) {
		    	System.out.println("IOException - write to file 3"+ e);
		    }
		}
	}
}
