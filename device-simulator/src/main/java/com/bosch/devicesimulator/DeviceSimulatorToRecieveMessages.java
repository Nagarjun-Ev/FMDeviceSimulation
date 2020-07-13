package com.bosch.devicesimulator;

import java.io.IOException;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;

public class DeviceSimulatorToRecieveMessages {

    private static class AppMessageCallback implements MessageCallback {
    	  public IotHubMessageResult execute(Message msg, Object context) {
    	    System.out.println("Received message from hub: "
    	      + new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));

    	    return IotHubMessageResult.COMPLETE;
    	  }
    	}
	private static final String connectionString = "HostName=pjms-dev-iot-01.azure-devices.net;DeviceId=test1;x509=true;";
	 private static final String leafPublicPem =
				"-----BEGIN CERTIFICATE-----\r\n" + 
				"MIICEzCCAbigAwIBAgIFCgsMDQ4wCgYIKoZIzj0EAwIwNjEUMBIGA1UEAwwLdGVz\r\n" + 
				"dFNpZ25lcjIxETAPBgNVBAoMCE1TUl9URVNUMQswCQYDVQQGEwJVUzAgFw0xNzAx\r\n" + 
				"MDEwMDAwMDBaGA8zNzAxMDEzMTIzNTk1OVowMDEOMAwGA1UEAwwFdGVzdDExETAP\r\n" + 
				"BgNVBAoMCE1TUl9URVNUMQswCQYDVQQGEwJVUzBZMBMGByqGSM49AgEGCCqGSM49\r\n" + 
				"AwEHA0IABLMwFV6wky1oepS5jTL/C5uCOerpxnsH6yN2TaKJFDe0aTLBWQ1d9Zp5\r\n" + 
				"u4A4ua29DJi1S9paPOn5QNpCuWi3w4yjgbYwgbMwEwYDVR0lBAwwCgYIKwYBBQUH\r\n" + 
				"AwIwgZsGBmeBBQUEAQSBkDCBjQIBATBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IA\r\n" + 
				"BBdP1mVtuycFqVbsMg7IoKdOl245dmLvM+GbOr+q56FGDuqr5fP755lTfVTWsA2l\r\n" + 
				"waziWIaDrtysHd6UTSE4jgAwLQYJYIZIAWUDBAIBBCAREhMUBQYHCAECAwQFBgcI\r\n" + 
				"AQIDBAUGBwgBAgMEBQYHCDAKBggqhkjOPQQDAgNJADBGAiEA6GN4peNOO7seCZct\r\n" + 
				"bWzD1nf8i3414zIGbKK8PDmUXH4CIQCy+2bEhdM13erWpFz764rwR8paMJ1BQwUW\r\n" + 
				"d1Zj4lvncg==\r\n" + 
				"-----END CERTIFICATE-----";
static final String leafPrivateKey = "-----BEGIN PRIVATE KEY-----\r\n" + 
		"MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgi+wLJqCQ0xTkUMTU\r\n" + 
		"ZWr4GvTitfeq5AIL6IDdNCvSeFygCgYIKoZIzj0DAQehRANCAASzMBVesJMtaHqU\r\n" + 
		"uY0y/wubgjnq6cZ7B+sjdk2iiRQ3tGkywVkNXfWaebuAOLmtvQyYtUvaWjzp+UDa\r\n" + 
		"Qrlot8OM\r\n" + 
		"-----END PRIVATE KEY-----";
	private static final IotHubClientProtocol protocol =    
		    IotHubClientProtocol.HTTPS;
	private static DeviceClient client;
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
        DeviceClient deviceClient = null;

                // connect to iothub
                try
                {
                	//client = new DeviceClient(connectionString, protocol, leafPublicPem, false, leafPrivateKey, false);
client=new DeviceClient("HostName=pjms-imt1-platform-Iot.azure-devices.net;DeviceId=apple;SharedAccessKey=TzyUFjztVuvrVpqYmJf63Q==", IotHubClientProtocol.MQTT);
                	MessageCallback callback = new AppMessageCallback();
                	client.setMessageCallback(callback, null);
                	client.open();
                }
                catch (IOException e)
                {
                    System.out.println("Device client threw an exception: " + e.getMessage());
                    if (deviceClient != null)
                    {
                        deviceClient.closeNow();
                    }
                }
    } 
    
}
