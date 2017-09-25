package boip.vxcompany.nl.alfresco_client;

import org.apache.chemistry.opencmis.client.api.Document;

import boip.vxcompany.nl.alfresco_client.controller.AlfrescoDocumentMetadata;

public class Utils {

    public static AlfrescoDocumentMetadata toAlfrescoDocumentMetadata(Document document) {
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
        return admd;
    }

}
