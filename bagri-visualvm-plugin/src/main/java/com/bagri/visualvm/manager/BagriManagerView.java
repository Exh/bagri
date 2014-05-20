package com.bagri.visualvm.manager;

import com.bagri.visualvm.manager.ui.BagriMainPanel;
import com.bagri.visualvm.manager.util.Icons;
import com.sun.tools.visualvm.application.Application;
import com.sun.tools.visualvm.core.ui.DataSourceView;
import com.sun.tools.visualvm.core.ui.components.DataViewComponent;
import com.sun.tools.visualvm.tools.jmx.JmxModel;
import com.sun.tools.visualvm.tools.jmx.JmxModelFactory;
import org.openide.util.WeakListeners;

import javax.management.MBeanServerConnection;
import javax.swing.*;

public class BagriManagerView extends DataSourceView {
    private static final String BAGRI_MANAGER = "Bagri Manager";
    private static final String NOT_CONNECTED = "Not Connected";

    private Application application;
    private final MBeanServerConnection mbsc;
    private BagriMainPanel mainPanel;

    public BagriManagerView(Application application) {
        super(application, BAGRI_MANAGER, Icons.MAIN_ICON.getImage(), 60, false);
        this.application = application;
        JmxModel jmx = JmxModelFactory.getJmxModelFor(application);
        this.mbsc = jmx.getMBeanServerConnection();
    }


    @Override
    protected void removed() {
        if (mainPanel != null) {
            mainPanel.dispose();
        }
    }

    protected DataViewComponent createComponent() {
        DataViewComponent dvc;
        JmxModel jmx = JmxModelFactory.getJmxModelFor(application);
        if (jmx == null || jmx.getConnectionState() != JmxModel.ConnectionState.CONNECTED) {
            JTextArea textArea = new JTextArea();
            textArea.setBorder(BorderFactory.createEmptyBorder(25, 9, 9, 9));
            textArea.setEditable(false);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setText(NOT_CONNECTED);
            dvc = new DataViewComponent(
                    new DataViewComponent.MasterView(BAGRI_MANAGER, null, textArea), // NOI18N
                    new DataViewComponent.MasterViewConfiguration(true));
        } else {
            DataViewComponent.MasterView monitoringMasterView = new DataViewComponent.MasterView(BAGRI_MANAGER, null, new JLabel(" ")); // NOI18N
            DataViewComponent.MasterViewConfiguration monitoringMasterConfiguration = new DataViewComponent.MasterViewConfiguration(false);
            dvc = new DataViewComponent(monitoringMasterView, monitoringMasterConfiguration);

            dvc.configureDetailsView(new DataViewComponent.DetailsViewConfiguration(0.33, 0, -1, -1, -1, -1));

            dvc.configureDetailsArea(new DataViewComponent.DetailsAreaConfiguration(BAGRI_MANAGER, false), DataViewComponent.TOP_LEFT); // NOI18N

            mainPanel = new BagriMainPanel(mbsc);
            jmx.addPropertyChangeListener(WeakListeners.propertyChange(mainPanel, jmx));

            dvc.addDetailsView(new DataViewComponent.DetailsView("", null, 10, mainPanel, null), DataViewComponent.TOP_LEFT); // NOI18N
        }
        return dvc;
    }
}
