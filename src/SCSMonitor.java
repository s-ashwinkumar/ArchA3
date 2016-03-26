
/**
 * ****************************************************************************************************************
 *
 * Description:
 *
 * This class monitors the environmental control systems that control museum temperature and humidity. In addition to
 * monitoring the temperature and humidity, the SCSMonitor also allows a user to set the humidity and temperature
 * ranges to be maintained. If temperatures exceed those limits over/under alarm indicators are triggered.
 *
 * Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
 * on the local machine.
 *
 * Internal Methods:
 *	static private void Heater(MessageManagerInterface ei, boolean ON )
 *	static private void Chiller(MessageManagerInterface ei, boolean ON )
 *	static private void Humidifier(MessageManagerInterface ei, boolean ON )
 *	static private void Dehumidifier(MessageManagerInterface ei, boolean ON )
 *
 *****************************************************************************************************************
 */
import InstrumentationPackage.*;
import MessagePackage.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class SCSMonitor extends Thread {

    private MessageManagerInterface em = null;	// Interface object to the message manager
    private String MsgMgrIP = null;				// Message Manager IP address
    private int arm = 1;
    private boolean critical = true;

    boolean Registered = true;					// Signifies that this class is registered with an message manager.
    MessageWindow mw = null;					// This is the message window
    Indicator di;								// Door indicator
    Indicator wi;                               // Window indicator
    Indicator mi;								// Motion indicator
    int windowBroken = 0;
    int doorOpen = 0;
    int motionDetected = 0;

    public SCSMonitor() {
        // message manager is on the local system

        try {
            // Here we create an message manager interface object. This assumes
            // that the message manager is on the local machine

            em = new MessageManagerInterface();

        } catch (Exception e) {
            System.out.println("SCSMonitor::Error instantiating message manager interface: " + e);
            Registered = false;

        } // catch

    } //Constructor

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    public SCSMonitor(String MsgIpAddress) {
        // message manager is not on the local system

        MsgMgrIP = MsgIpAddress;

        try {
            // Here we create an message manager interface object. This assumes
            // that the message manager is NOT on the local machine

            em = new MessageManagerInterface(MsgMgrIP);
        } catch (Exception e) {
            System.out.println("SCSMonitor::Error instantiating message manager interface: " + e);
            Registered = false;

        } // catch

    } // Constructor

    public void setWindowBroken(int windowBroken) {
        this.windowBroken = windowBroken;
    }

    public void setDoorOpen(int doorOpen) {
        this.doorOpen = doorOpen;
    }

    public void setMotionDetected(int motionDetected) {
        this.motionDetected = motionDetected;
    }

    public void run() {
        Message Msg = null;				// Message object
        MessageQueue eq = null;			// Message Queue
        int MsgId = 0;					// User specified message ID

        int Delay = 1000;				// The loop delay (1 second)
        boolean Done = false;			// Loop termination flag
        boolean ON = true;				// Used to turn on heaters, chillers, humidifiers, and dehumidifiers
        boolean OFF = false;			// Used to turn off heaters, chillers, humidifiers, and dehumidifiers

        if (em != null) {
            // Now we create the ECS status and message panel
            // Note that we set up two indicators that are initially yellow. This is
            // because we do not know if the temperature/humidity is high/low.
            // This panel is placed in the upper left hand corner and the status
            // indicators are placed directly to the right, one on top of the other

            mw = new MessageWindow("SCS Monitoring Console", 0, 0);
            di = new Indicator("SECURITY UNK", mw.GetX() + mw.Width(), 0);

            mw.WriteMessage("Registered with the message manager.");

            try {
                mw.WriteMessage("   Participant id: " + em.GetMyId());
                mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime());

            } // try
            catch (Exception e) {
                System.out.println("Error:: " + e);

            } // catch

            /**
             * ******************************************************************
             ** Here we start the main simulation loop
             * *******************************************************************
             */
            while (!Done) {
                // Here we get our message queue from the message manager

                try {
                    eq = em.GetMessageQueue();

                } // try
                catch (Exception e) {
                    mw.WriteMessage("Error getting message queue::" + e);

                } // catch

                // If there are messages in the queue, we read through them.
                // We are looking for MessageIDs = 1 or 2. Message IDs of 1 are temperature
                // readings from the temperature sensor; message IDs of 2 are humidity sensor
                // readings. Note that we get all the messages at once... there is a 1
                // second delay between samples,.. so the assumption is that there should
                // only be a message at most. If there are more, it is the last message
                // that will effect the status of the temperature and humidity controllers
                // as it would in reality.
                int qlen = eq.GetSize();

                for (int i = 0; i < qlen; i++) {
                    Msg = eq.GetMessage();

                    if (arm == 1 && Msg.GetMessageId() == 3) // Security (arm disarm) reading
                    {
                        try {
                            int temp = Integer.valueOf(Msg.GetMessage());
                            switch (temp) {
                                case 1:
                                    doorOpen = 1;
                                    mw.WriteMessage("Door Trigger from sensor: ");
                                    break;
                                case 2:
                                    windowBroken = 1;
                                    mw.WriteMessage("window Trigger from sensor: ");
                                    break;
                                case 3:
                                    motionDetected = 1;
                                    mw.WriteMessage("Motion Trigger from sensor: ");
                                    break;
                                default:
                                    doorOpen = 0;
                                    windowBroken = 0;
                                    motionDetected = 0;
                                    mw.WriteMessage("No Trigger from sensor: ");
                            }

                        } // try
                        catch (Exception e) {
                            mw.WriteMessage("Error reading temperature: " + e);

                        } // catch

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

                        // Get rid of the indicators. The message panel is left for the
                        // user to exit so they can see the last message posted.
                        wi.dispose();

                    } // if

                } // for

                Door(doorOpen == 1);
                mw.WriteMessage("Door data to Controller:: " + (doorOpen == 1));
                Window(windowBroken == 1);
                mw.WriteMessage("Window data to Controller:: " + (windowBroken == 1));
                Motion(motionDetected == 1);
                mw.WriteMessage("Motion data to Controller:: " + (motionDetected == 1));

                if (arm == 1) // temperature is below threshhold
                {
                    mw.WriteMessage("System Armed:: ");
                    if (doorOpen == 1 || windowBroken == 1 || motionDetected == 1) {
                        di.SetLampColorAndMessage("Alarm Ringing", 3);
                    } else {
                        di.SetLampColorAndMessage("Alarm Not Ringing", 1);
                    }
                } else {

                    mw.WriteMessage("System Disarmed:: ");
                    di.SetLampColorAndMessage("System Deactivated", 2);

                }

                // This delay slows down the sample rate to Delay milliseconds
                try {
                    Thread.sleep(Delay);

                } // try
                catch (Exception e) {
                    System.out.println("Sleep error:: " + e);

                } // catch

            } // while

        } else {

            System.out.println("Unable to register with the message manager.\n\n");

        } // if

    } // main

    /**
     * *************************************************************************
     * CONCRETE METHOD:: IsRegistered Purpose: This method returns the
     * registered status
     *
     * Arguments: none
     *
     * Returns: boolean true if registered, false if not registered
     *
     * Exceptions: None
     *
     **************************************************************************
     */
    public boolean IsRegistered() {
        return (Registered);

    }

    /**
     * *************************************************************************
     * Arguments: arm 1 or 0
     *
     * Returns: nothing
     *
     * Exceptions: None
     *
     **************************************************************************
     */
    public void setArm(int arm) {
        this.arm = arm;
    }

    /**
     * *************************************************************************
     * CONCRETE METHOD:: Halt Purpose: This method posts an message that stops
     * the environmental control system.
     *
     * Arguments: none
     *
     * Returns: none
     *
     * Exceptions: Posting to message manager exception
     *
     **************************************************************************
     */
    public void Halt() {
        mw.WriteMessage("***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***");

        // Here we create the stop message.
        Message msg;

        msg = new Message((int) 99, "XXX");

        // Here we send the message to the message manager.
        try {
            em.SendMessage(msg);

        } // try
        catch (Exception e) {
            System.out.println("Error sending halt message:: " + e);

        } // catch

    } // Halt

    /**
     * *************************************************************************
     * CONCRETE METHOD:: Heater Purpose: This method posts messages that will
     * signal the temperature controller to turn on/off the heater
     *
     * Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
     * heater on or off.
     *
     * Returns: none
     *
     * Exceptions: Posting to message manager exception
     *
     **************************************************************************
     */
    private void Door(boolean ON) {
        // Here we create the message.

        Message msg;

        if (ON) {
            msg = new Message((int) 6, "D1");

        } else {

            msg = new Message((int) 6, "D0");

        } // if

        // Here we send the message to the message manager.
        try {
            em.SendMessage(msg);

        } // try
        catch (Exception e) {
            System.out.println("Error sending Door control message:: " + e);

        } // catch

    } // Heater

    /**
     * *************************************************************************
     * CONCRETE METHOD:: Chiller Purpose: This method posts messages that will
     * signal the temperature controller to turn on/off the chiller
     *
     * Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
     * chiller on or off.
     *
     * Returns: none
     *
     * Exceptions: Posting to message manager exception
     *
     **************************************************************************
     */
    private void Window(boolean ON) {
        // Here we create the message.

        Message msg;

        if (ON) {
            msg = new Message((int) 6, "W1");

        } else {

            msg = new Message((int) 6, "W0");

        } // if

        // Here we send the message to the message manager.
        try {
            em.SendMessage(msg);

        } // try
        catch (Exception e) {
            System.out.println("Error sending Window control message:: " + e);

        } // catch

    } // Chiller

    /**
     * *************************************************************************
     * CONCRETE METHOD:: Chiller Purpose: This method posts messages that will
     * signal the temperature controller to turn on/off the chiller
     *
     * Arguments: boolean ON(true)/OFF(false) - indicates whether to turn the
     * chiller on or off.
     *
     * Returns: none
     *
     * Exceptions: Posting to message manager exception
     *
     **************************************************************************
     */
    private void Motion(boolean ON) {
        // Here we create the message.

        Message msg;

        if (ON) {
            msg = new Message((int) 6, "M1");

        } else {

            msg = new Message((int) 6, "M0");

        } // if

        // Here we send the message to the message manager.
        try {
            em.SendMessage(msg);

        } // try
        catch (Exception e) {
            System.out.println("Error sending Motion control message:: " + e);

        } // catch

    }

} // SCSMonitor
