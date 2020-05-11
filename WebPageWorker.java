import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Web Page worker that coordinates request handling on behalf of the web server.
 * */
class WebPageWorker extends Thread {
	//Http codes and status strings are kept in constant arrays in HTTPConstructor class
	//The first index is httpOK.  Second index is for fileNotFound
	//There are probably better implementations than this
	static final int httpOK = 0;  
	static final int fileNotFound = 1;
	Socket sock;
	HashMap<String, String> requestParams; //Request parameter hashmap initialized after parsing the request in run() method
	
	/**
	 * Initialize the web page worker with a Socket and set up a hash map of request parameters for use between helper methods and the run method.
	 * */
	WebPageWorker(Socket s){
		sock = s;
		requestParams = new HashMap<String, String>();
	};
	
	/**
	 * run() method which is the main entry point into the thread responding to user requests after the server accepts a connection
	 * */
	public void run() {
		String response;
		String request = "";
		PrintStream out = null;
		BufferedReader in = null;
		try {
			//Allow reading input from the client through buffered reader
			in = new BufferedReader(
					new InputStreamReader(
							sock.getInputStream()
							)
					);
			//Allow writing responses to the client through a print stream
			out = new PrintStream(sock.getOutputStream());
		}catch(IOException ioe) {}
		String [] startLineParams;
		try {
			String startLine = in.readLine();  //Read the start line of the request
			
			if(startLine == null) {
				/**Noticed weird null pointer exceptions happening with the startLine here
				 * so I am going to just close the socket and return if this happens
				 * Not sure why it happens but I am going to ignore that case.
				 * */
				sock.close();  
				return;
			}
			System.out.println("Start Line is: " + startLine);
			System.out.println("");
			request+=startLine+"\r\n";  //Record that we have seen the start line of the request.
			startLineParams = startLine.split(" ");
			//Assume no spaces in the requestURL.
			requestParams.put("httpRequestType", startLineParams[0]);  //Store request type (GET?  POST?) for minor extensibility in the future
			requestParams.put("requestUrl", startLineParams[1].replace("%20", " ")); //Store the requestUrl and replace %20 with spaces to avoid 404 error on valid files containing spaces.
			requestParams.put("httpVersion", startLineParams[2]); //Store the HTTP version being used by the client.
			
			System.out.println("Request URL is: "+ requestParams.get("requestUrl"));  //Print the request URL
			System.out.println("");
			String line;
			while(request.indexOf("\r\n\r\n") < 0) {
				//Read lines until the request read in contains an empty line in which case we assume the header has been read.
				//If the line read is null, then we can also assume that the header has been read.
				line = in.readLine();
				request+=line+"\r\n";
				if(line == null) {
					break;
				}
				
				
			}
			if(requestParams.get("requestUrl").endsWith("/favicon.ico")) {
				/**
				 * If a favicon is requested, return the 404 page and don't return without printing the request or response
				 * This is intended to keep the serverlog.txt file cleaner
				 * as it would be cluttered with data from favicon requests otherwise
				 * In this case, you only see the header for the favicon.ico request and nothing else.
				 * */
				out.print(handleFileNotFound());
				sock.close();
				return;
				
			}
			System.out.println(request);  //Print the request to the console.
			
			
			if(requestParams.get("requestUrl").startsWith("/cgi/addnums.fake-cgi?")) {
				//If the cgi url is requested, call the handleCGI helper.
				response = handleCGI();
			}
			else {
				//Prepend "." to the URL to start at working directory for the server and use URL as relative path.
				String relativeFileFolderName ="."+ requestParams.get("requestUrl");
				File requestedFileFolder = new File("."+ requestParams.get("requestUrl"));
				if(requestedFileFolder.exists() && isValid(requestedFileFolder)) {
					/**Check that the requested resource exists and that it is underneath the working directory
					 * ".." goes up the directory tree.  Therefore, just because the URL is a relative path from the working directory,
					 * that does not necessarily mean that the requested resource will be underneath the working directory.
					 * Assuming that everything checks out, store the requested resource as a relative path from the working directory
					 */
					requestParams.put("requestedFileFolder", relativeFileFolderName);
	
					if(requestedFileFolder.isDirectory()) {
						/**
						 * In the case that we are dealing with a folder, 
						 * call the folder helper function*/
						response = handleFolder();  	
					}
					else {
						/**In the case that we are dealing with a file,
						 * call the file helper function*/
						response = handleFile();  
						
					}
				}
				else {
					/**
					 * If none of the above cases apply, call the 404 file not found helper.
					 * */
					response = handleFileNotFound();
				}
				
				
			}
			//Print the response to the console prior to printing the response to requester.
			System.out.println(response);
			
			//Add three extra new lines to format console output a bit more elegantly.
			System.out.println("");
			System.out.println("");
			System.out.println("");
			//End of the padding console lines after server response.
			
			out.print(response);
			sock.close();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	
	/**
	 * handleFile method that returns the contents of a file.  If the extension is .htm or .html, we make a response with content-type text/html.
	 * Otherwise, we make response with content type text/plain.
	 * */
	private String handleFile() {
		String responseString;
		String requestedFile = requestParams.get("requestedFileFolder");
		String contentType = "text/plain"; //Default to sending plain text.
		
		if(requestedFile.endsWith(".htm") || requestedFile.endsWith(".html")) {
			//If we have a .htm or .html file extension, we will tell the browser that we are sending html.
			contentType ="text/html";
		}
		
		//Get the file content and then create the response to the browser.
		String contentString = FileSystemReader.getFileContent(new File(requestedFile));
		responseString = HTTPConstructor.constructHttpResponse(requestParams.get("httpVersion"), httpOK, contentType , contentString);
		
		return responseString;
		
	}

	
	/**
	 * handleFolder method that gets an HTML folder listing from HTTPConstructor and then asks HTTPConstructor to help build the response.
	 * */
	private String handleFolder() {
		String responseString = "";
		/**requestedFolder will be used for File System access libraries and is the of the form: "./<requestUrl>
		 * More precisely, requestedFolder is "." prepended to the requestUrl
		 * We assume the requestUrl starts with at least a "/" so for a request to localhost:2540, requestUrl is "/"
		 * requestedFolder would therefore be "./" in that case.
		 */
		String requestedFolder = requestParams.get("requestedFileFolder"); 
		String requestUrl = requestParams.get("requestUrl"); //requestUrl is the actual url the user asked for.
		String contentType = "text/html";
		ArrayList<String> folderList = FileSystemReader.listFolder(new File(requestedFolder));
		String contentString = HTTPConstructor.getSimpleFolderList(folderList, requestUrl);
		responseString = HTTPConstructor.constructHttpResponse(requestParams.get("httpVersion"), httpOK, contentType, contentString);
		return responseString;
	}

	
	/**
	 * handleFileFound method that constructs the HTTP response for 404 file not found.
	 * */
	private String handleFileNotFound() {
		String responseString = "";
		//Get the Not Found message and then construct the response with httpCode = fileNotFound
		String contentString = HTTPConstructor.getHTTPNotFoundMessage();
		responseString = HTTPConstructor.constructHttpResponse(requestParams.get("httpVersion"), fileNotFound, "text/html", contentString);
		
		return responseString;
	}

	/**
	 * isValid function takes in a File Object and checks if it is underneath the current working directory.
	 * For example, if the program starts under /usr/Camille, then we check if the requestedFileFolder
	 * (short hand for "Requested File or Folder") has a canonical path that starts with /usr/Camille
	 * This assumes that all subfolders and files under /usr/Camille will start with /usr/Camille as part of their canonical path.
	 * */
	boolean isValid(File requestedFileFolder) {
		
		try {
			return requestedFileFolder.getCanonicalPath().startsWith(MyWebServer.workingDirectory.getCanonicalPath());
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		return false;
	}

	
	/**
	 * handleCGI function that calls the parseArguments helper to get the cgi arguments and then uses the HTTPConstructor class
	 * to create the HTML that should be returned to the user.
	 * */
	String handleCGI() {
		String responseString = "";
		String contentString = "";
		HashMap<String, String> cgiParams = parseArguments(); //Delegate argument parsing to helper function
		String cgiCalculation = HTTPConstructor.getCgiAddNums(cgiParams); //Get the string result from the CGI call. 
		
		contentString += HTTPConstructor.getStartHtmlTag(); // <html>
		//Wrap the cgi generated String in a paragraph tag.
		contentString += HTTPConstructor.getParagraph(cgiCalculation); // e.g. <p>Dear Savannah, the sum of 8 and 6 is 14.</p>
		contentString += HTTPConstructor.getEndHtmlTag(); // </html>
		
		System.out.println("The CGI content html will be: " + contentString);
		System.out.println(""); //Add some padding to the output
		
		responseString = HTTPConstructor.constructHttpResponse(requestParams.get("httpVersion"), httpOK, "text/html", contentString);
		
		
		return responseString;
		
	}

	
	/**
	 * parseArguments helper function for the cgi functionality.
	 * The function reads the request URL and then parses for the arguments which it returns in a HashMap.
	 * The function simply parses arguments and does not do any parameter validation.
	 * The CGI function will include the logic to validate the parameters while this function merely parses the request URL
	 * */
	private HashMap<String, String> parseArguments() {
		HashMap<String, String> argumentMap = new HashMap<String, String>();
		String requestUrl = requestParams.get("requestUrl"); //Get the request URL from the request params that run() method parsed.
		
		//Get everything after "/cgi/addnums.fake-cgi?" in the request URL which is a String denoting the CGI parameters.
		String argumentString = requestUrl.substring("/cgi/addnums.fake-cgi?".length());
		//CGI parameters are separated by "&" e.g. "num1=20&num2=8&person="Melissa"
		String [] arguments = argumentString.split("&");
		//Each CGI argument is of the form: "key=value" e.g. "person=Lucy"
		for(String argument:arguments) {
			String [] keyValuePair = argument.split("=");
			if(keyValuePair.length == 2) {
				//keyValuePair should only have two elements because argument should have the form: "key=value"
				argumentMap.put(keyValuePair[0], keyValuePair[1]); //Add to the HashMap of arguments to pass to CGI function.
			}
		}
		
		System.out.println("The map of arguments to the CGI functionality are:\n" + argumentMap);
		System.out.println(""); //Add some padding to the output
		
		return argumentMap;
	}


}
