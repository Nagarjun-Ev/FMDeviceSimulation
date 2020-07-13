package com.bosch.devicesimulator;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationResult;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;

public class DeviceCreatedSimulator {

	//private static final String idScope = "0ne0008A915";
	private static final String idScope = "0ne000EB851";
	
	private static final String globalEndpoint = "global.azure-devices-provisioning.net";
//    private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.HTTPS;
	// private static final ProvisioningDeviceClientTransportProtocol
	// PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL =
	// ProvisioningDeviceClientTransportProtocol.AMQPS;
	// private static final ProvisioningDeviceClientTransportProtocol
	// PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL =
	// ProvisioningDeviceClientTransportProtocol.AMQPS_WS;
	private static final ProvisioningDeviceClientTransportProtocol PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL = ProvisioningDeviceClientTransportProtocol.MQTT;
	// private static final ProvisioningDeviceClientTransportProtocol
	// PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL =
	// ProvisioningDeviceClientTransportProtocol.MQTT_WS;
	private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milli seconds
	private static final String leafPublicPem ="-----BEGIN CERTIFICATE-----\r\n" +
			"MIICEjCCAbegAwIBAgIFCgsMDQ4wCgYIKoZIzj0EAwIwNTETMBEGA1UEAwwKcm9v\r\n" +
			"dFNpZ25lcjERMA8GA1UECgwITVNSX1RFU1QxCzAJBgNVBAYTAlVTMCAXDTE3MDEw\r\n" +
			"MTAwMDAwMFoYDzM3MDEwMTMxMjM1OTU5WjAwMQ4wDAYDVQQDDAVhcHBsZTERMA8G\r\n" +
			"A1UECgwITVNSX1RFU1QxCzAJBgNVBAYTAlVTMFkwEwYHKoZIzj0CAQYIKoZIzj0D\r\n" +
			"AQcDQgAEszAVXrCTLWh6lLmNMv8Lm4I56unGewfrI3ZNookUN7RpMsFZDV31mnm7\r\n" +
			"gDi5rb0MmLVL2lo86flA2kK5aLfDjKOBtjCBszATBgNVHSUEDDAKBggrBgEFBQcD\r\n" +
			"AjCBmwYGZ4EFBQQBBIGQMIGNAgEBMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE\r\n" +
			"F0/WZW27JwWpVuwyDsigp06Xbjl2Yu8z4Zs6v6rnoUYO6qvl8/vnmVN9VNawDaXB\r\n" +
			"rOJYhoOu3Kwd3pRNITiOADAtBglghkgBZQMEAgEEIBESExQFBgcIAQIDBAUGBwgB\r\n" +
			"AgMEBQYHCAECAwQFBgcIMAoGCCqGSM49BAMCA0kAMEYCIQDoY3il4047ux4Jly1t\r\n" +
			"bMPWd/yLfjXjMgZsorw8OZRcfgIhANpC4OWwcqWjvB8nsiwicDjuSRBYryOnNpVR\r\n" +
			"P93HR826\r\n" +
			"-----END CERTIFICATE-----";
	static final String leafPrivateKey = 	"-----BEGIN PRIVATE KEY-----\r\n" +
			"MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgi+wLJqCQ0xTkUMTU\r\n" +
			"ZWr4GvTitfeq5AIL6IDdNCvSeFygCgYIKoZIzj0DAQehRANCAASzMBVesJMtaHqU\r\n" +
			"uY0y/wubgjnq6cZ7B+sjdk2iiRQ3tGkywVkNXfWaebuAOLmtvQyYtUvaWjzp+UDa\r\n" +
			"Qrlot8OM\r\n" +
			"-----END PRIVATE KEY-----";

	static final String intermediateKey = "-----BEGIN CERTIFICATE-----\r\n"
			+ "MIIBgDCCASagAwIBAgIFDg0MCwowCgYIKoZIzj0EAwIwMzERMA8GA1UEAwwIZGV2\r\n"
			+ "dFJvb3QxETAPBgNVBAoMCE1TUl9URVNUMQswCQYDVQQGEwJVUzAgFw0xNzAxMDEw\r\n"
			+ "MDAwMDBaGA8zNzAxMDEzMTIzNTk1OVowNTETMBEGA1UEAwwKZGV2dFNpZ25lcjER\r\n"
			+ "MA8GA1UECgwITVNSX1RFU1QxCzAJBgNVBAYTAlVTMFkwEwYHKoZIzj0CAQYIKoZI\r\n"
			+ "zj0DAQcDQgAEF0/WZW27JwWpVuwyDsigp06Xbjl2Yu8z4Zs6v6rnoUYO6qvl8/vn\r\n"
			+ "mVN9VNawDaXBrOJYhoOu3Kwd3pRNITiOAKMjMCEwCwYDVR0PBAQDAgIEMBIGA1Ud\r\n"
			+ "EwEB/wQIMAYBAf8CAQEwCgYIKoZIzj0EAwIDSAAwRQIhAM26iba7MItUskDVhAU4\r\n"
			+ "64kNi9wxBoHXr5JPWsTNUeaGAiBuOgYy2+aqcPz5U82nvRzAF63Id7vUQWhvsPqX\r\n" + "jTBqYA==\r\n"
			+ "-----END CERTIFICATE-----";

	private static final Collection<String> signerCertificates = new LinkedList<String>();

	private static AtomicBoolean Succeed = new AtomicBoolean(false);

	static class ProvisioningStatus {
		ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
		Exception exception;
	}

	static class ProvisioningDeviceClientRegistrationCallbackImpl
			implements ProvisioningDeviceClientRegistrationCallback {
		@Override
		public void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult,
				Exception exception, Object context) {
			if (context instanceof ProvisioningStatus) {
				ProvisioningStatus status = (ProvisioningStatus) context;
				status.provisioningDeviceClientRegistrationInfoClient = provisioningDeviceClientRegistrationResult;
				status.exception = exception;
			} else {
				System.out.println("Received unknown context");
			}
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		System.out.println("Beginning setup.");
		ProvisioningDeviceClient provisioningDeviceClient = null;
		try {
			ProvisioningStatus provisioningStatus = new ProvisioningStatus();

			signerCertificates.add(intermediateKey);

			SecurityProvider securityProviderX509 = new SecurityProviderX509Cert(leafPublicPem, leafPrivateKey,
					signerCertificates);
			provisioningDeviceClient = ProvisioningDeviceClient.create(globalEndpoint, idScope,
					PROVISIONING_DEVICE_CLIENT_TRANSPORT_PROTOCOL, securityProviderX509);

			provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(),
					provisioningStatus);

			while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient
					.getProvisioningDeviceClientStatus() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED) {
				if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient
						.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR
						|| provisioningStatus.provisioningDeviceClientRegistrationInfoClient
								.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED
						|| provisioningStatus.provisioningDeviceClientRegistrationInfoClient
								.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED)

				{
					provisioningStatus.exception.printStackTrace();
					System.out.println("Registration error, bailing out");
					break;
				}
				System.out.println("Waiting for Provisioning Service to register");
				Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
			}

			if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient
					.getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED) {
				System.out.println("IotHUb Uri : "
						+ provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
				System.out.println("payload:"
						+ provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getProvisioningPayload());
				System.out.println("Device ID : "
						+ provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

			}
		} catch (ProvisioningDeviceClientException | InterruptedException e) {
			System.out.println("Provisioning Device Client threw an exception" + e.getMessage());
			if (provisioningDeviceClient != null) {
				provisioningDeviceClient.closeNow();
			}
		}

		System.out.println("Shutting down...");
		if (provisioningDeviceClient != null) {
			provisioningDeviceClient.closeNow();
		}

	}

	protected static class DeviceTwinStatusCallBack implements IotHubEventCallback {
		@Override
		public void execute(IotHubStatusCode status, Object context) {
			if ((status == IotHubStatusCode.OK) || (status == IotHubStatusCode.OK_EMPTY)) {
				Succeed.set(true);
			} else {
				Succeed.set(false);
			}
			System.out.println("IoT Hub responded to device twin operation with status " + status.name());
		}
	}

}
