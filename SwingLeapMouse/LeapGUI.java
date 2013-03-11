import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.BevelBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("serial")
public class LeapGUI extends JFrame implements WindowListener{

	Logger LOGGER = LogManager.getLogger(LeapGUI.class.getName());
	
	static int SIZE_X = 400;
	static int SIZE_Y = 250;
	
	static String PROPERTIES_FILE = "SwingLeapMouse.property";
	
	static String OL_ENABLED_PROP = "OL_Enabled";
	static String HAND_GESTURE_PROP = "Hand_Gesture";
	static String IS_VISIBLE_PROP = "Is_Visible";
	
	static String APP_NAME = "SwingLeapMouse Leap";
	static String APP_VERSION = "0.5a";
	
	static boolean isActive = false;
	
	JPanel mainPanel;
	
	static JButton btn_on;
	static JButton btn_off;
	
	static JButton btn_lclick;
	static JButton btn_lhold;
	static JButton btn_rclick;
	static JButton btn_extra;
	
	static JLabel lbl_status;
	static JLabel lbl_fingers;
	static JLabel lbl_counter;
	
	JCheckBox cbx_handsgesture;
	
	static JFrame mainFrame;
	
	TrayIcon trayIcon;
	
	static LeapMouse leap;
	
	static Image icon = Toolkit.getDefaultToolkit().getImage("images/icon.jpg");
	
	static FileOutputStream fos;
	static Properties prop;
	
	
	
	public void setupGUI(int V_SCREENS, int H_SCREENS) throws IOException{
		
		LOGGER.info("Setting up GUI.");
		
		LookAndFeel();
		
		leap = new LeapMouse();
		leap.setScreens(V_SCREENS, H_SCREENS);
		
		//Creating and setting up mainFrame
		mainFrame = new JFrame(APP_NAME + " v" + APP_VERSION);
		mainFrame.setIconImage(icon);
		mainFrame.setAlwaysOnTop(true);
		mainFrame.setPreferredSize(new Dimension( SIZE_X, SIZE_Y));
		mainFrame.setLocation(400, 400);
		mainFrame.setResizable(false);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		
		mainFrame.addWindowListener(this);
		//Creating and setting up mainPanel
		mainPanel = new JPanel();
		mainPanel.setBounds(0,0, SIZE_X, SIZE_Y);
		
		
		//Creating and declaring components
		setupMenu();
		
		setupButtons();
		
		setupTrayIcon();
		
		setupStatusBar();
		
		initProperties();
		
		setupCheckboxes();
		
		setupKeyboardShortcuts();
		
		
		
		mainFrame.add(mainPanel);
		    
		mainPanel.setLayout(null);
		mainFrame.pack();
		
	
		LOGGER.info("GUI correctly setup.");

		
	  }
	
	public boolean getActive(){
		 
		return isActive;
	}

	static public void setActive(boolean b) throws IOException{
		
		isActive = b;
		fos = new FileOutputStream(PROPERTIES_FILE);
		
		if(isActive){
		  prop.setProperty(OL_ENABLED_PROP, "1");
		  btn_on.setForeground(Color.GREEN);
		  btn_on.setBackground(null);
		  btn_off.setForeground(Color.BLACK);
		  btn_off.setBackground(Color.LIGHT_GRAY);
		  leap.start(); 
		}
		else{
		  prop.setProperty(OL_ENABLED_PROP, "0");
		  btn_on.setForeground(Color.BLACK);
		  btn_on.setBackground(Color.LIGHT_GRAY);
		  btn_off.setForeground(Color.RED);
		  btn_off.setBackground(null);
		  leap.stop(); 
		}
		
		prop.store(fos, "SwingLeapMouse Properties");
		fos.flush();
		fos.close();
		  
	}
	
	private void initProperties() throws IOException{

		
		prop = new Properties();
		
		try{
			prop.load(new FileInputStream(PROPERTIES_FILE));

			if(prop.getProperty(OL_ENABLED_PROP).equals("1"))
				setActive(true);
			else
				setActive(false);
			
			if(prop.getProperty(HAND_GESTURE_PROP).equals("0"))
				setHandsGesture(false);
			
			if(prop.getProperty(IS_VISIBLE_PROP).equals("0"))
				setWindowVisibility(false);
			else
				setWindowVisibility(true);
			
			LOGGER.info("Properties Loaded.");
		}
		catch(Exception e){
			LOGGER.debug("Initializing Properties...");
			fos = new FileOutputStream(PROPERTIES_FILE);
			
			prop.setProperty(OL_ENABLED_PROP, "0");
			
			prop.setProperty(HAND_GESTURE_PROP, "1");
			
			prop.setProperty(IS_VISIBLE_PROP, "1");
			
			prop.store(fos, "SwingLeapMouse Properties");
			fos.flush();
			fos.close();
			
			setActive(false);
			setWindowVisibility(true);
			
			LOGGER.info("Properties Initialized.");
		}
		

		
		LOGGER.debug(OL_ENABLED_PROP +" : "+ prop.getProperty(OL_ENABLED_PROP));
		LOGGER.debug(HAND_GESTURE_PROP +" : "+ prop.getProperty(HAND_GESTURE_PROP));
		LOGGER.debug(IS_VISIBLE_PROP +" : "+ prop.getProperty(IS_VISIBLE_PROP));
		
	}
	
	public void changeColorBtn(int btn_id, Color c){
		
		switch(btn_id){
		
		case 0: btn_lclick.setBackground(c); break;
		case 1: btn_lhold.setBackground(c); break;
		case 2: btn_rclick.setBackground(c); break;
		case 3: btn_extra.setBackground(c); break;
		default: break;
		
		}
		
	}
	

	public void setupMenu(){
		
		//File
		JMenuBar menubar = new JMenuBar();
		JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        
        ImageIcon exit_icon = new ImageIcon("images/close.png");
        
        JMenuItem exitItem = new JMenuItem("Exit",exit_icon);
        exitItem.setMnemonic(KeyEvent.VK_C);
        exitItem.setToolTipText("Exit application");
        exitItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	
            	LOGGER.info("Program closed correctly.");
                System.exit(0);
                
            }

        });
        
        file.add(exitItem);

        //Log
		JMenu log = new JMenu("Logs");
		log.setMnemonic(KeyEvent.VK_L);
        
        ImageIcon log_icon = new ImageIcon("images/log_icon.png");
        
        JMenuItem logItem = new JMenuItem("Show logs",log_icon);
        logItem.setMnemonic(KeyEvent.VK_A);
        logItem.setToolTipText("Show logs");
        logItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	ProcessBuilder pb = new ProcessBuilder("Notepad.exe", "logs/SwingLeapMouse.txt");
            	try {
					pb.start();
				} catch (IOException e) {
					LOGGER.warn(e.getMessage());
				}
            }

        });
        
        log.add(logItem);
  
        
        
        //About
		JMenu about = new JMenu("?");
		about.setMnemonic(KeyEvent.VK_S);
        
        ImageIcon about_icon = new ImageIcon("images/about_icon.png");
        
        JMenuItem aboutItem = new JMenuItem("About",about_icon);
        aboutItem.setMnemonic(KeyEvent.VK_A);
        aboutItem.setToolTipText("About " + APP_NAME + " v" + APP_VERSION);
        aboutItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	
            }

        });
        
        about.add(aboutItem);
        
        ImageIcon help_icon = new ImageIcon("images/help_icon.png");
        
        JMenuItem helpItem = new JMenuItem("Help",help_icon);
        helpItem.setMnemonic(KeyEvent.VK_H);
        helpItem.setToolTipText("Help");
        helpItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
               try {
            	   
            	   URL url = new File("C:/Program Files (x86)/Orzone ClientTool/doc/index.html").toURI().toURL();
            	   openWebpage(url);
            	   
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            }

        });
        
        about.add(helpItem);

        menubar.add(file);
        menubar.add(log);
        menubar.add(about);

        mainFrame.setJMenuBar(menubar);
	}
	
	
	public void setHandsGesture(boolean b) throws IOException{
		
		leap.setHandsGesture(b);
		fos = new FileOutputStream(PROPERTIES_FILE);
		
		if(b)	
			prop.setProperty(HAND_GESTURE_PROP, "1");

		else
			prop.setProperty(HAND_GESTURE_PROP, "0");

		prop.store(fos, "SwingLeapMouse Properties");
		fos.flush();
		fos.close();
	}
	
	public boolean getHandsGesture(){
		
		return leap.getHandsGesture();
	}
	
	private void setupKeyboardShortcuts(){

		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		manager.addKeyEventDispatcher (new KeyboardHandler());
	}
	
	private void setupButtons(){
		
		//Button ON
		btn_on = new JButton("ON");
		btn_on.setBounds(150, 30, 100, 60);
		
		btn_on.addActionListener(new ActionListener() {
		
		public void actionPerformed(ActionEvent e) {
              try {
				setActive(true);
			} catch (IOException e1) {
				LOGGER.warn(e1.getMessage());
			}
              
              LOGGER.info("SwingLeapMouse turned ON");
		    }
		});
		
		//Button OFF
		btn_off = new JButton("OFF");
		btn_off.setBounds(150, 100, 100, 60);
		btn_off.addActionListener(new ActionListener() {
			
		public void actionPerformed(ActionEvent e) {
				try {
					setActive(false);
				} catch (IOException e1) {
					LOGGER.warn(e1.getMessage());
				}
				
				LOGGER.info("SwingLeapMouse turned OFF");
				
		    }
		});
		
		//Leap Notification Buttons
		//Button Left Click
		btn_lclick = new JButton("L Click");
		btn_lclick.setBounds(20, 30, 90, 30);
		btn_lclick.setEnabled(false);
		
		//Button Left Hold
		btn_lhold = new JButton("L Hold");
		btn_lhold.setBounds(20, 60, 90, 30);
		btn_lhold.setEnabled(false);
		
		
		//Button Right Click
		btn_rclick = new JButton("R Click");
		btn_rclick.setBounds(20, 90, 90, 30);
		btn_rclick.setEnabled(false);
		
		//Button Extra
		btn_extra = new JButton("Extra");
		btn_extra.setBounds(20, 120, 90, 30);
		btn_extra.setEnabled(false);
		
		//Adding components to mainPanel
		mainPanel.add(btn_on);
		mainPanel.add(btn_off);
		 
		mainPanel.add(btn_lclick);
		mainPanel.add(btn_lhold);
		mainPanel.add(btn_rclick);
		mainPanel.add(btn_extra);
	}
	
	public void setupCheckboxes(){
		
		
		cbx_handsgesture = new JCheckBox("Hands Gesture");
		cbx_handsgesture.setBounds(280, 20, 180, 30);
		cbx_handsgesture.setSelected(getHandsGesture());
		
		cbx_handsgesture.addActionListener(new ActionListener() {
			
		public void actionPerformed(ActionEvent e) {
			try {
				if(cbx_handsgesture.isSelected())
				{
					setHandsGesture(true);
				}
				else{
					setHandsGesture(false);
				}
				
				} catch (IOException e1) {
					LOGGER.warn(e1.getMessage());
					}
			

			
		    }
		});
		
		mainPanel.add(cbx_handsgesture);
		
	}

	private void setupTrayIcon(){
		PopupMenu popMenu= new PopupMenu();
    	MenuItem item1 = new MenuItem("Show");
    	popMenu.add(item1); 
    	item1.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				setWindowVisibility(true);
			
		    	SystemTray.getSystemTray().remove(trayIcon);
				
				
			}
		});
    	
    	MenuItem item2 = new MenuItem("Exit");
    	popMenu.add(item2); 
    	item2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				LOGGER.info("Program terminated by User.");
				System.exit(0);
			}
		});
    	
    	
    	trayIcon = new TrayIcon(icon, "SwingLeapMouse", popMenu);
	}
	
	private void setupStatusBar(){
		
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		mainFrame.add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setPreferredSize(new Dimension(SIZE_X, 16));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
		
		lbl_fingers = new JLabel("Currently 1 fingers visible.");
		lbl_fingers.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(lbl_fingers);
		
		statusPanel.add(new JLabel("  |  Last Action Performed:  "));
		
		lbl_status = new JLabel("");
		lbl_status.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(lbl_status);
		
		statusPanel.add(new JLabel("  |  "));
		
		lbl_counter = new JLabel("0");
		lbl_counter.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(lbl_counter);

	}
	private class KeyboardHandler implements KeyEventDispatcher 
    {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e)
        {
        	try{
	            if (e.getKeyCode() == 38) //CTRL + UP key
	            {
	            	btn_on.doClick();
	            }
	            else if (e.getKeyCode() == 40) //down key
	            {
	            	btn_off.doClick();
	            }
	            else if (e.getKeyCode() == 34) //page down key
	            {
	            	setWindowVisibility(false);
	            
	            	SystemTray.getSystemTray().add(trayIcon);
	
	            }

        	}
        	catch(Exception ex){}

            return false;
        }
    }

    private void LookAndFeel() {

        LookAndFeelInfo[] looks = UIManager.getInstalledLookAndFeels();

        for (int i = 0; i < looks.length; i++) {

                // WindowsOS running
                if (looks[i].getClassName().equals(
                                ("com.sun.java.swing.plaf.windows.WindowsLookAndFeel"))) {
                        WindowsOS();
                        break;
                }

                // MacOS running
                if (looks[i].getClassName().equals("com.apple.laf.AquaLookAndFeel")) {
                        MacOS();
                       
                        break;
                }

                // OtherOS
                else {
                        OtherOS();

                }
                

        }
        
        LOGGER.info("Look&Feel initialized.");
    }
    
    private void WindowsOS() {

        try {
                String NameLaF = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
                UIManager.setLookAndFeel(NameLaF);

        } catch (Exception e) {
        	LOGGER.warn(e.getMessage());
        }
    }


    private void MacOS() {

        try {
                String NameLaF = "com.apple.laf.AquaLookAndFeel";
                UIManager.setLookAndFeel(NameLaF);

        } catch (Exception e) {
        	LOGGER.warn(e.getMessage());
        }
    }


    private void OtherOS() {

        try {
                String NameLaF = "javax.swing.plaf.metal.MetalLookAndFeel";
                UIManager.setLookAndFeel(NameLaF);
               

        } catch (Exception e) {
        	LOGGER.warn(e.getMessage());
        }
    }


    public void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void windowIconified(WindowEvent e) {
		
		setWindowVisibility(false);
		
		
	}
	
	private void setWindowVisibility(boolean b){
		
		mainFrame.setVisible(b);
		mainFrame.setState(java.awt.Frame.NORMAL);
		try{
			
			fos = new FileOutputStream(PROPERTIES_FILE);
			
			if(b)	
				prop.setProperty(IS_VISIBLE_PROP, "1");
	
			else
				{
					prop.setProperty(IS_VISIBLE_PROP, "0");
					SystemTray.getSystemTray().add(trayIcon);
				}
	
			prop.store(fos, "SwingLeapMouse Properties");
			fos.flush();
			fos.close();
		}
		catch(Exception e){}
	
	}
	
	
	
	
}
