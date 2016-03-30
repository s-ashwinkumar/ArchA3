/******************************************************************************************************************
*
* Description:
*
* This class simulates a device that controls a security system comprising of 3 sensors, window , door and motion detection. It polls the message manager for message ids = 6
* and reacts to them by turning on or off one of the three components. The following command are valid strings for controlling the security sensors:
*
*   D1 = Door break on
*   D0 = Door break off
*   W1 = Window break on
*   W0 = Window break off
*   M1 = motion detector on
*   M0 = motion detector off
*
* The state (on/off) is graphically displayed on the terminal in the indicator. Command messages are displayed in
* the message window. Once a valid command is relieved a confirmation message is sent with the id of -6 and the command in
* the command string.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*   static private void ConfirmMessage(MessageManagerInterface ei, String m )
*
******************************************************************************************************************/
import InstrumentationPackage.*;
import MessagePackage.*;
import java.util.*;

class MaintenanceConsole
{
    public static void main(String args[])
    {
        String MsgMgrIP;                    // Message Manager IP address
        Message Msg = null;                 // Message object
        MessageQueue eq = null;             // Message Queue
        int MsgId = 0;                      // User specified message ID
        MessageManagerInterface em = null;  // Interface object to the message manager
        boolean WindowState = false;        // Window state: false == off, true == on
        boolean DoorState = false;      // Door state: false == off, true == on
        boolean MSensorState = false;       // Motion Sensor state: false == off, true == on
        int Delay = 5000;                   // The loop delay (5 seconds)
        boolean Done = false;               // Loop termination flag
        // Map<Integer, String> Description = new HashMap<Integer, String>();

        // Description.put(1, "ECSConsole");
        // Description.put(2, "ECSConsole");
        // Description.put(-5, "TemperatureSensor");
        // Description.put(-4, "HumiditySensor");
        // Description.put(4, "Humidity Controller");
        // Description.put(5, "Temperature Controller");


        /////////////////////////////////////////////////////////////////////////////////
        // Get the IP address of the message manager
        /////////////////////////////////////////////////////////////////////////////////

        if ( args.length == 0 )
        {
            // message manager is on the local system

            System.out.println("\n\nAttempting to register on the local machine..." );

            try
            {
                // Here we create an message manager interface object. This assumes
                // that the message manager is on the local machine

                em = new MessageManagerInterface();
            }

            catch (Exception e)
            {
                System.out.println("Error instantiating message manager interface: " + e);

            } // catch

        } else {

            // message manager is not on the local system

            MsgMgrIP = args[0];

            System.out.println("\n\nAttempting to register on the machine:: " + MsgMgrIP );

            try
            {
                // Here we create an message manager interface object. This assumes
                // that the message manager is NOT on the local machine

                em = new MessageManagerInterface( MsgMgrIP );
            }

            catch (Exception e)
            {
                System.out.println("Error instantiating message manager interface: " + e);

            } // catch

        } // if

        // Here we check to see if registration worked. If ef is null then the
        // message manager interface was not properly created.

        if (em != null)
        {
            System.out.println("Registered with the message manager." );

            /* Now we create the temperature control status and message panel
            ** We put this panel about 1/3 the way down the terminal, aligned to the left
            ** of the terminal. The status indicators are placed directly under this panel
            */

            // float WinPosX = 0.4f;   //This is the X position of the message window in terms
            //                         //of a percentage of the screen height
            // float WinPosY = 0.0f;   //This is the Y position of the message window in terms
            //                         //of a percentage of the screen height

            // MessageWindow mw = new MessageWindow("service maintenance console", WinPosX, WinPosY);

            // // Put the status indicators under the panel...

            // Indicator di = new Indicator ("Door Not Triggered", mw.GetX(), mw.GetY()+mw.Height());
            // Indicator wi = new Indicator ("Window Not Triggered", mw.GetX()+(di.Width()*2), mw.GetY()+mw.Height());
            //             Indicator mi = new Indicator ("MSensors Not Triggered", mw.GetX()+(wi.Width()*4), mw.GetY()+mw.Height());

            // mw.WriteMessage("Registered with the message manager." );

            // try
            // {
            //     mw.WriteMessage("   Participant id: " + em.GetMyId() );
            //     mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

            // } // try

            // catch (Exception e)
            // {
            //     System.out.println("Error:: " + e);

            // } // catch

            /********************************************************************
            ** Here we start the main simulation loop
            *********************************************************************/

            //overall set
            Set<String> overAllSet = new HashSet<String>();
            while ( !Done )
            {
                System.out.println("-----Check Start-----");

                try
                {
                    eq = em.GetMessageQueue();

                } // try

                catch( Exception e )
                {
                    // mw.WriteMessage("Error getting message queue::" + e );

                } // catch

                // If there are messages in the queue, we read through them.
                // We are looking for MessageIDs = 6, this is a request to turn one of the
                // 3 sensors. Note that we get all the messages
                // at once... there is a 2.5 second delay between samples,.. so
                // the assumption is that there should only be a message at most.
                // If there are more, it is the last message that will effect the
                // output of the temperature as it would in reality.

                int qlen = eq.GetSize();
                Set<String> set = new HashSet<String>();
                int count = 0;
                for ( int i = 0; i < qlen; i++ )
                {

                    Msg = eq.GetMessage();
                    if (Msg.GetMessageId() == -100) {
                        String tempMessage = Msg.GetMessage();
                        if (set.contains(tempMessage))
                            continue;
                        else {
                            count++;
                            String[] description = tempMessage.split("#");
                            System.out.println( " " + String.valueOf(count) + ". " + description[0] + " is on.");
                            System.out.println( "   " +description[1]);
                            set.add(tempMessage);
                        }
                        overAllSet.add(tempMessage);
                    }


                    // If the message ID == 99 then this is a signal that the simulation
                    // is to end. At this point, the loop termination flag is set to
                    // true and this process unregisters from the message manager.

                    if ( Msg.GetMessageId() == 99 )
                    {
                        Done = true;

                        try
                        {
                            em.UnRegister();

                        } // try

                        catch (Exception e)
                        {
                            // mw.WriteMessage("Error unregistering: " + e);

                        } // catch

                        // mw.WriteMessage( "\n\nSimulation Stopped. \n");

                        // Get rid of the indicators. The message panel is left for the
                        // user to exit so they can see the last message posted.

                        // di.dispose();
                        // wi.dispose();
                        // mi.dispose();
                    } // if

                } // for

                //check for the health
                Iterator<String> it = overAllSet.iterator();
                while (it.hasNext()) {
                    String str = it.next();
                    if (set.contains(str))
                        continue;
                    else {
                        String[] description = str.split("#");
                        System.out.println( " * " + description[0] + " is off.");
                        System.out.println( description[1]);
                    }
                }

                System.out.println("-----Check End-----");

                try
                {
                    Thread.sleep( Delay );

                } // try

                catch( Exception e )
                {
                    System.out.println( "Sleep error:: " + e );

                } // catch

            } // while

        } else {

            System.out.println("Unable to register with the message manager.\n\n" );

        } // if

    } // main

    /***************************************************************************
    * CONCRETE METHOD:: ConfirmMessage
    * Purpose: This method posts the specified message to the specified message
    * manager. This method assumes an message ID of -6 which indicates a confirma-
    * tion of a command.
    *
    * Arguments: MessageManagerInterface ei - this is the messagemanger interface
    *            where the message will be posted.
    *
    *            string m - this is the received command.
    *
    * Returns: none
    *
    * Exceptions: None
    *
    ***************************************************************************/

    static private void ConfirmMessage(MessageManagerInterface ei, String m )
    {
        // Here we create the message.

        Message msg = new Message( (int) -6, m );

        // Here we send the message to the message manager.

        try
        {
            ei.SendMessage( msg );

        } // try

        catch (Exception e)
        {
            System.out.println("Error Confirming Message:: " + e);

        } // catch

    } // PostMessage

} // TemperatureController