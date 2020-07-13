package com.bosch.devicesimulator;

import java.io.IOException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;

public class DeviceConnectedSimulator {

	private static AtomicBoolean Succeed = new AtomicBoolean(false);

	private static class IotHubEventCallbackImpl implements IotHubEventCallback {
		@Override
		public void execute(IotHubStatusCode responseStatus, Object callbackContext) {
			System.out.println("Message received!");
		}
	}

	private static final String leafPublicPem = "-----BEGIN CERTIFICATE-----\r\n"
			+ "MIICEjCCAbegAwIBAgIFCgsMDQ4wCgYIKoZIzj0EAwIwNTETMBEGA1UEAwwKcm9v\r\n"
			+ "dFNpZ25lcjERMA8GA1UECgwITVNSX1RFU1QxCzAJBgNVBAYTAlVTMCAXDTE3MDEw\r\n"
			+ "MTAwMDAwMFoYDzM3MDEwMTMxMjM1OTU5WjAwMQ4wDAYDVQQDDAVhcHBsZTERMA8G\r\n"
			+ "A1UECgwITVNSX1RFU1QxCzAJBgNVBAYTAlVTMFkwEwYHKoZIzj0CAQYIKoZIzj0D\r\n"
			+ "AQcDQgAEszAVXrCTLWh6lLmNMv8Lm4I56unGewfrI3ZNookUN7RpMsFZDV31mnm7\r\n"
			+ "gDi5rb0MmLVL2lo86flA2kK5aLfDjKOBtjCBszATBgNVHSUEDDAKBggrBgEFBQcD\r\n"
			+ "AjCBmwYGZ4EFBQQBBIGQMIGNAgEBMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE\r\n"
			+ "F0/WZW27JwWpVuwyDsigp06Xbjl2Yu8z4Zs6v6rnoUYO6qvl8/vnmVN9VNawDaXB\r\n"
			+ "rOJYhoOu3Kwd3pRNITiOADAtBglghkgBZQMEAgEEIBESExQFBgcIAQIDBAUGBwgB\r\n"
			+ "AgMEBQYHCAECAwQFBgcIMAoGCCqGSM49BAMCA0kAMEYCIQDoY3il4047ux4Jly1t\r\n"
			+ "bMPWd/yLfjXjMgZsorw8OZRcfgIhANpC4OWwcqWjvB8nsiwicDjuSRBYryOnNpVR\r\n" + "P93HR826\r\n"
			+ "-----END CERTIFICATE-----";
	static final String leafPrivateKey = "-----BEGIN PRIVATE KEY-----\r\n"
			+ "MIGTAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBHkwdwIBAQQgi+wLJqCQ0xTkUMTU\r\n"
			+ "ZWr4GvTitfeq5AIL6IDdNCvSeFygCgYIKoZIzj0DAQehRANCAASzMBVesJMtaHqU\r\n"
			+ "uY0y/wubgjnq6cZ7B+sjdk2iiRQ3tGkywVkNXfWaebuAOLmtvQyYtUvaWjzp+UDa\r\n" + "Qrlot8OM\r\n"
			+ "-----END PRIVATE KEY-----";

	static final String intermediateKey = "-----BEGIN CERTIFICATE-----\r\n"
			+ "MIIBgDCCASegAwIBAgIFDg0MCwowCgYIKoZIzj0EAwIwNDESMBAGA1UEAwwJcm9v\r\n"
			+ "dEFwcGxlMREwDwYDVQQKDAhNU1JfVEVTVDELMAkGA1UEBhMCVVMwIBcNMTcwMTAx\r\n"
			+ "MDAwMDAwWhgPMzcwMTAxMzEyMzU5NTlaMDUxEzARBgNVBAMMCnJvb3RTaWduZXIx\r\n"
			+ "ETAPBgNVBAoMCE1TUl9URVNUMQswCQYDVQQGEwJVUzBZMBMGByqGSM49AgEGCCqG\r\n"
			+ "SM49AwEHA0IABBdP1mVtuycFqVbsMg7IoKdOl245dmLvM+GbOr+q56FGDuqr5fP7\r\n"
			+ "55lTfVTWsA2lwaziWIaDrtysHd6UTSE4jgCjIzAhMAsGA1UdDwQEAwICBDASBgNV\r\n"
			+ "HRMBAf8ECDAGAQH/AgEBMAoGCCqGSM49BAMCA0cAMEQCIBl3MR4MEx6PMj31O4gi\r\n"
			+ "/hxQXgp4XEbSfnE/mJMA3p3cAiAITKve2govFEbXBmnCsvYOSBKvLeCTsW9h1NoK\r\n" + "rgpgRw==\r\n"
			+ "-----END CERTIFICATE-----";
	private static final Collection<String> signerCertificates = new LinkedList<String>();

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		System.out.println("Starting...");
		System.out.println("Beginning setup.");
		// DeviceClient deviceClient = null;
		signerCertificates.add(intermediateKey);
		SecurityProvider securityProviderX509 = new SecurityProviderX509Cert(leafPublicPem, leafPrivateKey,
				signerCertificates);

//            SecurityProvider securityProviderX509 = new SecurityProviderX509Cert(leafPublicPem, leafPrivateKey, signerCertificates);

		// connect to iothub
		String iotHubUri = "PJMS-DEV-FM-IOTH-01.azure-devices.net";
		String deviceId = "apple";
		try {
			final DeviceClient deviceClient = DeviceClient.createFromSecurityProvider(iotHubUri, deviceId,
					securityProviderX509, IotHubClientProtocol.MQTT);
			// DeviceClient deviceClient= new
			// DeviceClient("HostName=pjms-imt1-platform-Iot.azure-devices.net;DeviceId=apple;SharedAccessKey=P++5x80azQCvDxNEwuQCEg==",
			// IotHubClientProtocol.MQTT);
			deviceClient.open();
			startDeviceTwin(deviceClient);
			Set<Property> reportedProperties = new HashSet<Property>() {
				{
					add(new Property("IMEINo", "123456789012345"));
//                            add(new Property("FirmwareVersion", 2));
					add(new Property("JOB_ID", "33333232-7381-11ea-9b2e-c7037068b490"));
					add(new Property("BATCH_ID", 1));
					add(new Property("DEVICE_SLNO", "apple"));
					add(new Property("REPORTED_FIRWARE_VERSION", "firmware1"));
					add(new Property("REPORTED_LOG", "tttt"));
//                  add(new Property("FirmwareVersion", 2));

				}
			};
			deviceClient.sendReportedProperties(reportedProperties);

//			Message messageToSendFromDeviceToHub = new Message();
			Random random = new Random();
			String dat = "[{\"tag\":\"sev\",\"val\":0},{\"tag\":\"acc\",\"val\":0.5},{\"tag\":\"pev\"}]";

			String health8 = "[{\"tag\":\"status.MIL\", \"val\":true},{\"tag\":\"CoolantTemperature\", \"val\":90},"
					+ "{\"tag\":\"Battery\", \"val\":20},{\"tag\":\"OilTemperature\", \"val\":40},"
					+ "{\"tag\":\"DTC\", \"val\":[\"P0001\"]}]";

			String health1 = "[{\"tag\":\"status.MIL\", \"val\":false},{\"tag\":\"CoolantTemperature\", \"val\":60},"
					+ "{\"tag\":\"Battery\", \"val\":20},{\"tag\":\"OilTemperature\", \"val\":60},"
					+ "{\"tag\":\"DTC\", \"val\":[\"P0001\"]}]";

			String health2 = "[{\"tag\":\"status.MIL\", \"val\":true},{\"tag\":\"CoolantTemperature\", \"val\":20},"
					+ "{\"tag\":\"Battery\", \"val\":20},{\"tag\":\"OilTemperature\", \"val\":30},"
					+ "{\"tag\":\"DTC\", \"val\":[\"P0001\"]}]";
			String health3 = "[{\"tag\":\"status.MIL\", \"val\":false},{\"tag\":\"CoolantTemperature\", \"val\":30},"
					+ "{\"tag\":\"Battery\", \"val\":20},{\"tag\":\"OilTemperature\", \"val\":20},"
					+ "{\"tag\":\"DTC\", \"val\":[\"P0001\"]}]";

			String health4 = "[{\"tag\":\"status.MIL\", \"val\":true},{\"tag\":\"CoolantTemperature\", \"val\":80},"
					+ "{\"tag\":\"Battery\", \"val\":20},{\"tag\":\"OilTemperature\", \"val\":10},"
					+ "{\"tag\":\"DTC\", \"val\":[\"P0001\"]}]";

			String health5 = "[{\"tag\":\"status.MIL\", \"val\":false},{\"tag\":\"CoolantTemperature\", \"val\":90},"
					+ "{\"tag\":\"Battery\", \"val\":20},{\"tag\":\"OilTemperature\", \"val\":90},"
					+ "{\"tag\":\"DTC\", \"val\":[\"P0001\"]}]";

			String health6 = "[{\"tag\":\"status.MIL\", \"val\":true},{\"tag\":\"CoolantTemperature\", \"val\":70},"
					+ "{\"tag\":\"Battery\", \"val\":20},{\"tag\":\"OilTemperature\", \"val\":80},"
					+ "{\"tag\":\"DTC\", \"val\":[\"P0001\"]}]";

			String health7 = "[{\"tag\":\"status.MIL\", \"val\":false},{\"tag\":\"CoolantTemperature\", \"val\":60},"
					+ "{\"tag\":\"Battery\", \"val\":30},{\"tag\":\"OilTemperature\", \"val\":70},"
					+ "{\"tag\":\"DTC\", \"val\":[\"P0001\"]}]";

			/*
			 * String health1 =
			 * "[{\"tag\":\"status:.MIL\", \"val\":0},{\"tag\":\"CoolantTemperature\", \"val\":1},"
			 * +
			 * "{\"tag\":\"Battery\", \"val\":0},{\"tag\":\"OilTemperature\", \"val\":1}]";
			 * String FMDevice1 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now() +
			 * "\", \"did\":\"FMDevice1\" ,\"dop\":1.3,\"lat\":11.111111,\"lng\":22.222222,\""
			 * + "spd\":10.5,\"igs\":true,\"evt\":\"SOS\",\"err\":1,\"dat\":" + health +
			 * "}";
			 * 
			 */ /*
				 * String FMDevice1 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now() +
				 * "\", \"did\":\"FMDevice1\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() *
				 * 90) + 10) / 1.0 + ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 +
				 * ",\"" + "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"HA\"" + dat + "\"}";
				 */

			String FMDevice1 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice1\" ,\"dop\":1.3,\"lat\":\"" + ((random.nextDouble() * 90 + 10) / 1.0)
					+ ",\"lng\":\"" + ((random.nextDouble() * 90 + 10) / 1.0) + ",\""
					+ "spd\":10.5,\"igs\":true,\"evt\":\"HA\",\"err\":1,\"dat\":" + dat + "}";
			String FMDevice2 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice2\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":1.5,\"igs\":true,\"err\":1,\"evt\":\"ACC\",\"dat\":" + dat + "}";
			String FMDevice3 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice3\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"ACC\",\"dat\":" + dat + "}";
			String FMDevice4 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice4\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"evt\":\"OBD\",\"err\":1,\"dat\":" + health1 + "}";
			String FMDevice5 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice5\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";
			String FMDevice6 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice6\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";
			String FMDevice7 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice7\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"evt\":\"OBD\",\"err\":1,\"dat\":" + health8 + "}";
			String FMDevice8 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice8\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"evt\":\"OBD\",\"err\":1,\"dat\":" + health3 + "}";
			String FMDevice13 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice13\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":2.7,\"igs\":true,\"evt\":\"TNT\",\"err\":1,\"dat\":" + health4 + "}";
			String FMDevice14 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice14\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":1.7,\"igs\":true,\"evt\":\"TNT\",\"err\":1,\"dat\":" + health5 + "}";
			String FMDevice15 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice15\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":1,\"igs\":true,\"evt\":\"OBD\",\"err\":1,\"dat\":" + health6 + "}";
			String FMDevice16 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice16\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":1.1,\"igs\":true,\"evt\":\"TNT\",\"err\":1,\"dat\":" + health7 + "}";
			String FMDevice17 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice17\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"evt\":\"TNT\",\"err\":1,\"dat\":" + health8 + "}";
			String FMDevice18 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice18\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"evt\":\"OBD\",\"err\":1,\"dat\":" + health8 + "}";
			String FMDevice19 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice19\" ,\"dop\":1.3,\"lat\":\"" + ((random.nextDouble() * 90 + 10) / 1.0)
					+ ",\"lng\":\"" + ((random.nextDouble() * 90 + 10) / 1.0) + ",\""
					+ "spd\":10.5,\"igs\":true,\"evt\":\"TNT\",\"err\":1,\"dat\":" + dat + "}";
			String FMDevice21 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice21\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":1.5,\"igs\":true,\"err\":1,\"evt\":\"SOS\",\"dat\":" + dat + "}";
			String FMDevice24 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice24\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";
			Message messageToSendFromDeviceToHub1 = new Message(FMDevice1);
			Message messageToSendFromDeviceToHub2 = new Message(FMDevice2);
			Message messageToSendFromDeviceToHub3 = new Message(FMDevice3);
			Message messageToSendFromDeviceToHub4 = new Message(FMDevice4);
			Message messageToSendFromDeviceToHub5 = new Message(FMDevice5);
			Message messageToSendFromDeviceToHub6 = new Message(FMDevice6);
			Message messageToSendFromDeviceToHub7 = new Message(FMDevice7);
			Message messageToSendFromDeviceToHub8 = new Message(FMDevice8);
			Message messageToSendFromDeviceToHub13 = new Message(FMDevice13);
			Message messageToSendFromDeviceToHub14 = new Message(FMDevice14);
			Message messageToSendFromDeviceToHub15 = new Message(FMDevice15);
			Message messageToSendFromDeviceToHub16 = new Message(FMDevice16);
			Message messageToSendFromDeviceToHub17 = new Message(FMDevice17);
			Message messageToSendFromDeviceToHub18 = new Message(FMDevice18);
			Message messageToSendFromDeviceToHub19 = new Message(FMDevice19);
			Message messageToSendFromDeviceToHub21 = new Message(FMDevice21);
			Message messageToSendFromDeviceToHub24 = new Message(FMDevice24);
			List<Message> messagesList = new ArrayList<>();
			messagesList.add(messageToSendFromDeviceToHub1);
			messagesList.add(messageToSendFromDeviceToHub2);
			messagesList.add(messageToSendFromDeviceToHub3);
			messagesList.add(messageToSendFromDeviceToHub4);
			messagesList.add(messageToSendFromDeviceToHub5);
			messagesList.add(messageToSendFromDeviceToHub6);
			messagesList.add(messageToSendFromDeviceToHub7);
			messagesList.add(messageToSendFromDeviceToHub8);
			messagesList.add(messageToSendFromDeviceToHub13);
			messagesList.add(messageToSendFromDeviceToHub14);
			messagesList.add(messageToSendFromDeviceToHub15);
			messagesList.add(messageToSendFromDeviceToHub16);
			messagesList.add(messageToSendFromDeviceToHub17);
			messagesList.add(messageToSendFromDeviceToHub18);
			messagesList.add(messageToSendFromDeviceToHub19);
			messagesList.add(messageToSendFromDeviceToHub21);
			messagesList.add(messageToSendFromDeviceToHub24);

			String FMDevice9 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice9\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";
			String FMDevice10 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice10\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";
			String FMDevice11 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice11\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";
			String FMDevice12 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice12\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";
			String FMDevice22 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice22\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";

			String FMDevice26 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice26\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":10.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";

			String FMDevice27 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + Instant.now()
					+ "\", \"did\":\"FMDevice27\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":22.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";

			String FMDevice28 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + new Date()
					+ "\", \"did\":\"FMDevice28\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":22.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";

			String FMDevice29 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + ZonedDateTime.now()
					+ "\", \"did\":\"FMDevice29\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":22.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";

			String FMDevice30 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + ZonedDateTime.now()
					+ "\", \"did\":\"FMDevice30\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":22.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";

			String JmeterDevice1 = "{\"ver\":1.0, \"seq\":1, \"tms\":\"" + ZonedDateTime.now()
					+ "\", \"did\":\"JmeterDevice1\" ,\"dop\":1.3,\"lat\":" + ((random.nextDouble() * 90) + 10) / 1.0
					+ ",\"lng\":" + ((random.nextDouble() * 90) + 10) / 1.0 + ",\""
					+ "spd\":22.5,\"igs\":true,\"err\":1,\"evt\":\"TNT\"}";

			Message messageToSendFromDeviceToHub26 = new Message(FMDevice26);
			Message messageToSendFromDeviceToHub27 = new Message(FMDevice27);
			Message messageToSendFromDeviceToHub28 = new Message(FMDevice28);
			Message messageToSendFromDeviceToHub29 = new Message(FMDevice29);
			Message messageToSendFromDeviceToHub30 = new Message(FMDevice30);
			Message messageToSendFromDeviceToHubJM1 = new Message(JmeterDevice1);

			Message messageToSendFromDeviceToHub9 = new Message(FMDevice9);
			Message messageToSendFromDeviceToHub10 = new Message(FMDevice10);
			Message messageToSendFromDeviceToHub11 = new Message(FMDevice11);
			Message messageToSendFromDeviceToHub12 = new Message(FMDevice12);
			Message messageToSendFromDeviceToHub22 = new Message(FMDevice22);
			messagesList.add(messageToSendFromDeviceToHub22);
			messagesList.add(messageToSendFromDeviceToHub26);
			List<Message> messagesListForNotReported = new ArrayList<>();
			messagesListForNotReported.add(messageToSendFromDeviceToHub9);
			messagesListForNotReported.add(messageToSendFromDeviceToHub10);
			messagesListForNotReported.add(messageToSendFromDeviceToHub11);
			messagesListForNotReported.add(messageToSendFromDeviceToHub12);

			System.out.println("Sending message from device to IoT Hub...");
			ExecutorService executor = Executors.newFixedThreadPool(25);

			Future<Integer> future = (Future<Integer>) executor.submit(() -> {
				try {
					for (int i = 0, j = 0; i < 1500000; i++) {
						long start = System.currentTimeMillis();
						Thread.sleep(100);
						deviceClient.sendEventAsync(messageToSendFromDeviceToHubJM1, new IotHubEventCallbackImpl(),
								null);
//						deviceClient.sendEventAsync(messagesList.get(j), new IotHubEventCallbackImpl(), null);
						System.out.println("response time" + (System.currentTimeMillis() - start) + ",device data : "
								+ messagesList.get(j).getConnectionDeviceId());
						j = ++j % messagesList.size();
					}
				} catch (Exception e) {
					if (deviceClient != null) {
						try {
							deviceClient.closeNow();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					throw new IllegalStateException("task interrupted", e);
				}
			});

		} catch (IOException e) {
			System.out.println("Device client threw an exception: " + e);
			e.printStackTrace();

		}

		System.out.println("Press any key to exit...");

		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();

		System.out.println("Shutting down...");
		scanner.close();

	}

	private static void startDeviceTwin(DeviceClient deviceClient)
			throws IllegalArgumentException, UnsupportedOperationException, IOException {
		deviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), null, new onProperty(), null);
	}

	protected static class onProperty implements com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack {
		public void TwinPropertyCallBack(Property property, Object context) {
			System.out.println("onProperty callback for " + (property.getIsReported() ? "reported" : "desired")
					+ " property " + property.getKey() + " to " + property.getValue() + ", Properties version:"
					+ property.getVersion());
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
