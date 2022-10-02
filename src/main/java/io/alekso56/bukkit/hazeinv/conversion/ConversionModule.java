package io.alekso56.bukkit.hazeinv.conversion;

import java.util.ServiceLoader;

/**Conversion module requirements:
 * - transfer from and to other plugin format.
 * - must extend ConversionModule
 * - must be internal and registered as a service.
 * @author Alekso56
 *
 */
public interface ConversionModule {
	//all the registered handlers
	static ServiceLoader<ConversionModule> handlers = ServiceLoader.load(ConversionModule.class);
	
	//Convert from haze to another plugin
	boolean ToExternalSource();
	
	//Convert from another plugin to haze
	boolean FromExternalSource();
	
	String getPluginName();

}
