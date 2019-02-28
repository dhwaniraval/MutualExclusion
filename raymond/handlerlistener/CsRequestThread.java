package raymond.handlerlistener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

import raymond.packets.CsReqPacket;
import raymond.packets.MeCompletePacket;
import raymond.packets.PacketType;
import raymond.packets.TokenPacket;
import raymond.process.MeNode;
import raymond.utlity.Definitions;
import raymond.utlity.Helper;

/**
 * @author Vincy Shrine, Dhwani Raval
 *
 */
public class CsRequestThread extends Thread {

	private String logPrefix;
	private TokenPacket token = null;
	private Helper helper = new Helper();

	SimpleDateFormat df = new SimpleDateFormat("hh:mm:ss.SSS", Locale.US);

	public CsRequestThread(String log) {
		this.logPrefix = log;
	}

	@Override
	public void run() {
		/* A process starts executing its critical section. */

		MeNode process = MeNode.getInstance();
		int pid = process.getId();
		int maxRequests = ThreadLocalRandom.current().nextInt(Definitions.MIN_CS_REQ_COUNT,
				Definitions.MAX_CS_REQ_COUNT + 1);

		System.out.println(df.format(new Date()) + logPrefix + "Going to make #" + maxRequests + " CS requests");

		for (int i = 0; i < maxRequests; i++) {

			long sleepTime = ThreadLocalRandom.current().nextLong(Definitions.T1, Definitions.T2);
			try {
				System.out.println(df.format(new Date()) + logPrefix + "Going to sleep : " + sleepTime / 1000
						+ " secs .. before executing Req#" + (i + 1));
				Thread.sleep(sleepTime);
				System.out.println(df.format(new Date()) + logPrefix + "Woke up after : " + sleepTime / 1000
						+ " secs .. before executing Req#" + (i + 1));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/*
			 * Store information about csRequest in packet
			 */
			CsReqPacket csRequest = new CsReqPacket();
			csRequest.setSenderId(process.getId());
			csRequest.setCsRequestString(pid + PacketType.CS_REQ.toString() + (i + 1));

			System.out.println();
			System.out
					.println(df.format(new Date()) + logPrefix + "New CS Request : " + csRequest.getCsRequestString());
			System.out.println();

			/*
			 * Process Pi wishing to enter in critical section sends REQUEST to its parent
			 * indicated by holder provided: Pi does not hold the token, and Pi’s request_q
			 * is empty.
			 */
			if (!process.isTokenWithMe() && process.getRequestQ().isEmpty()) {
				/* Send CS_REQUEST to its parent */

				csRequest.setMessageCount(csRequest.getMessageCount() + 1);
				helper.sendPacketToParentOrChildProcess(logPrefix + (i + 1), PacketType.CS_REQ, csRequest,
						process.getParent().getProcessId());
			}

			/* Adds its process ID in its request queue. */
			process.getRequestQ().add(pid);

			/*
			 * Store the requests a process serves
			 */
			process.addToCsRequestMap(pid, csRequest);

			System.out.println(df.format(new Date()) + logPrefix + "Req_Q = " + process.getRequestQ());

			if (process.isTokenWithMe() && process.getRequestQ().peek() == pid) {
				/*
				 * The process has the token and is the first element in the queue. Hence it
				 * will enter in its critical section.
				 */
				process.getRequestQ().poll();
				token = process.getToken();

				/*
				 * The process received the token, hence store it in successful completion map
				 * and remove from the serving map
				 */

				System.out.println();
				System.out.println(
						df.format(new Date()) + logPrefix + csRequest.getCsRequestString() + " received TOKEN in "
								+ process.fetchCsRequestPacketFromMap(pid).getMessageCount() + " messages");

				process.addToSuccessfulCsRequestMap(csRequest.getCsRequestString(),
						process.fetchCsRequestPacketFromMap(pid).getMessageCount());
				process.removeFromCsRequestMap(pid);
			} else {
				System.out.println(df.format(new Date()) + logPrefix + "Waiting for TOKEN...");

				/* Wait for the token. */
				try {
					token = MeNode.getInstance().getTokenQ().take();
					MeNode.getInstance().setToken(token);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			/* On receiving Token, start executing the critical section */
			process.setCsExecutionOngoing(true);
			System.out.println(df.format(new Date()) + logPrefix + "I have TOKEN");

			try {
				System.out.println(df.format(new Date()) + logPrefix + "Entering CS... for Req#" + (i + 1));
				Thread.sleep(Definitions.T3);
				System.out.println(df.format(new Date()) + logPrefix + "Exiting CS...  for Req#" + (i + 1));
			} catch (InterruptedException e) {
				System.err.println(df.format(new Date()) + logPrefix + "CS sleep interrupted: for Req#" + (i + 1)
						+ e.getMessage());
			}
			process.setCsExecutionOngoing(false);
			/* End of CS execution */

			System.out.println(df.format(new Date()) + logPrefix + "After CS Exit, Req_Q = " + process.getRequestQ());

			Integer topOfQ = process.getRequestQ().poll();

			if (topOfQ != null) {

				/*
				 * Send the TOKEN to the first element in its request queue.
				 */

				token.setCsRequest(process.fetchCsRequestPacketFromMap(topOfQ));

				System.out.println();
				System.out.println(df.format(new Date()) + logPrefix + "Next CS Req is "
						+ token.getCsRequest().getCsRequestString());
				System.out.println();

				if (process.getRequestQ().size() > 0) {

					/*
					 * The process's request queue has other processes which wants to enter in its
					 * critical section.
					 */
					System.out.println(df.format(new Date()) + logPrefix + "Req_Q is Non empty");
					token.setSenderToBeAddedToQ(true);

					process.incrementMessageCount(process.getRequestQ().peek());
					token.setNextCsRequest(process.fetchCsRequestPacketFromMap(process.getRequestQ().peek()));
				}

				token.setSenderId(pid);
				helper.sendPacketToParentOrChildProcess(logPrefix, PacketType.TOKEN, token, topOfQ);
				System.out.println(
						df.format(new Date()) + logPrefix + "After sending TOKEN, Req_Q <= " + process.getRequestQ());

				/*
				 * After sending the token, the process is no longer the root node. Its parent
				 * would be the one to which the token is being sent.
				 */

				process.removeFromCsRequestMap(topOfQ);
				process.setParent(process.getProcessInformation().get(topOfQ));
				process.setTokenWithMe(false);
				process.setToken(null);
			}
		}

		/*
		 * After x CS_Requests are made in between [20-40], send ME_COMPLETE to
		 * Coordinator
		 */

		System.out.println();
		System.out.println(df.format(new Date()) + logPrefix + "Request --> Message Count");

		for (Entry<String, Integer> entry : process.getSuccessfulCsRequestMap().entrySet()) {
			System.out.println(df.format(new Date()) + logPrefix + entry.getKey() + " --> " + entry.getValue());
		}
		System.out.println();
		helper.sendPacketToCoord(logPrefix, PacketType.ME_COMPLETE, new MeCompletePacket());

	}
}
