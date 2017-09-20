package boip.vxcompany.nl.alfresco_client.alfresco;

import java.util.HashMap;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SessionProvider {
    private static Map<String, Session> sessionMap = new HashMap<>();
    private static String ATOM_PUB_URL;

    @Value("${alfresco.atom_pubUrl}")
    public void setAtomPubUrl(String url) {
        ATOM_PUB_URL = url;
    }

    public static void deleteSession(String username) {
        sessionMap.remove(username);
    }

    public static Session getSession(String username) {
        Session result = sessionMap.get(username);
        return result;
    }

    public static Session authenticate(String username, String password) {
        System.out.println("received: " + username + " and " + password);

        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameters = new HashMap<String, String>();
        // User Credentials
        parameters.put(SessionParameter.USER, username);
        parameters.put(SessionParameter.PASSWORD, password);
        // Connection Settings
        parameters.put(SessionParameter.ATOMPUB_URL, ATOM_PUB_URL);
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        //        parameters.put(SessionParameter.REPOSITORY_ID, "");
        parameters.put(SessionParameter.AUTH_HTTP_BASIC, "true");
        parameters.put(SessionParameter.COOKIES, "true");

        // Create session. Alfresco only provides one repository.
        Repository repository = sessionFactory.getRepositories(parameters).get(0);
        Session session = repository.createSession();
        sessionMap.put(username, session);

        return session;
    }
}
