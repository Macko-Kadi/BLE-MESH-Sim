/************************************************************************
Description : Class for transferring simulation result into a .xls file
Created by : Dominika Zawi≈õlak
************************************************************************/

import  java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import  org.apache.poi.hssf.usermodel.HSSFSheet;
import  org.apache.poi.hssf.usermodel.HSSFWorkbook;
import  org.apache.poi.hssf.usermodel.HSSFRow;

public class ResultsExcel {

	static HSSFWorkbook workbook;
	static HSSFSheet sheet;
	static ArrayList<Double> Energy = new ArrayList<Double>();
	static ArrayList<Double> Hops = new ArrayList<Double>();
	static ArrayList<Double> Delay = new ArrayList<Double>();
	static double mean;
	static double confidenceInterval;
	
    public static void initExcelResults() {
        try {
        	long currDate = System.currentTimeMillis();
            String filename = Engine.excelPath;
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("FirstSheet");

    		Energy = new ArrayList<Double>();
    		Map<Byte, Double> energyMap = new HashMap<Byte, Double>();
    		double minEnergy = 0;
    		double maxEnergy = 0;
    		double totalEnergy = 0;
    		String minNodes = "zero";
    		String maxNodes = "zero";

    		for (Node n : Engine.LIST_OF_NODES) {
    			if (n.batteryPowered) {
    				n.drainBattery();
    				Energy.add(n.getUsedEnergyLevel());
    				energyMap.put(n.ID, n.getUsedEnergyLevel());
    				totalEnergy = totalEnergy + n.getUsedEnergyLevel();
    			} else {
//    				writer.println("Node ID " + n.ID + " is power supplied!");
    			}
    		}

    		minEnergy = Energy.stream().min(Comparator.comparing(i -> i)).get();
    		for (Entry<Byte, Double> entry : energyMap.entrySet()) {
    			if (entry.getValue().equals(minEnergy)) {
    				if (minNodes.equals("zero"))
    					minNodes = String.valueOf(entry.getKey());
    				else
    					minNodes = (minNodes + ", " + String.valueOf(entry.getKey()));
    			}
    		}

    		maxEnergy = Energy.stream().max(Comparator.comparing(i -> i)).get();
    		for (Entry<Byte, Double> entry : energyMap.entrySet()) {
    			if (entry.getValue().equals(maxEnergy)) {
    				if (maxNodes.equals("zero"))
    					maxNodes = String.valueOf(entry.getKey());
    				else
    					minNodes = (maxNodes + ", " + String.valueOf(entry.getKey()));
    			}
    		}
    		
            HSSFRow row0 = sheet.createRow((short)0);
            row0.createCell(0).setCellValue("Total energy used in simulation");
            row0.createCell(1).setCellValue(Helper.round(totalEnergy*1000,2));
    		
    		calculateConfidenceInterval(Energy);
    		
            HSSFRow row1 = sheet.createRow((short)1);
            row1.createCell(0).setCellValue("Average amount of energy used in simulation");
     //       row1.createCell(1).setCellValue(Helper.round(mean*1000,2) +" ± " + Helper.round(confidenceInterval*1000,2));
            row1.createCell(1).setCellValue(Helper.round(mean*1000,2));
                   
            HSSFRow row2 = sheet.createRow((short)2);
            row2.createCell(0).setCellValue("Smallest amount of used energy");
            row2.createCell(1).setCellValue(Helper.round(minEnergy*1000,2));
            
            HSSFRow row3 = sheet.createRow((short)3);
            row3.createCell(0).setCellValue("Biggest amount of used energy ");
            row3.createCell(1).setCellValue( Helper.round(maxEnergy*1000,2));
            
            HSSFRow row4 = sheet.createRow((short)4);
            row4.createCell(0).setCellValue("Number of generated messages: ");
            row4.createCell(1).setCellValue(Node.generatedPacketCount);
            
            HSSFRow row5 = sheet.createRow((short)5);
            row5.createCell(0).setCellValue("Number of received messages: ");
            row5.createCell(1).setCellValue(Node.packetReceivedCount);

            double difference = Node.generatedPacketCount -Node.packetReceivedCount; 
            HSSFRow row6 = sheet.createRow((short)6);
            row6.createCell(0).setCellValue("P of success: ");
            row6.createCell(1).setCellValue(Helper.round(100-100*difference/Node.generatedPacketCount,2));
            
            FileOutputStream fileOut = new FileOutputStream(filename);
            
            HSSFRow row7 = sheet.createRow((short)7);
            row7.createCell(0).setCellValue("Number of packets that reached destination more than once: ");
            row7.createCell(1).setCellValue(Node.duplicateCounter);
           
            
    		for (Packet p : Node.packetList) {
    			double hops = 20-(double) p.header.TTL;
    			Hops.add(hops);
    		}
            
        	calculateConfidenceInterval(Hops);
        	
            HSSFRow row8 = sheet.createRow((short)8);
            row8.createCell(0).setCellValue("Average amount of hops made by a packet: ");
       //     row8.createCell(1).setCellValue(Helper.round(mean,2) +" ± " + Helper.round(confidenceInterval,2));
            row8.createCell(1).setCellValue(Helper.round(mean,2));
            
    		for (Entry<String, Double> startPacket : Node.timeOfPacketGeneration.entrySet()) {
    			String tmppacketID = startPacket.getKey();
    			Double tmppacketStart = startPacket.getValue();
    			for (Entry<String, Double> receivePacket: Node.timeOfPacketReception.entrySet()) {
    				if(receivePacket.getKey().equals(tmppacketID)) {
    					Double packetDelay = (double) (receivePacket.getValue() - tmppacketStart);
    					Delay.add(packetDelay);
    				}		
    			}
    		}
    		
        	calculateConfidenceInterval(Delay);
        	
            HSSFRow row9 = sheet.createRow((short)9);
            row9.createCell(0).setCellValue("Average packet delay in simulation: ");
        //    row9.createCell(1).setCellValue(Helper.round(1000*mean,2) +" ± " + Helper.round(1000*confidenceInterval,2));
            row9.createCell(1).setCellValue(Helper.round(1000*mean,2));
            
            HSSFRow row10 = sheet.createRow((short)10);
            row10.createCell(0).setCellValue("% of collisions during simulation: ");
            row10.createCell(1).setCellValue(100*(float)(Node.collisionCounter)/(Node.collisionCounter+Node.noCollisionCounter));  
            
            HSSFRow row11 = sheet.createRow((short)11);
            row11.createCell(0).setCellValue("Relays: ");
            row11.createCell(1).setCellValue(Provisioner.listOfRelays.toString());  
            HSSFRow row12 = sheet.createRow((short)12);
            row12.createCell(0).setCellValue("Relays number: ");
            row12.createCell(1).setCellValue(Provisioner.listOfRelays.size());  
            
            HSSFRow row13 = sheet.createRow((short)13);
            row13.createCell(0).setCellValue("Number of wrong receptions during simulation: ");
            row13.createCell(1).setCellValue(Node.collisionCounter); 
            
            HSSFRow row14 = sheet.createRow((short)14);
            row14.createCell(0).setCellValue("Number of success receptions during simulation: ");
            row14.createCell(1).setCellValue(Node.noCollisionCounter); 
            
            HSSFRow row15 = sheet.createRow((short)15);
            row15.createCell(0).setCellValue("Number of transmissions (packets send) during simulation: ");
            row15.createCell(1).setCellValue(Node.sentPacketCount); 

            workbook.write(fileOut);
            fileOut.close();
//            workbook.close();
            System.out.println("Your excel file has been generated!");

        } catch ( Exception ex ) {
            System.out.println(ex);
        }
    }
   	

    	
    	private static double[] calculateConfidenceInterval(ArrayList<Double> energy2) {

    	    // calculate the mean value (= average)
    	    double sum = 0.0;
    	    for (Double num : energy2) {
    	        sum += num;
    	    }
    	    mean = sum / energy2.size();

    	    // calculate standard deviation
    	    double squaredDifferenceSum = 0.0;
    	    for (Double num : energy2) {
    	        squaredDifferenceSum += (num - mean) * (num - mean);
    	    }
    	    double variance = squaredDifferenceSum / (energy2.size()-1);
    	    double standardDeviation = Math.sqrt(variance);

    	    // value for 95% confidence interval, source: https://en.wikipedia.org/wiki/Confidence_interval#Basic_Steps
    	    double confidenceLevel = 1.96;
    	    confidenceInterval = confidenceLevel * standardDeviation / Math.sqrt(energy2.size());
    	    return new double[]{mean - confidenceInterval, mean + confidenceInterval};
    	}

    
    
}
