package pointCloudViewer;

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import eora3D.RGB3DPoint;

public class pointCloudViewer implements Runnable
{
	static private DatagramSocket socket = null;
	static private PointCloudObject m_pco = null;
	static private pointCloudViewer m_pcv = null;

	boolean m_running = false;
	
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
		try
		{
			socket = new DatagramSocket(7778, InetAddress.getLoopbackAddress());
		} catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Failed to open listen UDP socket 7778");
			System.exit(1);
		}
		if(socket.isClosed())
		{
			System.out.println("Failed to open listen UDP socket 7778");
			System.exit(1);
		}
		
		Thread l_pcvt = new Thread(m_pcv = new pointCloudViewer());
		
		new Thread(m_pco = new PointCloudObject()).start();
		
		l_pcvt.start();
	}
	
    public void run() {
    	byte[] buf = new byte[1];
    	
        while (!m_pco.m_finished) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {
            	socket.setSoTimeout(1000);
				socket.receive(packet);
			} catch (SocketTimeoutException te)
            {
				if(m_pco.m_finished)
				{
					socket.close();
					System.out.println("Finished");
					System.exit(0);
				}
				continue;
			} catch (IOException ioe)
            {

				// TODO Auto-generated catch block
				ioe.printStackTrace();
				System.exit(2);
			}
            System.out.println("Received cmd "+buf[0]);
            switch(buf[0])
            {
            	case 0: // Clear
            		m_pco.clear();
            		break;
            	case 1: // Add point, scale & pointsize
            		try
            		{
            			byte[] l_obj_buf = new byte[256];
                        DatagramPacket l_obj_packet = new DatagramPacket(l_obj_buf, l_obj_buf.length);
                        socket.receive(l_obj_packet);
                        System.out.println("Received size "+l_obj_packet.getLength());
						ByteArrayInputStream l_bais = new ByteArrayInputStream(l_obj_packet.getData());
						ObjectInputStream l_ois = new ObjectInputStream(l_bais);
						RGB3DPoint l_pt = (RGB3DPoint) l_ois.readObject();
						m_pco.addPoint(l_pt);
						m_pco.m_Pointsize = l_pt.m_pointsize;
						m_pco.m_Scale = l_pt.m_scale;
            		} catch (SocketTimeoutException te)
                    {
        				continue;
        			} catch(Exception e)
            		{
            			e.printStackTrace();
            			System.exit(3);
            		}
            		break;            		
            }
        }
        socket.close();
        System.out.println("Finished");
        System.exit(0);
    }
}
