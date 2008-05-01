package org.pentaho.ui.database.event;

import java.io.InputStream;

import org.pentaho.di.core.database.DatabaseInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.database.Messages;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulDomException;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.impl.XulEventHandler;

/**
 * Fragment handler deals with the logistics of replacing a portion of the dialog 
 * from a XUL fragment when the combination of database connection type and database 
 * access method calls for a replacement.
 *  
 * @author gmoran
 * @created Mar 19, 2008
 */
public class FragmentHandler extends XulEventHandler {
  
  private XulListbox connectionBox;
  private XulListbox accessBox;

  private String packagePath = "org/pentaho/ui/database/"; //$NON-NLS-1$
  
  public FragmentHandler() {
  }
  
  private void loadDatabaseOptionsFragment(String fragmentUri) throws XulException{
    
    
    XulComponent groupElement = document.getElementById("database-options-box"); //$NON-NLS-1$
    XulComponent parentElement = groupElement.getParent();

    XulDomContainer fragmentContainer = null;

    try {
      
      // Get new group box fragment ...
      // This will effectively set up the SWT parent child relationship...
      
      fragmentContainer = this.xulDomContainer.loadFragment(fragmentUri, Messages.getBundle());
      XulComponent newGroup = fragmentContainer.getDocumentRoot().getFirstChild();
      parentElement.replaceChild(groupElement, newGroup);
      
    } catch (XulException e) {
      e.printStackTrace();
      throw e;
    } catch (XulDomException e) {
      e.printStackTrace();
      throw new XulException(e);
    }
    
    if (fragmentContainer == null){
      return;
    }
    
  }
  
  /**
   * This method handles the resource-like loading of the XUL
   * fragment definitions based on connection type and access 
   * method. If there is a common definition, and no connection
   * specific override definition, then the common definition is used. 
   * Connection specific definition resources follow the naming 
   * pattern [connection type code]_[access method].xul.  
   */
  public void refreshOptions(){

    connectionBox = (XulListbox)document.getElementById("connection-type-list"); //$NON-NLS-1$
    accessBox = (XulListbox)document.getElementById("access-type-list"); //$NON-NLS-1$
    
    Object connectionKey = connectionBox.getSelectedItem();
    DatabaseInterface database = DataHandler.connectionMap.get(connectionKey);
    
    Object accessKey = accessBox.getSelectedItem();
    int access = DatabaseMeta.getAccessType((String)accessKey);
    
    String fragment = null;

    DataHandler dataHandler=null;
    try {
      dataHandler = (DataHandler)xulDomContainer.getEventHandler("dataHandler"); //$NON-NLS-1$
      dataHandler.pushCache();
    } catch (XulException e) {
      // TODO not a critical function, but should log a problem...
    }

    switch(access){
      case DatabaseMeta.TYPE_ACCESS_JNDI:
        fragment = getFragment(database, "_jndi.xul", "common_jndi.xul"); //$NON-NLS-1$ //$NON-NLS-2$
        break;
      case DatabaseMeta.TYPE_ACCESS_NATIVE:
        fragment = getFragment(database, "_native.xul", "common_native.xul"); //$NON-NLS-1$ //$NON-NLS-2$
        break;
      case DatabaseMeta.TYPE_ACCESS_OCI:
        fragment = getFragment(database, "_oci.xul", "common_native.xul"); //$NON-NLS-1$ //$NON-NLS-2$
        break;
      case DatabaseMeta.TYPE_ACCESS_ODBC:
        fragment = getFragment(database, "_odbc.xul", "common_odbc.xul"); //$NON-NLS-1$ //$NON-NLS-2$
        break;
      case DatabaseMeta.TYPE_ACCESS_PLUGIN:
        fragment = getFragment(database, "_plugin.xul", "common_native.xul"); //$NON-NLS-1$ //$NON-NLS-2$
        break;
    }
    
    try {
      loadDatabaseOptionsFragment(fragment.toLowerCase());
    } catch (XulException e) {
      // TODO should be reporting as an error dialog; need error dialog in XUL framework
      XulMessageBox messageBox = xulDomContainer.createMessageBox(
                Messages.getString("FragmentHandler.USER.CANT_LOAD_OPTIONS", database.getDatabaseTypeDescLong())); //$NON-NLS-1$
      messageBox.open();
    }

    XulTextbox portBox = (XulTextbox)document.getElementById("port-number-text"); //$NON-NLS-1$
    if (portBox != null){
      int port = database.getDefaultDatabasePort();
      if (port > 0){
        portBox.setValue(Integer.toString(port));
      }
    }
    
   if (dataHandler != null){
     dataHandler.popCache();
   }
    
  }
  
  private String getFragment(DatabaseInterface database, String extension, String defaultFragment ){
    String fragment = packagePath.concat(database.getDatabaseTypeDesc()).concat(extension);
    InputStream in = getClass().getClassLoader().getResourceAsStream(fragment.toLowerCase());
    if (in == null){
      fragment = packagePath.concat(defaultFragment);
    }
    return fragment;
  }

  @Override
  public Object getData() {
    return null;
  }

  @Override
  public void setData(Object arg0) {
  }


}
