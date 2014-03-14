package in.myrpc.client.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.Proxy;
import in.myrpc.shared.action.GetUserInfo;
import in.myrpc.shared.action.GetUserInfoResult;
import in.myrpc.shared.model.Account;
import in.myrpc.shared.model.Centerpoint;
import in.myrpc.shared.model.Role;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.orgama.client.Dispatch;
import org.orgama.client.OrgAsyncCallback;
import org.orgama.client.annotation.DisableCodeSplit;
import org.orgama.client.annotation.NameToken;
import org.orgama.client.except.ClientSideException;
import org.orgama.client.presenter.AuthInfoWidgetPresenter;
import org.orgama.client.presenter.OrgamaPresenter;
import org.orgama.shared.Logger;
import org.orgama.shared.auth.model.AuthState;
import org.orgama.shared.auth.model.ICompleteAuthState;

/**
 * Main/Initial presenter for the app. This presenter is bound to the empty name
 * token by default, so www.your-app.com/ will load this presenter and its
 * associated view
 */
@NameToken("")
@DisableCodeSplit
public class MainPresenter extends OrgamaPresenter<MainPresenter.Display> {

    /**
     * Interface that the view or any mocks will need to conform to
     */
    public interface Display extends View {

        void renderAuthPanel(AuthInfoWidgetPresenter authPanel);

        void renderStatus(String status);

        void renderAccountSection(String name, String role,
                ArrayList<String> centerpointNames,
                ArrayList<String> centerpointLocators);

        HasClickHandlers getClickableCenterpointLink();

        void renderEndpointList(String centerpoint,
                ArrayList<String> endpointIds, ArrayList<String> endpointNames);
    }

    private AuthInfoWidgetPresenter authPresenter;
    private ICompleteAuthState authState;

    private final HashMap<Long, HashMap<String, Centerpoint>> centerpointsByAccountId;
    private final HashMap<String, Account> accountsByName;

    public MainPresenter(Display view, Proxy proxy,
            AuthInfoWidgetPresenter authPresenter,
            ICompleteAuthState authState) {
        super(view, proxy);
        this.authPresenter = authPresenter;
        this.authState = authState;
        centerpointsByAccountId
                = new HashMap<Long, HashMap<String, Centerpoint>>();
        accountsByName = new HashMap<String, Account>();
    }

    @Override
    protected void onFirstLoad() {
    }

    @Override
    protected void bindToView(Display view) {
        view.renderAuthPanel(authPresenter);

        handleStatus(view);

        view.getClickableCenterpointLink().addClickHandler(
                new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent event) {
                        onCenterpoinClick(
                                ((MainPresenter.CenterpointClickEvent) event)
                                .getCenterpointId());
                    }
                });

    }

    private void handleStatus(final Display view) {

        if (authState.getAuthState() != AuthState.authenticated) {
            view.renderStatus("Please sign in or register to get started");
            return;
        }

        view.renderStatus("Checking account status ...");

        Dispatch.dispatch(new GetUserInfo(),
                new OrgAsyncCallback<GetUserInfoResult>() {

                    @Override
                    public void onFailure(ClientSideException ex) {
                        Logger.error("Failed to get user info", ex);
                    }

                    @Override
                    public void onSuccess(GetUserInfoResult result) {
                        if (result.getUser() == null) {
                            view.renderStatus("Your account is in the process "
                                    + "of verification.  Please click the link "
                                    + "in the email sent to your address");
                        }
                        else {
                            view.renderStatus("Showing accounts");
                            handleAccountDisplay(view, result.getAccounts(),
                                    result.getCenterpoints());
                        }
                    }
                });
    }

    /**
     * handle the display of verified user information including accounts,
     * permissions, and centerpoints
     *
     * @param accounts
     * @param centerpoints
     */
    private void handleAccountDisplay(Display view,
            EnumMap<Role, ArrayList<Account>> accounts,
            HashMap<String, Centerpoint> centerpoints) {

        assert (accounts != null);
        assert (centerpoints != null);

        for (Map.Entry<String, Centerpoint> entry : centerpoints.entrySet()) {
            Centerpoint centerpoint = entry.getValue();
            String locator = entry.getKey();

            assert (locator != null);
            assert (centerpoint != null);

            Long id = centerpoint.getAccountRef().getKey().getId();
            HashMap<String, Centerpoint> cpMap
                    = centerpointsByAccountId.get(id);

            if (cpMap == null) {
                cpMap = new HashMap<String, Centerpoint>();
                centerpointsByAccountId.put(id, cpMap);
            }

            cpMap.put(locator, centerpoint);
        }

        Role[] roleDisplayOrder = new Role[]{
            Role.owner
        };

        EnumMap<Role, String> roleNameMap
                = new EnumMap<Role, String>(Role.class);

        roleNameMap.put(Role.owner, "Owner");

        for (Role role : roleDisplayOrder) {
            String roleName = roleNameMap.get(role);

            ArrayList<Account> accountList = accounts.get(role);
            assert (accountList != null);

            for (Account account : accountList) {
                assert (account != null);
                HashMap<String, Centerpoint> centerpointsForAccount
                        = centerpointsByAccountId.get(account.getId());

                ArrayList<String> centerPointNames = new ArrayList<String>();
                ArrayList<String> centerPointLocators = new ArrayList<String>();

                for (Map.Entry<String, Centerpoint> entry
                        : centerpointsForAccount.entrySet()) {
                    Centerpoint centerpoint = entry.getValue();
                    String locator = entry.getKey();

                    centerPointNames.add(centerpoint.getName());
                    centerPointLocators.add(locator);
                }

                view.renderAccountSection(account.getName(), roleName,
                        centerPointNames, centerPointLocators);
            }

        }

    }

    /**
     * handles the click of a centerpoint name in the display
     *
     * @param centerpoinName
     */
    private void onCenterpoinClick(String centerpoinId) {

        ArrayList<String> endpointIds = new ArrayList<String>();
        ArrayList<String> endpointNames = new ArrayList<String>();

        endpointIds.add("3435-3456-35363652345-3454");
        endpointIds.add("5u0s-asdf-sgy5uovd0gs-s4fi");

        endpointNames.add("Endpoint 1");
        endpointNames.add("Endpoint 2");

        getView().renderEndpointList(centerpoinId, endpointIds, endpointNames);

    }

    /**
     * special click event that has a centerpoint name in it. We'll cut this
     */
    public static class CenterpointClickEvent extends ClickEvent {

        private String centerpointId;

        public CenterpointClickEvent(String centerpointId) {
            this.centerpointId = centerpointId;
        }

        /**
         * @return the centerpointName
         */
        public String getCenterpointId() {
            return centerpointId;
        }

    }


}
