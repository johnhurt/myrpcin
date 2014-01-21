package in.myrpc.client.view;

import com.google.gwt.appengine.channel.client.Channel;
import com.google.gwt.appengine.channel.client.ChannelError;
import com.google.gwt.appengine.channel.client.ChannelFactory;
import com.google.gwt.appengine.channel.client.Socket;
import com.google.gwt.appengine.channel.client.SocketListener;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import in.myrpc.client.presenter.MainPresenter;
import in.myrpc.shared.action.MessageDevice;
import in.myrpc.shared.action.MessageDeviceResult;
import in.myrpc.shared.action.OpenChannel;
import in.myrpc.shared.action.OpenChannelResult;
import org.orgama.client.Dispatch;
import org.orgama.client.OrgAsyncCallback;
import org.orgama.client.except.ClientSideException;
import org.orgama.client.presenter.AuthInfoWidgetPresenter;
import org.orgama.client.view.OrgamaView;
import org.orgama.client.widget.WatermarkedTextBox;
import org.orgama.shared.Logger;

/**
 * Implementation of the main view for this orgama project
 * @author kguthrie
 */
public class MainView extends OrgamaView
        implements MainPresenter.Display, SocketListener {

    @UiField
    HTMLPanel pnlAuth;

    @UiField
    WatermarkedTextBox txtUsername;

    @UiField
    Button btnConnect;

    @UiField
    Label lblToken;

    @UiField
    WatermarkedTextBox txtMessage;

    @UiField
    WatermarkedTextBox txtTarget;

    @UiField
    Button btnSend;

    private Socket socket;

	@Override
	public void renderAuthPanel(AuthInfoWidgetPresenter authPanel) {
		pnlAuth.add(authPanel.getWidget());
	}

    @Override
    protected void onBind() {
        btnConnect.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Dispatch.dispatch(new OpenChannel(txtUsername.getText()),
                        new OrgAsyncCallback<OpenChannelResult>() {

                            @Override
                            public void onFailure(ClientSideException ex) {
                                Logger.error("", ex);
                            }

                            @Override
                            public void onSuccess(OpenChannelResult result) {
                                lblToken.setText("Your token is "
                                        + result.getToken());
                                ChannelFactory.createChannel(result.getToken(),
                                        new AsyncCallback<Channel>() {

                                            @Override
                                            public void onFailure(
                                                    Throwable caught) {
                                                Logger.error("", caught);
                                            }

                                            @Override
                                            public void onSuccess(
                                                    Channel result) {
                                                socket = result.open(
                                                        MainView.this);
                                            }
                                        });
                            }
                        });
            }
        });

        btnSend.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                Dispatch.dispatch(new MessageDevice(
                        txtMessage.getText(),
                        txtTarget.getText()),
                        new AsyncCallback<MessageDeviceResult>() {

                            @Override
                            public void onFailure(Throwable caught) {
                                Logger.error("Message send fail", caught);
                            }

                            @Override
                            public void onSuccess(MessageDeviceResult result) {

                            }
                        });
            }
        });
    }

    @Override
    public void onOpen() {
        Logger.info("Channel opened");
    }

    @Override
    public void onMessage(String message) {
        Logger.info("Message: " + message);
    }

    @Override
    public void onError(ChannelError error) {
        Logger.error("Error " + error.getCode());
    }

    @Override
    public void onClose() {
        Logger.info("Channel closed");
    }

}
