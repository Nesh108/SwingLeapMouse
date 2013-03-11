/******************************************************************************\
* Author: Alberto Vaccari
* LeapMouse.java
* 
* This app simulates a mouse, based on the Sample.java for LeapMotion
*           
*           
\******************************************************************************/

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import com.leapmotion.leap.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

class LeapListener extends Listener {
	
	Logger LOGGER = LogManager.getLogger(LeapMouse.class.getName());
	
	int HORIZONTAL_SCREENS = 2;
	int VERTICAL_SCREENS = 1;
	
	LeapGUI guiController;
	
	//True for Debugging
	boolean DEBUG = true;
	
	int counter = 0;
	
	//0 = Key Tap 
	//1 = Finger Tap
	int CLICK_TYPE = 1;
	
	boolean USE_CALIBRATED_SCREEN = false;
	
	boolean HANDS_GESTURE = true;
	
	//Just to control the speed, it can be changed accordingly to needs
	int SLOW = 20;
	
	//Screen resolution, it should match the current screen resolution for more precise movements
	int SCREEN_X = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
	int SCREEN_Y = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;

	
	float cur_x = 0, cur_y = 0;
	
	int fingers_count = 0;
	int prev_fingers_count = 0;
	
	boolean Lclicked = false;
	boolean Mclicked = false;
	boolean Rclicked = false;
	boolean moveWindow = false;
	boolean keystroke = false;
	boolean LHold = false;
	
	boolean Swype = false;
	boolean Circle = false;
	
    public void onInit(Controller controller) {
    	guiController = new LeapGUI();
    	LOGGER.info("Initialized");
    	LOGGER.info("Current screen resolution: " + SCREEN_X +"x" + SCREEN_Y);
    	LOGGER.info("Screen Setup: " + VERTICAL_SCREENS + "x" + HORIZONTAL_SCREENS);
    }

    public void onConnect(Controller controller) {
        LOGGER.info("Connected");
    }

    public void onDisconnect(Controller controller) {
        LOGGER.info("Disconnected");
    }

    public void onExit(Controller controller) {
        LOGGER.info("Exited");
    }

    public void onFrame(Controller controller) {
    	
        // Get the most recent frame and report some basic information
        Frame frame = controller.frame();

	        
		    if(frame.gestures().get(0).type() == Gesture.Type.TYPE_KEY_TAP && !Lclicked && CLICK_TYPE == 0) {
	        	
		    	//If Key Tap Mode enabled 
		    	//Left Click
	    		clickMouse(0);
        		releaseMouse(0);
        		Lclicked = true;

		    	
      	    	if(DEBUG)
      	    		LOGGER.debug("Key Tap - Click");
		    	slow();
		    } else if (frame.gestures().get(0).type() == Gesture.Type.TYPE_SWIPE && !Swype && HANDS_GESTURE) {
        	    	
        	    	paste();
        	    	Swype = true;

    	        	paste();
          	    	if(DEBUG)
          	    		LOGGER.debug("Swype - Paste");
          	    	
          	    	LeapGUI.lbl_status.setText("Swype - Paste");

        	        slow();
        	    } else if (frame.gestures().get(0).type() == Gesture.Type.TYPE_CIRCLE && !Circle && HANDS_GESTURE) {
        	    	
        	    	Circle = true; 	
        	    	CircleGesture circle = new CircleGesture(frame.gestures().get(0));
        	        float progress = circle.progress();
        	        
        	        if (progress > 2.0f) {
        	        	
        	        	copy();
          	    		if(DEBUG)
          	    			LOGGER.debug("Circle - Copy");
          	    		
          	    		LeapGUI.lbl_status.setText("Circle - Copy");
        	        }      	    	
        	    	
        	        slow();
        	        
        	    }        	    
        	    else
        	    {
        	    	Circle = false; 	
        	    	Swype = false;
        	    	
        	    }
        	    
        
        if (!frame.fingers().empty()) {
          
            // Get fingers
            FingerList fingers = frame.fingers();
            fingers_count = frame.fingers().count();
            
            if(DEBUG && fingers_count != prev_fingers_count)
            {
            	LOGGER.debug("Currently " + fingers_count + " fingers visible.");
            	
            }
            
            LeapGUI.lbl_fingers.setText("Currently " + fingers_count + " fingers visible.");
            
            prev_fingers_count = fingers_count;
            
            if (!fingers.empty()) {
                // Calculate the hand's average finger tip position
                Vector avgPos = Vector.zero();
                for (Finger finger : fingers) {
                    avgPos = avgPos.plus(finger.tipPosition());
                }
                avgPos = avgPos.divide(fingers.count());
              
                
                if(USE_CALIBRATED_SCREEN && counter == 0){
	                //New Pointing System using first calibrated screen. Thanks to wooster @ freenode IRC
	                ScreenList screens = controller.calibratedScreens();
	                
	                if (screens.empty()) return;
	                Screen s = screens.get(0);
	                PointableList pointables = frame.hands().get(0).pointables();
	                
	                if(pointables.empty()) return;
	                Pointable firstPointable = pointables.get(0);
	                Vector intersection = s.intersect(
	                        firstPointable,
	                        true, // normalize
	                        1.0f // clampRatio
	                        );
	
			        // if the user is not pointing at the screen all components of
			        // the returned vector will be Not A Number (NaN)
			        // isValid() returns true only if all components are finite
			        if (!intersection.isValid()) return;
			
			        float x = s.widthPixels() * intersection.getX();
			        // flip y coordinate to standard top-left origin
			        float y = s.heightPixels() * (1.0f - intersection.getY());
			        moveMouse(x, y);
			        
                } else if(counter == 0)
                {
                    moveMouse(avgPos.getX() * 10 * HORIZONTAL_SCREENS, SCREEN_X - avgPos.getY() * 5 * VERTICAL_SCREENS);
                }
			
                
                
               //If Finger Tap Mode enabled 
               if(CLICK_TYPE == 1){ 
	               // Left Click
	               if(fingers.count() == 1 && !Lclicked && avgPos.getZ()<=-70)
	                {
	            	   
	            	    counter = 0;
	            	    clickMouse(0);
	                	releaseMouse(0);
	                	Lclicked = true;
	                	
	                    if(DEBUG)
	                    	LOGGER.debug("LClicked  - Finger Tap");

	                    
	                    LeapGUI.lbl_status.setText("LClicked  - Finger Tap");
	                	
	                }
	               else if(fingers.count() == 1 && Lclicked && avgPos.getZ()<=-70){

	                	
	                	if(counter == 25){
		            	    clickMouse(0);
		                	releaseMouse(0);
		            	    clickMouse(0);
		                	releaseMouse(0);
		                	counter = 0;
	                	}
	                	else
	                	{
	                		counter++;
	                	}
	               }
	            	   
	                else if(fingers.count() != 1 || avgPos.getZ()>0)
	                {
	
	                	Lclicked = false;
	                	slow();
	                	counter = 0;
	                }
	               
	               LeapGUI.lbl_counter.setText(""+counter);
               }
                // Left Click hold
                if(fingers.count() == 2 && !LHold && avgPos.getZ()<=-70)
                {
                	clickMouse(0);
                	LHold = true;
                	
                    if(DEBUG)
                    	LOGGER.debug("LHold");
                    
                    LeapGUI.lbl_status.setText("LHold");
                	
                }
                
                else if(fingers.count() != 2 || avgPos.getZ()>0)
                {
                	if(LHold)
                		releaseMouse(0);
                	LHold = false;
                	slow();
                	
                }
                
                
                
                // Right Click
                if(fingers.count() == 3 && !Rclicked && avgPos.getZ()<=-70)
                {
                	clickMouse(1);
                	releaseMouse(1);
                	
                	Rclicked = true;
                	
                    if(DEBUG)
                    	LOGGER.debug("RClicked");

                    LeapGUI.lbl_status.setText("RClicked");
                }
                
                else if(fingers.count() != 3 ||  avgPos.getZ()>0)
                {
                	Rclicked = false;
                	slow();
                	
                }
                
                // Middle Click
                if(fingers.count() == 5 && !Mclicked && avgPos.getZ()<=-70)
                {
                	clickMouse(2);
                	releaseMouse(2);
                	
                	Rclicked = true;
                	
                    if(DEBUG)
                    	LOGGER.debug("MClicked");

                    LeapGUI.lbl_status.setText("MClicked");
                }
                
                else if(fingers.count() != 5 ||  avgPos.getZ()>0)
                {
                	Mclicked = false;
                	slow();
                	
                }

                
                // Place both hands on device
                if(frame.hands().count()>1 && HANDS_GESTURE){
                
                	Hand hand1 = frame.hands().get(0);
                	Vector normal1 = hand1.palmNormal();
                	Hand hand2 = frame.hands().get(1);
                	Vector normal2 = hand2.palmNormal();
	               
	                if(hand1.fingers().count() >= 5 && !keystroke && avgPos.getZ()<=20 && (normal1.roll() <5 || normal1.roll() > -5) && (normal2.roll() <5 || normal2.roll() > -5))
	                {
	                	
	                	showHideDesktop();
	                	
	                    if(DEBUG)
	                    	LOGGER.debug("Show/Hide Desktop");
	                	
	                    LeapGUI.lbl_status.setText("Show/Hide Desktop");
	                    
	                    guiController.changeColorBtn(0, Color.gray);
	                    
	                	keystroke = true;
	                	
	                	// To slow down the framerate, I found this would help avoid any sort of incorrect behaviour 
	                	slow();
	                	slow();
	                	slow();
	                	slow();
	                	
	                	
	                	
	                }
	                else
	                {
	                	guiController.changeColorBtn(0, null);
	                	keystroke = false;
	                	
	                }
	                
	                
	                
	                
	            }

            }
            
            slow();
        }
        else
        {
        	LeapGUI.lbl_fingers.setText("Currently 0 fingers visible.");
        	
        }
    }
    
    
    // Slows down the frame rate
    private void slow(){
    	try {
			Thread.sleep(SLOW);
		} catch (InterruptedException e) {
			LOGGER.warn(e.getMessage());
			
		}
    }
    
    public void moveMouse(float x, float y)
    {
    	 Robot mouseHandler;
    	 
    	 if(cur_x != x || cur_y != y){
	    	
    		 cur_x = x;
	    	 cur_y = y;
	    	 
				try {
					
					mouseHandler = new Robot();
					mouseHandler.mouseMove((int)x, (int)y);
					
					
				} catch (AWTException e) {
					LOGGER.warn(e.getMessage());
				}
	    	 }
    	 
    	 
    }
    
    // 0: Left
    // 1: Right
    // 2: Middle  -not implemented yet-
    public void clickMouse(int value)
    {
    	int input;
    	
    	switch(value){
    		case 0: input = InputEvent.BUTTON1_MASK; guiController.changeColorBtn(0, Color.gray); guiController.changeColorBtn(1, Color.gray); break;
    		case 1: input = InputEvent.BUTTON3_MASK; guiController.changeColorBtn(2, Color.gray); break;
    		case 2: input = InputEvent.BUTTON2_MASK; guiController.changeColorBtn(5, Color.gray); break;
    		default: input = 0;
    	}
    	
    	 Robot mouseHandler;
    	 
    	
				try {
					
					mouseHandler = new Robot();
					mouseHandler.mousePress(input);
					
				} catch (AWTException e) {
					LOGGER.warn(e.getMessage());
				}
	    	 
    	 
    	 
    }
 
    // 0: Left
    // 1: Right
    // 2: Middle  -not implemented yet-
    public void releaseMouse(int value)
    {
    	int input;
    	
    	switch(value){
    		case 0: input = InputEvent.BUTTON1_MASK; guiController.changeColorBtn(0, null); guiController.changeColorBtn(1, null); break;
    		case 1: input = InputEvent.BUTTON3_MASK; guiController.changeColorBtn(2, null); break;
    		case 2: input = InputEvent.BUTTON2_MASK; guiController.changeColorBtn(5, null); break;
    		default: input = 0;
    	}
    	
    	 Robot mouseHandler;
    	 
    	
				try {
					
					mouseHandler = new Robot();
					mouseHandler.mouseRelease(input);
					
				} catch (AWTException e) {
					LOGGER.warn(e.getMessage());
				}
	    	 
    	 
    	 
    }   

    
    public void showHideDesktop()
    {
    	 Robot keyHandler;
    	 
    	
				try {
					
					keyHandler = new Robot();
					keyHandler.keyPress(KeyEvent.VK_WINDOWS);
					keyHandler.keyPress(KeyEvent.VK_D);
					keyHandler.keyRelease(KeyEvent.VK_WINDOWS);
					keyHandler.keyRelease(KeyEvent.VK_D);
					
				} catch (AWTException e) {
					LOGGER.warn(e.getMessage());
				}
	    	 
    	 
    	 
    }

    public void copy()
    {
    	 Robot keyHandler;
    	 
    	
				try {
					
					keyHandler = new Robot();
					keyHandler.keyPress(KeyEvent.VK_CONTROL);
					keyHandler.keyPress(KeyEvent.VK_C);
					keyHandler.keyRelease(KeyEvent.VK_CONTROL);
					keyHandler.keyRelease(KeyEvent.VK_C);
					
				} catch (AWTException e) {
					LOGGER.warn(e.getMessage());
				}
	    	 
    	 
    	 
    }

    public void paste()
    {
    	 Robot keyHandler;
    	 
    	
				try {
					
					keyHandler = new Robot();
					keyHandler.keyPress(KeyEvent.VK_CONTROL);
					keyHandler.keyPress(KeyEvent.VK_V);
					keyHandler.keyRelease(KeyEvent.VK_CONTROL);
					keyHandler.keyRelease(KeyEvent.VK_V);
					
				} catch (AWTException e) {
					LOGGER.warn(e.getMessage());
				}
	    	 
    	 
    	 
    }

    
    public void setDebug(boolean d){
    	DEBUG = d;
    }
    
    public void setClickType(int i){
    	CLICK_TYPE = i;
    }
    
    public void setCalibratedScren(boolean d){
    	USE_CALIBRATED_SCREEN = d;
    }
    
    public void setHandsGesture(boolean b){
    	HANDS_GESTURE = b;
    }
    
    public boolean getHandsGesture(){
    	return HANDS_GESTURE;
    }
}

public class LeapMouse {
	
	Controller controller;
	LeapListener listener;
	
    LeapMouse(){

        listener = new LeapListener();
        controller = new Controller();
        controller.enableGesture( Gesture.Type.TYPE_KEY_TAP );
        controller.enableGesture( Gesture.Type.TYPE_SWIPE );
        controller.enableGesture( Gesture.Type.TYPE_CIRCLE );

    }
    
    //Public method for changing Debug mode
    public void setDebug(boolean b){
    	
    	listener.setDebug(b);
    }

    //Public method for changing Click type
    public void setClickType(int i){
    	
    	listener.setClickType(i);
    }
    
    //Public method for changing Hands Gesture mode
    public void setHandsGesture(boolean b){
    	
    	listener.setHandsGesture(b);
    }
    
    public boolean getHandsGesture(){
    	
    	return listener.getHandsGesture();
    }
    
    //Public method for either using the calibrated screen or not
    public void setCalibratedScreen(boolean b){
    	
    	listener.setCalibratedScren(b);
    }
    
    public void stop(){
    	
    	controller.removeListener(listener);
    }
    
    public void start(){
    	
    	controller.addListener(listener);
    }
    
    public void setScreens(int V_SCREENS, int H_SCREENS){
    	
    	listener.VERTICAL_SCREENS = V_SCREENS;
    	listener.HORIZONTAL_SCREENS = H_SCREENS;
    }
    
}
