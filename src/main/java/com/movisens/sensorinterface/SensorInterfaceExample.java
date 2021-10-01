package com.movisens.sensorinterface;

import java.io.IOException;
import java.util.List;

import de.movisens.sensorinterface.ConnectorFactory;
import de.movisens.sensorinterface.MeasurementConfiguration;
import de.movisens.sensorinterface.MeasurementInformation;
import de.movisens.sensorinterface.MeasurementStatus;
import de.movisens.sensorinterface.Sensor;

public class SensorInterfaceExample {

	public static void main(String[] args) throws IOException {

		List<Sensor> sensors = ConnectorFactory.getConnector().getSensors();

		for (Sensor sensor : sensors) {

			if (sensor.connect()) {

				try (sensor) {

					System.out.println("sensor detected: " + sensor.getUniqueId());

					System.out.println("SerialNumber: " + sensor.getSerialNumber());

					System.out.println("Battery: " + sensor.getBatteryStatus() + "%");

					MeasurementStatus measurementStatus = sensor.getMeasurementStatus();
					System.out.println("MeasurementStatus: " + measurementStatus);

					if (measurementStatus == MeasurementStatus.STOPPED) {

						MeasurementInformation measurementInformation = sensor.getMeasurementInformation();
						System.out.println(measurementInformation);

						if (measurementInformation.getNumWrittenSectors() > 0) {

							String path = "build/" + System.currentTimeMillis();
							String measurementId = "participant0001";

							sensor.saveAll(path, measurementId, p -> {
								System.out.println(p + "%");
							});
						}

						sensor.deleteData();

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
