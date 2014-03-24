package in.myrpc.server.config;

import com.google.inject.Singleton;
import in.myrpc.server.ChannelPresenceServlet;
import in.myrpc.server.EmailVerificationServlet;
import in.myrpc.server.EndpointConnectionServlet;
import in.myrpc.server.EndpointProvisioningServlet;
import in.myrpc.server.RpcServlet;
import in.myrpc.server.handler.GetUserInfoHandler;
import in.myrpc.server.service.AccountService;
import in.myrpc.server.service.AccountServiceImpl;
import in.myrpc.server.service.CenterpointService;
import in.myrpc.server.service.CenterpointServiceImpl;
import in.myrpc.server.service.EmailService;
import in.myrpc.server.service.EmailServiceImpl;
import in.myrpc.server.service.EndpointService;
import in.myrpc.server.service.EndpointServiceImpl;
import in.myrpc.server.service.LocatorService;
import in.myrpc.server.service.LocatorServiceImpl;
import in.myrpc.server.service.PermissionService;
import in.myrpc.server.service.PermissionServiceImpl;
import in.myrpc.server.service.PooledChannelService;
import in.myrpc.server.service.PooledChannelServiceImpl;
import in.myrpc.server.service.UserService;
import in.myrpc.server.service.UserServiceImpl;
import in.myrpc.shared.action.GetUserInfo;
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

        bindHandler(GetUserInfo.class, GetUserInfoHandler.class);
	}

	@Override
	protected void addServerBindings() {
        bindAuthSourceProvider();
        bindServices();
        bindServlets();
	}

    /**
     * Bind the data service
     */
    protected void bindServices() {
        bind(PooledChannelService.class)
                .to(PooledChannelServiceImpl.class)
                .in(Singleton.class);
        bind(EmailService.class)
                .to(EmailServiceImpl.class)
                .in(Singleton.class);
        bind(UserService.class)
                .to(UserServiceImpl.class)
                .in(Singleton.class);
        bind(AccountService.class)
                .to(AccountServiceImpl.class)
                .in(Singleton.class);
        bind(PermissionService.class)
                .to(PermissionServiceImpl.class)
                .in(Singleton.class);
        bind(CenterpointService.class)
                .to(CenterpointServiceImpl.class)
                .in(Singleton.class);
        bind(EndpointService.class)
                .to(EndpointServiceImpl.class)
                .in(Singleton.class);
        bind(LocatorService.class)
                .to(LocatorServiceImpl.class)
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

    /**
     * Bind the servlets served by this app
     */
    protected void bindServlets() {

        bind(ChannelPresenceServlet.class)
                .in(Singleton.class);

        bind(EmailVerificationServlet.class)
                .in(Singleton.class);

        bind(EndpointProvisioningServlet.class)
                .in(Singleton.class);

        bind(EndpointConnectionServlet.class)
                .in(Singleton.class);

        bind(RpcServlet.class)
                .in(Singleton.class);

        serve("/_ah/channel/connected/", "/_ah/channel/disconnected/")
                .with(ChannelPresenceServlet.class);
    }

}
