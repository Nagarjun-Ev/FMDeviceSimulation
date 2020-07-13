// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.*;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

/**
 * Provisioning Sample for X509
 */
public class ProvisioningX509Sample
{
    private static final String idScope = "0ne0005B804";
    private static final String globalEndpoint = "global.azure-devices-provisioning.net";
    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.AMQPS_WS;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
    //private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT_WS;
    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milli seconds
    private static final String leafPublicPem = "-----BEGIN CERTIFICATE-----\n" + 
    		"MIICKzCCAdCgAwIBAgIFCgsMDQ4wCgYIKoZIzj0EAwIwQjEgMB4GA1UEAwwXbWlj\n" + 
    		"cm9zb2Z0cmlvdGNvcmVzaWduZXIxETAPBgNVBAoMCE1TUl9URVNUMQswCQYDVQQG\n" + 
    		"EwJVUzAgFw0xNzAxMDEwMDAwMDBaGA8zNzAxMDEzMTIzNTk1OVowPDEaMBgGA1UE\n" + 
    		"AwwRbWljcm9zb2Z0cmlvdGNvcmUxETAPBgNVBAoMCE1TUl9URVNUMQswCQYDVQQG\n" + 
    		"EwJVUzBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABLMwFV6wky1oepS5jTL/C5uC\n" + 
    		"OerpxnsH6yN2TaKJFDe0aTLBWQ1d9Zp5u4A4ua29DJi1S9paPOn5QNpCuWi3w4yj\n" + 
    		"gbYwgbMwEwYDVR0lBAwwCgYIKwYBBQUHAwIwgZsGBmeBBQUEAQSBkDCBjQIBATBZ\n" + 
    		"MBMGByqGSM49AgEGCCqGSM49AwEHA0IABBdP1mVtuycFqVbsMg7IoKdOl245dmLv\n" + 
    		"M+GbOr+q56FGDuqr5fP755lTfVTWsA2lwaziWIaDrtysHd6UTSE4jgAwLQYJYIZI\n" + 
    		"AWUDBAIBBCAREhMUBQYHCAECAwQFBgcIAQIDBAUGBwgBAgMEBQYHCDAKBggqhkjO\n" + 
    		"PQQDAgNJADBGAiEA6GN4peNOO7seCZctbWzD1nf8i3414zIGbKK8PDmUXH4CIQCQ\n" + 
    		"dRi2dn3bQatk4VvWrYY/6QC1WltZHXJobieuaMWayw==\n" + 
    		"-----END CERTIFICATE-----";
    private static final String leafPrivateKey = "-----BEGIN PRIVATE KEY-----\n" + 
    		"MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgi+wLJqCQ0xTkUMTU\n" + 
    		"ZWr4GvTitfeq5AIL6IDdNCvSeFygCgYIKoZIzj0DAQehRANCAASzMBVesJMtaHqU\n" + 
    		"uY0y/wubgjnq6cZ7B+sjdk2iiRQ3tGkywVkNXfWaebuAOLmtvQyYtUvaWjzp+UDa\n" + 
    		"Qrlot8OM\n" + 
    		"-----END PRIVATE KEY-----";

    private static final Collection<String> signerCertificates = new LinkedList<>(Arrays.asList("-----BEGIN CERTIFICATE-----\n"+
    		"MIIBmjCCAUCgAwIBAgIFDg0MCwowCgYIKoZIzj0EAwIwQDEeMBwGA1UEAwwVbWlj\n"+
    		"cm9zb2Z0cmlvdGNvcmVyb290MREwDwYDVQQKDAhNU1JfVEVTVDELMAkGA1UEBhMC\n"+
    		"VVMwIBcNMTcwMTAxMDAwMDAwWhgPMzcwMTAxMzEyMzU5NTlaMEIxIDAeBgNVBAMM\n"+
    		"F21pY3Jvc29mdHJpb3Rjb3Jlc2lnbmVyMREwDwYDVQQKDAhNU1JfVEVTVDELMAkG\n"+
    		"A1UEBhMCVVMwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQXT9ZlbbsnBalW7DIO\n"+
    		"yKCnTpduOXZi7zPhmzq/quehRg7qq+Xz++eZU31U1rANpcGs4liGg67crB3elE0h\n"+
    		"OI4AoyMwITALBgNVHQ8EBAMCAgQwEgYDVR0TAQH/BAgwBgEB/wIBATAKBggqhkjO\n"+
    		"PQQDAgNIADBFAiBkPNBKpTzmJNr/nQX67+XC19pf1uswsZ8L8SXkirZ8sQIhAOS4\n"+
    		"+/Zh2ywbnce6XV6zcNVXFosusqfgPMffVwwYEuU6\n"+
    		"-----END CERTIFICATE-----\n"));

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
                    Message messageToSendFromDeviceToHub =  new Message("Whatever message you would like to send");

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
}
