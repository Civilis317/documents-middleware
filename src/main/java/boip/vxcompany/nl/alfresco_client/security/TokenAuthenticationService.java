package boip.vxcompany.nl.alfresco_client.security;

import static java.util.Collections.emptyList;

import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import boip.vxcompany.nl.alfresco_client.alfresco.SessionProvider;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

public class TokenAuthenticationService {

    static void addAuthentication(HttpServletResponse response, String username) throws UnsupportedEncodingException {
        String JWT = JwtConfig.getToken(username);
        response.addHeader(JwtConfig.HEADER_STRING, JwtConfig.TOKEN_PREFIX + " " + JWT);
    }

    static Authentication getAuthentication(HttpServletRequest request)
            throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException, UnsupportedEncodingException {
        String token = request.getHeader(JwtConfig.HEADER_STRING);
        if (token != null) {
            String user = JwtConfig.getUser(token);

            // does this user have an Alfresco session?
            return SessionProvider.getSession(user) != null ? new UsernamePasswordAuthenticationToken(user, null, emptyList()) : null;
        } else {
            System.out.println("No token found...");
        }
        return null;
    }
}
