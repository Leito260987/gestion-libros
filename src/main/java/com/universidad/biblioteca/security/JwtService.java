package com.universidad.biblioteca.security;

import com.universidad.biblioteca.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de emision y validacion de JWT. Usa secretos distintos para access
 * y refresh tokens, y firma HS256. Los tokens llevan un claim "type" que impide
 * usar un refresh token como access token y viceversa.
 */
@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_UID = "uid";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final JwtProperties props;
    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtService(JwtProperties props) {
        this.props = props;
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.refreshSecret()));
    }

    public String generateAccessToken(UserPrincipal user) {
        List<String> roles = user.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList());
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.expirationMs());
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_UID, user.getId())
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .issuer(props.issuer())
                .issuedAt(now)
                .expiration(exp)
                .signWith(accessKey)
                .compact();
    }

    public String generateRefreshToken(UserPrincipal user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + props.refreshExpirationMs());
        return Jwts.builder()
                .subject(user.getUsername())
                .claim(CLAIM_UID, user.getId())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .issuer(props.issuer())
                .issuedAt(now)
                .expiration(exp)
                .signWith(refreshKey)
                .compact();
    }

    /** Extrae y valida (firma, expiracion, issuer, tipo) un access token. */
    public Claims parseAccessToken(String token) {
        Claims claims = parse(token, accessKey);
        requireType(claims, TYPE_ACCESS);
        return claims;
    }

    /** Extrae y valida un refresh token. Lanza JwtException si no es valido. */
    public Claims parseRefreshToken(String token) {
        Claims claims = parse(token, refreshKey);
        requireType(claims, TYPE_REFRESH);
        return claims;
    }

    public long getAccessExpirationSeconds() {
        return props.expirationMs() / 1000;
    }

    public String getSubject(Claims claims) {
        return claims.getSubject();
    }

    private Claims parse(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private void requireType(Claims claims, String expected) {
        if (!expected.equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("Tipo de token invalido; se esperaba: " + expected);
        }
    }
}
