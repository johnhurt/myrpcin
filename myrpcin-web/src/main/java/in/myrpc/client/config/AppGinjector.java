package in.myrpc.client.config;

import com.google.gwt.inject.client.GinModules;
import com.gwtplatform.dispatch.client.gin.DispatchAsyncModule;
import org.orgama.client.config.OrgGinModule;
import org.orgama.client.config.OrgGinjector;
import org.orgama.structure.client.OrgamaStructureModule;
import org.orgama.structure.client.OrgamaStructureInjector;


/**
 * This is client side injector for the application.
 */
@GinModules({
		DispatchAsyncModule.class, 
		OrgGinModule.class, 
		AppConfig.class, 
		OrgamaStructureModule.class})
public interface AppGinjector extends OrgGinjector, OrgamaStructureInjector {


}
