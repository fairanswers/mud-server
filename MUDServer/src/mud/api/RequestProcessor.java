package mud.api;

/*
 * Copyright (c) 2013 Jeremy N. Harton
 * 
 * Released under the MIT License:
 * LICENSE.txt, http://opensource.org/licenses/MIT
 * 
 * NOTE: license provided with code controls, if any
 * changes are made to the one referred to.
 */

import java.util.List;
import java.util.LinkedList;

import mud.objects.Player;

public class RequestProcessor implements Runnable {
	
	private boolean running;
	
	private final APIServer as;
	private final MUDServerAPI msa;
	
	protected RequestProcessor(APIServer parent, MUDServerAPI mudSrv) {
		as = parent;
		msa = mudSrv;
	}
	
	@Override
	public void run() {
		running = true;
		
		while( running ) {
			Request request = as.requests.poll();

			if(request != null) {
				if( as.validate( request.getAPIKey() ) ) {
					System.out.println("API Key is valid!");
					
					System.out.println("Processing Request...");
					processRequest(request);
					System.out.println("Done");
				}
				else {
					System.out.println("API Key is invalid!");
					request.response = "APIServer> Invalid API Key!";
				}
				
				as.processed.add(request);
				System.out.println("Added processed request to processed queue!");
			}
		}
	}
	
	private void processRequest(Request request) {			
		if(request.getType() == RequestType.DATA) {
			if(request.getParam().equals("who")) {
				List<String> responseData = new LinkedList<String>();
				for( Player p : msa.getPlayers()) {
					responseData.add("P(" + p.getName() + "," + p.getPClass().getAbrv() + "," + p.getLevel() + ")"); 
				}
				request.response = "response-data " + "for:" + request.getAPIKey() + " " + responseData; 
			}
			else {
				request.response = "APIServer> Invalid Parameter";
			}
		}

		request.processed = true;
	}

	public void stop() {
		this.running = false;
	}
}