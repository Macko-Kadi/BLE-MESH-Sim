import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dominika.Zawislak
 */
public class ResultsFrame extends javax.swing.JFrame {

	private static final long serialVersionUID = 1L;
	public static ResultsFrame resultsframe;
	
	public static void initResultsFrame() {
		ResultsFrame resultsframe = new ResultsFrame();
		resultsframe.setVisible(true);
		JTextNodes.setText(String.valueOf(Topology.NR_OF_NODES));
		jTextTopology.setText(Topology.topologyType);
		jTextAlgorithm.setText(Engine.algorithm);
		simulationResultsFrame();

	}
	
	public static void simulationResultsFrame() {
		energyUsageFrame();
		// dodac
		packetDelayFrame();
		packetLossFrame();
		// dodac
		hopsCounterFrame();
		duplicateCounterFrame();
		collisionCounter();
	}
	
	private static void energyUsageFrame() {
		ArrayList<Double> Energy = new ArrayList<Double>();
		Map<Byte, Double> energyMap = new HashMap<Byte, Double>();
		double minEnergy = 0;
		double maxEnergy = 0;
		double totalEnergy = 0;
		String minNodes = "zero";
		String maxNodes = "zero";

		ResultsFrame.textArea1.append("Energy consumption:  \n");
		for (Node n : Engine.LIST_OF_NODES) {
			if (n.batteryPowered) {
				n.drainBattery();

				ResultsFrame.textArea1
						.append("Node ID " + n.ID + " used energy : " + n.getUsedEnergyLevel() + "mWh \n");

				Energy.add(n.getUsedEnergyLevel());
				energyMap.put(n.ID, n.getUsedEnergyLevel());
				totalEnergy = totalEnergy + n.getUsedEnergyLevel();
			} else {
				ResultsFrame.textArea1.append("Node ID " + n.ID + " is power supplied!");
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
		ResultsFrame.textArea1.append("\n");
		ResultsFrame.textArea1
				.append("Smallest amount of used energy " + minEnergy + " mWh, by node(s) " + minNodes + " \n");
		;
		ResultsFrame.textArea1
				.append("Biggest amount of used energy " + maxEnergy + " mWh, by node(s) " + maxNodes + " \n");
		ResultsFrame.textArea1.append("Total energy used in simulation: " + totalEnergy + "mWh \n");
		ResultsFrame.textArea1.append("========================================================================= \n");

	}

	private static void packetDelayFrame() {
		ResultsFrame.textArea1.append("Packet delay: \n");

		ResultsFrame.textArea1.append("========================================================================= \n");

	}

	private static void packetLossFrame() {
		ResultsFrame.textArea1.append("Packet loss:  \n");
		ResultsFrame.textArea1.append("Number of generated messages: " + Node.packetCount + "\n");
		ResultsFrame.textArea1.append("Number of received messages: " + Node.packetReceivedCount + "\n");
		ResultsFrame.textArea1.append("Number of backoff procedures: " + Node.retransmit + "\n");
		ResultsFrame.textArea1.append("========================================================================= \n");
	}

	private static void hopsCounterFrame() {
		ResultsFrame.textArea1.append("iloœæ przeskoków? "+"\n");
		for (Packet p : Node.packetList) {
			ResultsFrame.textArea1.append("Packet: "+p.header.packetID+" 	TTL: "+p.header.TTL+"\n");
		}
		ResultsFrame.textArea1.append("========================================================================= \n");
	}

	private static void duplicateCounterFrame() {
		ResultsFrame.textArea1.append("Number of packets that reached destination more than once: " + Node.duplicateCounter+"\n");
		for (int d = 0; d < Node.duplicateList.size(); d++) {
		//	ResultsFrame.textArea1.append("Packet ID: " + Node.duplicateList.get(d)+"\n");
		}

		ResultsFrame.textArea1.append("========================================================================= \n");

	}
	
	private static void collisionCounter() {
		ResultsFrame.textArea1.append("Number of collisions during simulation: " + Node.collisionCounter+"\n");
		ResultsFrame.textArea1.append("========================================================================= \n");
		ResultsFrame.textArea1.append("Number of no collisions during simulation: " + Node.noCollisionCounter+"\n");

		
	}	
	
	

    /**
     * Creates new form ResultsFrame
     */
    public ResultsFrame() {
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        textArea1 = new java.awt.TextArea();
        jLabel1 = new javax.swing.JLabel();
        JTextNodes = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextTopology = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextAlgorithm = new javax.swing.JTextField();

        JTextNodes.setEnabled(false);
        jTextTopology.setEnabled(false);
        jTextAlgorithm.setEnabled(false);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BLE Mesh Simulator");
		setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		try {
			setIconImage(ImageIO.read(new File("C:\\Users\\Dominika.Zawislak\\Downloads\\BLE-MESH-Sim-master\\bluetooth_logo.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}


        jLabel1.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        jLabel1.setText("Topology:");

        JTextNodes.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        JTextNodes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                JTextNodesActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        jLabel2.setText("Number of nodes:");

        jTextTopology.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        jTextTopology.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextTopologyActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        jLabel3.setText("Algorithm:");

        jTextAlgorithm.setFont(new java.awt.Font("Calibri", 0, 14)); // NOI18N
        jTextAlgorithm.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextAlgorithmActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(textArea1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(JTextNodes, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextTopology, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(JTextNodes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextTopology, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextAlgorithm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 483, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>                        

    private void JTextNodesActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // TODO add your handling code here:
    }                                          

    private void jTextTopologyActionPerformed(java.awt.event.ActionEvent evt) {                                              
        // TODO add your handling code here:
    }                                             

    private void jTextAlgorithmActionPerformed(java.awt.event.ActionEvent evt) {                                               
        // TODO add your handling code here:
    }                                              

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ResultsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ResultsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ResultsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ResultsFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ResultsFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify                     
    private static javax.swing.JTextField JTextNodes;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private static javax.swing.JTextField jTextAlgorithm;
    private static javax.swing.JTextField jTextTopology;
    public static java.awt.TextArea textArea1;
    // End of variables declaration                   
}
