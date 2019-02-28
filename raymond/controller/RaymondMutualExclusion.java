package raymond.controller;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import raymond.handlerlistener.Consumer;
import raymond.handlerlistener.ServerListener;
import raymond.packets.Packet;
import raymond.packets.PacketType;
import raymond.packets.RegisterPacket;
import raymond.process.MeNode;
import raymond.utlity.Definitions;
import raymond.utlity.Helper;

/**
 * RaymondMutualExclusion contains the main method to start the project
 * 
 * @author Dhwani Raval, Vincy Shrine
 *
 */
public class RaymondMutualExclusion {

	public static void main(String[] args) {

		// Process cmd line args, config file
		processInputs(args);

		MeNode process = MeNode.getInstance();

		if (Definitions.AM_I_COORDINATOR) {
			Definitions.PID = 1;
			Thread tcpReceiverThread = new Thread(new ServerListener(Definitions.COORD_PORT));
			tcpReceiverThread.start();
		} else {

			// 1. Send REGISTER msg to COORDINATOR
			Socket coordSocket = null;
			try {
				coordSocket = new Socket(Definitions.COORDINATOR_HOSTNAME, Definitions.COORD_PORT);
			} catch (IOException e) {
				System.err.println("REGISTRATION error: " + e.getMessage());
				System.exit(0);
			}

			Packet packet = (new Helper()).sendTcpMessage(new RegisterPacket(), coordSocket, PacketType.REGISTER + " ");
			if (packet instanceof RegisterPacket) {
				RegisterPacket registerPacket = (RegisterPacket) packet;
				process.setProcessId(registerPacket.getProcessId());
				Definitions.PID = registerPacket.getProcessId();
				process.setPortNo(registerPacket.getPortNo());

				System.out.println("REGISTRATION complete. Obtained processID: " + process.getId() + ", portNo: "
						+ process.getPortNo());
			}

			// 2. Start Listening to neighbors on port
			Thread tcpReceiverThread = new Thread(new ServerListener(process.getPortNo()));
			tcpReceiverThread.start();

		}

		// Consumer thread to serve COMPUTE packets
		process.getConsumerQThread().submit(new Consumer());

	}

	/**
	 * Process cmd line args and config file
	 * 
	 * @param args The Command line arguments
	 * 
	 * @return void
	 */
	private static void processInputs(String[] args) {
		boolean argsCoord = false;
		if (args.length > 0 && ("-c").equals(args[0]))
			argsCoord = true;

		File configFile = new File("dsConfig");

		if (!configFile.exists()) {
			System.err.println("Config file \"dsConfig\" does not exist");
			System.exit(0);
		}

		try {
			Helper.readConfig(configFile, argsCoord);
		} catch (UnknownHostException e) {
			System.err.println("Config file error: " + e.getMessage());
			System.exit(0);
		}
	}
}
