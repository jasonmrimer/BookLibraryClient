/* LibraryConstants allow LibraryServer and
 * LibraryClient to communicate via integer
 * while maintaining a readable program
 * Designed by Jason Rimer, December 15, 2013.
 */
package project4;

public interface LibraryConstants {
	public static final int RENT = 1;
	public static final int RETURN = 2;
	public static final int RENT_SUCCESS = 3;
	public static final int RETURN_SUCCESS = 4;
	public static final int HOLD = 5;
	public static final int CANCEL = 6;
	public static final int RENT_ERROR = 7;
	public static final int RETURN_ERROR = 8;
	public static final int WAITING = 9;
	public static final int RENT_LIMIT = 10;
	public static final int RETURN_LIMIT = 11;
	public static final int LIBRARY_ERROR = 99;
}
