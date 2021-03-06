/************************************************************************
Description : Class for creating topology from a file
Created by : Dominika Zawiślak
************************************************************************/

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Topology {

	public static String topologyFilePath = Engine.topologyFilePath;
//	public static String topologyFileName = ("16_mesh.txt");
	
//	public static String topologyFileName = ("36_chain.txt");
//	public static String topologyFileName = ("36_mesh.txt");
//	public static String topologyFileName = ("36_U.txt");
	
//	public static String topologyFileName = ("4_mesh.txt");
//	public static String topologyFileName = ("9_mesh.txt");
//	public static String topologyFileName = ("10_mesh.txt");
	public static String topologyFileName = Engine.topologyFilename;
//	public static String topologyFileName = ("49_mesh.txt");
//	public static String topologyFileName = ("100_mesh.txt");
	
//	public static String topologyFileName = ("36_mesh_2.txt");
//	public static String topologyFileName = ("36_mesh_3.txt");
	
	public static String topologyFile = (topologyFilePath+topologyFileName);
	public static String topologyType = "MESH";
	public static int NR_OF_NODES = 0; 
    static Map<Float,Float > positionMap = new HashMap<Float, Float>();
    static Map<Byte, Map<Float,Float >> nodesMap = new HashMap<Byte, Map<Float,Float >>();
	
	
	public static void readFile() {

		try {
			
			FileInputStream fstream = new FileInputStream(topologyFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			int linecounter=0;
			boolean start=false;
			while ((line=br.readLine()) != null) {
				String[] strlines = line.split(" ");
				String first = strlines[0];
				if(first.equals("nodes")) { //next line after nodes
					start = true;
					line=br.readLine();
					strlines = line.split(" ");
					}
				if (start) {
				//	System.out.println("ID: "+strlines[0]+" x: "+ strlines[1]+" y; "+ strlines[2]);
					byte nID=Byte.valueOf(strlines[0]);
					float file_x=Float.valueOf(strlines[1]);
					float file_y=Float.valueOf(strlines[2]);
					positionMap.put(file_x,file_y);
					Map<Float, Float> tmp = new HashMap<Float, Float>();
					tmp.put(file_x,file_y);
					nodesMap.put(nID,tmp);
					tmp.remove(tmp);	
					NR_OF_NODES++;
				}
				

			linecounter++;
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	
}
