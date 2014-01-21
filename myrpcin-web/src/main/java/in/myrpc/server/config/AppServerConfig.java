package in.myrpc.server.config;

import com.google.inject.Singleton;
import in.myrpc.server.handler.MessageDeviceHandler;
import in.myrpc.server.handler.OpenChannelHandler;
import in.myrpc.server.service.DeviceService;
import in.myrpc.server.service.DeviceServiceImpl;
import in.myrpc.shared.action.MessageDevice;
import in.myrpc.shared.action.OpenChannel;
import org.orgama.server.auth.IFacebookUserService;
import org.orgama.server.auth.source.FacebookAndGoogleAuthServiceProvider;
import org.orgama.server.auth.source.IAuthServiceProvider;
import org.orgama.server.config.BaseExtensionModule;

/**
 * Configuration for the server side dependency injections.  Two methods are
 *
 */
public class AppServerConfig extends BaseExtensionModule {

	@Override
    protected void addHandlerBindings() {
        bindHandler(OpenChannel.class, OpenChannelHandler.class);
        bindHandler(MessageDevice.class, MessageDeviceHandler.class);
	}

	@Override
	protected void addServerBindings() {
        bindAuthSourceProvider();
        bindServices();
	}

    protected void bindServices() {
        bind(DeviceService.class)
                .to(DeviceServiceImpl.class)
                .in(Singleton.class);
    }

	/**
	 * single overridable method for testing against a certain auth source
	 * provider
	 */
	protected void bindAuthSourceProvider() {
		bind(IAuthServiceProvider.class)
				.to(FacebookAndGoogleAuthServiceProvider.class);

		bindConstant()
				.annotatedWith(IFacebookUserService.ApplictionId.class)
                .to("YourFacebookApplicationId");

    }

}
