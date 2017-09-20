package boip.vxcompany.nl.alfresco_client.controller;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentType;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import boip.vxcompany.nl.alfresco_client.alfresco.SessionProvider;
import boip.vxcompany.nl.alfresco_client.message.Message;
import boip.vxcompany.nl.alfresco_client.security.JwtConfig;

@RestController
public class AlfrescoController {

    @Value("${alfresco.targetfolder}")
    private String targetFolder;

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoController.class);

    @RequestMapping(path = "/send-message", method = RequestMethod.POST)
    public @ResponseBody String sendMessage(HttpServletRequest request, @RequestBody Message payload) throws IOException {
        //        Session session = SessionProvider.authenticate("admin", "admin");
        String authToken = request.getHeader(JwtConfig.HEADER_STRING);
        String user = JwtConfig.getUser(authToken);
        payload.setSender(user);
        Session session = SessionProvider.getSession(user);
        Folder folder = (Folder) session.getObjectByPath("/admin-inbox");
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss").format(new Date());
        String docName = "message-" + timeStamp + ".txt";
        createDocument(session, folder, docName, payload);
        return "Ok";
    }

    @RequestMapping(path = "/destroy-session", method = RequestMethod.POST)
    public @ResponseBody String destroySession(HttpServletRequest request) {
        String authToken = request.getHeader(JwtConfig.HEADER_STRING);
        String user = JwtConfig.getUser(authToken);
        SessionProvider.deleteSession(user);
        return "Ok";
    }

    @RequestMapping(path = "/documents", method = RequestMethod.GET)
    public @ResponseBody List<AlfrescoDocumentMetadata> getDocumentMetadata(HttpServletRequest request) {
        String authToken = request.getHeader(JwtConfig.HEADER_STRING);
        String user = JwtConfig.getUser(authToken);
        logger.debug(user);
        return getDocuments(user);
    }

    @RequestMapping(value = "/document/{docId}", method = RequestMethod.GET)
    public void getFile(@PathVariable("docId") String docId, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String authToken = request.getHeader(JwtConfig.HEADER_STRING);
        String user = JwtConfig.getUser(authToken);
        Session session = SessionProvider.getSession(user);
        Document document = (Document) session.getObject(docId);
        response.setContentType(document.getContentStreamMimeType());
        InputStream in = downloadDocumentByID(session, docId);
        byte[] buf = new byte[1024];
        int n = 0;
        while ((n = in.read(buf)) > 0) {
            response.getOutputStream().write(buf, 0, n);
        }
        response.getOutputStream().close();
        in.close();
    }

    // get list of documents in user inbox folder
    private List<AlfrescoDocumentMetadata> getDocuments(String username) {
        List<AlfrescoDocumentMetadata> resultList = new ArrayList<>();
        Session session = SessionProvider.getSession(username);
        //        Folder folder = (Folder) session.getObjectByPath("/User Homes/" + username + "/inbox");
        Folder folder = (Folder) session.getObjectByPath(targetFolder.replace("@USER@", username));
        ItemIterable<CmisObject> contentItems = folder.getChildren();
        for (CmisObject contentItem : contentItems) {
            if (contentItem instanceof Document) {
                Document document = (Document) contentItem;
                AlfrescoDocumentMetadata admd = new AlfrescoDocumentMetadata();
                admd.setName(document.getName());
                admd.setType(document.getType().getDisplayName());
                admd.setContentUrl(document.getContentUrl());
                admd.setDescription(document.getDescription());
                admd.setDocId(document.getId());
                admd.setDocumentType(document.getDocumentType().getDisplayName());
                admd.setMimeType(document.getContentStreamMimeType());
                admd.setSize(document.getContentStreamLength());
                admd.setModifiedDate(document.getLastModificationDate());
                admd.setModifiedBy(document.getLastModifiedBy());
                resultList.add(admd);
            }
        }
        return resultList;
    }

    // get document stream from Alfresco
    private InputStream downloadDocumentByID(Session session, String documentID) {
        Document document = (Document) session.getObject(documentID);
        ContentStream cs = document.getContentStream(null);
        BufferedInputStream in = new BufferedInputStream(cs.getStream());
        return in;
    }

    // create a new document
    public Document createDocument(Session session, Folder parentFolder, String documentName, Message message) throws IOException {
        Map<String, Object> newDocumentProps = new HashMap<String, Object>();
        String typeId = "cmis:document";
        newDocumentProps.put(PropertyIds.OBJECT_TYPE_ID, typeId);
        newDocumentProps.put(PropertyIds.NAME, documentName);

        // Setup document content
        String mimetype = "text/plain; charset=UTF-8";
        StringBuffer sb = new StringBuffer(1024);
        sb.append("Sender: ").append(message.getSender()).append("\n\n");
        sb.append("Subject: ").append(message.getSubject()).append("\n\n");
        sb.append(message.getMsgBody());
        String documentText = sb.toString();
        byte[] bytes = documentText.getBytes("UTF-8");
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ContentStream contentStream = session.getObjectFactory().createContentStream(documentName, bytes.length, mimetype, input);
        VersioningState versioningState = VersioningState.NONE;
        DocumentType docType = (DocumentType) session.getTypeDefinition(typeId);
        if (Boolean.TRUE.equals(docType.isVersionable())) {
            logger.info("Document type " + typeId + " is versionable, setting MAJOR version state.");
            versioningState = VersioningState.MAJOR;
        }
        Document newDocument = parentFolder.createDocument(newDocumentProps, contentStream, versioningState);
        return newDocument;
    }

}
