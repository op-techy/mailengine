package com.mailengine.mailengine.security;

import com.mailengine.mailengine.entity.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMs;

    /**
     * Generates a JWT (JSON Web Token) for the specified user, including the user's ID, role, and email as claims.
     * The token is signed with a secret key and includes an expiration date.
     *
     * @param user the {@link User} object for which the token is being generated.
     *             The user's ID, role, and email will be included as claims in the token.
     * @return a signed JWT as a {@link String}, containing the user's details and an expiration timestamp.
     */
    public String generateToken(User user){
        return Jwts.builder()
                .subject(user.getId().toString())        // who this token belongs to
                .claim("role", user.getRole().name())    // extra info to embed
                .claim("email", user.getEmail())
                .issuedAt(new Date())                    // when it was created
                .expiration(new Date(System.currentTimeMillis() + expirationMs)) // when it expires
                .signWith(getSigningKey() )               // sign it so nobody can tamper
                .compact();                              // build it into a string
    }

    /**
     * Extracts the user ID from the given JWT token by parsing its subject field.
     * The subject field in the token is assumed to represent the user ID.
     *
     * @param token the JWT token as a {@link String} from which the user ID will be extracted.
     *              The token should be valid and signed with the correct signing key.
     * @return the extracted user ID as a {@link String} obtained from the subject field of the token.
     */
    public String extractUserId(String token) {
        // extract the subject from the token
        return Jwts.parser()
                .verifyWith(getSigningKey())   // use the same key to verify
                .build()
                .parseSignedClaims(token)      // parse it — throws exception if invalid/expired
                .getPayload()
                .getSubject();                 // get what you need from it
    }

    /**
     * Validates a given JWT token by checking if it is parseable and not expired.
     *
     * @param token the JWT token as a {@link String} to be validated.
     *              The token should be signed with the correct signing key.
     * @return {@code true} if the token is valid (parseable and not expired),
     *         {@code false} otherwise.
     */
    public boolean isTokenValid(String token) {
        // return true if token is parseable and not expired
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Generates a signing key for HMAC-based algorithms using the secret key.
     *
     * @return the generated {@link SecretKey} to be used for signing or verifying JWT tokens.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
