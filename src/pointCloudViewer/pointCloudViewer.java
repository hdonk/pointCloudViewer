package pointCloudViewer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import eora3D.PointCloudObject;
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
            	DataInputStream l_dis = null;
				DataOutputStream l_dos = null;
	            try {
					l_dis = new DataInputStream(l_conn_socket.getInputStream());
					l_dos = new DataOutputStream(l_conn_socket.getOutputStream());
	            } catch(Exception e)
	            {
	            	e.printStackTrace();
	            	try {
	            		l_dos.close();
	            		l_dis.close();
						l_conn_socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	            	continue;
	            }
            	while((!l_conn_socket.isClosed()) && l_conn_socket.isConnected())
            	{
        			int l_cmd = 0;
		            try {
//		            	System.out.println("Reading object");
						l_cmd = l_dis.readInt();
//		            	System.out.println("Read object");
	            	} catch (EOFException eof)
		            {
						try {
		            		l_dos.close();
		            		l_dis.close();
							l_conn_socket.close();
						} catch (IOException ioe) {
							ioe.printStackTrace();
						}
						break;
					} catch (IOException ioe)
		            {
						ioe.printStackTrace();
						try {
		            		l_dos.close();
		            		l_dis.close();
							l_conn_socket.close();
						} catch (IOException ioe2) {
							ioe2.printStackTrace();
						}
						break;
					}
//		            System.out.println("Received cmd "+l_cmd);
		            switch(l_cmd)
		            {
		            	case 0: // Clear
		            		m_pco.clear();
		            		break;
		            	case 1:
		            		float l_s, l_ps;
		            		try
		            		{
			            		l_s = l_dis.readFloat();
			            		l_ps = l_dis.readFloat();
		            		} catch(Exception e)
		            		{
		            			e.printStackTrace();
		            			break;
		            		}
		            		m_pco.m_Scale = l_s;
		            		m_pco.m_Pointsize = l_ps;
		            		break;
		            	case 2: // Add point
		            		try
		            		{
		            			//System.out.println("x "+l_cmd.m_point.m_x+" y "+l_cmd.m_point.m_y);
		            			int l_layer = l_dis.readInt();
		            			RGB3DPoint l_pt = new RGB3DPoint();
		            			l_pt.read(l_dis);
								m_pco.addPoint(l_layer, l_pt);
		            		} catch(Exception e)
		            		{
		            			e.printStackTrace();
		            			System.exit(3);
		            		}
		            		break;
		            	case 3: // Sync
		        			try {
		        				l_dos.writeInt(3);
		        				l_dos.flush();
		        			} catch (IOException e) {
		        				// TODO Auto-generated catch block
		        				e.printStackTrace();
		        				System.exit(4);
		        			}
							m_pco.m_refresh = true;
		        			//System.gc();
		            		break;
		            	case 4: // Turntable rotation
		            		try
		            		{
		            			//System.out.println("x "+l_cmd.m_point.m_x+" y "+l_cmd.m_point.m_y);
		            			float l_angle = l_dis.readFloat();
		            			int l_Zrotoff = l_dis.readInt();
		            			int l_Xrotoff = l_dis.readInt();
		            			int l_Ydispoff = l_dis.readInt();
		            			int l_leftfilter = l_dis.readInt();
		            			int l_rightfilter = l_dis.readInt();
		            			int l_topfilter = l_dis.readInt();
		            			int l_bottomfilter = l_dis.readInt();
		            			int l_fronttfilter = l_dis.readInt();
		            			int l_backfilter = l_dis.readInt();
		            			System.out.println("TT rot "+l_angle+" Z "+l_Zrotoff+" X "+l_Xrotoff);
		            			m_pco.setTT(l_angle, l_Zrotoff, l_Xrotoff, l_Ydispoff,
				            			l_leftfilter,
				            			l_rightfilter,
				            			l_topfilter,
				            			l_bottomfilter,
				            			l_fronttfilter,
				            			l_backfilter);
		            			m_pco.m_refresh = true;
		            		} catch(Exception e)
		            		{
		            			e.printStackTrace();
		            			System.exit(5);
		            		}
		            		break;
		            }
            	}
				m_pco.m_refresh = true;
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
