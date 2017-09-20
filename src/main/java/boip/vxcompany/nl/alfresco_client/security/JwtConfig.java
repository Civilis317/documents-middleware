package boip.vxcompany.nl.alfresco_client.security;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtConfig {
    private static final long EXPIRATIONTIME = 864_000_000; // 10 days
    private static final String SECRET = "17b450ec-96fe-11e7-abc4-cec278b6b50a";
    public static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";

    public static String getToken(String username) throws UnsupportedEncodingException {
        return Jwts.builder().setSubject(username).setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME)).signWith(SignatureAlgorithm.HS512, SECRET.getBytes("UTF-8")).compact();
    }

    public static String getUser(String token) {
        try {
            return Jwts.parser().setSigningKey(SECRET.getBytes("UTF-8")).parseClaimsJws(token.replace(TOKEN_PREFIX + " ", "")).getBody().getSubject();
        } catch (Exception e) {
            return null;
        }
    }

}
