/**
 * DeviceInfo.java
 * Created: 13.01.2017
 * (c) 2017 Martin Dreier
 */
package de.martindreier.airtwitch.airplay;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import javax.jmdns.ServiceInfo;

/**
 * Holds information about a registered and resolved device.
 *
 * @author Martin Dreier <martin@martindreier.de>
 *
 */
public class DeviceInfo
{
	/**
	 * Device name.
	 */
	private final String							name;
	/**
	 * Qualified device name.
	 */
	private final String							qualifiedName;
	/**
	 * IPv4 addresses.
	 */
	private final List<Inet4Address>	inet4Addresses;
	/**
	 * IPv6 addresses.
	 */
	private final List<Inet6Address>	inet6Addresses;
	/**
	 * Connection port.
	 */
	private final int									port;
	/**
	 * Model information.
	 */
	private final Optional<String>		model;
	/**
	 * Device key.
	 */
	private String										key;

	/**
	 * Create device information object from service information.
	 *
	 * @param info
	 *          Information from resolved service.
	 */
	public DeviceInfo(ServiceInfo info)
	{
		if (info == null)
		{
			throw new IllegalArgumentException("Service informatio must not be null");
		}
		name = info.getName();
		key = info.getKey();
		qualifiedName = info.getQualifiedName();
		inet4Addresses = Arrays.asList(info.getInet4Addresses());
		inet6Addresses = Arrays.asList(info.getInet6Addresses());
		port = info.getPort();
		Enumeration<String> propertyNames = info.getPropertyNames();
		String modelName = null;
		while (propertyNames.hasMoreElements())
		{
			String propertyName = propertyNames.nextElement();
			if (propertyName.equals("model"))
			{
				modelName = info.getPropertyString(propertyName);
				break;
			}
		}
		model = Optional.ofNullable(modelName);
	}

	/**
	 * Get the device name.
	 *
	 * @return The device name.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Get the qualified device name.
	 *
	 * @return The qualified device name.
	 */
	public String getQualifiedName()
	{
		return qualifiedName;
	}

	/**
	 * Get the device model information, if available.
	 *
	 * @return The model information. If the information is not available the
	 *         {@link Optional} is empty.
	 */
	public Optional<String> getModel()
	{
		return model;
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append(qualifiedName);
		builder.append(" (");
		model.ifPresent(modelName -> builder.append(modelName).append(", "));
		for (Inet4Address inet4Address : inet4Addresses)
		{
			builder.append(inet4Address).append(", ");
		}
		for (Inet6Address inet6Address : inet6Addresses)
		{
			builder.append(inet6Address).append(", ");
		}
		builder.append("port ").append(port).append(")");
		return builder.toString();
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof DeviceInfo)
		{
			return key.equals(((DeviceInfo) other).key);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		return key.hashCode();
	}
}
