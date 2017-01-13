/**
 * AirPlayServiceListener.java
 * Created: 13.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.airplay;

import java.util.function.Consumer;
import java.util.logging.Logger;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;

/**
 * Listener for events of the AirPlay service.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class AirPlayServiceListener implements ServiceListener
{
	/**
	 * Log instance.
	 */
	private static final Logger		log	= Logger.getLogger(AirPlayServiceListener.class.getName());
	private Consumer<DeviceInfo>	deviceResolvedListener;

	/**
	 * Create a new listener.
	 *
	 * @param deviceResolvedListener
	 *          Callback will be called when a device (service) is resolved.
	 */
	public AirPlayServiceListener(Consumer<DeviceInfo> deviceResolvedListener)
	{
		if (deviceResolvedListener == null)
		{
			throw new IllegalArgumentException("Device listener must not be null");
		}
		this.deviceResolvedListener = deviceResolvedListener;
	}

	/**
	 * @see javax.jmdns.ServiceListener#serviceAdded(javax.jmdns.ServiceEvent)
	 */
	@Override
	public void serviceAdded(ServiceEvent event)
	{
		log.entering(this.getClass().getName(), "serviceAdded", event);
		log.fine(() -> String.format("Service added: name %s; type %s", event.getName(), event.getType()));
		log.exiting(this.getClass().getName(), "serviceAdded");
	}

	@Override
	public void serviceRemoved(ServiceEvent event)
	{
		log.entering(this.getClass().getName(), "serviceRemoved", event);
		log.fine(() -> String.format("Service removed: name %s; type %s", event.getName(), event.getType()));
		log.exiting(this.getClass().getName(), "serviceRemoved");
	}

	@Override
	public void serviceResolved(ServiceEvent event)
	{
		log.entering(this.getClass().getName(), "serviceResolved", event);
		log.fine(() -> String.format("Service resolved: name %s; type %s", event.getName(), event.getType()));
		DeviceInfo info = new DeviceInfo(event.getInfo());
		log.info(() -> String.format("New device resolved: %s", info.toString()));
		deviceResolvedListener.accept(info);
		log.exiting(this.getClass().getName(), "serviceResolved");
	}

}
