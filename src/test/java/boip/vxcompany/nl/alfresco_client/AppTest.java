package boip.vxcompany.nl.alfresco_client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    /*
    public void testSession() {
        Session session = SessionProvider.authenticate("jceasar", "welcome1");
        downloadDocumentByID(session, "d601cb1d-c0bf-4bcf-b9cb-e7ad5054d1ec;1.0");
    }
    
    public void downloadDocumentByID(Session session, String documentID) {
        //String    fullPath= destinationFolder + fileName;
        Document document = (Document) session.getObject(documentID);
        System.out.println(document.getId());
        String fullPath = "./" + document.getName();
        try {
            ContentStream cs = document.getContentStream(null);
            BufferedInputStream in = new BufferedInputStream(cs.getStream());
            FileOutputStream fos = new FileOutputStream(fullPath);
            OutputStream bufferedOutputStream = new BufferedOutputStream(fos);
            byte[] buf = new byte[1024];
            int n = 0;
            while ((n = in.read(buf)) > 0) {
                bufferedOutputStream.write(buf, 0, n);
            }
            bufferedOutputStream.close();
            fos.close();
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
    }
    
    private CmisObject getObject(Session session, String username, String objectName) {
        CmisObject object = null;
        try {
            Folder parentFolder = (Folder) session.getObjectByPath("/User Homes/" + username + "/inbox");
            String path2Object = parentFolder.getPath();
            if (!path2Object.endsWith("/")) {
                path2Object += "/";
            }
            path2Object += objectName;
            object = session.getObjectByPath(path2Object);
        } catch (CmisObjectNotFoundException nfe0) {
            // Nothing to do, object does not exist
        }
        return object;
    }
    */

}
