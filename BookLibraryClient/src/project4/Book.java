/* Book holds the information on each book
 * in the LibraryDatabase as well as holds
 * the function to rent/return.
 * Designed by Jason Rimer, December 15, 2013
 */
package project4;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//Book Class
public class Book implements java.io.Serializable, LibraryConstants {
	private static final long serialVersionUID = 1L;
	private Double price;
	private int indexOfAuthor;
	private int indexOfBook;
	private String genre;
	private String title;
	private String line;
	private int currentStock;
	private int maxStock;
	private static Lock lock = new ReentrantLock();
	//Constructors
	public Book(){
		//without a line passed in, all will result in "unknown" or 0
		this.line = "";
		setGenre();
		setIndexOfAuthor();
		setIndexOfBook();
		setPrice();
		setTitle();
		maxStock = 1;
		currentStock = 1;
	}
	public Book(String line){
		this.line = line;
		setGenre();
		setIndexOfAuthor();
		setIndexOfBook();
		setPrice();
		setTitle();
		maxStock = 1;
		currentStock = 1;
	}
//	//accessor methods
//	public String getAuthorName(){
//		String author = new String();
//		//find author name
//		for (Integer j : getAuthorMap().keySet()) {
//			if (this.indexOfAuthor == getAuthorMap().get(j).getIndex()){
//				author = getAuthorMap().get(j).getName();
//				break;
//			}
//			else author = "unknown";		
//		}	
//		return author;
//	}
	public int getCurrentStock() {
		return currentStock;
	}
	public String getGenre(){
		return genre;
	}
	public int getIndexOfAuthor(){
		return indexOfAuthor;
	}
	public int getIndexOfBook(){
		return indexOfBook;
	}
	public Double getPrice(){
		return price;
	}
	public String getTitle(){
		return title;
	}
	public boolean equals(Object o){
		if (o instanceof Book){
			if (this.getTitle().equalsIgnoreCase(((Book)o).getTitle()) && this.indexOfAuthor == ((Book)o).getIndexOfAuthor()) return true;
			else return false;
		}
		else return false;
	}
	@Override
	public String toString(){
		return ("Book Object; Index: " + this.indexOfBook + "; Title: " + this.title +"; Price: $" + String.format("%.2f", this.price) + "; Genre: " + this.genre + "; Index of Author: " + this.indexOfAuthor); 
	}
	//mutator methods
	//These mutators use specific identifiers to parse the line sent to the constructor and set the variables
	//If no match is present, "unknown" is set
	private void setGenre(){
		if (line.contains(":genre")) {
			genre = line.substring(line.indexOf("(", line.indexOf(":genre")) + 1,line.indexOf("):", line.indexOf(":genre(")));
		}	
		else genre = "unknown";
	}
	private void setIndexOfAuthor(){
		if (line.contains(":index of author")) {
			indexOfAuthor = Integer.parseInt(line.substring(line.indexOf("(", line.indexOf(":index of author")) + 1,line.indexOf("):", line.indexOf(":index of author("))));
		}	
		else indexOfAuthor = 0;
	}
	private void setIndexOfBook(){
		if (line.contains(":index(")) {
			indexOfBook = Integer.parseInt(line.substring(line.indexOf("(", line.indexOf(":index(")) + 1,line.indexOf("):", line.indexOf(":index("))));
		}	
		else indexOfBook = 0;
	}
	public void setIndexOfBook(int index){
		this.indexOfBook = index;
	}
	public void increaseStock(){
		currentStock++;
		maxStock++;
	}
	private void setPrice(){
		if (line.contains(":price")) {
			price = Double.parseDouble(line.substring(line.indexOf("(", line.indexOf(":price")) + 1,line.indexOf("):", line.indexOf(":price("))));
		}	
		else price = 0.00;
	}
	private void setTitle(){
		if (line.contains(":title")) {
			title = line.substring(line.indexOf("(", line.indexOf(":title")) + 1,line.indexOf("):", line.indexOf(":title(")));
		}	
		else title = "unknown";
	}
	public synchronized int rentOrReturnBook(int rentOrReturn){
		try{
			lock.lock();
			Thread.sleep(2000);
			if (rentOrReturn == RENT){
				if (currentStock == 0){
					lock.unlock();
					return RENT_ERROR;
				}
				else{
					currentStock--;
					lock.unlock();
					return RENT_SUCCESS;
				}
			}
			else if (rentOrReturn == RETURN){
				Thread.sleep(500);
				if (currentStock == maxStock){
					lock.unlock();
					return RETURN_ERROR;
				}
				else{
					currentStock++;		
					lock.unlock();
					return RETURN_SUCCESS;
				}
			}
			else {
				lock.unlock();
				return LIBRARY_ERROR;
			}
		} catch (InterruptedException e){
			e.printStackTrace();
			lock.unlock();
			return LIBRARY_ERROR;
		}
		finally{
			lock.unlock();
		}
	}
	public Lock getLock() {
		return lock;
	}
}

