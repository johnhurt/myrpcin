package in.myrpc.client;

import in.myrpc.client.config.AppGinjector;
import org.orgama.client.OrgEntryPoint;
import org.orgama.client.config.OrgGinjector;
import com.google.gwt.core.client.GWT;

/**
 * Entry point classes define
 * <code>onModuleLoad()</code>.
 */
public class AppMain extends OrgEntryPoint {
    
    /**
     * The applications entrypoint should provide a reference to the class
     * representing the application's injector
     * @return 
     */
    @Override
    protected OrgGinjector getInjector() {
        return GWT.create(AppGinjector.class);
    }
    
    
    /**
     * called before the injector is initialized.  
     */
    @Override
    protected void beforeInjection() {
        
    }
    
    /**
     * called after the injector is initialized.
     */
    @Override
    protected void afterInjection() {
        
    }
    
}