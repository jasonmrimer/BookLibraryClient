/* LibraryClient connects to the LibraryServer 
 * and allows users to view, rent, and return
 * books from the library.
 * Designed by Jason Rimer, December 15, 2013.
 */
package project4;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class LibraryClient extends JApplet implements Runnable, LibraryConstants{
	private boolean isStandAlone = false;
	public boolean waiting;
	private DataInputStream fromServer;
	private DataOutputStream toServer;
	private DefaultTableModel tableModel = new DefaultTableModel();
	private int intReceived = 0;
	private JButton jbCancel = new JButton("Cancel");
	private JButton jbRent = new JButton("Rent");
	private JButton jbReturn = new JButton("Return");
	private JTextArea jtextArea = new JTextArea("");
	private JTable jtLibraryBookTable = new JTable();
	private JPanel jpLowerButtonPanel = new JPanel();
	private LibraryDatabase library;
	private static final long serialVersionUID = 1L;
	private ObjectInputStream objectFromServer;
	private String host = "localhost";
	private ExecutorService executor;
	//main
	public static void main(String args[]){
		JFrame frame = new JFrame("Library Patron");
		//applet instance
		LibraryClient client = new LibraryClient();
		client.isStandAlone = true;
		//get host
		if (args.length == 1) client.host = args[0];
		//Add applet to frame
		frame.add(client, java.awt.BorderLayout.CENTER);
		client.init();
		client.start();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
	//initialize the applet
	@Override
	public void init(){
		//created thread pool
		executor = Executors.newCachedThreadPool();
		//set the text area promting the user to a more visible area
		jtextArea.setBackground(Color.BLACK);
		jtextArea.setForeground(Color.WHITE);
		setLayout(new BorderLayout());
		//add text area
		add(jtextArea, BorderLayout.NORTH);
		//create socket, initialized later
		Socket socket;
		try{
			//display while connecting to inform user of process
			jtextArea.append("Connecting to server...");
			if (isStandAlone)
				socket = new Socket(host,8000);
			else
				socket = new Socket(getCodeBase().getHost(),8000);
			//Create an input stream to receive data from the server
			fromServer = new DataInputStream(socket.getInputStream());
			//Create an output stream to send data to the server
			toServer = new DataOutputStream(socket.getOutputStream());
			//Create object input stream to receive object from the server
			objectFromServer = new ObjectInputStream(socket.getInputStream());
		}
		catch (Exception ex){
			System.err.println(ex);  
		}
		//execute the thread
		executor.execute(new Thread(this));
	}
	//the run method retrieves the library and displays it via Display
	@Override
	public void run(){
		try {
			jtextArea.setText("Connected to server. Choose a book then Rent or Return.");
			//get library from server and create a display
			library = (LibraryDatabase) objectFromServer.readObject();
			//execute display as a thread
			executor.execute(new Thread(new Display()));
		} catch (IOException e) {
			System.err.println(e);
		} catch (ClassNotFoundException e) {
			System.err.println(e);
		} catch (Exception e){
			System.err.println(e);
		}
	}
	//thread to process a cancellation of a request that sends a CANCEL to the server then cleans up the process
	class Cancel implements Runnable{
		@Override
		public void run(){
			try{
				if (intReceived == WAITING){
					toServer.writeInt(CANCEL);
					jtextArea.setText("Cancelled request for " + jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(), 1));
				}
				//catch all of the extra WAITING int while the process resolves; otherwise, a WAITING gets sent to the server undesirably
				while (intReceived == WAITING){
					intReceived = fromServer.readInt();
				}
			}
			catch (IOException e){
				System.out.println("Cancelling");
				e.printStackTrace();
			}
		}
		@Override
		public String toString(){
			return "The Cancel class is trigerred by the Cancel button and stops the Wait loop to cancel a request to the server.";
		}
	}
	//display as its own thread to handle all changes; runs display
	class Display implements Runnable{
		@Override
		public void run(){
			//call sort to create table
			tableModel.addColumn("Index");
			tableModel.addColumn("Title");
			tableModel.addColumn("Genre");
			tableModel.addColumn("Price");
			for(Integer key : library.getBookMap().keySet()){
				tableModel.addRow(new Object[]{
						library.getBookMap().get(key).getIndexOfBook(),
						library.getBookMap().get(key).getTitle(),
						library.getBookMap().get(key).getGenre(),
						library.getBookMap().get(key).getPrice()   						
				});					
			}
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
			Comparator<Double> myComp = new Comparator<Double>(){
				@Override
				public int compare(Double o1, Double o2) {
					return Double.compare(o1, o2);
				}
			};
			sorter.setComparator(3, myComp);
			jtLibraryBookTable.setModel(tableModel);
			jtLibraryBookTable.setRowSorter(sorter);
			jtLibraryBookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			//add
			jpLowerButtonPanel.setLayout(new GridLayout(1,3));
			//Rent listener executes new rent thread
			jbRent.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					executor.execute(new Thread(new RentAndReturn(RENT)));
				}
			});
			//Return listener executes new return thread
			jbReturn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					executor.execute(new Thread(new RentAndReturn(RETURN)));
				}
			});
			//Cancel listener executes new cancel thread
			jbCancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					executor.execute(new Thread(new Cancel()));
				}
			});
			//add buttons
			jpLowerButtonPanel.add(jbRent);
			jpLowerButtonPanel.add(jbReturn);
			jpLowerButtonPanel.add(jbCancel);
			//add table
			add(new JScrollPane(jtLibraryBookTable), BorderLayout.CENTER);
			add(jpLowerButtonPanel,BorderLayout.SOUTH);
			//ensure display appears
			revalidate();
		}
	}
	//rents or returns books from or to the server and can wait if necessary
	class RentAndReturn implements Runnable{
		private int rentOrReturn;
		public RentAndReturn(int rentOrReturn){
			this.rentOrReturn = rentOrReturn;
		}
		@Override
		public void run(){
			try {
				//inform user action is being taken
				jtextArea.setText("Requesting " + (String) jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(), 1) +
						", please wait.");
				//trigger rent action on server
				toServer.writeInt(rentOrReturn);
				//send rent book index
				toServer.writeInt((int) jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(), 0));
				//get result of rent attempt
				intReceived = fromServer.readInt();
				//if the book is locked by another client, start a waiting loop
				if (intReceived == WAITING){
					jtextArea.setText("Please wait, other patrons are trying to rent this book (you may cancel at any time)");
					executor.execute(new Thread(new WaitForServer()));
				}
				else{
					//determine outcome; inform user
					switch (intReceived) {
						case (RENT_SUCCESS):
							jtextArea.setText((String)
								jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(), 1) + " rented.");
						break;
						case (RENT_ERROR): 	
							jtextArea.setText((String)
								jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(), 1) + " out of stock.");
						break;
						case (RENT_LIMIT):
							jtextArea.setText("Have have reached the checkout limit. Please return books.");
						break;
						case (RETURN_SUCCESS): jtextArea.setText((String)
								jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(),1) + " returned.");
						break;
						case (RETURN_ERROR):	jtextArea.setText((String)
								jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(),1) + " full - no returns.");						
						break;
						case (RETURN_LIMIT):
							jtextArea.setText("You do not have any books to return. Rent first.");
						break;
					}
				}
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		@Override
		public String toString(){
			return "The Rent And Return class allows the client to rent and return books from and to the server.";
		}
	}
	//Waits for server to unlock book and send result - can be cancelled
	class WaitForServer implements Runnable{
		@Override
		public void run(){
			try{
				//loop with server while waiting
				while (intReceived == WAITING){
					intReceived = fromServer.readInt();
					if (intReceived == WAITING)	toServer.writeInt(WAITING);
				}
				//after the wait, if not cancelled, determine outcome
				switch (intReceived) {
					case (ERROR):
						jtextArea.setText("Error, please try again.");
					break;
					case (RENT_SUCCESS):
						jtextArea.setText((String)
							jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(), 1) + " rented.");
					break;
					case (RENT_ERROR): 	
						jtextArea.setText((String)
							jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(), 1) + " out of stock.");
					break;
					case (RENT_LIMIT):
						jtextArea.setText("Have have reached the checkout limit. Please return books.");
					break;
					case (RETURN_SUCCESS): jtextArea.setText((String)
							jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(),1) + " returned.");
					break;
					case (RETURN_ERROR):	jtextArea.setText((String)
							jtLibraryBookTable.getValueAt(jtLibraryBookTable.getSelectedRow(),1) + " full - no returns.");						
					break;
					case (RETURN_LIMIT):
						jtextArea.setText("You do not have any books to return. Rent first.");
					break;
				}
			}
			catch (EOFException e1){
				System.out.print("Disconnected from server");
				e1.printStackTrace();
			}
			catch (IOException e){
				System.out.print("Client in waitForServer");
				e.printStackTrace();
			}
		}
		@Override
		public String toString(){
			return "The Wait class is trigerred when another client locked a book and waits for the lock to be released (may be cancelled by the Cancel class.";
		}
	}
}
