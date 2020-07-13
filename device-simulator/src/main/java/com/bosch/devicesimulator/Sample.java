package com.bosch.devicesimulator;

import java.net.HttpURLConnection;
import java.net.URL;

public class Sample {

	public static void main(String[] args) throws Exception {
		//https://portal.azure.com/
		//https://dev.boschindia-mobilitysolutions.com
		System.out.println("Hai sai");
		 URL url = new URL("https://dev1.boschindia-mobilitysolutions.com/platformadmin/api/v1/health");//your url i.e fetch data from .
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         conn.setRequestProperty("Accept", "application/json");
         System.out.println("Response code:"+conn.getResponseCode());
         if (conn.getResponseCode() != 200) {
             throw new RuntimeException("Failed : HTTP Error code : "
                     + conn.getResponseCode());
         }

	}

}
