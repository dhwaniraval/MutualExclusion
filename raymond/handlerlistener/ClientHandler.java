package raymond.handlerlistener;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import raymond.packets.CsReqPacket;
import raymond.packets.MeCompletePacket;
import raymond.packets.MeStartPacket;
import raymond.packets.PacketType;
import raymond.packets.ParentPacket;
import raymond.packets.ReadyPacket;
import raymond.packets.RegisterPacket;
import raymond.packets.TerminatePacket;
import raymond.packets.TokenPacket;
import raymond.process.Coordinator;
import raymond.process.MeNode;
import raymond.utlity.Definitions;
import raymond.utlity.Helper;

/**
 * ClientHandler class receives the messages from other processes and performs
 * the necessary actions. The class manages the following types of message
 * packets: <br>
 * 1. Register_Packet <br>
 * 2. Parent_Packet <br>
 * 3. Ready_Packet <br>
 * 4. Me_Start_Packet <br>
 * 5. Cs_Req_Packet <br>
 * 6. Token_Packet <br>
 * 7. Me_Complete_Packet <br>
 * 8. Terminate_Packet
 * 
 * @author Vincy Shrine, Dhwani Raval
 *
 */
public class ClientHandler implements Runnable {

	MeNode process;
	String logPrefix;
	Helper helper;
	Coordinator coordinator;

	private Socket socket;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	static private int connectionCounter;

	SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US);

	/**
	 * A public constructor that initializes the values of the sockets required for
	 * establishing connections
	 */
	public ClientHandler(Socket socket) throws IOException {
		this.socket = socket;
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
		coordinator = Coordinator.getInstance();
		process = MeNode.getInstance();
		helper = new Helper();
		connectionCounter++;
	}

	@Override
	public void run() {
		try {
			logPrefix = " Process " + process.getId() + ": ";

			while (true) {
				Object readObject = ois.readObject();

				if (readObject != null) {

					if (readObject instanceof RegisterPacket && Definitions.AM_I_COORDINATOR) {

						/*
						 * Coordinator receives REGISTER message from all process
						 */
						String hostName = socket.getInetAddress().getCanonicalHostName();
						System.out.println(df.format(new Date()) + logPrefix + "Received REGISTER <= from " + hostName);
						RegisterPacket packet = coordinator.addToRegisteredNodes(hostName, socket.getPort());
						oos.writeObject(packet);
						disconnect();

						if (coordinator.isRegistrationComplete()) {

							/*
							 * Sleep for sometime until all other process have started their listeners
							 */
							Thread.sleep(2000);
							coordinator.sendParentInfoToAllProcess(logPrefix);
						}
						break;

					} else if (readObject instanceof ParentPacket) {

						/*
						 * Process receives Parent info and info about all other processes from
						 * Coordinator
						 */
						ParentPacket packet = (ParentPacket) readObject;
						process.setParent(packet.getNode());
						process.setProcessInformation(packet.getAllProcessMap());
						System.out.println(df.format(new Date()) + logPrefix + "Received PARENT from COORDINATOR <= "
								+ packet.getNode().getProcessId());
						System.out.println(df.format(new Date()) + logPrefix + "Sending READY To COORDINATOR");

						/*
						 * Every process on receiving its parent information will send a READY message
						 * to coordinator
						 */
						helper.sendPacketToCoord(logPrefix, PacketType.READY, new ReadyPacket());
						disconnect();
						break;

					} else if (readObject instanceof ReadyPacket && Definitions.AM_I_COORDINATOR) {

						/*
						 * Coordinator received READY messages from all the processes and sends ME_START
						 * message to all the processes
						 */
						ReadyPacket packet = (ReadyPacket) readObject;
						System.out.println(
								df.format(new Date()) + logPrefix + "Received READY <= from " + packet.getSenderId());
						coordinator.addToReadyNodes(packet.getSenderId());
						if (coordinator.isAllProcessReady()) {
							System.out.println(
									df.format(new Date()) + logPrefix + "All processes are ready to begin computation");

							/*
							 * Coordinator sends ME_START message to all its neighbors
							 */

							helper.sendPacketToAllProcess(logPrefix, PacketType.ME_START, new MeStartPacket());

							/*
							 * Coordinator instantiates token object. Coordinator now behaves as a normal
							 * process. By default, the coordinator will be the root node which contains the
							 * token to enter into its critical section
							 */
							process.setToken(new TokenPacket());
							new CsRequestThread(logPrefix).start();
						}
						disconnect();
						break;

					} else if (readObject instanceof MeStartPacket) {

						/*
						 * A process received ME_START message from Coordinator to begin its
						 * computations
						 */

						MeStartPacket packet = (MeStartPacket) readObject;
						System.out.println(df.format(new Date()) + logPrefix + "Received ME_START <= from "
								+ packet.getSenderId());
						new CsRequestThread(logPrefix).start();

					} else if (readObject instanceof CsReqPacket) {

						/*
						 * A process received a CS_REQUEST message from its child process
						 */
						process.getComputeQ().put((CsReqPacket) readObject);

					} else if (readObject instanceof TokenPacket) {
						process.getComputeQ().put((TokenPacket) readObject);
					}

					else if (readObject instanceof MeCompletePacket && Definitions.AM_I_COORDINATOR) {

						/*
						 * Coordinator receives ME_COMPLETE from all the processes.
						 */
						MeCompletePacket packet = (MeCompletePacket) readObject;
						System.out.println(df.format(new Date()) + logPrefix + "Received ME_COMPLETE <= from "
								+ packet.getSenderId());
						coordinator.addToCompleteNodes(packet.getSenderId());
						if (coordinator.isAllProcessComplete()) {
							System.out.println(df.format(new Date()) + logPrefix + "All processes have COMPLETED");

							/*
							 * Coordinator sends TERMINATE message to all the processes.
							 */
							helper.sendPacketToAllProcess(logPrefix, PacketType.TERMINATE, new TerminatePacket());
							disconnect();
							process.getConsumerQThread().shutdownNow();
							System.out.println(df.format(new Date()) + logPrefix + "Terminates...");
							System.exit(0);
						}
					} else if (readObject instanceof TerminatePacket) {

						/*
						 * A process received a TERMINATE message from the Coordinator.
						 */
						System.out.println(df.format(new Date()) + logPrefix + "Received TERMINATE <= from "
								+ ((TerminatePacket) readObject).getSenderId());
						disconnect();

						process.getConsumerQThread().shutdownNow();
						System.out.println(df.format(new Date()) + logPrefix + "Terminates...");
						System.exit(0);
						break;
					}
				}
			}
		} catch (EOFException e) {
			System.out.println(df.format(new Date()) + logPrefix + "Error: IO: " + e.getMessage() + " from: "
					+ socket.getInetAddress().getHostName());
			e.printStackTrace();
		} catch (IOException | InterruptedException e) {
			System.out.println(df.format(new Date()) + logPrefix + "Error: IO: " + e.getMessage() + " from: "
					+ socket.getInetAddress().getHostName());
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.out.println(df.format(new Date()) + logPrefix + "Error: CNF: " + e.getMessage() + " from: "
					+ socket.getInetAddress().getHostName());
			e.printStackTrace();
		} finally {
			System.out.println(df.format(new Date()) + logPrefix + "Successfully Completed Handling: "
					+ socket.getInetAddress().getHostName());
			connectionCounter--;
		}
	}

	/**
	 * A method to close the resources while closing a connection
	 * 
	 * @param <code>Void</code>
	 * @return Void
	 */
	public void disconnect() {
		try {
			if (oos != null) {
				oos.flush();
				oos.close();
			}
			if (ois != null)
				ois.close();
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			System.out.println(df.format(new Date()) + logPrefix + "Error: Socket Close: " + e.getMessage() + " from: "
					+ socket.getInetAddress().getHostName());
		}
	}
}
