import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;


/**
 * HTTPConstructor class that constructs the HTTP response and holds libraries for HTML tags as well.
 * */
class HTTPConstructor {
	static final String[] httpCodes = {"200", "404"};  // 200 = OK, 404 = Not Found
	static final String[] httpMessages = {"OK", "Not Found"}; 
	static final String htmlLineBreak = "<br>\n";  //HTML line break tag.  Using \n for better raw HTML readability
	static final String httpHeaderEndLine = "\r\n"; //Empty line followed by carriage return & line feed to denote the end of the HTTP header
	
	
	/**
	 * getHttpResponseStartLine function that will create the start line for the HTTP response.
	 * The function takes an httpVersion and an index into httpCodes and httpMessages.
	 * */
	static String getHttpResponseStartLine(String httpVersion, int httpCode) {
		/**httpVersion is the HTTP version from the request
		 * httpCode is an index into httpCodes and httpMessages array which is not the most elegant implementation
		 * httpCode = 0 means that we are dealing with a 200 OK status
		 * httpCode = 1 means that we are dealing with a 404 Not Found status 
		 * Probably better to initialize a constant hash map instead or simply static constant strings.
		 */
		
		return String.join(" ", httpVersion, httpCodes[httpCode], httpMessages[httpCode])+"\r\n";
		
	}
	
	/**
	 * Connection close part of the HTTP response.
	 * */
	static String getConnectionClose() {
		return String.join(": ", "Connection", "close")+"\r\n";
	}
	
	
	/**
	 * DateTime string used in the HTTP response
	 * */
	static String getHttpDate() {
		//Get the current date in the form: Sun, 03 May 2020 11:18:50 GMT" using the US locale
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.US);
		
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		Date now = new Date();
		String formattedDate = dateFormat.format(now);
		//Date String in HTTP response will be of the form: "Date: Sun, 03 May 2020 11:18:50 GMT" followed by a new line. 
		return String.join(": ", "Date", formattedDate)+"\r\n";
	}
	
	
	/**
	 * getLink method that takes in a String path and
	 * returns a String representing an anchor tag to the path specified
	 * */
	static String getLink(String path) {
		
		/**
		 * Tag will be of the form: "<a href="path">path</a>" followed by an html line break (<br>)
		 * and a new line for easier readability
		*/
		return "<a href=" + "\"" + path +"\"" + ">" + path + "</a>"+htmlLineBreak;
	}
	
	/**
	 * Takes a String text and returns a String
	 * representing a header tag with the parameter text.
	 * */
	static String getHeaderOne(String headerText) {
		//Tag will be of the form: "<h1>headerText</h1>" followed by a new line
		return "<h1>" + headerText + "</h1>\n";
		
	}
	
	/**
	 * constructHttpResponse method that takes:
	 * 1. An HTTP version
	 * 2. An index into the httpCode and httpMessage arrays
	 * 3. A String content type
	 * 4. A String representing the request content
	 * 
	 * Return value is the HTTP response from the information provided the above specified parameters
	 * */
	static String constructHttpResponse(String httpVersion, int httpCode, String contentType, String requestContent) {
		String httpResponse ="";
		
		httpResponse+= getHttpResponseStartLine(httpVersion, httpCode); // HTTP/1.1 200 OK, HTTP/1.1 404 Not Found
		httpResponse+= getHttpDate(); //Date: <CurrentDate>
		httpResponse+= getServerDetails(); //Server: Apache
		httpResponse+= getContentLengthString(requestContent.length());  //Content-Length: <requestContent.length()>
		httpResponse+= getContentTypeString(contentType);  //Content-Type: <contentType>
		httpResponse+= getConnectionClose(); //Connection: close
		httpResponse+= httpHeaderEndLine;  //New line to denote end of header
		
		//After the header, insert the content.
		httpResponse+= requestContent;
		
		return httpResponse;
	}
	
	/**
	 *getContentLengthString function that takes an integer and returns the content length string for the HTTP response. 
	 **/
	static String getContentLengthString(int length) {
		//Since length is an integer, I concatenate it to an empty String to convert it properly for String.join()
		return String.join(": ","Content-Length",length+"")+"\r\n";
	}
	
	/**
	 *getContentTypeString that takes a contentType and returns the content length string for the HTTP response. 
	 **/
	static String getContentTypeString(String contentType) {
		
		return String.join(": ", "Content-Type",contentType)+"\r\n";
	}

	
	/**
	 * getServerDetails method that pretends to be an Apache Web Server by returning "Server: Apache" followed the the HTTP new line.
	 * */
	static String getServerDetails() {
		
		return String.join(": ","Server" ,"Apache")+"\r\n";
	}
	
	/**
	 * getSimpleFolderList takes an ArrayList of files and folders as well as a parent directory and:
	 * 
	 * 1. Makes an HTML header element indicating the parent directory
	 * 2. Makes a list of links for each folder or file in the ArrayList
	 * 3. Wraps everything in an HTML tag.
	 * */
	static String getSimpleFolderList(ArrayList<String> fileFolders, String parentDirectory) {
		String folderListString = "";
		folderListString+=getStartHtmlTag(); //<html>
		
		//Add a slash at the end of the folder name so that the working directory is displayed as "Index of /"
		folderListString+= getHeaderOne("Index of " + parentDirectory.substring(0, parentDirectory.length()-1)+"/");
		
		for(String fileFolder: fileFolders) {
			//For each file or subfolder of the parent directory, delegate to getLink for getting an HTML link.
			folderListString+=getLink(fileFolder);
		}
		folderListString+=getEndHtmlTag(); //</html>
		
		return folderListString;
	}
	
	/**
	 * getStartHtmlTag method that returns the starting HTML tag followed by a new line.
	 * */
	static String getStartHtmlTag() {
		return "<html>\n";
	}
	
	/**
	 * getEndHtmlTag method that returns the closing HTML tag followed by a new line.
	 * */
	static String getEndHtmlTag() {
		return "</html>\n";
	}
	
	/**
	 * getParagraph method that takes in a String and returns an HTML String
	 * that encloses the parameter paragraphContent inside paragraph tags.
	 * I use new lines to potentially improve raw HTML readability
	 * */
	static String getParagraph(String paragraphContent) {
		return "<p>\n" + paragraphContent + "\n</p>\n";
	}
	
	/**
	 * getCgiAddNums function that takes a HashMap of parameters and returns a String based on parameters that it gets from the HashMap.
	 * */
	static String getCgiAddNums(HashMap<String, String> cgiParams) {
		Integer num1 = 0; 
		Integer num2 = 0; 
		String person = "";
		
		if(!cgiParams.containsKey("num1") || !cgiParams.containsKey("num2") || !cgiParams.containsKey("person") ) {
			//If any of the parameters for the CGI function are missing, tell the user that we could not parse the request.
			return "Improper arguments were passed to the CGI method and therefore we could not parse your request.";
		}
		try {
			
			num1 = Integer.parseInt(cgiParams.get("num1"));
			num2 = Integer.parseInt(cgiParams.get("num2"));
			person = cgiParams.get("person");
		} catch(NumberFormatException nfe) {
			//In case the user gives us an invalid number and parseInt fails, tell the user that the numbers were not parseable.
			return "Numbers were not parseable by the CGI method.  Please try again.";
		}
		
		return "Dear " + person + ", the sum of " + num1 + " and " + num2 + " is " + (num1+num2);
	}
	
	/**
	 * getHTTPNotFoundMessage returns the HTML to return if the page is not found.
	 * */
	static String getHTTPNotFoundMessage() {
		String notFoundContent = "";
		notFoundContent+=getStartHtmlTag(); // <html>
		//Wrap the 404 message in a paragraph
		notFoundContent+=getParagraph("You are trying access a page that either does not exist or that you should not be accessing");
		notFoundContent+=getEndHtmlTag(); // </html>
		return notFoundContent;
		
		
	}
}
