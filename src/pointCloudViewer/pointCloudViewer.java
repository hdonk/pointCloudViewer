package pointCloudViewer;

import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import eora3D.PointCmd;
import eora3D.RGB3DPoint;

public class pointCloudViewer implements Runnable
{
	static private ServerSocket socket = null;
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
			socket = new ServerSocket(7778, 1, InetAddress.getLoopbackAddress());
		} catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Failed to open listen TCP socket 7778");
			System.exit(1);
		}
		if(socket.isClosed())
		{
			System.out.println("Failed to open listen TCP socket 7778");
			System.exit(1);
		}
		
		Thread l_pcvt = new Thread(m_pcv = new pointCloudViewer());
		
		new Thread(m_pco = new PointCloudObject()).start();
		
		l_pcvt.start();
	}
	
    public void run() {
        while (!m_pco.m_finished) {
        	while(true)
        	{
        		Socket l_conn_socket = null;
        		try
        		{
	            	socket.setSoTimeout(1000);
	            	System.out.println("Accept");
	            	l_conn_socket = socket.accept();
        			
        		} catch (SocketTimeoutException te)
	            {
					if(m_pco.m_finished)
					{
						try {
							socket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
            	System.out.println("Accepted");

            	ObjectInputStream l_ois = null;
	            try {
					l_ois = new ObjectInputStream(l_conn_socket.getInputStream());
	            } catch(Exception e)
	            {
	            	e.printStackTrace();
	            	try {
						l_conn_socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            	continue;
	            }
            	while((!l_conn_socket.isClosed()) && l_conn_socket.isConnected())
            	{
        			PointCmd l_cmd = null;
		            try {
						l_cmd = (PointCmd) l_ois.readObject();
					} catch (Exception ioe)
		            {
		
						// TODO Auto-generated catch block
						ioe.printStackTrace();
						try {
							l_conn_socket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						continue;
					}
		            //System.out.println("Received cmd "+l_cmd.m_cmd);
		            switch(l_cmd.m_cmd)
		            {
		            	case 0: // Clear
		            		m_pco.clear();
		            		break;
		            	case 1: // Add point, scale & pointsize
		            		try
		            		{
								m_pco.addPoint(l_cmd.m_point);
								m_pco.m_Pointsize = l_cmd.m_point.m_pointsize;
								m_pco.m_Scale = l_cmd.m_point.m_scale;
		            		} catch(Exception e)
		            		{
		            			e.printStackTrace();
		            			System.exit(3);
		            		}
		            		break;            		
		            }
            	}
        	}
        }
        try {
			socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("Finished");
        System.exit(0);
    }
}
