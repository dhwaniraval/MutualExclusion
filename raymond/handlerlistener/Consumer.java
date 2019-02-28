package raymond.handlerlistener;

import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

import raymond.packets.CsReqPacket;
import raymond.packets.Packet;
import raymond.packets.PacketType;
import raymond.packets.TokenPacket;
import raymond.process.MeNode;
import raymond.utlity.Helper;

/**
 * @author Vincy Shrine, Dhwani Raval
 * 
 *         The Consumer version of Thread
 */
public class Consumer implements Runnable {

	private MeNode process;
	private Helper helper = new Helper();

	SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US);

	@Override
	public void run() {

		process = MeNode.getInstance();
		// coordinator = Coordinator.getInstance();

		ArrayBlockingQueue<Packet> queue = process.getComputeQ();
		Socket clientSocket = null;

		String logPrefix = " Process " + process.getId() + ": ";
		System.out.println(df.format(new Date()) + logPrefix + "Consumer thread is started...Awaiting packets..");
		try {
			while (true) {

				Packet qPacket = queue.take();

				if (qPacket instanceof CsReqPacket) {
					CsReqPacket packet = (CsReqPacket) qPacket;
					Integer requestedBy = packet.getSenderId();

					System.out.println(df.format(new Date()) + logPrefix + "Received " + packet.getCsRequestString()
							+ "<= from " + requestedBy);

					/* Adds requesting process ID in its request queue. */

					process.getRequestQ().add(requestedBy);
					System.out.println(df.format(new Date()) + logPrefix + "Req_Q = " + process.getRequestQ());

					/*
					 * Store the requests a process serves
					 */

					process.addToCsRequestMap(requestedBy, packet);

					/*
					 * A process which has the token becomes the root node. On receiving request, it
					 * sends the token to its child process which requested the critical section.
					 * After sending the token it will no longer be a root node, hence update its
					 * parent value to the recipient of the token.
					 */

					if (process.isTokenWithMe()) {
						/* This is the Root Node */
						if (!process.isCsExecutionOngoing()) {

							/*
							 * If the process is not executing in its critical section, it sends the token
							 * to the first element of its request queue.
							 */
							System.out.println(df.format(new Date()) + logPrefix + "Before sending TOKEN, Req_Q = "
									+ process.getRequestQ());

							Integer topOfQ = process.getRequestQ().poll();
							if (topOfQ != null) {

								if (topOfQ == process.getId()) {
									/*
									 * The root itself wants to enter in the critical section and has made a request
									 * prior to its child.
									 */
									process.getTokenQ().put(process.getToken());

									System.out.println(df.format(new Date()) + logPrefix + "Request: "
											+ packet.getCsRequestString() + " Messages: "
											+ process.fetchCsRequestPacketFromMap(process.getId()).getMessageCount());

									/*
									 * The process received the token, hence store it in successful completion map
									 * and remove from the serving map
									 */
									process.addToSuccessfulCsRequestMap(packet.getCsRequestString(),
											process.fetchCsRequestPacketFromMap(process.getId()).getMessageCount());
									process.removeFromCsRequestMap(process.getId());
								} else {

									/*
									 * Send the TOKEN to the first element in its request queue.
									 */

									if (process.getRequestQ().size() > 0) {
										/*
										 * The process's request queue has other processes which wants to enter in its
										 * critical section.
										 */

										/*
										 * Send the information about next csRequest packet in token
										 */

										process.getToken().setSenderToBeAddedToQ(true);
										process.incrementMessageCount(process.getRequestQ().peek());
										process.getToken().setNextCsRequest(
												process.fetchCsRequestPacketFromMap(process.getRequestQ().peek()));
									}

									/*
									 * Store the csReq info in token
									 */
									process.getToken().setSenderId(process.getId());
									process.getToken().setCsRequest(process.fetchCsRequestPacketFromMap(topOfQ));

									helper.sendPacketToParentOrChildProcess(logPrefix, PacketType.TOKEN,
											process.getToken(), topOfQ);

									System.out.println(df.format(new Date()) + logPrefix
											+ "After sending TOKEN, Req_Q <= " + process.getRequestQ());

									/*
									 * After sending the token, the process is no longer the root node. Its parent
									 * would be the one to which the token is being sent.
									 */
									process.setParent(process.getProcessInformation().get(topOfQ));
									process.setTokenWithMe(false);
									process.setToken(null);
								}

							}

						}
					} else if (process.getRequestQ().size() == 1) {

						/*
						 * The process does not have a token and either this process or any of its child
						 * has made the first request to enter in the critical section. It sends a
						 * CS_REQUEST to its parent process only ONCE.
						 */

						packet.setMessageCount(packet.getMessageCount() + 1);
						packet.setSenderId(process.getId());

						helper.sendPacketToParentOrChildProcess(logPrefix, PacketType.CS_REQ, packet,
								process.getParent().getProcessId());
					}
				} else if (qPacket instanceof TokenPacket) {

					TokenPacket token = (TokenPacket) qPacket;
					System.out.println(
							df.format(new Date()) + logPrefix + "Received TOKEN <= from " + token.getSenderId());

					process.setTokenWithMe(true);
					process.setParent(null);

					/*
					 * Store information about the token for csReq and message count
					 */

					String csRequestString = token.getCsRequest().getCsRequestString();
					int messageCount = token.getCsRequest().getMessageCount();

					/*
					 * The previous root from which the token is obtained has a non-empty request
					 * queue, hence the sender ID makes a request for the token
					 */

					if (token.isSenderToBeAddedToQ()) {
						process.getRequestQ().add(token.getSenderId());

						/*
						 * Send info about next csReq
						 */
						process.addToCsRequestMap(token.getSenderId(), token.getNextCsRequest());
						token.setSenderToBeAddedToQ(false);
					}

					System.out.println(df.format(new Date()) + logPrefix + "After receiving TOKEN, req_Q = "
							+ process.getRequestQ());

					/*
					 * The first element in the request queue will enter inside the critical section
					 */
					Integer topOfQ = process.getRequestQ().poll();

					if (topOfQ != null) {
						// String[] processId =
						// token.getCsRequest().getCsRequestString().split("C");

						if (topOfQ == process.getId()) {
							System.out.println(df.format(new Date()) + logPrefix + csRequestString
									+ " received TOKEN in " + messageCount + " messages");

							process.addToSuccessfulCsRequestMap(csRequestString, messageCount);
							process.getTokenQ().put(token);
							process.removeFromCsRequestMap(topOfQ);

						} else {
							CsReqPacket cs = process.fetchCsRequestPacketFromMap(topOfQ);
							cs.setMessageCount(messageCount);

							token.setCsRequest(cs);

							if (process.getRequestQ().size() > 0) {
								/*
								 * The process's request queue has other processes which wants to enter in its
								 * critical section.
								 */
								token.setSenderToBeAddedToQ(true);
								process.incrementMessageCount(process.getRequestQ().peek());
								token.setNextCsRequest(
										process.fetchCsRequestPacketFromMap(process.getRequestQ().peek()));
							}

							token.setSenderId(process.getId());

							helper.sendPacketToParentOrChildProcess(logPrefix, PacketType.TOKEN, token, topOfQ);
							process.removeFromCsRequestMap(topOfQ);

							System.out.println(df.format(new Date()) + logPrefix + "After sending TOKEN,Req_Q = "
									+ process.getRequestQ());

							process.setParent(process.getProcessInformation().get(topOfQ));
							process.setTokenWithMe(false);
							process.setToken(null);
						}
					}
				}
			}
		} catch (NumberFormatException e1) {
			System.out.println(df.format(new Date()) + logPrefix + "NF error: " + e1.getMessage());
		} catch (InterruptedException e1) {
			System.out.println(df.format(new Date()) + logPrefix + "Sleep interrupted " + e1.getMessage());
		} finally {
			if (clientSocket != null)
				try {
					clientSocket.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			process.getConsumerQThread().shutdownNow();
			System.out.println(df.format(new Date()) + logPrefix + "Going to die...");
			System.exit(0);
		}
	}
}
