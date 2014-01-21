package in.myrpc.server.config;

import org.orgama.server.config.BaseServletContextListener;


/**
 * Context Listener for this application.  This is where the guice bindings
 * contained in this extension module as well as any other module are 
 * registered so that they can be injected from the beginning.
 */
public class AppContextListener extends BaseServletContextListener {

	@Override
	protected void addExtensions() {

		//Add this app's configuration
		add(new AppServerConfig());

		//Add the server modules for any other orgama extensions here
		//add(new FancyExtension());
	}
}
