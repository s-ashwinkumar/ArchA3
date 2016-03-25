
/**
 * ****************************************************************************************************************
 * Description: This class is the console for the museum environmental control system. This process consists of two
 * threads. The SCSMonitor object is a thread that is started that is responsible for the monitoring and control of
 * the museum environmental systems. The main thread provides a text interface for the user to change the temperature
 * and humidity ranges, as well as shut down the system.
 *
 * Parameters: None
 *
 * Internal Methods: None
 *
 *****************************************************************************************************************
 */
import TermioPackage.*;
import MessagePackage.*;

public class SCSConsole {

    public static void main(String args[]) {
        Termio UserInput = new Termio();	// Termio IO Object
        boolean Done = false;				// Main loop flag
        String Option = null;				// Menu choice from user
        Message Msg = null;					// Message object
        boolean Error = false;				// Error flag
        SCSMonitor Monitor = null;			// The environmental control system monitor
        int sensor;

        /////////////////////////////////////////////////////////////////////////////////
        // Get the IP address of the message manager
        /////////////////////////////////////////////////////////////////////////////////
        if (args.length != 0) {
            // message manager is not on the local system

            Monitor = new SCSMonitor(args[0]);

        } else {

            Monitor = new SCSMonitor();

        } // if

        // Here we check to see if registration worked. If ef is null then the
        // message manager interface was not properly created.
        if (Monitor.IsRegistered()) {
            Monitor.start(); // Here we start the monitoring and control thread

            while (!Done) {
                // Here, the main thread continues and provides the main menu

                System.out.println("\n\n\n\n");
                System.out.println("Security Control System (ECS) Command Console: \n");

                if (args.length != 0) {
                    System.out.println("Using message manger at: " + args[0] + "\n");
                } else {
                    System.out.println("Using local message manger \n");
                }

                System.out.println("Select an Option: \n");
                System.out.println("1: Turn On Door sensor");
                System.out.println("2: Turn On window sensor");
                System.out.println("3: Turn On Motion sensor");
                System.out.println("4: Turn Off Door sensor");
                System.out.println("5: Turn Off window sensor");
                System.out.println("6: Turn Off Motion sensor");
                System.out.println("7: Turn Off All sensors");
                System.out.println("X: Stop System\n");
                System.out.print("\n>>>> ");
                Option = UserInput.KeyboardReadString();

                //////////// option 1 to 6 ////////////
                if (Option.equals("1") || Option.equals("2") || Option.equals("3") || Option.equals("4") || Option.equals("5") || Option.equals("6")) {
                    Monitor.SetSensor(Integer.parseInt(Option));

                } // if

                //////////// option 7 ////////////    
                if (Option.equals("7") ) {
                   
                    Monitor.SetSensor(0);
                } // if
                //////////// option X ////////////
                if (Option.equalsIgnoreCase("X")) {
                    // Here the user is done, so we set the Done flag and halt
                    // the environmental control system. The monitor provides a method
                    // to do this. Its important to have processes release their queues
                    // with the message manager. If these queues are not released these
                    // become dead queues and they collect messages and will eventually
                    // cause problems for the message manager.

                    Monitor.Halt();
                    Done = true;
                    System.out.println("\nConsole Stopped... Exit monitor mindow to return to command prompt.");
                    Monitor.Halt();

                } // if

            } // while

        } else {

            System.out.println("\n\nUnable start the monitor.\n\n");

        } // if

    } // main

} // ECSConsole
