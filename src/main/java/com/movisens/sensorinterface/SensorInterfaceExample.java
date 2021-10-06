package com.movisens.sensorinterface;

import java.io.IOException;
import java.util.List;

import de.movisens.sensorinterface.ConnectorFactory;
import de.movisens.sensorinterface.FirmwareAvailability;
import de.movisens.sensorinterface.MeasurementConfiguration;
import de.movisens.sensorinterface.MeasurementInformation;
import de.movisens.sensorinterface.MeasurementStatus;
import de.movisens.sensorinterface.ParticipantInfo;
import de.movisens.sensorinterface.Sensor;

public class SensorInterfaceExample {

	public static void main(String[] args) throws IOException {

		System.setProperty("java.util.logging.config.file", "logging.properties");

		List<Sensor> sensors = ConnectorFactory.getConnector().getSensors();

		for (Sensor sensor : sensors) {

			if (sensor.connect()) {

				try (sensor) {

					/*
					 * show information regarding sensor and measurement
					 */
					MeasurementStatus measurementStatus = sensor.getMeasurementStatus();
					MeasurementInformation measurementInformation = sensor.getMeasurementInformation();
					System.out.println("UniqueId: " + sensor.getUniqueId());
					System.out.println("SerialNumber: " + sensor.getSerialNumber());
					System.out.println("Battery: " + sensor.getBatteryStatus() + "%");
					System.out.println("MeasurementStatus: " + measurementStatus);
					System.out.println(measurementInformation);

					if (measurementStatus == MeasurementStatus.STOPPED) {

						/*
						 * download measurement if available
						 */
						if (measurementInformation.getRecordedDuration() > 0) {

							String path = "build/" + System.currentTimeMillis();
							String measurementId = "participant0001";

							sensor.saveAll(path, measurementId, p -> {
								System.out.println("Progress: " + p + "%");
							});

							sensor.deleteData();
						}

						/*
						 * update firmware if newer firmware is available
						 */
						if (sensor.checkFirmware() == FirmwareAvailability.FIRMWARE_NEWER_AVAILABLE) {

							if (sensor.updateFirmware()) {
								System.out.println("sensor firmware updated");
							}
						}

						/*
						 * switch led on and off
						 */
						sensor.setLedRed(true);
						sensor.setLedRed(false);

						if (measurementInformation.getRecordedDuration() == 0) {

							/*
							 * set participant for next measurement
							 */
							ParticipantInfo participantInfo = new ParticipantInfo();
							participantInfo.age = 43;
							sensor.setParticipantInfo(participantInfo);

							/*
							 * start new measurement
							 */
							MeasurementConfiguration measurementConfiguration = new MeasurementConfiguration();
							measurementConfiguration.setDuration(60);

							if (sensor.startMeasurement(measurementConfiguration)) {
								System.out.println("Measurement started");
							}
						}
					}
				}
			}
		}
	}
}
