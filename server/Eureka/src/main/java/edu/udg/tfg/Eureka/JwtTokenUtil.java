/*
package edu.udg.tfg.Eureka;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class JwtTokenUtil {

    @Value("classpath:eureka.ppk")
    private Resource privateFile;
    @Value("classpath:eureka.pub")
    private Resource publicFile;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @PostConstruct
    public void init() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DSA");
        keyPairGen.initialize(2048);

        //Generating the pair of keys
        KeyPair pair = keyPairGen.generateKeyPair();

        privateKey = loadPrivateKey();
        publicKey = loadPublicKey();
    }

    private PrivateKey loadPrivateKey() throws Exception {
        InputStream inputStream = privateFile.getInputStream();
            byte[] keyBytes = inputStream.readAllBytes();
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("DSA");
            return kf.generatePrivate(spec);


    }

    private PublicKey loadPublicKey() throws Exception {
        InputStream inputStream = publicFile.getInputStream();
        byte[] keyBytes = inputStream.readAllBytes();
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("DSA");
        return kf.generatePublic(spec);
    }



    public String generateToken(String username) {

        return Jwts.builder()
                .subject(username)
                .signWith(privateKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
*/
