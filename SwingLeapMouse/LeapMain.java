import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class LeapMain {

	
	
	  public static void main(String[] args) throws IOException {
		  
		  Logger LOGGER = LogManager.getLogger(LeapMain.class.getName()); 
		  
		  LOGGER.info("Application started.");

		  // Default setup
		  int V_SCREENS = 1;
		  int H_SCREENS = 2;
		  
		  try{
			  
			  V_SCREENS = Integer.parseInt(args[0]) > 0 ? Integer.parseInt(args[0]) : 1;
			  H_SCREENS = Integer.parseInt(args[1]) > 0 ? Integer.parseInt(args[1]) : 2;
			  
		  }
		  catch(Exception e){

			  LOGGER.warn("Using default screen setup");
		  }
		  
		  LeapGUI initializer = new LeapGUI();
		  initializer.setupGUI(V_SCREENS, H_SCREENS); 
		  
	  }

}
