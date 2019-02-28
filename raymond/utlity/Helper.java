package raymond.utlity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import raymond.packets.Node;
import raymond.packets.Packet;
import raymond.packets.PacketType;
import raymond.process.Coordinator;
import raymond.process.MeNode;

/**
 * @author Dhwani Raval, Vincy Shrine
 * 
 *         A class that contains the helper methods for the project
 */
public class Helper {

	static Random rand = new Random();
	SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US);

	/**
	 * This method reads the ds config file and stores the information
	 * 
	 * @param configFile The configuration file
	 * @param argsCoord  The information about the process being a coordinator
	 *                   process or normal process
	 * 
	 * @return void
	 */
	public static void readConfig(File configFile, boolean argsCoord) throws UnknownHostException {
		String thisHostName = InetAddress.getLocalHost().getCanonicalHostName();

		try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
			String line = br.readLine();
			String coordHostName = line.substring(line.lastIndexOf(" ") + 1);

			Definitions.COORDINATOR_HOSTNAME = coordHostName;
			line = br.readLine();

			Definitions.MAX_PROCESS = Integer.parseInt(line.substring(line.lastIndexOf(" ") + 1));
			line = br.readLine();

			Definitions.T1 = Long.parseLong(line.substring(line.lastIndexOf(" ") + 1));
			line = br.readLine();

			Definitions.T2 = Long.parseLong(line.substring(line.lastIndexOf(" ") + 1));
			line = br.readLine();

			Definitions.T3 = Long.parseLong(line.substring(line.lastIndexOf(" ") + 1));

			// Double check that cmdline arg is -c and hostname is coordin's
			if (argsCoord)
				Definitions.AM_I_COORDINATOR = true;

			MeNode node = MeNode.getInstance();
			node.setHostName(thisHostName);

			if (Definitions.AM_I_COORDINATOR == true) {
				node.setProcessId(1);
				System.out.println("Process " + thisHostName + ": I AM THE COORDINATOR");

				if (("PARENT LIST").equals(br.readLine())) {
					Map<Integer, Integer> childParentMap = new HashMap<>();
					for (; (line = br.readLine()) != null;) {
						String lineValues[] = line.trim().split(" ");

						if (lineValues.length > 2) {
							// A child has more than one parents
						} else {
							Integer key = Integer.parseInt(lineValues[0]);
							int parentId = Integer.parseInt(lineValues[1]);
							childParentMap.put(key, parentId);
						}
					}
					Coordinator.getInstance().setConfigMap(childParentMap);
					Coordinator.getInstance().setHostName(thisHostName);
					System.out.println("Child=Parent map: " + childParentMap);
				}
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
		}
	}

	/**
	 * A method to generate random integer
	 * 
	 * @param min The minimum range of the integer
	 * @param max The maximum range of the integer
	 * 
	 * @return Integer The random value
	 */
	public static Integer getNextRandom(int min, int max) {
		return rand.nextInt(max) + min;
	}

	/**
	 * A method to send a TCP message
	 * 
	 * @param msg          The message to be sent
	 * @param clientSocket The socket from which the message is sent
	 * 
	 * @param Packet       The packet type that is supposed to be sent
	 */
	public Packet sendTcpMessage(Object msg, Socket clientSocket, String logPrefix) {
		try {
			clientSocket.setReuseAddress(true);
			System.out.println(df.format(new Date()) + logPrefix);

			OutputStream os = clientSocket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			InputStream is = clientSocket.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			oos.writeObject(msg);
			clientSocket.shutdownOutput();
			oos.flush();

			Object readObject = ois.readObject();

			if (readObject instanceof Packet) {
				oos.close();
				ois.close();
				clientSocket.close();
				return (Packet) readObject;
			}

			oos.close();
			ois.close();
			clientSocket.close();
		} catch (ClassNotFoundException e) {
			System.out.println(df.format(new Date()) + logPrefix + "Error: " + e.getMessage());
		} catch (IOException e) {
			System.out.println(df.format(new Date()) + logPrefix + "Error: " + e.getMessage());
		}
		return null;
	}

	/**
	 * A method to send a TCP message
	 * 
	 * @param msg The message to be sent
	 * @param pid The process id to which the message will be sent
	 * 
	 * @param     void
	 */
	public void sendTcpMessage(Object msg, String logPrefix, int pid) {
		MeNode process = MeNode.getInstance();
		Map<Integer, ObjectOutputStream> sockets = process.getSocketStreamMap();
		ObjectOutputStream oos = sockets.get(pid);
		Socket clientSocket;

		if (oos == null) {
			try {
				Node neighbor = process.getParent();
				clientSocket = new Socket(neighbor.getHostName(), neighbor.getPortNo());
				oos = new ObjectOutputStream(clientSocket.getOutputStream());
				sockets.put(pid, oos);
				oos.writeObject(msg);
				oos.writeObject(msg);

			} catch (IOException e) {
				System.out.println(df.format(new Date()) + logPrefix + "Error: " + e.getMessage());
			}
		}

	}

	/**
	 * A method to send the packet to the coordinator
	 * 
	 * @param type   The type of the Packet
	 * @param Packet The packet that has to be sent
	 * 
	 * @return void
	 */
	public void sendPacketToCoord(String logPrefix, PacketType type, Packet packet) {
		Socket coordSocket = null;
		try {
			coordSocket = new Socket(Definitions.COORDINATOR_HOSTNAME, Definitions.COORD_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (packet != null)
			(new Helper()).sendTcpMessage(packet, coordSocket, logPrefix + "Send " + type + " to CO-ORD");
	}

	/**
	 * A method to send the packets to all the process
	 * 
	 * @param type   The type of the Packet
	 * @param Packet The packet that has to be sent
	 * 
	 * @return void
	 */
	public void sendPacketToAllProcess(String logPrefix, PacketType type, Packet packet) {
		MeNode process = MeNode.getInstance();
		Map<Integer, ObjectOutputStream> sockets = process.getSocketStreamMap();

		for (Entry<Integer, Node> entry : process.getProcessInformation().entrySet()) {
			Node child = entry.getValue();
			Integer id = entry.getKey();
			Socket clientSocket;

			if (id == process.getId())
				continue;
			try {
				ObjectOutputStream oos = sockets.get(child.getProcessId());

				if (oos == null) {
					try {
						clientSocket = new Socket(child.getHostName(), child.getPortNo());
						oos = new ObjectOutputStream(clientSocket.getOutputStream());
						sockets.put(id, oos);
					} catch (IOException e) {
						System.out.println(e.getMessage());
						continue;
					}
				}

				switch (type) {
				case ME_START:
					System.out.println(df.format(new Date()) + logPrefix + "Send " + type + " to => " + id);
					oos.writeObject(packet);
					oos.flush();
					break;
				case TERMINATE:
					System.out.println(df.format(new Date()) + logPrefix + "Send " + type + " to => " + id);
					oos.writeObject(packet);
					oos.flush();
					break;
				case CS_REQ:
					System.out.println(df.format(new Date()) + logPrefix + "Send " + type + " to => " + id);
					oos.writeObject(packet);
					oos.flush();
					break;
				default:
					break;
				}
			} catch (IOException e) {
				System.out.println(df.format(new Date()) + logPrefix + "Error: Send msg => " + entry.getKey() + ": "
						+ child.getHostName() + child.getPortNo() + e.getMessage());
			}
		}
	}

	/**
	 * A method to send packet to parent or child process of a particular process
	 * 
	 * @param type   The type of Packet sent
	 * @param Packet token The packet to be sent for mutual exclusion
	 * @param id     The process id
	 * 
	 * @return void
	 */
	public void sendPacketToParentOrChildProcess(String logPrefix, PacketType type, Packet token, int id) {

		MeNode process = MeNode.getInstance();
		Node parentOrChildNode = process.getProcessInformation().get(id);

		Map<Integer, ObjectOutputStream> sockets = process.getSocketStreamMap();
		ObjectOutputStream oos = sockets.get(parentOrChildNode.getProcessId());
		Socket clientSocket;
		try {

			if (oos == null) {
				clientSocket = new Socket(parentOrChildNode.getHostName(), parentOrChildNode.getPortNo());
				oos = new ObjectOutputStream(clientSocket.getOutputStream());
				sockets.put(id, oos);
			}

			System.out.println(df.format(new Date()) + logPrefix + "Send " + type + " to => " + id);
			oos.writeObject(token);
			oos.flush();
		} catch (IOException e) {
			System.out.println(df.format(new Date()) + logPrefix + "Error: Send msg => " + id + ": "
					+ parentOrChildNode.getHostName() + parentOrChildNode.getPortNo() + e.getMessage());
		}
	}
}
