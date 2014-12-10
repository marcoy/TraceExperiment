package utils;

import com.sun.btrace.CommandListener;
import com.sun.btrace.client.Client;
import com.sun.btrace.comm.Command;

import java.io.IOException;

public class Creator {
    public static Client createClient(final int port) {
        return new Client(port, ".", true, true, true, false, null);
    }

    public static CommandListener createCmdListener() {
        return new CommandListener() {
            @Override
            public void onCommand(Command command) throws IOException {

            }
        };
    }
}
