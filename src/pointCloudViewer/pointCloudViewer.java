package pointCloudViewer;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class pointCloudViewer {

	public pointCloudViewer() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		try {
		    for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
//		            UIManager.put( "ScrollBar.minimumThumbSize", new Dimension( 1, 1 ) ); 
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}
		new Thread(new PointCloudObject()).start();
	}
}
