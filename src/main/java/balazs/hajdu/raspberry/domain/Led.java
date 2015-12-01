package balazs.hajdu.raspberry.domain;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;

/**
 * A GPIO client which can turn on or off a LED.
 *
 * @author Hajdu Balazs
 */
public class Led {

    private final GpioPinDigitalOutput led;

    public Led() {
        GpioController gpio = GpioFactory.getInstance();
        led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00);
    }

    public void lightsOn() {
        led.high();
    }

    public void lightsOff() {
        led.low();
    }
}
