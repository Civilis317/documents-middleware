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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import boip.vxcompany.nl.alfresco_client.Utils;
import boip.vxcompany.nl.alfresco_client.alfresco.SessionProvider;
import boip.vxcompany.nl.alfresco_client.message.Message;
import boip.vxcompany.nl.alfresco_client.security.JwtConfig;

@RestController
public class AlfrescoController {

    @Value("${alfresco.targetfolder}")
    private String targetFolder;

    private static final Logger logger = LoggerFactory.getLogger(AlfrescoController.class);

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public @ResponseBody AlfrescoDocumentMetadata fileUpload(HttpServletRequest request, @RequestPart("title") String title, @RequestPart("description") String description, @RequestPart("uploadFile") MultipartFile file)
            throws Exception {

        String authToken = request.getHeader(JwtConfig.HEADER_STRING);
        String user = JwtConfig.getUser(authToken);

        request.getParameterMap().forEach((k, v) -> logger.info("key: {}, value: {}", k, v));

        Session session = SessionProvider.getSession(user);
        Folder folder = (Folder) session.getObjectByPath("/admin-inbox");

        Document document = saveDocument(session, folder, file.getOriginalFilename(), file, title, description);
        return Utils.toAlfrescoDocumentMetadata(document);
    }

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
                resultList.add(Utils.toAlfrescoDocumentMetadata((Document) contentItem));
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
    private Document createDocument(Session session, Folder parentFolder, String documentName, Message message) throws IOException {
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

    private Document saveDocument(Session session, Folder parentFolder, String documentName, MultipartFile file, String title, String description) throws Exception {
        // Setup document metadata
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, documentName);

        // add title and description
        List<String> secondary = new ArrayList<>();
        secondary.add("P:cm:titled");
        properties.put(PropertyIds.SECONDARY_OBJECT_TYPE_IDS, secondary);
        properties.put("cm:title", title);
        properties.put("cm:description", description);

        // closable resource: try(..){..}catch(e){..}, the inputstream is automatically closed, also no finally block needed
        try (InputStream is = file.getInputStream()) {
            String mimetype = file.getContentType();
            ContentStream contentStream = session.getObjectFactory().createContentStream(documentName, file.getSize(), mimetype, is);

            // Create versioned document object
            Document newDocument = parentFolder.createDocument(properties, contentStream, VersioningState.MAJOR);

            logger.info("Created new document: " + getDocumentPath(newDocument) + " [version=" + newDocument.getVersionLabel() + "][creator=" + newDocument.getCreatedBy() + "][created="
                    + date2String(newDocument.getCreationDate().getTime()) + "]");
            return newDocument;

        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Save document failed: ", e);
        }
    }

    private String getDocumentPath(Document document) {
        String path2Doc = getParentFolderPath(document);
        if (!path2Doc.endsWith("/")) {
            path2Doc += "/";
        }
        path2Doc += document.getName();
        return path2Doc;
    }

    private String getParentFolderPath(Document document) {
        Folder parentFolder = getDocumentParentFolder(document);
        return parentFolder == null ? "Un-filed" : parentFolder.getPath();
    }

    private Folder getDocumentParentFolder(Document document) {
        // Get all the parent folders (could be more than one if multi-filed)
        List<Folder> parentFolders = document.getParents();

        // Grab the first parent folder
        if (parentFolders.size() > 0) {
            if (parentFolders.size() > 1) {
                logger.info("The " + document.getName() + " has more than one parent folder, it is multi-filed");
            }

            return parentFolders.get(0);
        } else {
            logger.info("Document " + document.getName() + " is un-filed and does not have a parent folder");
            return null;
        }
    }

    /**
     * Returns date as a string
     *
     * @param date date object
     * @return date as a string formatted with "yyyy-MM-dd HH:mm:ss z"
     */
    private String date2String(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(date);
    }
}
