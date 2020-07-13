package com.bosch.devicesimulator;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
public class ProvisioningX509SampleApplication
{
    private static final String idScope = "0ne0005B804";
    private static final String globalEndpoint = "global.azure-devices-provisioning.net";
//    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS_WS;
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT_WS;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milli seconds
    private static final String leafPublicPem =
    		"-----BEGIN CERTIFICATE-----\r\n" + 
    		"MIICEDCCAbagAwIBAgIFCgsMDQ4wCgYIKoZIzj0EAwIwNTETMBEGA1UEAwwKdGVz\r\n" + 
    		"dFNpZ25lcjERMA8GA1UECgwITVNSX1RFU1QxCzAJBgNVBAYTAlVTMCAXDTE3MDEw\r\n" + 
    		"MTAwMDAwMFoYDzM3MDEwMTMxMjM1OTU5WjAvMQ0wCwYDVQQDDAR0ZXN0MREwDwYD\r\n" + 
    		"VQQKDAhNU1JfVEVTVDELMAkGA1UEBhMCVVMwWTATBgcqhkjOPQIBBggqhkjOPQMB\r\n" + 
    		"BwNCAASzMBVesJMtaHqUuY0y/wubgjnq6cZ7B+sjdk2iiRQ3tGkywVkNXfWaebuA\r\n" + 
    		"OLmtvQyYtUvaWjzp+UDaQrlot8OMo4G2MIGzMBMGA1UdJQQMMAoGCCsGAQUFBwMC\r\n" + 
    		"MIGbBgZngQUFBAEEgZAwgY0CAQEwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQX\r\n" + 
    		"T9ZlbbsnBalW7DIOyKCnTpduOXZi7zPhmzq/quehRg7qq+Xz++eZU31U1rANpcGs\r\n" + 
    		"4liGg67crB3elE0hOI4AMC0GCWCGSAFlAwQCAQQgERITFAUGBwgBAgMEBQYHCAEC\r\n" + 
    		"AwQFBgcIAQIDBAUGBwgwCgYIKoZIzj0EAwIDSAAwRQIhAOhjeKXjTju7HgmXLW1s\r\n" + 
    		"w9Z3/It+NeMyBmyivDw5lFx+AiBEGR9MAVu50eTqnjQQ8Nixjj8ZkaUyZyBej/l7\r\n" + 
    		"BaHMPQ==\r\n" + 
    		"-----END CERTIFICATE-----";
    static final String leafPrivateKey = "-----BEGIN PRIVATE KEY-----\r\n" + 
    		"MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgi+wLJqCQ0xTkUMTU\r\n" + 
    		"ZWr4GvTitfeq5AIL6IDdNCvSeFygCgYIKoZIzj0DAQehRANCAASzMBVesJMtaHqU\r\n" + 
    		"uY0y/wubgjnq6cZ7B+sjdk2iiRQ3tGkywVkNXfWaebuAOLmtvQyYtUvaWjzp+UDa\r\n" + 
    		"Qrlot8OM\r\n" + 
    		"-----END PRIVATE KEY-----";
    
    static final String intermediateKey="-----BEGIN CERTIFICATE-----\r\n" + 
    		"MIIBgjCCASigAwIBAgIFDg0MCwowCgYIKoZIzj0EAwIwNTETMBEGA1UEAwwKdGVz\r\n" + 
    		"dENvbW1vbjERMA8GA1UECgwITVNSX1RFU1QxCzAJBgNVBAYTAlVTMCAXDTE3MDEw\r\n" + 
    		"MTAwMDAwMFoYDzM3MDEwMTMxMjM1OTU5WjA1MRMwEQYDVQQDDAp0ZXN0U2lnbmVy\r\n" + 
    		"MREwDwYDVQQKDAhNU1JfVEVTVDELMAkGA1UEBhMCVVMwWTATBgcqhkjOPQIBBggq\r\n" + 
    		"hkjOPQMBBwNCAAQXT9ZlbbsnBalW7DIOyKCnTpduOXZi7zPhmzq/quehRg7qq+Xz\r\n" + 
    		"++eZU31U1rANpcGs4liGg67crB3elE0hOI4AoyMwITALBgNVHQ8EBAMCAgQwEgYD\r\n" + 
    		"VR0TAQH/BAgwBgEB/wIBATAKBggqhkjOPQQDAgNIADBFAiEAo3JhnDW0o6wfNu2l\r\n" + 
    		"dQbsmT+fARQWX5cJePP/D3AerIsCIH0w0tOC6+PIVcgXfkBx/A90+ubRTYv6UDXM\r\n" + 
    		"8wAuqTqL\r\n" + 
    		"-----END CERTIFICATE-----";

    private static final Collection<String> signerCertificates = new LinkedList<String>();

    
    private static AtomicBoolean Succeed = new AtomicBoolean(false);

    
    static class ProvisioningStatus
    {
        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
        Exception exception;
    }

    static class ProvisioningDeviceClientRegistrationCallbackImpl implements ProvisioningDeviceClientRegistrationCallback
    {
        @Override
        public void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult, Exception exception, Object context)
        {
            if (context instanceof ProvisioningStatus)
            {
                ProvisioningStatus status = (ProvisioningStatus) context;
                status.provisioningDeviceClientRegistrationInfoClient = provisioningDeviceClientRegistrationResult;
                status.exception = exception;
            }
            else
            {
                System.out.println("Received unknown context");
            }
        }
    }

    private static class IotHubEventCallbackImpl implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext)
        {
            System.out.println("Message received!");
        }
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");
        ProvisioningDeviceClient provisioningDeviceClient = null;
        DeviceClient deviceClient = null;
        try
        {
            ProvisioningStatus provisioningStatus = new ProvisioningStatus();
            
            signerCertificates.add(intermediateKey);

            SecurityProvider securityProviderX509 = new SecurityProviderX509Cert(leafPublicPem, leafPrivateKey, signerCertificates);
            provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, idScope, PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL,
                                                                       securityProviderX509);

            provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(), provisioningStatus);

            while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED ||
                        provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED )

                {
                    provisioningStatus.exception.printStackTrace();
                    System.out.println("Registration error, bailing out");
                    break;
                }
                System.out.println("Waiting for Provisioning Service to register");
                Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
            }

            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED)
            {
                System.out.println("IotHUb Uri : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
                System.out.println("Device ID : " + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

                // connect to iothub
                String iotHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
                String deviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
                try
                {
                    deviceClient = DeviceClient.createFromSecurityProvider(iotHubUri, deviceId, securityProviderX509, IotHubClientProtocol.MQTT);
                    deviceClient.open();
                    startDeviceTwin(deviceClient);
                    Set<Property> reportedProperties = new HashSet<Property>()
                    {
                        {
                            add(new Property("IMEINo", 1234567891));
//                            add(new Property("FirmwareVersion", 2));
                            
                        }
                    };
                    deviceClient.sendReportedProperties(reportedProperties);
                    
                    Message messageToSendFromDeviceToHub =  new Message("slaType-special, deviceId-"+deviceId);

                    System.out.println("Sending message from device to IoT Hub...");
                    deviceClient.sendEventAsync(messageToSendFromDeviceToHub, new IotHubEventCallbackImpl(), null);
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
        catch (ProvisioningDeviceClientException | InterruptedException e)
        {
            System.out.println("Provisioning Device Client threw an exception" + e.getMessage());
            if (provisioningDeviceClient != null)
            {
                provisioningDeviceClient.closeNow();
            }
        }

        System.out.println("Press any key to exit...");

        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();

        System.out.println("Shutting down...");
        if (provisioningDeviceClient != null)
        {
            provisioningDeviceClient.closeNow();
        }
        if (deviceClient != null)
        {
            deviceClient.closeNow();
        }
    }
    
    
    
   private static void startDeviceTwin(DeviceClient deviceClient) throws IllegalArgumentException, UnsupportedOperationException, IOException{
	   deviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), null, new onProperty(), null);
    }
   protected static class onProperty implements com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack
   {
       public void TwinPropertyCallBack(Property property, Object context)
       {
           System.out.println(
                   "onProperty callback for " + (property.getIsReported()?"reported": "desired") +
                           " property " + property.getKey() +
                           " to " + property.getValue() +
                           ", Properties version:" + property.getVersion());
       }
   }

   protected static class DeviceTwinStatusCallBack implements IotHubEventCallback
   {
       @Override
       public void execute(IotHubStatusCode status, Object context)
       {
           if((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY))
           {
               Succeed.set(true);
           }
           else
           {
               Succeed.set(false);
           }
           System.out.println("IoT Hub responded to device twin operation with status " + status.name());
       }
   }
}