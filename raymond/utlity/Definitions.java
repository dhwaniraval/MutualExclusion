package raymond.utlity;

/**
 * @author Dhwani Raval, Vincy Shrine
 * 
 *         Definitions class stores all the information obtained from the
 *         configuration file
 * 
 */
public class Definitions {

	public static final int COMPUTE_Q_SIZE = 100;

	public static final int MAX_POOL_SIZE = 10;
	public static String COORDINATOR_HOSTNAME;
	public static boolean AM_I_COORDINATOR = Boolean.FALSE;
	public static int PID;

	public static int MAX_PROCESS = 100;
	public static int COORD_PORT = 9001;

	public static long T1 = 1000;
	public static long T2 = 5000;
	public static long T3 = 6000;

	public static AlgorithmType ALGORITHM;

	// Cs Request count to be changed
	public static final int MIN_CS_REQ_COUNT = 20;
	public static final int MAX_CS_REQ_COUNT = 20;

}
