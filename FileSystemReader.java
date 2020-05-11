import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * FileSystemReader class that handles the more complex File System access requirements.
 * */
class FileSystemReader {
	
	/**
	 * listFolder function that lists the files and folders for a given File object.
	 * We assume that the file object represents a folder.  If it isn't, we throw an error.
	 * It is therefore up to the user of this method to ensure he is checking that the File Object
	 * is in fact representing a folder and not a file.
	 * */
	static ArrayList<String> listFolder(File file){
		
		
		ArrayList<String> result = new ArrayList<String>();
		//Assume that the calling function has already checked that file is a folder
		try {
			System.out.println("Getting the folder listing for: " + file.getCanonicalPath());
			System.out.println(""); //Add some padding to the console output
			
		
			if(!file.getCanonicalPath().equals(MyWebServer.workingDirectory.getCanonicalPath())) {
				result.add("../"); 
				//If we are retrieving the working directory for the web server (we are accessing a sub folder), then return a parent folder link.
			}
			
			for(String fileFolder: file.list()) {
				
					if(new File(file.getCanonicalPath()+"/"+fileFolder).isDirectory()) {
						result.add(fileFolder+"/"); //Append a "/" to the fileFolder String if it represents a folder.
					}
					else {
						result.add(fileFolder);
					}
				
			}
		
			System.out.println("The listing for " + file.getCanonicalPath()+ " is (NOTE that ../ means parent folder):\n" + result);
			System.out.println("");  //Add some padding to the console output
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return result;
		
	}

	
	/**
	 * getFileContent method that takes in a File Object and reads in the file content as a String.
	 * The method then returns the String content to the caller.
	 * */
	static String getFileContent(File file) {
		String content;
		String fullFileContent = "";
		
		try {	
			System.out.println("Getting file content for: " + file.getCanonicalPath());
			System.out.println(""); //Add some padding to the console output
			
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedFileReader = new BufferedReader(fileReader);
			//Read content into temp variable called content and then check if the content that we read in is null.
			while((content = bufferedFileReader.readLine())!=null) {
				//Separate each line of the file content with a new line.  We will follow the Linux standard for new lines.
				fullFileContent+=content+"\n";
			}
			
			//Log file content
			System.out.println("File content for " + file.getCanonicalPath() + " is:\n"+ fullFileContent);
			System.out.println(""); //Add some padding to the console output
			
		bufferedFileReader.close();
		} catch (FileNotFoundException e) {
			//Should not happen as we should have checked that the file existed beforehand but this makes the JVM happy.
		
			e.printStackTrace();
		} catch (IOException e) {
			//In case the file cannot be read for some reason.
			e.printStackTrace();
		}
		
		return fullFileContent;
	}
	
	
	
	
}
