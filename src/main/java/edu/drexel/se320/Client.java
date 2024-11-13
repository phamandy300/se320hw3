package edu.drexel.se320;

import java.io.IOException;
import java.lang.IllegalArgumentException;
import java.lang.StringBuilder;
import java.lang.UnsupportedOperationException;
import java.nio.channels.NotYetConnectedException;

public class Client {

    private String lastResult;
    StringBuilder sb = null;
    private final ServerConnection conn;

    public Client(ServerConnection conn) {
        this.conn = conn;
        lastResult = "";
    }

    public Client() {
        this(new ServerConnection() {
            private boolean connected = false;
            private boolean continueReading = false;

            public boolean connectTo(String address) throws IOException {
                connected = true;
                return address.equalsIgnoreCase("CORRECT_ADDRESS");
            }

            public boolean requestFileContents(String filename) throws IOException {
                if (connected) return filename.equalsIgnoreCase("CORRECT_FILE");
                else throw new NotYetConnectedException();
            }

            public String read() throws IOException {
                if (connected && continueReading) {
                    return null;
                }
                return null;
            }

            public boolean moreBytes() throws IOException {
                if (connected) {
                    continueReading = true;
                    return false;
                }
                return false;
            }

            public void closeConnection() throws IOException {
                if (connected) {
                    connected = false;
                } else {
                    throw new NotYetConnectedException();
                }
            }
        });
    }

    public String requestFile(String server, String file) {
        if (server == null)
            throw new IllegalArgumentException("Null server address");
        if (file == null)
            throw new IllegalArgumentException("Null file");

	// We'll use a StringBuilder to construct large strings more efficiently
	// than repeated linear calls to string concatenation.
        sb = new StringBuilder();

        try {
            if (conn.connectTo(server)) {
                boolean validFile = conn.requestFileContents(file);
                if (validFile) {
                    while (conn.moreBytes()) {
                        String tmp = conn.read();
                        if (tmp != null) {
                            sb.append(tmp);
                        }
                    }
                    conn.closeConnection();
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }

        String result = sb.toString();
        lastResult = result;
        return result;
    }
}

