import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Map;
import javax.swing.JFrame;

public class TopologyFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public TopologyFrame() {
		this.setPreferredSize(new Dimension(600, 600));
		this.pack();
		this.setVisible(true);

		setTitle("BLE Mesh Simulator - Topology");
		setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		// try {
		// setIconImage(ImageIO.read(new
		// File("C:\\Users\\Dominika\\eclipse-workspace\\Baza_Schroniska\\cat.png")));
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

	}

	public void paint(Graphics g) {
		super.paint(g);

		int rx = 0, ry = 0;
		for (Map.Entry<Float, Float> entry2 : Topology.positionMap.entrySet()) {
			rx = Math.round(entry2.getKey());
			ry = Math.round(entry2.getValue());
			if (Topology.topologyType.equals("Chain"))
				g.fillOval(rx+100, ry+100, 10, 10);
			else
				g.fillOval(rx, ry, 10, 10);

			System.out.println("x: " + rx + " y: " + ry);
		}
	}

	public static void runTopologyFrame() {
		TopologyFrame frame = new TopologyFrame();
	}
}
