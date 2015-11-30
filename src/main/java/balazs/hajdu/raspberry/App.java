package balazs.hajdu.raspberry;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Hello world!
 *
 */
public class App {
	// TODO setup twitter
	private static Twitter twitter;

	private static void configureSensor(final GpioPinDigitalOutput led, final GpioPinDigitalInput sensor) {
		sensor.addListener(new GpioPinListenerDigital() {

			private void handleSensorInput(final GpioPinDigitalOutput led, final GpioPinDigitalStateChangeEvent event) {
				if (event.getState().isLow()) {
					notifyLightsOff(twitter, led);
				} else {
					notifyLightsOn(twitter, led);
				}
			}

			private void notifyLightsOn(final Twitter twitter, final GpioPinDigitalOutput led) {
				led.low();
				try {
					twitter.updateStatus("led low");
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}

			private void notifyLightsOff(final Twitter twitter, final GpioPinDigitalOutput led) {
				led.high();
				try {
					twitter.updateStatus("led high");
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				handleSensorInput(led, event);
			}
		});
	}

	public static void main(String[] args) {
		final GpioController gpio = GpioFactory.getInstance();
		final GpioPinDigitalOutput led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06);
		final GpioPinDigitalInput sensor = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01);
		try {
			Document document = Jsoup.connect("http://sports.yahoo.com/nba/scoreboard/").get();
			System.out.println(document.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		configureSensor(led, sensor);
		System.out.println("Hello World!");
	}
}
