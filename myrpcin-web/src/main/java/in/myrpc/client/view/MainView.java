package in.myrpc.client.view;

import com.google.gwt.appengine.channel.client.Socket;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import in.myrpc.client.presenter.MainPresenter;
import java.util.ArrayList;
import java.util.Iterator;
import org.orgama.client.event.ClickHandlerArray;
import org.orgama.client.presenter.AuthInfoWidgetPresenter;
import org.orgama.client.view.OrgamaView;

/**
 * Implementation of the main view for this orgama project
 * @author kguthrie
 */
public class MainView extends OrgamaView
        implements MainPresenter.Display {

    private static ClickHandlerArray centerpointClickHandlers;

    public static native void exportJavascriptMethod() /*-{
     $wnd.centerpointLinkClick =
     $entry(@in.myrpc.client.view.MainView::centerpointLinkClick(Ljava/lang/String;));
     }-*/;

    /**
     * method that is called from javascript to indicate the click of a link
     *
     * @param centerpointName
     */
    public static void centerpointLinkClick(String centerpointName) {
        centerpointClickHandlers.fireEvent(
                new MainPresenter.CenterpointClickEvent(centerpointName));
    }

    @UiField
    HTMLPanel pnlAuth;

    @UiField
    Label lblStatus;

    @UiField
    HTMLPanel pnlAccounts;

    private Socket socket;

	@Override
	public void renderAuthPanel(AuthInfoWidgetPresenter authPanel) {
        pnlAuth.add(authPanel.getWidget());
        centerpointClickHandlers = new ClickHandlerArray();
    }

    @Override
    protected void onBind() {
        exportJavascriptMethod();
    }

    @Override
    public void renderStatus(String status) {
        lblStatus.setText(status);
    }

    @Override
    public void renderAccountSection(String name, String role,
            ArrayList<String> centerpointNames,
            ArrayList<String> centerpointLocators) {
        assert (centerpointLocators.size() == centerpointNames.size());

        StringBuilder html = new StringBuilder();

        html.append("<div style='padding-left:10px'><h3>");
        html.append(name);
        html.append("<span style='color:darkgray'> - ");
        html.append(role);
        html.append("</span></h3><ul>");

        Iterator<String> locatorIterator = centerpointLocators.iterator();
        Iterator<String> nameIterator = centerpointNames.iterator();

        while (locatorIterator.hasNext()) {
            String centerpointName = nameIterator.next();
            String centerpointLocator = locatorIterator.next();
            html.append("<li><a onclick='window.centerpointLinkClick(\"");
            html.append(centerpointLocator);
            html.append("\"); var e=document.getElementById(\"");
            html.append(centerpointLocator);
            html.append("\").style; e.display = e.display == \"none\" ? ");
            html.append("\"block\" : \"none\";' ");
            html.append("style='color:blue; cursor:pointer; ");
            html.append("text-decoration:underline;'>");
            html.append(centerpointName);
            html.append("</a><span style='color:darkgray'> - ");
            html.append(centerpointLocator);
            html.append("</span><div id='");
            html.append(centerpointLocator);
            html.append("' style='display:none'>loading...</div></li>");
        }

        html.append("</ul>");
        html.append("</div>");

        pnlAccounts.add(new HTML(
                SafeHtmlUtils.fromTrustedString(html.toString())));
    }

    @Override
    public HasClickHandlers getClickableCenterpointLink() {
        return centerpointClickHandlers;
    }

    @Override
    public void renderEndpointList(String centerpointId,
            ArrayList<String> endpointIds, ArrayList<String> endpointNames) {

        assert (endpointIds.size() == endpointNames.size());

        StringBuilder html = new StringBuilder();

        html.append("<ul style='margin-left:10px;'>");

        Iterator<String> idIterator = endpointIds.iterator();
        Iterator<String> nameIterator = endpointNames.iterator();

        while (idIterator.hasNext()) {
            String id = idIterator.next();
            String name = nameIterator.next();

            html.append("<li>");
            html.append(name);
            html.append(" <span style='color:darkgray'>- ");
            html.append(id);
            html.append("</li>");
        }

        html.append("</ul>");

        Element el = DOM.getElementById(centerpointId);
        el.setInnerSafeHtml(SafeHtmlUtils.fromTrustedString(html.toString()));
    }
}
