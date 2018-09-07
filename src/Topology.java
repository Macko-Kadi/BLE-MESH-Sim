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

	public static String topologyFilePath = "C:\\Users\\Dominika.Zawislak\\Desktop\\is\\BLE-MESH-Sim-master\\testtopology\\";
//	public static String topologyFileName = ("4_mesh.txt");
//	public static String topologyFileName = ("9_chain.txt");
//	public static String topologyFileName = ("17_chain_dense.txt");
//	public static String topologyFileName = ("12_mesh.txt");
//	public static String topologyFileName = ("25_mesh_dense.txt");
	public static String topologyFileName = ("30_U.txt");
//	public static String topologyFileName = ("_U.txt");
	public static String topologyFile = (topologyFilePath+topologyFileName);
	public static String topologyType;
	public static int NR_OF_NODES; 
    static Map<Float,Float > positionMap = new HashMap<Float, Float>();
    static Map<Byte, Map<Float,Float >> nodesMap = new HashMap<Byte, Map<Float,Float >>();
	
	
	public static void readFile() {

		try {
			
			FileInputStream fstream = new FileInputStream(topologyFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			int linecounter=0;
			
			while ((line=br.readLine()) != null) {
				if (linecounter==0) {
					NR_OF_NODES=Integer.valueOf(line);
					System.out.println("nr: "+NR_OF_NODES);
				}
				else if(linecounter==1) {
					topologyType=line;
					System.out.println("topology: "+topologyType);
				}
				else {
				String[] strlines = line.split(" ");
				System.out.println("ID: "+strlines[0]+" x: "+ strlines[1]+" y; "+ strlines[2]);
				byte nID=Byte.valueOf(strlines[0]);
				float file_x=Float.valueOf(strlines[1]);
				float file_y=Float.valueOf(strlines[2]);
				positionMap.put(file_x,file_y);
				Map<Float, Float> tmp = new HashMap<Float, Float>();
				tmp.put(file_x,file_y);
				nodesMap.put(nID,tmp);
				tmp.remove(tmp);	
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
