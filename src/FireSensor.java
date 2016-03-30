
/**
 * ****************************************************************************************************************
 * Description:
 *
 * This class simulates a fire sensor. 
 *
 *****************************************************************************************************************
 */
import InstrumentationPackage.*;
import MessagePackage.*;
import java.util.*;

class FireSensor {

    public static void main(String args[]) {
        String MsgMgrIP;				// Message Manager IP address
        Message Msg = null;				// Message object
        MessageQueue eq = null;			// Message Queue
        int MsgId = 0;					// User specified message ID
        MessageManagerInterface em = null;// Interface object to the message manager
        boolean state = false;	// Door state: false == off, true == on
        int Delay = 2500;				// The loop delay (5 seconds)
        boolean Done = false;			// Loop termination flag

        /////////////////////////////////////////////////////////////////////////////////
        // Get the IP address of the message manager
        /////////////////////////////////////////////////////////////////////////////////
        if (args.length == 0) {
            // message manager is on the local system

            System.out.println("\n\nAttempting to register on the local machine...");

            try {
                // Here we create an message manager interface object. This assumes
                // that the message manager is on the local machine

                em = new MessageManagerInterface();
            } catch (Exception e) {
                System.out.println("Error instantiating message manager interface: " + e);

            } // catch

        } else {

            // message manager is not on the local system
            MsgMgrIP = args[0];

            System.out.println("\n\nAttempting to register on the machine:: " + MsgMgrIP);

            try {
                // Here we create an message manager interface object. This assumes
                // that the message manager is NOT on the local machine

                em = new MessageManagerInterface(MsgMgrIP);
            } catch (Exception e) {
                System.out.println("Error instantiating message manager interface: " + e);

            } // catch

        } // if

        // Here we check to see if registration worked. If ef is null then the
        // message manager interface was not properly created.
        if (em != null) {

            // We create a message window. Note that we place this panel about 1/2 across
            // and 1/3 down the screen
            float WinPosX = 0.7f; 	//This is the X position of the message window in terms
            //of a percentage of the screen height
            float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
            //of a percentage of the screen height

            MessageWindow mw = new MessageWindow("Fire Sensor", WinPosX, WinPosY);

            mw.WriteMessage("Registered with the message manager.");

            try {
                mw.WriteMessage("   Participant id: " + em.GetMyId());
                mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime());

            } // try
            catch (Exception e) {
                mw.WriteMessage("Error:: " + e);

            } // catch

            while (!Done) {
                // Post the current temperature
                //HeartBeat.SendHeartBeat(em, "Fire Sensor#XXX ");
                // Get the message queue
                try {
                    eq = em.GetMessageQueue();

                } // try
                catch (Exception e) {
                    mw.WriteMessage("Error getting message queue::" + e);

                } // catch

                // If there are messages in the queue, we read through them.
                // We are looking for MessageIDs = -5, this means the the heater
                // or chiller has been turned on/off. Note that we get all the messages
                // at once... there is a 2.5 second delay between samples,.. so
                // the assumption is that there should only be a message at most.
                // If there are more, it is the last message that will effect the
                // output of the temperature as it would in reality.
                int qlen = eq.GetSize();

                for (int i = 0; i < qlen; i++) {
                    Msg = eq.GetMessage();

                    if (Msg.GetMessageId() == -12) {
                        if (Msg.GetMessage().equalsIgnoreCase("ON"))
                        {
                            state = true;

                        } // if

                        if (Msg.GetMessage().equalsIgnoreCase("OFF"))
                        {
                            state = false;

                        } // if

                    } // if

                    //if(Msg.GetMessageId() == -13 && Msg.GetMessage().equalsIgnoreCase("ON")){
                    //    state = false;
                    //}
                    
                    // If the message ID == 99 then this is a signal that the simulation
                    // is to end. At this point, the loop termination flag is set to
                    // true and this process unregisters from the message manager.
                    if (Msg.GetMessageId() == 99) {
                        Done = true;

                        try {
                            em.UnRegister();

                        } // try
                        catch (Exception e) {
                            mw.WriteMessage("Error unregistering: " + e);

                        } // catch

                        mw.WriteMessage("\n\nSimulation Stopped. \n");

                    } // if

                } // for
                if (state) {
                    PostFire(em, "ON");
                    mw.WriteMessage("Fire alarm");
                }else{
                    //PostFire(em, "OFF");
                    mw.WriteMessage("Nn Fire");
                }
                    
                // Here we wait for a 2.5 seconds before we start the next sample
                try {
                    Thread.sleep(Delay);

                } // try
                catch (Exception e) {
                    mw.WriteMessage("Sleep error:: " + e);

                } // catch

            } // while

        } else {

            System.out.println("Unable to register with the message manager.\n\n");

        } // if

    } // main


    /**
     * *************************************************************************
     * CONCRETE METHOD:: PostTemperature Purpose: This method posts the
     * specified temperature value to the specified message manager. This method
     * assumes an message ID of 1.
     *
     * Arguments: MessageManagerInterface ei - this is the messagemanger
     * interface where the message will be posted.
     *
     * 0,1,2,3 : 0 for no sensor on, 1 for door, 2 for window and 3 for motion
     * sensor
     *
     * Returns: none
     *
     * Exceptions: None
     *
     **************************************************************************
     */
    static private void PostFire(MessageManagerInterface ei, String context) {
        // Here we create the message.

        Message msg = new Message((int) 22, context);

        // Here we send the message to the message manager.
        try {
            ei.SendMessage(msg);
            //System.out.println( "Sent Temp Message" );

        } // try
        catch (Exception e) {
            System.out.println("Error Posting fire alarm " + e);

        } // catch

    } // PostTemperature

} // TemperatureSensor
