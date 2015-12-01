package balazs.hajdu.raspberry;

import balazs.hajdu.raspberry.domain.Sensor;

/**
 * Simple application which reads data from a bmp180 temperature sensor through i2c protocol.
 *
 * @author Hajdu Balazs
 */
public class App {

	public static void main(String[] args) {
		System.out.println("The temperature monitoring has started.");
		System.out.println("To stop the program, please use CTRL+C");
		Sensor sensor = new Sensor();
	}
}
