/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */


package  org.jasig.portal.channels.groupsmanager;

/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
import  java.util.*;
import  java.io.*;
import  org.jasig.portal.EntityTypes;  /** @todo remove when groups/EntityTypes is removed */
import  org.jasig.portal.*;
import  org.jasig.portal.groups.*;
import  org.jasig.portal.services.*;
import  org.jasig.portal.ChannelRuntimeData;
import  org.jasig.portal.security.*;
import  org.jasig.portal.ChannelStaticData;
import  org.w3c.dom.Node;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Element;
import  org.w3c.dom.Document;
import  javax.xml.parsers.*;


/**
 * @todo
 * refactor: reexamine common functions in GroupsManagerXML, Utility,
 * GroupsManagerCommand, and wrapper classes to come up consistent approach
 *
 * IInitialGroupsContextStore will be used to make the stores swappable through an
 * entry in portal.properties. At this point the RDBMInitialGroupContextStore is
 * hardcoded in InitialGroupsContextImpl.getFactory().
 */

 /**
 * Contains a groups of static methods used to centralize the generation and
 * retrieval of xml elements for groups and entities.
 */
public class GroupsManagerXML
      implements GroupsManagerConstants {
   private static int UID = 0;

   /**
    * Returns a Document for all InitialContexts for which the user has
    * permissions. This method is called when CGroupsManager is instantiated.
    * @param sd
    * @return Document
    */
   public static Document getGroupsManagerXml (ChannelStaticData sd) {
      String rkey = null;
      IEntityGroup entGrp = null;
      IGroupMember aGroupMember = null;
      Element igcElement;
      Document viewDoc = getNewDocument();
      Element viewRoot = viewDoc.createElement("CGroupsManager");
      viewDoc.appendChild(viewRoot);
      Element apRoot = getAuthorizationXml(sd, null, viewDoc);
      viewRoot.appendChild(apRoot);
      Element etRoot = getEntityTypesXml(viewDoc);
      viewRoot.appendChild(etRoot);
      Element igcRoot = GroupsManagerXML.createElement(GROUP_TAGNAME, viewDoc, true);
      igcRoot.setAttribute("expanded", "true");
      Element rdfElem = createRdfElement(null, viewDoc);
      igcRoot.appendChild(rdfElem);
      //* Cut this section into a new method to create group xml without a groupmember object
      viewRoot.appendChild(igcRoot);
      try {
         RDBMInitialGroupContextStore igcHome = RDBMInitialGroupContextStore.singleton();
         // have to get userID from runtimedata
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): JUST BEFORE USER IS SET ");
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): USER SET TO "
               + sd.getAuthorizationPrincipal().getKey());
         java.util.Iterator igcItr = igcHome.findInitialGroupContextsForOwner(AuthorizationService.instance().getGroupMember(sd.getAuthorizationPrincipal()));
         IInitialGroupContext igc = null;
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): igc contains: ");
         // Now that we know if there are any initial group contexts, we can set the attributes.
         if (igcItr.hasNext()) {
            Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): Initial group contexts found for this user:");
         }
         else {
            igcRoot.setAttribute("expanded", "false");
            igcRoot.setAttribute("hasMembers", "false");
            Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): No initial group contexts for this user.");
         }
         while (igcItr.hasNext()) {
            igc = (IInitialGroupContext)igcItr.next();
            Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupsManagerXML(): igc = /n"
                  + igc);
            rkey = igc.getGroupID();
            entGrp = retrieveGroup(rkey);
            //aGroupMember = (IGroupMember) entGrp;
            igcElement = getGroupMemberXml(entGrp, igc.isExpanded(), null, viewDoc);
            igcElement.setAttribute("ownerType", igc.getOwnerType());
            igcRoot.appendChild(igcElement);
         }
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupsManagerXML::getGroupsManagerXML(): ERROR"
               + e.toString());
      }
      return  viewDoc;
   }

   /**
    * Creates an element for the provided Document. Alternatively, can
    * set default values.
    * @param name
    * @param xmlDoc
    * @param setGrpDefault
    * @return Element
    */
   public static Element createElement (String name, Document xmlDoc, boolean setGrpDefault) {
      //* Maybe I should have all parms in a java.util.HashMap
      Element grpRoot = xmlDoc.createElement(name);
      grpRoot.setAttribute("selected", "false");
      // set default values
      if (setGrpDefault) {
         grpRoot.setAttribute("id", "0");
         //grpRoot.setAttribute("key", "");
         grpRoot.setAttribute("expanded", "false");
         // hasMembers is used to determine if the GroupMember has been retrieved
         // (ie. the element has been fully setup)
         //grpRoot.setAttribute("hasMembers", "false");
      }
      // ?? Element rdf = createRdfElement(xmlDoc, title, description, creator);
      // ?? grpRoot.appendChild(rdf);
      //* Cut this section into a new method to create group xml without a groupmember object
      return  grpRoot;
   }

   /**
    * Returns an RDF element for the provided Document
    * @param entGrp IEntityGroup
    * @param xmlDoc Document
    * @return Element
    */
   public static Element createRdfElement (IEntityGroup entGrp, Document xmlDoc) {
      String entName;
      String entDesc;
      String entCreator;
      if (entGrp == null){
         // use default values
         entName = ROOT_GROUP_TITLE;
         entDesc = ROOT_GROUP_DESCRIPTION;
         entCreator = "Default";
      }
      else{
         // get values from IEntityGroup
         entName = entGrp.getName();
         entDesc = entGrp.getDescription();
         entCreator = GroupsManagerXML.getEntityName(entGrp.getLeafType(), entGrp.getCreatorID());
      }
      //* Maybe I should have all parms in a java.util.HashMap
      Element rdfElem = (Element)xmlDoc.createElement("rdf:RDF");
      rdfElem.setAttribute("xmlns:dc", "http://purl.org/dc/elements/1.1/");
      rdfElem.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): CREATING ELEMENT RDF DESCRIPTION");
      Element rdfDesc = (Element)xmlDoc.createElement("rdf:Description");
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): CREATING ELEMENT DCTITLE");
      Element dcTitle = (Element)xmlDoc.createElement("dc:title");
      dcTitle.appendChild(xmlDoc.createTextNode(entName));
      rdfDesc.appendChild(dcTitle);
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): CREATING ELEMENT dcDESCRIPTION");
      Element dcDescription = (Element)xmlDoc.createElement("dc:description");
      dcDescription.appendChild(xmlDoc.createTextNode(entDesc));
      rdfDesc.appendChild(dcDescription);
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): CREATING ELEMENT dcCREATOR");
      Element dcCreator = (Element)xmlDoc.createElement("dc:creator");
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): APPENDING TO dcCREATOR");
      dcCreator.appendChild(xmlDoc.createTextNode(entCreator));
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): APPENDING TO RDFDESC");
      rdfDesc.appendChild(dcCreator);
      Utility.logMessage("DEBUG", "GroupsManagerXML::createRdfElement(): APPENDING TO RDF");
      rdfElem.appendChild(rdfDesc);
      return  rdfElem;
   }

   /**
    * Returns an empty IEntityGroup
    * @param nonPersistentElement Element
    * @return IEntityGroup
    */
   public static IEntityGroup dummyGroup(Element nonPersistentElement){
      /** @todo delete this method...no longer needed */
      IEntityGroup entGrp = null;
      try {
         entGrp = new EntityGroupImpl(null,Class.forName(nonPersistentElement.getAttribute("entityType")));
      }
      catch (ClassNotFoundException cnfe){
         Utility.logMessage("INFO", "gotcha again for search element...class not found");
      }
      catch (GroupsException ge){
         Utility.logMessage("INFO", "gotcha again for search element...groups exception");
      }
      return entGrp;
   }

   /**
    * Expands an element
    * @param expandedElem Element
    * @param xmlDoc Document
    */
   public static void expandGroupElementXML(Element expandedElem, Document xmlDoc){
      //Utility.printElement(expandElem,"Group to be expanded was found (not null): \n" );
      boolean hasGroupsXML = !(expandedElem.getElementsByTagName(GROUP_TAGNAME).getLength()
            == 0);
      boolean hasEntitiesXML = !(expandedElem.getElementsByTagName(ENTITY_TAGNAME).getLength()
            == 0);
      boolean hasMembers = (expandedElem.getAttribute("hasMembers").equals("true"));
      Utility.logMessage("DEBUG", "ExpandGroup::execute(): Expanded element has Members = "
            + hasMembers);

      if (hasMembers) {
         expandedElem.setAttribute("expanded", "true");
         Utility.logMessage("DEBUG", "ExpandGroup::execute(): About to retrieve children");
         // Have to check for non persistent search element before doing retrieval
         IGroupMember entGrp = (!isPersistentGroup(expandedElem) ?
            null :
            (IGroupMember)retrieveGroup(expandedElem.getAttribute("key")));
         GroupsManagerXML.getGroupMemberXml(entGrp, true, expandedElem, xmlDoc);
         //Utility.printDoc(xmlDoc, "renderXML: +++++++++ After children are retrieved +++++++++");
      }
   }

   /**
    * Returns an element holding the user's permissions used to determine access
    * privileges in the Groups Manager channel.
    * @param sd
    * @param apRoot
    * @param xmlDoc
    * @return Element
    */
   public static Element getAuthorizationXml (ChannelStaticData sd, Element apRoot, Document xmlDoc) {
      IAuthorizationPrincipal ap = sd.getAuthorizationPrincipal();
      String princTagname = "principal";
      if (ap != null && apRoot == null) {
         apRoot = xmlDoc.createElement(princTagname);
         apRoot.setAttribute("token", ap.getPrincipalString());
         apRoot.setAttribute("type", ap.getType().getName());
         String name = ap.getKey();
         try {
            name = EntityNameFinderService.instance().getNameFinder(ap.getType()).getName(name);
         } catch (Exception e) {
            Utility.logMessage("ERROR", e.toString());
         }
         apRoot.setAttribute("name", name);
      }
      try {
         // owner, activity, target
         IPermission[] perms = ap.getAllPermissions(OWNER, null, null);
         for (int yy = 0; yy < perms.length; yy++) {
            Element prm = getPermissionXml(xmlDoc, perms[yy].getPrincipal(), perms[yy].getActivity(),
                  perms[yy].getType(), perms[yy].getTarget());
            apRoot.appendChild(prm);
         }
      } catch (org.jasig.portal.AuthorizationException ae) {
         Utility.logMessage("ERROR", "GroupsManagerXML::getAuthorzationXml: authorization exception "
               + ae.getMessage());
      }
      return  apRoot;
   }

   /**
    * Returns an element from an xml document for a unique id. An error is
    * displayed if more than one element is found.
    * @param aDoc
    * @param id
    * @return Element
    */
   public static Element getElementById (Document aDoc, String id) {
      int i;
      Collection elems = new java.util.ArrayList();
      Element elem = null;
      Element retElem = null;
      org.w3c.dom.NodeList nList;
      String tagname = ENTITY_TAGNAME;
      boolean isDone = false;
      while (!isDone) {
         nList = aDoc.getElementsByTagName(tagname);
         for (i = 0; i < nList.getLength(); i++) {
            elem = (Element)nList.item(i);
            if (elem.getAttribute("id").equals(id)) {
               elems.add(elem);
            }
         }
         if (tagname.equals(ENTITY_TAGNAME)) {
            tagname = GROUP_TAGNAME;
         }
         else {
            isDone = true;
         }
         if (elems.size() != 1) {
            if (elems.size() > 1) {
               LogService.log(LogService.ERROR, "GroupsManagerXML::getElementById:  More than one element found for Id: "
                     + id);
            }
         }
         else {
            retElem = (Element)elems.iterator().next();
         }
      }
      return  retElem;
   }

   /**
    * Returns an Element from a Document for a tagname and element id
    * @param aDoc
    * @param tagname
    * @param id
    * @return Element
    */
   public static Element getElementByTagNameAndId (Document aDoc, String tagname,
         String id) {
      int i;
      Element elem = null;
      Element selElem = null;
      org.w3c.dom.NodeList nList = aDoc.getElementsByTagName(tagname);
      for (i = 0; i < nList.getLength(); i++) {
         elem = (Element)nList.item(i);
         if (elem.getAttribute("id").equals(id)) {
            selElem = elem;
            break;
         }
      }
      return  selElem;
   }

   /**
    * Returns the value of an element for a given name
    * @param anElem Element
    * @param tagname String
    * @return String
    */
   public static String getElementValueForTagName (Element anElem, String tagname) {
      Utility.logMessage("DEBUG", "GroupsManagerXML:getElementValueForTagName(): retrieve element value for tagname: " + tagname);
      String retValue = null;
      NodeList nList = anElem.getElementsByTagName(tagname);
      if (nList.getLength() > 0) {
         retValue = nList.item(0).getFirstChild().getNodeValue();
      }
      retValue = (retValue != null ? retValue : "");
      Utility.logMessage("DEBUG", "GroupsManagerXML:getElementValueForTagName(): tagname " + tagname + " = " + retValue);
      return retValue;
   }
   /**
    * Returns a name from the EntityNameFinderService, for a key and class
    * @param typClass
    * @param aKey
    * @return String
    */
   public static String getEntityName (Class typClass, String aKey) {
      //if (aKey != null){ return "BOGUS_ENTITY_NAME";}
      String entName = "";
      String msg;
      long time1 = Calendar.getInstance().getTime().getTime();
      long time2 = 0;
      Utility.logMessage("DEBUG", "GroupsManagerXML.getEntityName(Class,String): Retrieving entity for entityType: " + typClass.getName() + " key: " + aKey);
      try {
         entName = EntityNameFinderService.instance().getNameFinder(typClass).getName(aKey);
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupsManagerXML.getEntityName(Class,String): ERROR retrieving entity "
               + e.toString());
      }
      time2 = Calendar.getInstance().getTime().getTime();
      msg = "GroupsManagerXML.getEntityName(Class,String) timer: " + String.valueOf(time2 - time1)
            + " ms total";
      Utility.logMessage("DEBUG", msg);
      Utility.logMessage("DEBUG", "GroupsManagerXML.getEntityName(Class,String): typClass/aKey/entName = " + typClass + "/" + aKey + "/" + entName);
      return  entName;
   }

   /**
    * Returns a name from the EntityNameFinderService, for a key and classname
    * @param className
    * @param aKey
    * @return String
    */
   public static String getEntityName (String className, String aKey) {
      String entName = "";
      Utility.logMessage("DEBUG", "GroupsManagerXML.getEntityName(String,String): Retrieving entity for entityType: " + className + " key: " + aKey);
      try {
         entName = getEntityName(Class.forName(className), aKey);
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupsManagerXML.getEntityName(String,String): ERROR retrieving entity "
               + e.toString());
      }
      return  entName;
   }

   /**
    * Returns a HashMap of entity types. These are the entity types that can be added
    * to a group. We determine this by retrieving all entity types from the EntityTypes
    * class and using the GroupService class to determine which types have a root
    * group.
    * @return HashMap
    */
   public static HashMap getEntityTypes () {
      HashMap entTypes = new HashMap(5);
      /** @todo can't determine size due to private methods */
      //HashMap entTypes = new HashMap(EntityTypes.singleton().getEntityTypesByType().size());
      String entName;
      String entClassName;
      Iterator entTypesItr = EntityTypes.singleton().getAllEntityTypes();
      while (entTypesItr.hasNext()) {
         Class entType = (Class)entTypesItr.next();
         entClassName = entType.getName();
         //entName = entClassName.substring(entClassName.lastIndexOf('.') + 1);
         entName = EntityTypes.singleton().getDescriptiveNameForType(entType);
         try {
            if (GroupService.getRootGroup(entType) != null) {
               entTypes.put(entName, entClassName);
               Utility.logMessage("DEBUG", "GroupsManagerXML::getEntityTypes Added : "
                     + entName + " -- " + entClassName);
            }
            else {
               Utility.logMessage("DEBUG", "GroupsManagerXML::getEntityTypes Did NOT Add : "
                     + entName + " -- " + entClassName);
            }
         } catch (Exception e) {
            // an exception means we do not want to add this entity to the list
            Utility.logMessage("DEBUG", "GroupsManagerXML::getEntityTypes [Exception] Did NOT Add : "
                  + entName + " -- " + entClassName);
         }
      }
      return  entTypes;
   }

   /**
    * Returns an element holding the entity types used in uPortal.
    * @param xmlDoc
    * @return Element
    */
   public static Element getEntityTypesXml (Document xmlDoc) {
      Element etRoot = xmlDoc.createElement("entityTypes");
      HashMap entTypes = getEntityTypes();
      Iterator entTypeKeys = entTypes.keySet().iterator();
      while (entTypeKeys.hasNext()) {
         Object key = entTypeKeys.next();
         String entType = (String)entTypes.get(key);
         Element etElem = xmlDoc.createElement("entityType");
         etElem.setAttribute("name", (String)key);
         etElem.setAttribute("type", entType);
         etRoot.appendChild(etElem);
      }
      return  etRoot;
   }

   /**
    * Returns an Element with the expanded attribute set to true from a
    * Document for a tagname and IGroupMember key. This could be used for
    * cloning elements that have already be expanded thereby avoiding the extra
    * time required to retrieve and create an element.
    * @param aDoc
    * @param tagname
    * @param key
    * @return Element
    */
   public static Element getExpandedElementForTagNameAndKey (Document aDoc, String tagname,
         String key) {
      java.util.Iterator nodeItr = getNodesByTagNameAndKey(aDoc, tagname, key);
      Element curElem = null;
      Element expElem = null;
      while (nodeItr.hasNext()) {
         curElem = (Element)nodeItr.next();
         if (curElem.getAttribute("expanded").equals("true")) {
            expElem = curElem;
            break;
         }
      }
      return  expElem;
   }

   /**
    * Returns an Element for an IGroupMember. If an element is passed in,
    * it is acted upon (eg. expand the group), otherwise a new one is created.
    * @param gm
    * @param isContextExpanded
    * @param anElem
    * @param aDoc
    * @return Element
    */
   public static Element getGroupMemberXml (IGroupMember gm, boolean isContextExpanded,
         Element anElem, Document aDoc) {
      // search elements are nonPersistent and come in as a null group member.
      if (gm == null) {return null;}
      Element rootElem = null;
      GroupsManagerWrapperFactory wf = GroupsManagerWrapperFactory.instance();
      if (gm.isEntity()) {
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): About to get entity wrapper");
         IGroupsManagerWrapper rap = (IGroupsManagerWrapper)wf.get(ENTITY_TAGNAME);
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): Got entity wrapper");
         if (rap != null) {
            rootElem = rap.getXml(gm, anElem, aDoc);
         }
      }
      else {
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): element parm is null = "
               + (anElem == null));
         rootElem = (anElem != null ? anElem : GroupsManagerXML.createElement(GROUP_TAGNAME,
               aDoc, false));
         rootElem.setAttribute("expanded", String.valueOf(isContextExpanded));
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): About to get group wrapper");
         IGroupsManagerWrapper rap = (IGroupsManagerWrapper)wf.get(GROUP_TAGNAME);
         Utility.logMessage("DEBUG", "GroupsManagerXML::getGroupMemberXml(): Got group wrapper");
         if (rap != null) {
            rootElem = rap.getXml(gm, rootElem, aDoc);
         }
      }
      return  rootElem;
   }

   /**
    * Returns a new Document
    * @return Document
    */
   public static Document getNewDocument () {
      Document aDoc = null;
      Utility.logMessage("DEBUG", "GroupsManagerXML::getNewDocument(): About to get new Document");
      try{
         aDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      }
      catch(ParserConfigurationException pce){
         Utility.logMessage("ERROR", "GroupsManagerXML::getNewDocument(): Unable to get new Document\n"
               + pce);
      }
      return aDoc;
   }

   /**
    * Returns the next sequential identifier which is used to uniquely
    * identify an element. This identifier is held in the Element "id" attribute.
    * "0" is reserved for the Group containing the Initial Contexts for the user.
    * @return String
    */
   public static synchronized String getNextUid () {
      // max size of int = (2 to the 32 minus 1) = 2147483647
      Utility.logMessage("DEBUG", "GroupsManagerXML::getNextUid(): Start");
      if (UID > 2147483600) {
         // the value 0 is reserved for the group holding the initial group contexts
         UID = 0;
      }
      return  String.valueOf(++UID);
   }

   /**
    * Even though we know we will find a single element, we sometimes want
    * it returned in an iterator in order to streamline processing.
    * @param aDoc
    * @param id
    * @return iterator
    */
   public static java.util.Iterator getNodesById (Document aDoc, String id) {
      int i;
      Collection nodes = new java.util.ArrayList();
      Element elem = (Element)getElementById(aDoc, id);
      nodes.add(elem);
      return  nodes.iterator();
   }

   /**
    * Returns an iterator of Nodes for a Document for a tagname and IGroupMember key
    * @param aDoc
    * @param tagname
    * @param key
    * @return Iterator
    */
   public static java.util.Iterator getNodesByTagNameAndKey (Document aDoc, String tagname,
         String key) {
      int i;
      Collection nodes = new java.util.ArrayList();
      Element elem = null;
      org.w3c.dom.NodeList nList = aDoc.getElementsByTagName(tagname);

      for (i = 0; i < nList.getLength(); i++) {
         elem = (Element)nList.item(i);
         if (elem.getAttribute("key").equals(key)) {
            nodes.add(nList.item(i));
         }
      }
      Utility.logMessage("DEBUG", "GroupsManagerXML::getNodesByTagNameAndKey: Number of nodes found for tagname " + tagname + " and Key: "
            + key + " is: " + nodes.size());
      return  nodes.iterator();
   }

   /**
    * Returns an iterator of Nodes for an Element for a tagname and IGroupMember key
    * @param anElem
    * @param tagname
    * @param key
    * @return Iterator
    */
   public static java.util.Iterator getNodesByTagNameAndKey (Element anElem, String tagname,
         String key) {
      int i;
      Collection nodes = new java.util.ArrayList();
      Element elem = null;
      org.w3c.dom.NodeList nList = anElem.getElementsByTagName(tagname);

      for (i = 0; i < nList.getLength(); i++) {
         elem = (Element)nList.item(i);
         if (elem.getAttribute("key").equals(key)) {
            nodes.add(nList.item(i));
         }
      }
      Utility.logMessage("DEBUG", "GroupsManagerXML::getNodesByTagNameAndKey: Number of nodes found for tagname " + tagname + " and Key: "
            + key + " is: " + nodes.size());
      return  nodes.iterator();
   }

   /**
    * Returns an element for a permission.
    * @param xmlDoc
    * @param prmPrincipal
    * @param prmActivity
    * @param prmType
    * @param prmTarget
    * @return Element
    */
   public static Element getPermissionXml (Document xmlDoc, String prmPrincipal,
         String prmActivity, String prmType, String prmTarget) {
      Element prm = xmlDoc.createElement("permission");
      prm.setAttribute("principal", prmPrincipal);
      prm.setAttribute("activity", prmActivity);
      prm.setAttribute("type", prmType);
      prm.setAttribute("target", prmTarget);
      return  prm;
   }

   /**
    * Group elements that hold search results are non-persistent and should be treated differently.
    * For example, they do not have a "key" attribute so code that attempts to retreive
    * a GroupMember should not be attempted.
    * @param anElem Element
    * @return boolean
    */
   public static boolean isPersistentGroup(Element anElem){
      boolean rval = true;
      if (anElem == null){
         /** @todo this should be an error */
         Utility.logMessage("INFO", "GroupsManagerXML::isPersistentGroup(): anElem is null");
      }
      if (!Utility.areEqual(anElem.getNodeName(), GROUP_TAGNAME)
              || Utility.areEqual(anElem.getAttribute("searchResults"), "true")) {
         rval= false;
      }
      return rval;
   }

   /**
    * Updates all nodes for the same IEntityGroup with information about the IEntityGroup.
    * @param model  Document
    * @param entGrp  IEntityGroup
    * @throws GroupsException
    */
   public static void refreshAllNodes (Document model, IEntityGroup entGrp)
         throws GroupsException {
      String updKey = entGrp.getKey();
      Node updNode;
      Element updElem;
      Utility.logMessage("DEBUG", "GroupsManagerXML::refreshAllNodes(): About to refresh all nodes for IEntityGroup: "
            + updKey);
      Iterator updatedNodes = GroupsManagerXML.getNodesByTagNameAndKey(model, GROUP_TAGNAME,
            updKey);
      Utility.logMessage("DEBUG", "GroupsManagerXML::refreshAllNodes(): About to gather all elements for key: "
            + updKey);
      while (updatedNodes.hasNext()) {
         updNode = (Node)updatedNodes.next();
         updElem = (Element)updNode;
         refreshElement (updElem, entGrp);
      }
      return;
   }

   /**
    * Updates all nodes representing the same IEntityGroup that is represented by the
    * anElem, if the anElem is out of date with the IEntityGroup.
    * @param model  Document
    * @param anElem Element
    */
   public static void refreshAllNodesIfRequired (Document model, Element anElem){
      // A search element holds search results and should not be refreshed
      // because it is not persistent.
      if (!isPersistentGroup(anElem)) {
         return;
      }
      try{
         // refresh nodes if the anElem is out of date
         if (refreshRequired(anElem, null)) {
            Utility.logMessage("Debug", "GroupsManagerXML::refreshAllNodesIfRequired(): Element needs refreshing : "
               + anElem);
            refreshAllNodes(model,retrieveGroup(anElem.getAttribute("key")));
         }
      } catch (GroupsException ge){
         Utility.logMessage("INFO", "GroupsManagerXML::refreshAllNodesIfRequired(): "
            + "Unable to refresh all elements for IEntityGroup represented by element: "
            + anElem);
      }
      return;
   }

   /**
    * Updates all nodes representing the same IEntityGroup that is represented by the
    * anElem, if the anElem is out of date with the IEntityGroup. Additionally, we
    * do the same for each child node (one level down at this time).
    * @param model  Document
    * @param parentElem Element
    */
   public static void refreshAllNodesRecursivelyIfRequired (Document model, Element parentElem){
      /** @todo not really recursive, we only go one level down, reconcile this
       *  in code or by changing name */
      if (parentElem == null){
         /** @todo this should be an error */
         Utility.logMessage("INFO", "GroupsManagerXML::refreshAllNodesRecursivelyIfRequired(): parentElem is null");
      }
      Element childElem;
      Node childNode;
      NodeList childNodes;
      String childType;
      refreshAllNodesIfRequired(model, parentElem);
      String parentType = parentElem.getAttribute("type");
      Node parentNode = (Node)parentElem;
      childNodes = parentNode.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
         childNode = (org.w3c.dom.Node)childNodes.item(i);
         childElem = (Element)childNode;
         childType = childElem.getAttribute("type");
         if (Utility.notEmpty(childType)){
            refreshAllNodesIfRequired(model, childElem);
         }
         // Does parent have any new children
         // The wrapper will automatically add new children if expanded attribute is set to "true"
         // but we have to set it back to the original value.
         String saveExpand = parentElem.getAttribute("expanded");
         // Have to check for non persistent search element before doing retrieval
         IGroupMember parentGM = ((!isPersistentGroup(parentElem) ?
            null :
            (IGroupMember)retrieveGroup(parentElem.getAttribute("key"))));
         getGroupMemberXml (parentGM , true, parentElem, model);
         parentElem.setAttribute("expanded", saveExpand);
      }
      return;
   }

   /**
    * Updates an Element with information about the IEntityGroup.
    * @param updElem  Element
    * @param entGrp  IEntityGroup
    */
   public static void refreshElement (Element updElem, IEntityGroup entGrp) {
      // A search element holds search results and should not be refreshed
      // because it is not persistent.
      if (!isPersistentGroup(updElem)) {
         return;
      }
      IEntityGroup updEntGrp = (entGrp != null ? entGrp : retrieveGroup (updElem.getAttribute("key")));
      //Utility.logMessage("DEBUG", "GroupsManagerXML::refreshElement(): About to update xml for element id: " + updElem.getAttribute("id") + " Key: " + updElem.getAttribute("key"));
      Utility.printElement(updElem, "Element before update------");
      NodeList nList = updElem.getElementsByTagName("dc:title");
      if (nList.getLength() > 0) {
         Node titleNode = nList.item(0);
         titleNode.getFirstChild().setNodeValue(entGrp.getName());
      }

      nList = updElem.getElementsByTagName("dc:description");
      if (nList.getLength() > 0) {
         Node descNode = nList.item(0);
         descNode.getFirstChild().setNodeValue(entGrp.getDescription());
      }

      nList = updElem.getElementsByTagName("dc:creator");
      if (nList.getLength() > 0) {
         Node descNode = nList.item(0);
         String crtName = GroupsManagerXML.getEntityName(updEntGrp.getLeafType(), updEntGrp.getCreatorID());
         descNode.getFirstChild().setNodeValue(crtName);
      }
      Utility.printElement(updElem, "Element after update++++++");
      return;
   }

   /**
    * Updates an Element with information about the IEntityGroup.
    * @param chkElem Element
    * @param entGrp  IEntityGroup
    * @return boolean
    *
    */
   public static boolean refreshRequired (Element chkElem, IEntityGroup entGrp) {
      // A search element holds search results and should not be refreshed
      // because it is not persistent.
      if (!isPersistentGroup(chkElem)) {
         return false;
      }
      IEntityGroup chkEntGrp = (entGrp != null ? entGrp : retrieveGroup (chkElem.getAttribute("key")));
      Utility.logMessage("DEBUG", "GroupsManagerXML::refreshRequired(): About to check if element needs to be refreshed for Element ID: "
            + chkElem.getAttribute("id") + " Key: " + chkElem.getAttribute("key"));
      String elemValue = getElementValueForTagName(chkElem, "dc:title");
      if (!Utility.areEqual(elemValue, chkEntGrp.getName())){
         Utility.logMessage("DEBUG", "GroupsManagerXML::refreshRequired(): Name has changed!!");
         return true;
      }
      elemValue = getElementValueForTagName(chkElem, "dc:description");
      if (!Utility.areEqual(elemValue, chkEntGrp.getDescription())){
         Utility.logMessage("DEBUG", "GroupsManagerXML::refreshRequired(): Description has changed!!");
         return true;
      }

      return false;
   }

   /**
    * Returns an IEntity for the key.
    * @param aKey
    * @param aType
    * @return IEntity
    */
   public static IEntity retrieveEntity (String aKey, String aType) {
      IEntity ent = null;
      try {
         Class iEntityClass = Class.forName(aType);
         ent = GroupService.getEntity(aKey, iEntityClass);
      } catch (Exception e) {
         Utility.logMessage("ERROR", "EntityWrapper.retrieveEntity(): ERROR retrieving entity "
               + e.toString());
      }
      return  ent;
   }

   /**
    * Returns an IEntityGroup for the key.
    * @param aKey
    * @return IEntityGroup
    */
   public static IEntityGroup retrieveGroup (String aKey) {
      Utility.logMessage("DEBUG", "GroupsManagerXML::retrieveGroup(): About to search for Group: "
            + aKey);
      IEntityGroup grp = null;
      try {
         if (aKey != null){
            grp = GroupService.findGroup(aKey);
         }
      } catch (Throwable th) {
         Utility.logMessage("ERROR", "GroupsManagerXML::retrieveGroup(): Could not retrieve Group Member ("
               + aKey + "): \n" + th);
      }
      return  grp;
   }

   /**
    * Returns the IGroupMember represented by an Element
    * @param aDoc
    * @param id
    * @return IGroupMember
    */
   public static IGroupMember retrieveGroupMemberForElementId (Document aDoc, String id) {
      Element gmElem = getElementById(aDoc, id);
      IGroupMember gm;

      // A null is returned if the element is null OR if the element is for a group that
      // is non-persistent
      if (gmElem == null || (Utility.areEqual(gmElem.getNodeName(), GROUP_TAGNAME) && !isPersistentGroup(gmElem))) {
         Utility.logMessage("INFO", "GroupsManagerXML::retrieveGroupMemberForElementId(): Unable to retrieve the element with id = "
               + id);
         return  null;
      }
      else {
         Utility.logMessage("DEBUG", "GroupsManagerXML::retrieveGroupMemberForElementId(): The child type = "
               + gmElem.getTagName());
      }
      String gmKey = gmElem.getAttribute("key");
      Utility.logMessage("DEBUG", "GroupsManagerXML::retrieveGroupMemberForElementId(): About to retrieve group member ("
            + gmElem.getTagName() + " for key: " + gmKey);
      if (gmElem.getTagName().equals(GROUP_TAGNAME)) {
         gm = (IGroupMember)GroupsManagerXML.retrieveGroup(gmKey);
      }
      else {
         gm = (IGroupMember)GroupsManagerXML.retrieveEntity(gmKey,gmElem.getAttribute("type"));
      }
      return  gm;
   }
}



