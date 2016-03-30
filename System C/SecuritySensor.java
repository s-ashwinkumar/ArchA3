
/**
 * ****************************************************************************************************************
 * Description:
 *
 * This class simulates a security sensor. It polls the message manager for messages corresponding to changes in state
 * of the door window and motion sensors and switches them off or on.
 *
 * Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
 * on the local machine.
 *
 * Internal Methods:
 *	float GetRandomNumber()
 *	boolean CoinToss()
 *   void PostTemperature(MessageManagerInterface ei, float temperature )
 *
 *****************************************************************************************************************
 */
import InstrumentationPackage.*;
import MessagePackage.*;
import java.util.*;

class SecuritySensor {

    public static void main(String args[]) {
        String MsgMgrIP;				// Message Manager IP address
        Message Msg = null;				// Message object
        MessageQueue eq = null;			// Message Queue
        int MsgId = 0;					// User specified message ID
        MessageManagerInterface em = null;// Interface object to the message manager
        boolean DoorTriggered = false;	// Door state: false == off, true == on
        boolean WindowTriggered = false;	// Window state: false == off, true == on
        boolean MSensorTriggered = false;	// Motion Sensor state: false == off, true == on
        int sensor = 0;
        int Delay = 2500;				// The loop delay (5 seconds)
        boolean Done = false;			// Loop termination flag
        Random random = new Random();
        int ID = random.nextInt(20)%(20+1);

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
            float WinPosY = 0.0f; 	//This is the Y position of the message window in terms
            //of a percentage of the screen height

            MessageWindow mw = new MessageWindow("Security Sensor", WinPosX, WinPosY);

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
                HeartBeat.SendHeartBeat(em, "Security Sensor-" + String.valueOf(ID) + "#Security Sensor detects window break, door break, and motion detection.");
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

                    if (Msg.GetMessageId() == -6) {
                        if (Msg.GetMessage().equalsIgnoreCase("D1")) // Door Triggered
                        {
                            DoorTriggered = true;

                        } // if

                        if (Msg.GetMessage().equalsIgnoreCase("D0")) // Door Intact
                        {
                            DoorTriggered = false;

                        } // if

                        if (Msg.GetMessage().equalsIgnoreCase("W1")) // window Triggered
                        {
                            WindowTriggered = true;

                        } // if

                        if (Msg.GetMessage().equalsIgnoreCase("W0")) // window Intact
                        {
                            WindowTriggered = false;

                        }// if

                        if (Msg.GetMessage().equalsIgnoreCase("M1")) // Motion Sensor Triggered
                        {
                            MSensorTriggered = true;

                        } // if

                        if (Msg.GetMessage().equalsIgnoreCase("M0l")) // Motion Sensor Intact
                        {
                            MSensorTriggered = false;

                        } // if

                    } // if
                    
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
                if (DoorTriggered) {
                        PostSecurity(em, 1);
                        mw.WriteMessage("Door Triggerred:: ");
                    }
                    if (WindowTriggered) {
                        PostSecurity(em, 2);
                        mw.WriteMessage("Window Triggerred:: ");
                    }
                    if (MSensorTriggered) {
                        PostSecurity(em, 3);
                        mw.WriteMessage("Motion Triggerred:: ");
                    }
                    if (!DoorTriggered && !MSensorTriggered && !WindowTriggered) {
                        PostSecurity(em, 0);
                        mw.WriteMessage("Current Sensor set (0 for off)::  ");
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
     * CONCRETE METHOD:: GetRandomNumber Purpose: This method provides the
     * simulation for on and off
     *
     * Arguments: None.
     *
     * Returns: float
     *
     * Exceptions: None
     *
     **************************************************************************
     */
    static private int GetRandomNumber() {
        Random r = new Random();
        int Val;
        Val = r.nextInt();
        return ((Val % 3));

    } // GetRandomNumber

    /**
     * *************************************************************************
     * CONCRETE METHOD:: CoinToss Purpose: This method provides a random true or
     * false value used for determining the positiveness or negativeness of the
     * drift value.
     *
     * Arguments: None.
     *
     * Returns: boolean
     *
     * Exceptions: None
     *
     **************************************************************************
     */
    static private boolean CoinToss() {
        Random r = new Random();

        return (r.nextBoolean());

    } // CoinToss

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
    static private void PostSecurity(MessageManagerInterface ei, int sensor) {
        // Here we create the message.

        Message msg = new Message((int) 3, String.valueOf(sensor));

        // Here we send the message to the message manager.
        try {
            ei.SendMessage(msg);
            //System.out.println( "Sent Temp Message" );

        } // try
        catch (Exception e) {
            System.out.println("Error Posting Security:: " + e);

        } // catch

    } // PostTemperature

} // TemperatureSensor
