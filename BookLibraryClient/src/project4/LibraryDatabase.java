/*Library Database by Jason Rimer, 27Nov13
 * This project builds a library of books and authors from a text file.
 * It displays an interface from which the users searches that library
 * and the search returns books/authors matching that search criteria.
 * It also displays a sortable view of authors and books as well as 
 * a tree view of books by genre.
 */
package project4;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.TreeMap;

public class LibraryDatabase implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//variables
	private Book tempBook = new Book();
	private String filePath;
	private ArrayList<String> lines;
	private TreeMap<String, String> genreMap = new TreeMap<String, String>();
	private final TreeMap<Integer, Book> bookMap;
	transient private BufferedReader input;
	private boolean bookExists = false;
	//Constructor
	LibraryDatabase(){
		bookMap = new TreeMap<Integer, Book>();
		filePath = "/Users/Atlas/Documents/Eclipse/workspace/CMSC335/src/Project2/LibraryDatabase";
		lines = new ArrayList<String>();
		buildLibrary();
	}
	LibraryDatabase(String filePath){
		bookMap = new TreeMap<Integer, Book>();
		this.filePath = filePath;
		lines = new ArrayList<String>();
		buildLibrary();
	}
	//accessor methods
	public TreeMap<Integer, Book> getBookMap(){
		return (TreeMap<Integer, Book>) bookMap;
	}
	public TreeMap<String, String> getGenreMap(){
		return (TreeMap<String, String>) genreMap;
	}
	@Override
	public String toString(){
		return "The Library class builds the library from a text file.";
	}
	//methods
	//This method sends each line of text into a corresponding class of Author or Book based on the starting letter A or B.
	//build library by parsing lines and creating Authors & Books
	private void buildLibrary(){
		createStringList();
		String errorReadOut = "";
		TreeMap<Integer, Book> errorBookMap = new TreeMap<Integer, Book>();
		int intKey = 1;
		while (!lines.isEmpty()){
			if (lines.get((lines.size()-1)).startsWith("A")){
				lines.remove((lines.size()-1));	
			}
			else if (lines.get((lines.size()-1)).startsWith("B")){
				tempBook = new Book(lines.get(lines.size()-1));
				for (Integer key : bookMap.keySet()){
					if (bookMap.get(key).equals(tempBook)){
						bookExists  = true;
						bookMap.get(key).increaseStock();
					}
				}
				if (bookMap.containsKey(tempBook.getIndexOfBook()) && !bookExists){
					errorBookMap.put(intKey, tempBook);
					System.out.println("Error: Books contain the same index in the text file. The indices have been corrected.");
				}
				else if (!bookExists){
					bookMap.put(tempBook.getIndexOfBook(), tempBook);
					genreMap.put(tempBook.getGenre(), tempBook.getGenre());
				}
				bookExists = false;
				lines.remove((lines.size()-1));	
			}		
			else {
				errorReadOut.concat(lines.size()-1 + "\n");
				lines.remove((lines.size()-1));	
			}
		}
		if (errorReadOut != "") {
			System.out.print("The following lines are incorrect in the text file and were not included:\n" + errorReadOut);
		}
		//catch any Book with the same index and give the next available index
		while (!errorBookMap.isEmpty()){
			for(Integer checkKey : bookMap.keySet()){
				if (bookMap.higherKey(checkKey) != null && bookMap.higherKey(checkKey) - checkKey > 1){
					errorBookMap.firstEntry().getValue().setIndexOfBook(checkKey + 1);
					bookMap.put(errorBookMap.firstEntry().getValue().getIndexOfBook(), errorBookMap.pollFirstEntry().getValue());
					break;
				}
				else if (bookMap.higherKey(checkKey) == null) {
					errorBookMap.firstEntry().getValue().setIndexOfBook(checkKey + 1);
					bookMap.put(errorBookMap.firstEntry().getValue().getIndexOfBook(), errorBookMap.pollFirstEntry().getValue());
					break;
				}
			}
		}
	}
	//reads lines from text database and adds them to a String ArrayList to be parsed later into Books or Authors
	private void createStringList(){
		try {
			input = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
			while (input.ready()){
				lines.add(input.readLine());
			}
		} 
		catch (EOFException ex){
			System.out.print("End of File exception.");
		}
		catch (FileNotFoundException ex){
			System.out.print("File not found.");
		}
		catch (IOException ex){
			System.out.print("Input/Output exception.");
		}	
	}
}
