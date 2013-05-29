
package org.openbravo.erpWindows.PriceList;


import org.openbravo.erpCommon.reference.*;



import org.codehaus.jettison.json.JSONObject;
import org.openbravo.erpCommon.utility.*;
import org.openbravo.data.FieldProvider;
import org.openbravo.utils.FormatUtilities;
import org.openbravo.utils.Replace;
import org.openbravo.base.secureApp.HttpSecureAppServlet;
import org.openbravo.base.secureApp.VariablesSecureApp;
import org.openbravo.base.exception.OBException;
import org.openbravo.scheduling.ProcessBundle;
import org.openbravo.scheduling.ProcessRunner;
import org.openbravo.erpCommon.businessUtility.WindowTabs;
import org.openbravo.xmlEngine.XmlDocument;
import java.util.Vector;
import java.util.StringTokenizer;
import org.openbravo.database.SessionInfo;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.sql.Connection;
import org.apache.log4j.Logger;

public class PriceListVersion extends HttpSecureAppServlet {
  private static final long serialVersionUID = 1L;
  
  private static Logger log4j = Logger.getLogger(PriceListVersion.class);
  
  private static final String windowId = "146";
  private static final String tabId = "238";
  private static final String defaultTabView = "RELATION";
  private static final int accesslevel = 3;
  private static final String moduleId = "0";
  
  @Override
  public void init(ServletConfig config) {
    setClassInfo("W", tabId, moduleId);
    super.init(config);
  }
  
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) throws IOException,
      ServletException {
    VariablesSecureApp vars = new VariablesSecureApp(request);
    String command = vars.getCommand();
    
    boolean securedProcess = false;
    if (command.contains("BUTTON")) {
      try {
        securedProcess = "Y".equals(org.openbravo.erpCommon.businessUtility.Preferences
            .getPreferenceValue("SecuredProcess", true, vars.getClient(), vars.getOrg(), vars
                .getUser(), vars.getRole(), windowId));
      } catch (PropertyException e) {
      }
    
     
      if (command.contains("103")) {
        SessionInfo.setProcessType("P");
        SessionInfo.setProcessId("103");
        SessionInfo.setModuleId("0");
        if (securedProcess) {
          classInfo.type = "P";
          classInfo.id = "103";
        }
      }
     
     
    }
    if (!securedProcess) {
      setClassInfo("W", tabId, moduleId);
    }
    super.service(request, response);
  }
  

  public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException,ServletException {
    TableSQLData tableSQL = null;
    VariablesSecureApp vars = new VariablesSecureApp(request);
    Boolean saveRequest = (Boolean) request.getAttribute("autosave");
    
    if(saveRequest != null && saveRequest){
      String currentOrg = vars.getStringParameter("inpadOrgId");
      String currentClient = vars.getStringParameter("inpadClientId");
      boolean editableTab = (!org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(), tabId)
                            && (currentOrg.equals("") || Utility.isElementInList(Utility.getContext(this, vars,"#User_Org", windowId, accesslevel), currentOrg)) 
                            && (currentClient.equals("") || Utility.isElementInList(Utility.getContext(this, vars, "#User_Client", windowId, accesslevel),currentClient)));
    
        OBError myError = new OBError();
        String commandType = request.getParameter("inpCommandType");
        String strmPricelistVersionId = request.getParameter("inpmPricelistVersionId");
         String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");
        if (editableTab) {
          int total = 0;
          
          if(commandType.equalsIgnoreCase("EDIT") && !strmPricelistVersionId.equals(""))
              total = saveRecord(vars, myError, 'U', strPM_PriceList_ID);
          else
              total = saveRecord(vars, myError, 'I', strPM_PriceList_ID);
          
          if (!myError.isEmpty() && total == 0)     
            throw new OBException(myError.getMessage());
        }
        vars.setSessionValue(request.getParameter("mappingName") +"|hash", vars.getPostDataHash());
        vars.setSessionValue(tabId + "|Header.view", "EDIT");
        
        return;
    }
    
    try {
      tableSQL = new TableSQLData(vars, this, tabId, Utility.getContext(this, vars, "#AccessibleOrgTree", windowId, accesslevel), Utility.getContext(this, vars, "#User_Client", windowId), Utility.getContext(this, vars, "ShowAudit", windowId).equals("Y"));
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    String strOrderBy = vars.getSessionValue(tabId + "|orderby");
    if (!strOrderBy.equals("")) {
      vars.setSessionValue(tabId + "|newOrder", "1");
    }

    if (vars.commandIn("DEFAULT")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID", "");

      String strM_PriceList_Version_ID = vars.getGlobalVariable("inpmPricelistVersionId", windowId + "|M_PriceList_Version_ID", "");
            if (strPM_PriceList_ID.equals("")) {
        strPM_PriceList_ID = getParentID(vars, strM_PriceList_Version_ID);
        if (strPM_PriceList_ID.equals("")) throw new ServletException("Required parameter :" + windowId + "|M_PriceList_ID");
        vars.setSessionValue(windowId + "|M_PriceList_ID", strPM_PriceList_ID);

        refreshParentSession(vars, strPM_PriceList_ID);
      }


      String strView = vars.getSessionValue(tabId + "|PriceListVersion.view");
      if (strView.equals("")) {
        strView = defaultTabView;

        if (strView.equals("EDIT")) {
          if (strM_PriceList_Version_ID.equals("")) strM_PriceList_Version_ID = firstElement(vars, tableSQL);
          if (strM_PriceList_Version_ID.equals("")) strView = "RELATION";
        }
      }
      if (strView.equals("EDIT")) 

        printPageEdit(response, request, vars, false, strM_PriceList_Version_ID, strPM_PriceList_ID, tableSQL);

      else printPageDataSheet(response, vars, strPM_PriceList_ID, strM_PriceList_Version_ID, tableSQL);
    } else if (vars.commandIn("DIRECT")) {
      String strM_PriceList_Version_ID = vars.getStringParameter("inpDirectKey");
      
        
      if (strM_PriceList_Version_ID.equals("")) strM_PriceList_Version_ID = vars.getRequiredGlobalVariable("inpmPricelistVersionId", windowId + "|M_PriceList_Version_ID");
      else vars.setSessionValue(windowId + "|M_PriceList_Version_ID", strM_PriceList_Version_ID);
      
      
      String strPM_PriceList_ID = getParentID(vars, strM_PriceList_Version_ID);
      
      vars.setSessionValue(windowId + "|M_PriceList_ID", strPM_PriceList_ID);
      vars.setSessionValue("191|Price List.view", "EDIT");

      refreshParentSession(vars, strPM_PriceList_ID);

      vars.setSessionValue(tabId + "|PriceListVersion.view", "EDIT");

      printPageEdit(response, request, vars, false, strM_PriceList_Version_ID, strPM_PriceList_ID, tableSQL);

    } else if (vars.commandIn("TAB")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID", false, false, true, "");
      vars.removeSessionValue(windowId + "|M_PriceList_Version_ID");
      refreshParentSession(vars, strPM_PriceList_ID);


      String strView = vars.getSessionValue(tabId + "|PriceListVersion.view");
      String strM_PriceList_Version_ID = "";
      if (strView.equals("")) {
        strView = defaultTabView;
        if (strView.equals("EDIT")) {
          strM_PriceList_Version_ID = firstElement(vars, tableSQL);
          if (strM_PriceList_Version_ID.equals("")) strView = "RELATION";
        }
      }
      if (strView.equals("EDIT")) {

        if (strM_PriceList_Version_ID.equals("")) strM_PriceList_Version_ID = firstElement(vars, tableSQL);
        printPageEdit(response, request, vars, false, strM_PriceList_Version_ID, strPM_PriceList_ID, tableSQL);

      } else printPageDataSheet(response, vars, strPM_PriceList_ID, "", tableSQL);
    } else if (vars.commandIn("SEARCH")) {
vars.getRequestGlobalVariable("inpParamName", tabId + "|paramName");

        vars.getRequestGlobalVariable("inpParamUpdated", tabId + "|paramUpdated");
        vars.getRequestGlobalVariable("inpParamUpdatedBy", tabId + "|paramUpdatedBy");
        vars.getRequestGlobalVariable("inpParamCreated", tabId + "|paramCreated");
        vars.getRequestGlobalVariable("inpparamCreatedBy", tabId + "|paramCreatedBy");
            String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");

      
      vars.removeSessionValue(windowId + "|M_PriceList_Version_ID");
      String strM_PriceList_Version_ID="";

      String strView = vars.getSessionValue(tabId + "|PriceListVersion.view");
      if (strView.equals("")) strView=defaultTabView;

      if (strView.equals("EDIT")) {
        strM_PriceList_Version_ID = firstElement(vars, tableSQL);
        if (strM_PriceList_Version_ID.equals("")) {
          // filter returns empty set
          strView = "RELATION";
          // switch to grid permanently until the user changes the view again
          vars.setSessionValue(tabId + "|PriceListVersion.view", strView);
        }
      }

      if (strView.equals("EDIT")) 

        printPageEdit(response, request, vars, false, strM_PriceList_Version_ID, strPM_PriceList_ID, tableSQL);

      else printPageDataSheet(response, vars, strPM_PriceList_ID, strM_PriceList_Version_ID, tableSQL);
    } else if (vars.commandIn("RELATION")) {
            String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");
      

      String strM_PriceList_Version_ID = vars.getGlobalVariable("inpmPricelistVersionId", windowId + "|M_PriceList_Version_ID", "");
      vars.setSessionValue(tabId + "|PriceListVersion.view", "RELATION");
      printPageDataSheet(response, vars, strPM_PriceList_ID, strM_PriceList_Version_ID, tableSQL);
    } else if (vars.commandIn("NEW")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");


      printPageEdit(response, request, vars, true, "", strPM_PriceList_ID, tableSQL);

    } else if (vars.commandIn("EDIT")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");

      @SuppressWarnings("unused") // In Expense Invoice tab this variable is not used, to be fixed
      String strM_PriceList_Version_ID = vars.getGlobalVariable("inpmPricelistVersionId", windowId + "|M_PriceList_Version_ID", "");
      vars.setSessionValue(tabId + "|PriceListVersion.view", "EDIT");

      setHistoryCommand(request, "EDIT");
      printPageEdit(response, request, vars, false, strM_PriceList_Version_ID, strPM_PriceList_ID, tableSQL);

    } else if (vars.commandIn("NEXT")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");
      String strM_PriceList_Version_ID = vars.getRequiredStringParameter("inpmPricelistVersionId");
      
      String strNext = nextElement(vars, strM_PriceList_Version_ID, tableSQL);

      printPageEdit(response, request, vars, false, strNext, strPM_PriceList_ID, tableSQL);
    } else if (vars.commandIn("PREVIOUS")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");
      String strM_PriceList_Version_ID = vars.getRequiredStringParameter("inpmPricelistVersionId");
      
      String strPrevious = previousElement(vars, strM_PriceList_Version_ID, tableSQL);

      printPageEdit(response, request, vars, false, strPrevious, strPM_PriceList_ID, tableSQL);
    } else if (vars.commandIn("FIRST_RELATION")) {
vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");

      vars.setSessionValue(tabId + "|PriceListVersion.initRecordNumber", "0");
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=RELATION");
    } else if (vars.commandIn("PREVIOUS_RELATION")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");

      String strInitRecord = vars.getSessionValue(tabId + "|PriceListVersion.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", windowId);
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      if (strInitRecord.equals("") || strInitRecord.equals("0")) {
        vars.setSessionValue(tabId + "|PriceListVersion.initRecordNumber", "0");
      } else {
        int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
        initRecord -= intRecordRange;
        strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
        vars.setSessionValue(tabId + "|PriceListVersion.initRecordNumber", strInitRecord);
      }
      vars.removeSessionValue(windowId + "|M_PriceList_Version_ID");
      vars.setSessionValue(windowId + "|M_PriceList_ID", strPM_PriceList_ID);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=RELATION");
    } else if (vars.commandIn("NEXT_RELATION")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");

      String strInitRecord = vars.getSessionValue(tabId + "|PriceListVersion.initRecordNumber");
      String strRecordRange = Utility.getContext(this, vars, "#RecordRange", windowId);
      int intRecordRange = strRecordRange.equals("")?0:Integer.parseInt(strRecordRange);
      int initRecord = (strInitRecord.equals("")?0:Integer.parseInt(strInitRecord));
      if (initRecord==0) initRecord=1;
      initRecord += intRecordRange;
      strInitRecord = ((initRecord<0)?"0":Integer.toString(initRecord));
      vars.setSessionValue(tabId + "|PriceListVersion.initRecordNumber", strInitRecord);
      vars.removeSessionValue(windowId + "|M_PriceList_Version_ID");
      vars.setSessionValue(windowId + "|M_PriceList_ID", strPM_PriceList_ID);
      response.sendRedirect(strDireccion + request.getServletPath() + "?Command=RELATION");
    } else if (vars.commandIn("FIRST")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");
      
      String strFirst = firstElement(vars, tableSQL);

      printPageEdit(response, request, vars, false, strFirst, strPM_PriceList_ID, tableSQL);
    } else if (vars.commandIn("LAST_RELATION")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");

      String strLast = lastElement(vars, tableSQL);
      printPageDataSheet(response, vars, strPM_PriceList_ID, strLast, tableSQL);
    } else if (vars.commandIn("LAST")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");
      
      String strLast = lastElement(vars, tableSQL);

      printPageEdit(response, request, vars, false, strLast, strPM_PriceList_ID, tableSQL);
    } else if (vars.commandIn("SAVE_NEW_RELATION", "SAVE_NEW_NEW", "SAVE_NEW_EDIT")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");
      OBError myError = new OBError();      
      int total = saveRecord(vars, myError, 'I', strPM_PriceList_ID);      
      if (!myError.isEmpty()) {        
        response.sendRedirect(strDireccion + request.getServletPath() + "?Command=NEW");
      } 
      else {
		if (myError.isEmpty()) {
		  myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=RowsInserted");
		  myError.setMessage(total + " " + myError.getMessage());
		  vars.setMessage(tabId, myError);
		}        
        if (vars.commandIn("SAVE_NEW_NEW")) response.sendRedirect(strDireccion + request.getServletPath() + "?Command=NEW");
        else if (vars.commandIn("SAVE_NEW_EDIT")) response.sendRedirect(strDireccion + request.getServletPath() + "?Command=EDIT");
        else response.sendRedirect(strDireccion + request.getServletPath() + "?Command=RELATION");
      }
    } else if (vars.commandIn("SAVE_EDIT_RELATION", "SAVE_EDIT_NEW", "SAVE_EDIT_EDIT", "SAVE_EDIT_NEXT")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");
      String strM_PriceList_Version_ID = vars.getRequiredGlobalVariable("inpmPricelistVersionId", windowId + "|M_PriceList_Version_ID");
      OBError myError = new OBError();
      int total = saveRecord(vars, myError, 'U', strPM_PriceList_ID);      
      if (!myError.isEmpty()) {
        response.sendRedirect(strDireccion + request.getServletPath() + "?Command=EDIT");
      } 
      else {
        if (myError.isEmpty()) {
          myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=RowsUpdated");
          myError.setMessage(total + " " + myError.getMessage());
          vars.setMessage(tabId, myError);
        }
        if (vars.commandIn("SAVE_EDIT_NEW")) response.sendRedirect(strDireccion + request.getServletPath() + "?Command=NEW");
        else if (vars.commandIn("SAVE_EDIT_EDIT")) response.sendRedirect(strDireccion + request.getServletPath() + "?Command=EDIT");
        else if (vars.commandIn("SAVE_EDIT_NEXT")) {
          String strNext = nextElement(vars, strM_PriceList_Version_ID, tableSQL);
          vars.setSessionValue(windowId + "|M_PriceList_Version_ID", strNext);
          response.sendRedirect(strDireccion + request.getServletPath() + "?Command=EDIT");
        } else response.sendRedirect(strDireccion + request.getServletPath() + "?Command=RELATION");
      }
    } else if (vars.commandIn("DELETE")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");

      String strM_PriceList_Version_ID = vars.getRequiredStringParameter("inpmPricelistVersionId");
      //PriceListVersionData data = getEditVariables(vars, strPM_PriceList_ID);
      int total = 0;
      OBError myError = null;
      if (org.openbravo.erpCommon.utility.WindowAccessData.hasNotDeleteAccess(this, vars.getRole(), tabId)) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
        vars.setMessage(tabId, myError);
      } else {
        try {
          total = PriceListVersionData.delete(this, strM_PriceList_Version_ID, strPM_PriceList_ID, Utility.getContext(this, vars, "#User_Client", windowId, accesslevel), Utility.getContext(this, vars, "#User_Org", windowId, accesslevel));
        } catch(ServletException ex) {
          myError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          if (!myError.isConnectionAvailable()) {
            bdErrorConnection(response);
            return;
          } else vars.setMessage(tabId, myError);
        }
        if (myError==null && total==0) {
          myError = Utility.translateError(this, vars, vars.getLanguage(), Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
          vars.setMessage(tabId, myError);
        }
        vars.removeSessionValue(windowId + "|mPricelistVersionId");
        vars.setSessionValue(tabId + "|PriceListVersion.view", "RELATION");
      }
      if (myError==null) {
        myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=RowsDeleted");
        myError.setMessage(total + " " + myError.getMessage());
        vars.setMessage(tabId, myError);
      }
      response.sendRedirect(strDireccion + request.getServletPath());

     } else if (vars.commandIn("BUTTONProcCreate103")) {
        vars.setSessionValue("button103.strproccreate", vars.getStringParameter("inpproccreate"));
        vars.setSessionValue("button103.strProcessing", vars.getStringParameter("inpprocessing", "Y"));
        vars.setSessionValue("button103.strOrg", vars.getStringParameter("inpadOrgId"));
        vars.setSessionValue("button103.strClient", vars.getStringParameter("inpadClientId"));
        
        
        HashMap<String, String> p = new HashMap<String, String>();
        
        
        //Save in session needed params for combos if needed
        vars.setSessionObject("button103.originalParams", FieldProviderFactory.getFieldProvider(p));
        printPageButtonFS(response, vars, "103", request.getServletPath());    
     } else if (vars.commandIn("BUTTON103")) {
        String strM_PriceList_Version_ID = vars.getGlobalVariable("inpmPricelistVersionId", windowId + "|M_PriceList_Version_ID", "");
        String strproccreate = vars.getSessionValue("button103.strproccreate");
        String strProcessing = vars.getSessionValue("button103.strProcessing");
        String strOrg = vars.getSessionValue("button103.strOrg");
        String strClient = vars.getSessionValue("button103.strClient");
        
        
        if ((org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(), tabId)) || !(Utility.isElementInList(Utility.getContext(this, vars, "#User_Client", windowId, accesslevel),strClient)  && Utility.isElementInList(Utility.getContext(this, vars, "#User_Org", windowId, accesslevel),strOrg))){
          OBError myError = Utility.translateError(this, vars, vars.getLanguage(), Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
          vars.setMessage(tabId, myError);
          printPageClosePopUp(response, vars);
        }else{       
          printPageButtonProcCreate103(response, vars, strM_PriceList_Version_ID, strproccreate, strProcessing);
        }


    } else if (vars.commandIn("SAVE_BUTTONProcCreate103")) {
        String strM_PriceList_Version_ID = vars.getGlobalVariable("inpKey", windowId + "|M_PriceList_Version_ID", "");
        @SuppressWarnings("unused")
        String strproccreate = vars.getStringParameter("inpproccreate");
        String strProcessing = vars.getStringParameter("inpprocessing");
        OBError myMessage = null;
        try {
          String pinstance = SequenceIdData.getUUID();
          PInstanceProcessData.insertPInstance(this, pinstance, "103", (("M_PriceList_Version_ID".equalsIgnoreCase("AD_Language"))?"0":strM_PriceList_Version_ID), strProcessing, vars.getUser(), vars.getClient(), vars.getOrg());
          String strdeleteold = vars.getStringParameter("inpdeleteold", "N");
PInstanceProcessData.insertPInstanceParam(this, pinstance, "10", "DeleteOld", strdeleteold, vars.getClient(), vars.getOrg(), vars.getUser());

          
          ProcessBundle bundle = ProcessBundle.pinstance(pinstance, vars, this);
          new ProcessRunner(bundle).execute(this);
          
          PInstanceProcessData[] pinstanceData = PInstanceProcessData.select(this, pinstance);
          myMessage = Utility.getProcessInstanceMessage(this, vars, pinstanceData);
        } catch (ServletException ex) {
          myMessage = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
          if (!myMessage.isConnectionAvailable()) {
            bdErrorConnection(response);
            return;
          } else vars.setMessage(tabId, myMessage);
        }
        //close popup
        if (myMessage!=null) {
          if (log4j.isDebugEnabled()) log4j.debug(myMessage.getMessage());
          vars.setMessage(tabId, myMessage);
        }
        printPageClosePopUp(response, vars);






    } else if (vars.commandIn("SAVE_XHR")) {
      String strPM_PriceList_ID = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");
      OBError myError = new OBError();
      JSONObject result = new JSONObject();
      String commandType = vars.getStringParameter("inpCommandType");
      char saveType = "NEW".equals(commandType) ? 'I' : 'U';
      try {
        int total = saveRecord(vars, myError, saveType, strPM_PriceList_ID);
        if (myError.isEmpty()) {
          myError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=RowsUpdated");
          myError.setMessage(total + " " + myError.getMessage());
          myError.setType("Success");
        }
        result.put("oberror", myError.toMap());
        result.put("tabid", vars.getStringParameter("tabID"));
        result.put("redirect", strDireccion + request.getServletPath() + "?Command=" + commandType);
      } catch (Exception e) {
        log4j.error("Error saving record (XHR request): " + e.getMessage(), e);
        myError.setType("Error");
        myError.setMessage(e.getMessage());
      }

      response.setContentType("application/json");
      PrintWriter out = response.getWriter();
      out.print(result.toString());
      out.flush();
      out.close();
    } else if (vars.getCommand().toUpperCase().startsWith("BUTTON") || vars.getCommand().toUpperCase().startsWith("SAVE_BUTTON")) {
      pageErrorPopUp(response);
    } else pageError(response);
  }
  private PriceListVersionData getEditVariables(Connection con, VariablesSecureApp vars, String strPM_PriceList_ID) throws IOException,ServletException {
    PriceListVersionData data = new PriceListVersionData();
    ServletException ex = null;
    try {
    data.name = vars.getRequiredStringParameter("inpname");     data.validfrom = vars.getRequiredStringParameter("inpvalidfrom");     data.mDiscountschemaId = vars.getRequiredStringParameter("inpmDiscountschemaId");     data.mDiscountschemaIdr = vars.getStringParameter("inpmDiscountschemaId_R");     data.mPricelistVersionBaseId = vars.getStringParameter("inpmPricelistVersionBaseId");     data.mPricelistVersionBaseIdr = vars.getStringParameter("inpmPricelistVersionBaseId_R");     data.description = vars.getStringParameter("inpdescription");     data.isactive = vars.getStringParameter("inpisactive", "N");     data.proccreate = vars.getStringParameter("inpproccreate");     data.mPricelistVersionGenerate = vars.getRequiredStringParameter("inpmPricelistVersionGenerate");     data.mPricelistVersionId = vars.getRequestGlobalVariable("inpmPricelistVersionId", windowId + "|M_PriceList_Version_ID");     data.mPricelistId = vars.getRequiredStringParameter("inpmPricelistId");     data.adOrgId = vars.getRequiredGlobalVariable("inpadOrgId", windowId + "|AD_Org_ID");     data.adClientId = vars.getRequiredGlobalVariable("inpadClientId", windowId + "|AD_Client_ID"); 
      data.createdby = vars.getUser();
      data.updatedby = vars.getUser();
      data.adUserClient = Utility.getContext(this, vars, "#User_Client", windowId, accesslevel);
      data.adOrgClient = Utility.getContext(this, vars, "#AccessibleOrgTree", windowId, accesslevel);
      data.updatedTimeStamp = vars.getStringParameter("updatedTimestamp");

      data.mPricelistId = vars.getGlobalVariable("inpmPricelistId", windowId + "|M_PriceList_ID");


    
    

    
    }
    catch(ServletException e) {
    	vars.setEditionData(tabId, data);
    	throw e;
    }
    // Behavior with exception for numeric fields is to catch last one if we have multiple ones
    if(ex != null) {
      vars.setEditionData(tabId, data);
      throw ex;
    }
    return data;
  }


  private void refreshParentSession(VariablesSecureApp vars, String strPM_PriceList_ID) throws IOException,ServletException {
      
      PriceListData[] data = PriceListData.selectEdit(this, vars.getSessionValue("#AD_SqlDateTimeFormat"), vars.getLanguage(), strPM_PriceList_ID, Utility.getContext(this, vars, "#User_Client", windowId), Utility.getContext(this, vars, "#AccessibleOrgTree", windowId, accesslevel));
      if (data==null || data.length==0) return;
          vars.setSessionValue(windowId + "|AD_Org_ID", data[0].adOrgId);    vars.setSessionValue(windowId + "|IsSOPriceList", data[0].issopricelist);    vars.setSessionValue(windowId + "|Costbased", data[0].costbased);    vars.setSessionValue(windowId + "|M_PriceList_ID", data[0].mPricelistId);    vars.setSessionValue(windowId + "|AD_Client_ID", data[0].adClientId);
      vars.setSessionValue(windowId + "|M_PriceList_ID", strPM_PriceList_ID); //to ensure key parent is set for EM_* cols

      FieldProvider dataField = null; // Define this so that auxiliar inputs using SQL will work
      
  }
  
  
  private String getParentID(VariablesSecureApp vars, String strM_PriceList_Version_ID) throws ServletException {
    String strPM_PriceList_ID = PriceListVersionData.selectParentID(this, strM_PriceList_Version_ID);
    if (strPM_PriceList_ID.equals("")) {
      log4j.error("Parent record not found for id: " + strM_PriceList_Version_ID);
      throw new ServletException("Parent record not found");
    }
    return strPM_PriceList_ID;
  }

    private void refreshSessionEdit(VariablesSecureApp vars, FieldProvider[] data) {
      if (data==null || data.length==0) return;
          vars.setSessionValue(windowId + "|AD_Client_ID", data[0].getField("adClientId"));    vars.setSessionValue(windowId + "|AD_Org_ID", data[0].getField("adOrgId"));    vars.setSessionValue(windowId + "|M_PriceList_Version_ID", data[0].getField("mPricelistVersionId"));
    }

    private void refreshSessionNew(VariablesSecureApp vars, String strPM_PriceList_ID) throws IOException,ServletException {
      PriceListVersionData[] data = PriceListVersionData.selectEdit(this, vars.getSessionValue("#AD_SqlDateTimeFormat"), vars.getLanguage(), strPM_PriceList_ID, vars.getStringParameter("inpmPricelistVersionId", ""), Utility.getContext(this, vars, "#User_Client", windowId), Utility.getContext(this, vars, "#AccessibleOrgTree", windowId, accesslevel));
      if (data==null || data.length==0) return;
      refreshSessionEdit(vars, data);
    }

  private String nextElement(VariablesSecureApp vars, String strSelected, TableSQLData tableSQL) throws IOException, ServletException {
    if (strSelected == null || strSelected.equals("")) return firstElement(vars, tableSQL);
    if (tableSQL!=null) {
      String data = null;
      try{
        String strSQL = ModelSQLGeneration.generateSQLonlyId(this, vars, tableSQL, (tableSQL.getTableName() + "." + tableSQL.getKeyColumn() + " AS ID"), new Vector<String>(), new Vector<String>(), 0, 0);
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValuesOnlyId());
        data = execquery.selectAndSearch(ExecuteQuery.SearchType.NEXT, strSelected, tableSQL.getKeyColumn());
      } catch (Exception e) { 
        log4j.error("Error getting next element", e);
      }
      if (data!=null) {
        if (data!=null) return data;
      }
    }
    return strSelected;
  }

  private int getKeyPosition(VariablesSecureApp vars, String strSelected, TableSQLData tableSQL) throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("getKeyPosition: " + strSelected);
    if (tableSQL!=null) {
      String data = null;
      try{
        String strSQL = ModelSQLGeneration.generateSQLonlyId(this, vars, tableSQL, (tableSQL.getTableName() + "." + tableSQL.getKeyColumn() + " AS ID"), new Vector<String>(), new Vector<String>(),0,0);
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValuesOnlyId());
        data = execquery.selectAndSearch(ExecuteQuery.SearchType.GETPOSITION, strSelected, tableSQL.getKeyColumn());
      } catch (Exception e) { 
        log4j.error("Error getting key position", e);
      }
      if (data!=null) {
        // split offset -> (page,relativeOffset)
        int absoluteOffset = Integer.valueOf(data);
        int page = absoluteOffset / TableSQLData.maxRowsPerGridPage;
        int relativeOffset = absoluteOffset % TableSQLData.maxRowsPerGridPage;
        log4j.debug("getKeyPosition: absOffset: " + absoluteOffset + "=> page: " + page + " relOffset: " + relativeOffset);
        String currPageKey = tabId + "|" + "currentPage";
        vars.setSessionValue(currPageKey, String.valueOf(page));
        return relativeOffset;
      }
    }
    return 0;
  }

  private String previousElement(VariablesSecureApp vars, String strSelected, TableSQLData tableSQL) throws IOException, ServletException {
    if (strSelected == null || strSelected.equals("")) return firstElement(vars, tableSQL);
    if (tableSQL!=null) {
      String data = null;
      try{
        String strSQL = ModelSQLGeneration.generateSQLonlyId(this, vars, tableSQL, (tableSQL.getTableName() + "." + tableSQL.getKeyColumn() + " AS ID"), new Vector<String>(), new Vector<String>(),0,0);
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValuesOnlyId());
        data = execquery.selectAndSearch(ExecuteQuery.SearchType.PREVIOUS, strSelected, tableSQL.getKeyColumn());
      } catch (Exception e) { 
        log4j.error("Error getting previous element", e);
      }
      if (data!=null) {
        return data;
      }
    }
    return strSelected;
  }

  private String firstElement(VariablesSecureApp vars, TableSQLData tableSQL) throws IOException, ServletException {
    if (tableSQL!=null) {
      String data = null;
      try{
        String strSQL = ModelSQLGeneration.generateSQLonlyId(this, vars, tableSQL, (tableSQL.getTableName() + "." + tableSQL.getKeyColumn() + " AS ID"), new Vector<String>(), new Vector<String>(),0,1);
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValuesOnlyId());
        data = execquery.selectAndSearch(ExecuteQuery.SearchType.FIRST, "", tableSQL.getKeyColumn());

      } catch (Exception e) { 
        log4j.debug("Error getting first element", e);
      }
      if (data!=null) return data;
    }
    return "";
  }

  private String lastElement(VariablesSecureApp vars, TableSQLData tableSQL) throws IOException, ServletException {
    if (tableSQL!=null) {
      String data = null;
      try{
        String strSQL = ModelSQLGeneration.generateSQLonlyId(this, vars, tableSQL, (tableSQL.getTableName() + "." + tableSQL.getKeyColumn() + " AS ID"), new Vector<String>(), new Vector<String>(),0,0);
        ExecuteQuery execquery = new ExecuteQuery(this, strSQL, tableSQL.getParameterValuesOnlyId());
        data = execquery.selectAndSearch(ExecuteQuery.SearchType.LAST, "", tableSQL.getKeyColumn());
      } catch (Exception e) { 
        log4j.error("Error getting last element", e);
      }
      if (data!=null) return data;
    }
    return "";
  }

  private void printPageDataSheet(HttpServletResponse response, VariablesSecureApp vars, String strPM_PriceList_ID, String strM_PriceList_Version_ID, TableSQLData tableSQL)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: dataSheet");

    String strParamName = vars.getSessionValue(tabId + "|paramName");

    boolean hasSearchCondition=false;
    vars.removeEditionData(tabId);
    hasSearchCondition = (tableSQL.hasInternalFilter() && ("").equals(strParamName)) || !(("").equals(strParamName) || ("%").equals(strParamName)) ;
    String strOffset = vars.getSessionValue(tabId + "|offset");
    String selectedRow = "0";
    if (!strM_PriceList_Version_ID.equals("")) {
      selectedRow = Integer.toString(getKeyPosition(vars, strM_PriceList_Version_ID, tableSQL));
    }

    String[] discard={"isNotFiltered","isNotTest"};
    if (hasSearchCondition) discard[0] = new String("isFiltered");
    if (vars.getSessionValue("#ShowTest", "N").equals("Y")) discard[1] = new String("isTest");
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpWindows/PriceList/PriceListVersion_Relation", discard).createXmlDocument();

    boolean hasReadOnlyAccess = org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(), tabId);
    ToolBar toolbar = new ToolBar(this, true, vars.getLanguage(), "PriceListVersion", false, "document.frmMain.inpmPricelistVersionId", "grid", "..", "".equals("Y"), "PriceList", strReplaceWith, false, false, false, false, !hasReadOnlyAccess);
    toolbar.setTabId(tabId);
    
    toolbar.setDeleteable(true && !hasReadOnlyAccess);
    toolbar.prepareRelationTemplate("N".equals("Y"), hasSearchCondition, !vars.getSessionValue("#ShowTest", "N").equals("Y"), false, Utility.getContext(this, vars, "ShowAudit", windowId).equals("Y"));
    xmlDocument.setParameter("toolbar", toolbar.toString());

    xmlDocument.setParameter("keyParent", strPM_PriceList_ID);
    xmlDocument.setParameter("parentFieldName", Utility.getFieldName("2298", vars.getLanguage()));


    StringBuffer orderByArray = new StringBuffer();
      vars.setSessionValue(tabId + "|newOrder", "1");
      String positions = vars.getSessionValue(tabId + "|orderbyPositions");
      orderByArray.append("var orderByPositions = new Array(\n");
      if (!positions.equals("")) {
        StringTokenizer tokens=new StringTokenizer(positions, ",");
        boolean firstOrder = true;
        while(tokens.hasMoreTokens()){
          if (!firstOrder) orderByArray.append(",\n");
          orderByArray.append("\"").append(tokens.nextToken()).append("\"");
          firstOrder = false;
        }
      }
      orderByArray.append(");\n");
      String directions = vars.getSessionValue(tabId + "|orderbyDirections");
      orderByArray.append("var orderByDirections = new Array(\n");
      if (!positions.equals("")) {
        StringTokenizer tokens=new StringTokenizer(directions, ",");
        boolean firstOrder = true;
        while(tokens.hasMoreTokens()){
          if (!firstOrder) orderByArray.append(",\n");
          orderByArray.append("\"").append(tokens.nextToken()).append("\"");
          firstOrder = false;
        }
      }
      orderByArray.append(");\n");
//    }

    xmlDocument.setParameter("selectedColumn", "\nvar selectedRow = " + selectedRow + ";\n" + orderByArray.toString());
    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    xmlDocument.setParameter("windowId", windowId);
    xmlDocument.setParameter("KeyName", "mPricelistVersionId");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    //xmlDocument.setParameter("buttonReference", Utility.messageBD(this, "Reference", vars.getLanguage()));
    try {
      WindowTabs tabs = new WindowTabs(this, vars, tabId, windowId, false);
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      xmlDocument.setParameter("childTabContainer", tabs.childTabs());
      String hideBackButton = vars.getGlobalVariable("hideMenu", "#Hide_BackButton", "");
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "PriceListVersion_Relation.html", "PriceList", "W", strReplaceWith, tabs.breadcrumb(), hideBackButton.equals("true"));
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "PriceListVersion_Relation.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.relationTemplate());
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
    {
      OBError myMessage = vars.getMessage(tabId);
      vars.removeMessage(tabId);
      if (myMessage!=null) {
        xmlDocument.setParameter("messageType", myMessage.getType());
        xmlDocument.setParameter("messageTitle", myMessage.getTitle());
        xmlDocument.setParameter("messageMessage", myMessage.getMessage());
      }
    }
    if (vars.getLanguage().equals("en_US")) xmlDocument.setParameter("parent", PriceListVersionData.selectParent(this, strPM_PriceList_ID));
    else xmlDocument.setParameter("parent", PriceListVersionData.selectParentTrl(this, strPM_PriceList_ID));

    xmlDocument.setParameter("grid", Utility.getContext(this, vars, "#RecordRange", windowId));
xmlDocument.setParameter("grid_Offset", strOffset);
xmlDocument.setParameter("grid_SortCols", positions);
xmlDocument.setParameter("grid_SortDirs", directions);
xmlDocument.setParameter("grid_Default", selectedRow);


    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageEdit(HttpServletResponse response, HttpServletRequest request, VariablesSecureApp vars,boolean boolNew, String strM_PriceList_Version_ID, String strPM_PriceList_ID, TableSQLData tableSQL)
    throws IOException, ServletException {
    if (log4j.isDebugEnabled()) log4j.debug("Output: edit");
    
    HashMap<String, String> usedButtonShortCuts;
  
    HashMap<String, String> reservedButtonShortCuts;
  
    usedButtonShortCuts = new HashMap<String, String>();
    
    reservedButtonShortCuts = new HashMap<String, String>();
    
    
    
    String strOrderByFilter = vars.getSessionValue(tabId + "|orderby");
    String orderClause = " M_PriceList_Version.Name";
    if (strOrderByFilter==null || strOrderByFilter.equals("")) strOrderByFilter = orderClause;
    /*{
      if (!strOrderByFilter.equals("") && !orderClause.equals("")) strOrderByFilter += ", ";
      strOrderByFilter += orderClause;
    }*/
    
    
    String strCommand = null;
    PriceListVersionData[] data=null;
    XmlDocument xmlDocument=null;
    FieldProvider dataField = vars.getEditionData(tabId);
    vars.removeEditionData(tabId);
    String strParamName = vars.getSessionValue(tabId + "|paramName");

    boolean hasSearchCondition=false;
    hasSearchCondition = (tableSQL.hasInternalFilter() && ("").equals(strParamName)) || !(("").equals(strParamName) || ("%").equals(strParamName)) ;

       String strParamSessionDate = vars.getGlobalVariable("inpParamSessionDate", Utility.getTransactionalDate(this, vars, windowId), "");
      String buscador = "";
      String[] discard = {"", "isNotTest"};
      
      if (vars.getSessionValue("#ShowTest", "N").equals("Y")) discard[1] = new String("isTest");
    if (dataField==null) {
      if (!boolNew) {
        discard[0] = new String("newDiscard");
        data = PriceListVersionData.selectEdit(this, vars.getSessionValue("#AD_SqlDateTimeFormat"), vars.getLanguage(), strPM_PriceList_ID, strM_PriceList_Version_ID, Utility.getContext(this, vars, "#User_Client", windowId), Utility.getContext(this, vars, "#AccessibleOrgTree", windowId, accesslevel));
  
        if (!strM_PriceList_Version_ID.equals("") && (data == null || data.length==0)) {
          response.sendRedirect(strDireccion + request.getServletPath() + "?Command=RELATION");
          return;
        }
        refreshSessionEdit(vars, data);
        strCommand = "EDIT";
      }

      if (boolNew || data==null || data.length==0) {
        discard[0] = new String ("editDiscard");
        strCommand = "NEW";
        data = new PriceListVersionData[0];
      } else {
        discard[0] = new String ("newDiscard");
      }
    } else {
      if (dataField.getField("mPricelistVersionId") == null || dataField.getField("mPricelistVersionId").equals("")) {
        discard[0] = new String ("editDiscard");
        strCommand = "NEW";
        boolNew = true;
      } else {
        discard[0] = new String ("newDiscard");
        strCommand = "EDIT";
      }
    }
    
    
    
    if (dataField==null) {
      if (boolNew || data==null || data.length==0) {
        refreshSessionNew(vars, strPM_PriceList_ID);
        data = PriceListVersionData.set(strPM_PriceList_ID, "", Utility.getDefault(this, vars, "AD_Client_ID", "@AD_Client_ID@", "146", "", dataField), Utility.getDefault(this, vars, "AD_Org_ID", "@AD_Org_ID@", "146", "", dataField), "Y", Utility.getDefault(this, vars, "CreatedBy", "", "146", "", dataField), PriceListVersionData.selectDef2992_0(this, Utility.getDefault(this, vars, "CreatedBy", "", "146", "", dataField)), Utility.getDefault(this, vars, "UpdatedBy", "", "146", "", dataField), PriceListVersionData.selectDef2994_1(this, Utility.getDefault(this, vars, "UpdatedBy", "", "146", "", dataField)), Utility.getDefault(this, vars, "Name", "@#Date@", "146", "", dataField), Utility.getDefault(this, vars, "Description", "", "146", "", dataField), Utility.getDefault(this, vars, "ValidFrom", "@#Date@", "146", "", dataField), Utility.getDefault(this, vars, "ProcCreate", "", "146", "N", dataField), Utility.getDefault(this, vars, "M_Pricelist_Version_Base_ID", "", "146", "", dataField), Utility.getDefault(this, vars, "M_DiscountSchema_ID", "", "146", "", dataField), Utility.getDefault(this, vars, "M_Pricelist_Version_Generate", "N", "146", "N", dataField));
        
      }
     }
      
    String currentPOrg=PriceListData.selectOrg(this, strPM_PriceList_ID);
    String currentOrg = (boolNew?"":(dataField!=null?dataField.getField("adOrgId"):data[0].getField("adOrgId")));
    if (!currentOrg.equals("") && !currentOrg.startsWith("'")) currentOrg = "'"+currentOrg+"'";
    String currentClient = (boolNew?"":(dataField!=null?dataField.getField("adClientId"):data[0].getField("adClientId")));
    if (!currentClient.equals("") && !currentClient.startsWith("'")) currentClient = "'"+currentClient+"'";
    
    boolean hasReadOnlyAccess = org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(), tabId);
    boolean editableTab = (!hasReadOnlyAccess && (currentOrg.equals("") || Utility.isElementInList(Utility.getContext(this, vars, "#User_Org", windowId, accesslevel),currentOrg)) && (currentClient.equals("") || Utility.isElementInList(Utility.getContext(this, vars, "#User_Client", windowId, accesslevel), currentClient)));
    if (editableTab)
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpWindows/PriceList/PriceListVersion_Edition",discard).createXmlDocument();
    else
      xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpWindows/PriceList/PriceListVersion_NonEditable",discard).createXmlDocument();

    xmlDocument.setParameter("tabId", tabId);
    ToolBar toolbar = new ToolBar(this, editableTab, vars.getLanguage(), "PriceListVersion", (strCommand.equals("NEW") || boolNew || (dataField==null && (data==null || data.length==0))), "document.frmMain.inpmPricelistVersionId", "", "..", "".equals("Y"), "PriceList", strReplaceWith, true, false, false, Utility.hasTabAttachments(this, vars, tabId, strM_PriceList_Version_ID), !hasReadOnlyAccess);
    toolbar.setTabId(tabId);
    toolbar.setDeleteable(true);
    toolbar.prepareEditionTemplate("N".equals("Y"), hasSearchCondition, vars.getSessionValue("#ShowTest", "N").equals("Y"), "STD", Utility.getContext(this, vars, "ShowAudit", windowId).equals("Y"));
    xmlDocument.setParameter("toolbar", toolbar.toString());

    // set updated timestamp to manage locking mechanism
    if (!boolNew) {
      xmlDocument.setParameter("updatedTimestamp", (dataField != null ? dataField
          .getField("updatedTimeStamp") : data[0].getField("updatedTimeStamp")));
    }
    
    boolean concurrentSave = vars.getSessionValue(tabId + "|concurrentSave").equals("true");
    if (concurrentSave) {
      //after concurrent save error, force autosave
      xmlDocument.setParameter("autosave", "Y");
    } else {
      xmlDocument.setParameter("autosave", "N");
    }
    vars.removeSessionValue(tabId + "|concurrentSave");

    try {
      WindowTabs tabs = new WindowTabs(this, vars, tabId, windowId, true, (strCommand.equalsIgnoreCase("NEW")));
      xmlDocument.setParameter("parentTabContainer", tabs.parentTabs());
      xmlDocument.setParameter("mainTabContainer", tabs.mainTabs());
      // if (!strM_PriceList_Version_ID.equals("")) xmlDocument.setParameter("childTabContainer", tabs.childTabs(false));
	  // else xmlDocument.setParameter("childTabContainer", tabs.childTabs(true));
	  xmlDocument.setParameter("childTabContainer", tabs.childTabs(false));
	  String hideBackButton = vars.getGlobalVariable("hideMenu", "#Hide_BackButton", "");
      NavigationBar nav = new NavigationBar(this, vars.getLanguage(), "PriceListVersion_Relation.html", "PriceList", "W", strReplaceWith, tabs.breadcrumb(), hideBackButton.equals("true"), !concurrentSave);
      xmlDocument.setParameter("navigationBar", nav.toString());
      LeftTabsBar lBar = new LeftTabsBar(this, vars.getLanguage(), "PriceListVersion_Relation.html", strReplaceWith);
      xmlDocument.setParameter("leftTabs", lBar.editionTemplate(strCommand.equals("NEW")));
    } catch (Exception ex) {
      throw new ServletException(ex);
    }
		
    
    xmlDocument.setParameter("parentOrg", currentPOrg);
    xmlDocument.setParameter("commandType", strCommand);
    xmlDocument.setParameter("buscador",buscador);
    xmlDocument.setParameter("windowId", windowId);
    xmlDocument.setParameter("changed", "");
    xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
    xmlDocument.setParameter("theme", vars.getTheme());
    final String strMappingName = Utility.getTabURL(tabId, "E", false);
    xmlDocument.setParameter("mappingName", strMappingName);
    xmlDocument.setParameter("confirmOnChanges", Utility.getJSConfirmOnChanges(vars, windowId));
    //xmlDocument.setParameter("buttonReference", Utility.messageBD(this, "Reference", vars.getLanguage()));

    xmlDocument.setParameter("paramSessionDate", strParamSessionDate);

    xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
    OBError myMessage = vars.getMessage(tabId);
    vars.removeMessage(tabId);
    if (myMessage!=null) {
      xmlDocument.setParameter("messageType", myMessage.getType());
      xmlDocument.setParameter("messageTitle", myMessage.getTitle());
      xmlDocument.setParameter("messageMessage", myMessage.getMessage());
    }
    xmlDocument.setParameter("displayLogic", getDisplayLogicContext(vars, boolNew));
    
    
     if (dataField==null) {
      xmlDocument.setData("structure1",data);
      
    } else {
      
        FieldProvider[] dataAux = new FieldProvider[1];
        dataAux[0] = dataField;
        
        xmlDocument.setData("structure1",dataAux);
      
    }
    
      
   
    try {
      ComboTableData comboTableData = null;
xmlDocument.setParameter("ValidFrom_Format", vars.getSessionValue("#AD_SqlDateFormat"));
comboTableData = new ComboTableData(vars, this, "19", "M_DiscountSchema_ID", "", "151", Utility.getReferenceableOrg(vars, (dataField!=null?dataField.getField("adOrgId"):data[0].getField("adOrgId").equals("")?vars.getOrg():data[0].getField("adOrgId"))), Utility.getContext(this, vars, "#User_Client", windowId), 0);
Utility.fillSQLParameters(this, vars, (dataField==null?data[0]:dataField), comboTableData, windowId, (dataField==null?data[0].getField("mDiscountschemaId"):dataField.getField("mDiscountschemaId")));
xmlDocument.setData("reportM_DiscountSchema_ID","liststructure", comboTableData.select(!strCommand.equals("NEW")));
comboTableData = null;
comboTableData = new ComboTableData(vars, this, "18", "M_Pricelist_Version_Base_ID", "188", "", Utility.getReferenceableOrg(vars, (dataField!=null?dataField.getField("adOrgId"):data[0].getField("adOrgId").equals("")?vars.getOrg():data[0].getField("adOrgId"))), Utility.getContext(this, vars, "#User_Client", windowId), 0);
Utility.fillSQLParameters(this, vars, (dataField==null?data[0]:dataField), comboTableData, windowId, (dataField==null?data[0].getField("mPricelistVersionBaseId"):dataField.getField("mPricelistVersionBaseId")));
xmlDocument.setData("reportM_Pricelist_Version_Base_ID","liststructure", comboTableData.select(!strCommand.equals("NEW")));
comboTableData = null;
xmlDocument.setParameter("ProcCreate_BTNname", Utility.getButtonName(this, vars, "2897", "ProcCreate_linkBTN", usedButtonShortCuts, reservedButtonShortCuts));boolean modalProcCreate = org.openbravo.erpCommon.utility.Utility.isModalProcess("103"); 
xmlDocument.setParameter("ProcCreate_Modal", modalProcCreate?"true":"false");
xmlDocument.setParameter("Created_Format", vars.getSessionValue("#AD_SqlDateTimeFormat"));xmlDocument.setParameter("Created_Maxlength", Integer.toString(vars.getSessionValue("#AD_SqlDateTimeFormat").length()));
xmlDocument.setParameter("Updated_Format", vars.getSessionValue("#AD_SqlDateTimeFormat"));xmlDocument.setParameter("Updated_Maxlength", Integer.toString(vars.getSessionValue("#AD_SqlDateTimeFormat").length()));
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new ServletException(ex);
    }

    xmlDocument.setParameter("scriptOnLoad", getShortcutScript(usedButtonShortCuts, reservedButtonShortCuts));
    
    final String refererURL = vars.getSessionValue(tabId + "|requestURL");
    vars.removeSessionValue(tabId + "|requestURL");
    if(!refererURL.equals("")) {
    	final Boolean failedAutosave = (Boolean) vars.getSessionObject(tabId + "|failedAutosave");
		vars.removeSessionValue(tabId + "|failedAutosave");
    	if(failedAutosave != null && failedAutosave) {
    		final String jsFunction = "continueUserAction('"+refererURL+"');";
    		xmlDocument.setParameter("failedAutosave", jsFunction);
    	}
    }

    if (strCommand.equalsIgnoreCase("NEW")) {
      vars.removeSessionValue(tabId + "|failedAutosave");
      vars.removeSessionValue(strMappingName + "|hash");
    }

    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    out.println(xmlDocument.print());
    out.close();
  }

  private void printPageButtonFS(HttpServletResponse response, VariablesSecureApp vars, String strProcessId, String path) throws IOException, ServletException {
    log4j.debug("Output: Frames action button");
    response.setContentType("text/html; charset=UTF-8");
    PrintWriter out = response.getWriter();
    XmlDocument xmlDocument = xmlEngine.readXmlTemplate(
        "org/openbravo/erpCommon/ad_actionButton/ActionButtonDefaultFrames").createXmlDocument();
    xmlDocument.setParameter("processId", strProcessId);
    xmlDocument.setParameter("trlFormType", "PROCESS");
    xmlDocument.setParameter("language", "defaultLang = \"" + vars.getLanguage() + "\";\n");
    xmlDocument.setParameter("type", strDireccion + path);
    out.println(xmlDocument.print());
    out.close();
  }

    private void printPageButtonProcCreate103(HttpServletResponse response, VariablesSecureApp vars, String strM_PriceList_Version_ID, String strproccreate, String strProcessing)
    throws IOException, ServletException {
      log4j.debug("Output: Button process 103");
      String[] discard = {"newDiscard"};
      response.setContentType("text/html; charset=UTF-8");
      PrintWriter out = response.getWriter();
      XmlDocument xmlDocument = xmlEngine.readXmlTemplate("org/openbravo/erpCommon/ad_actionButton/ProcCreate103", discard).createXmlDocument();
      xmlDocument.setParameter("key", strM_PriceList_Version_ID);
      xmlDocument.setParameter("processing", strProcessing);
      xmlDocument.setParameter("form", "PriceListVersion_Edition.html");
      xmlDocument.setParameter("window", windowId);
      xmlDocument.setParameter("css", vars.getTheme());
      xmlDocument.setParameter("language", "defaultLang=\"" + vars.getLanguage() + "\";");
      xmlDocument.setParameter("directory", "var baseDirectory = \"" + strReplaceWith + "/\";\n");
      xmlDocument.setParameter("processId", "103");
      xmlDocument.setParameter("cancel", Utility.messageBD(this, "Cancel", vars.getLanguage()));
      xmlDocument.setParameter("ok", Utility.messageBD(this, "OK", vars.getLanguage()));
      
      {
        OBError myMessage = vars.getMessage("103");
        vars.removeMessage("103");
        if (myMessage!=null) {
          xmlDocument.setParameter("messageType", myMessage.getType());
          xmlDocument.setParameter("messageTitle", myMessage.getTitle());
          xmlDocument.setParameter("messageMessage", myMessage.getMessage());
        }
      }

          try {
    xmlDocument.setParameter("DeleteOld", "N");
    } catch (Exception ex) {
      throw new ServletException(ex);
    }

      
      out.println(xmlDocument.print());
      out.close();
    }




    private String getDisplayLogicContext(VariablesSecureApp vars, boolean isNew) throws IOException, ServletException {
      log4j.debug("Output: Display logic context fields");
      String result = "var strShowAudit=\"" +(isNew?"N":Utility.getContext(this, vars, "ShowAudit", windowId)) + "\";\n";
      return result;
    }


    private String getReadOnlyLogicContext(VariablesSecureApp vars) throws IOException, ServletException {
      log4j.debug("Output: Read Only logic context fields");
      String result = "";
      return result;
    }




 
  private String getShortcutScript( HashMap<String, String> usedButtonShortCuts, HashMap<String, String> reservedButtonShortCuts){
    StringBuffer shortcuts = new StringBuffer();
    shortcuts.append(" function buttonListShorcuts() {\n");
    Iterator<String> ik = usedButtonShortCuts.keySet().iterator();
    Iterator<String> iv = usedButtonShortCuts.values().iterator();
    while(ik.hasNext() && iv.hasNext()){
      shortcuts.append("  keyArray[keyArray.length] = new keyArrayItem(\"").append(ik.next()).append("\", \"").append(iv.next()).append("\", null, \"altKey\", false, \"onkeydown\");\n");
    }
    shortcuts.append(" return true;\n}");
    return shortcuts.toString();
  }
  
  private int saveRecord(VariablesSecureApp vars, OBError myError, char type, String strPM_PriceList_ID) throws IOException, ServletException {
    PriceListVersionData data = null;
    int total = 0;
    if (org.openbravo.erpCommon.utility.WindowAccessData.hasReadOnlyAccess(this, vars.getRole(), tabId)) {
        OBError newError = Utility.translateError(this, vars, vars.getLanguage(), Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
        myError.setError(newError);
        vars.setMessage(tabId, myError);
    }
    else {
        Connection con = null;
        try {
            con = this.getTransactionConnection();
            data = getEditVariables(con, vars, strPM_PriceList_ID);
            data.dateTimeFormat = vars.getSessionValue("#AD_SqlDateTimeFormat");            
            String strSequence = "";
            if(type == 'I') {                
        strSequence = SequenceIdData.getUUID();
                if(log4j.isDebugEnabled()) log4j.debug("Sequence: " + strSequence);
                data.mPricelistVersionId = strSequence;  
            }
            if (Utility.isElementInList(Utility.getContext(this, vars, "#User_Client", windowId, accesslevel),data.adClientId)  && Utility.isElementInList(Utility.getContext(this, vars, "#User_Org", windowId, accesslevel),data.adOrgId)){
		     if(type == 'I') {
		       total = data.insert(con, this);
		     } else {
		       //Check the version of the record we are saving is the one in DB
		       if (PriceListVersionData.getCurrentDBTimestamp(this, data.mPricelistVersionId).equals(
                vars.getStringParameter("updatedTimestamp"))) {
                total = data.update(con, this);
               } else {
                 myError.setMessage(Replace.replace(Replace.replace(Utility.messageBD(this,
                    "SavingModifiedRecord", vars.getLanguage()), "\\n", "<br/>"), "&quot;", "\""));
                 myError.setType("Error");
                 vars.setSessionValue(tabId + "|concurrentSave", "true");
               } 
		     }		            
          
            }
                else {
            OBError newError = Utility.translateError(this, vars, vars.getLanguage(), Utility.messageBD(this, "NoWriteAccess", vars.getLanguage()));
            myError.setError(newError);            
          }
          releaseCommitConnection(con);
        } catch(Exception ex) {
            OBError newError = Utility.translateError(this, vars, vars.getLanguage(), ex.getMessage());
            myError.setError(newError);   
            try {
              releaseRollbackConnection(con);
            } catch (final Exception e) { //do nothing 
            }           
        }
            
        if (myError.isEmpty() && total == 0) {
            OBError newError = Utility.translateError(this, vars, vars.getLanguage(), "@CODE=DBExecuteError");
            myError.setError(newError);
        }
        vars.setMessage(tabId, myError);
            
        if(!myError.isEmpty()){
            if(data != null ) {
                if(type == 'I') {            			
                    data.mPricelistVersionId = "";
                }
                else {                    
                    
                }
                vars.setEditionData(tabId, data);
            }            	
        }
        else {
            vars.setSessionValue(windowId + "|M_PriceList_Version_ID", data.mPricelistVersionId);
        }
    }
    return total;
  }

  public String getServletInfo() {
    return "Servlet PriceListVersion. This Servlet was made by Wad constructor";
  } // End of getServletInfo() method
}
