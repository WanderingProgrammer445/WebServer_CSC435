/**-----------------------------------------------------------------
1. Henry Zhou / Date: May 2, 2020

2. Using Java 1.8 Update 241

3. Precise command-line compilation examples / instructions:



> javac MyWebServer.java


4. Precise examples / instructions to run this program:


In a shell window:

> java MyWebServer

Start the Mozilla Firefox browser (or any other tool that can be used to send HTTP GET requests).

Enter a URL into the browser based on which document or folder you want to be served.

For example, if you are running the browser on the same computer as the server and the server's folder structure looks like this:

	./MyWebServer.class
	./Folder1/
	./Folder2/
	./cat.html

http://localhost:2540/cat.html will send you the contents of cat.html 
(Assume that "./" denotes the current folder and in this example, it is the home directory where the server was started.)

If you are running the browser on a different machine than the server, replace localhost with the public IP of the server.
For example, if the public IP of the server is: 130.9.8.7, then http://localhost:2540/cat.html would become http://130.9.8.7:2540/cat.html

Note that we can use "." to mean the current folder and ".." to mean the parent folder so that:

	http://localhost/subfolder/../cat.html AND http://localhost/cat.html mean the same thing.
	This is probably not the best implementation but makes navigation easier.
	It can be extended to redirect to reduce redundancy
	
	
	If you want to view the log file in the browser while in the middle of running the server, you can do the following on Windows:
	
	java MyWebServer >> serverlog.txt
	
	This will append all output to STDOUT to serverlog.txt 
	

5. List of files needed for running the program.

	a. MyWebServer.java
	b. checklist-mywebserver.html

6. Notes:

This is not a secure implementation as it does not use a secure channel.
I pretend to be an Apache Web Server by using that as the hard-coded Server part of the response.
I use "Connection: close" and I also may not have the correct date format for non-US users but I assume users are in the US.
The server can only handle GET requests but can be extended to handle POST requests
If the user does not terminate the HTTP request with a blank line, the server may hang or exhibit unexpected behavior so it is recommended
to format your HTTP request headers properly with a terminating \r\n\r\n to denote the end of the HTTP request header.

http://localhost/subfolder/../cat.html AND http://localhost/cat.html mean the same thing.
	This is probably not the best implementation but makes navigation easier.
	It can be extended to redirect to reduce redundancy
	
I got some weird null pointer exceptions where the start line was null in some cases so I just close the socket and return in those cases.
Not sure why that happens but it happened.


----------------------------------------------------------*/

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * MyWebServer main class that starts listening on port 2540 and then delegates to the WebPageWorker class.
 * */
public class MyWebServer {
	//Working Directory for the WebServer that can be referenced by the other classes for security checks among other things.
	static File workingDirectory; 
	
	
	public static void main(String[] args) throws IOException {
		int port_number = 2540;
		
		//Get the current directory on start up so that we know that we will not serve files outside of this directory
		workingDirectory = new File(".");  
		
		ServerSocket webServ = new ServerSocket(port_number);
		System.out.println("Starting up the Web Server on port " + port_number);
		//Add a couple lines of padding to the console output
		System.out.println("");
		System.out.println("");
		//End of console output padding.
		
		while(true) {
			Socket newConnection = webServ.accept();
			new WebPageWorker(newConnection).start(); //Delegate connection to the WebPageWorker
		}
	}

	

	
}
