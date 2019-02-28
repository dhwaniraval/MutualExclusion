package raymond.handlerlistener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import raymond.process.MeNode;
import raymond.utlity.Definitions;

/**
 * @author Dhwani Raval, Vincy Shrine
 *
 */
public class ServerListener implements Runnable {

	private ExecutorService executor = Executors.newFixedThreadPool(Definitions.MAX_POOL_SIZE);
	private int portNo;
	SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US);

	public ServerListener(int portNo) {
		this.portNo = portNo;
	}

	@Override
	public void run() {

		MeNode process = MeNode.getInstance();

		String logPrefix = " Process " + process.getId() + ": ";
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(portNo);
			serverSocket.setReuseAddress(true);
		} catch (IOException e) {
			System.out.println(df.format(new Date()) + logPrefix + "Error: Create socket: " + e.getMessage() + portNo);
		}
		System.out.println(df.format(new Date()) + logPrefix + "Waiting for incoming connections at: " + portNo);
		try {
			while (true) {
				// Listens for a connection and accepts it
				// The method blocks until a connection is made
				Socket incomingSocket = serverSocket.accept();
				executor.execute(new ClientHandler(incomingSocket));
			}
		} catch (IOException e) {
			System.out.println(df.format(new Date()) + logPrefix + "Error: " + e.getMessage() + portNo);
			System.exit(1);
		} finally {
			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}