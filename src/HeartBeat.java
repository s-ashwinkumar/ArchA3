import InstrumentationPackage.*;
import MessagePackage.*;
import java.util.*;


class HeartBeat{
    static public void SendHeartBeat(MessageManagerInterface ei, String m )
    {
        // Here we create the message.

        Message msg = new Message( (int) -100, m );

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
}