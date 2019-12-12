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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileSelected {

	public static String topologyFilePath = Topology.topologyFilePath;
	static ArrayList<Byte> listOfRelays = new ArrayList<Byte>();
	
	public static ArrayList<Byte> readFile(String FileName) {
		final String topologyFile = (topologyFilePath+FileName);
		try {
			
			FileInputStream fstream = new FileInputStream(topologyFile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			//topology is in the 6th line
			for (int i = 0; i < 5; i++)
		        br.readLine();	
			line=br.readLine();
			String[] strlines = line.split(" ");
			for (int i=0;i<strlines.length;i++){
				byte id=(byte)Integer.parseInt(strlines[i]);
				listOfRelays.add(id);
				System.out.println("relay: "+(byte)Integer.parseInt(strlines[i]));
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return listOfRelays;
	}
	
	
}
