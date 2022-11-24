package com.matopohl.user_management.service.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
public class RSAService {

    @Value("${my.RSA.public}")
    private String PUBLIC;

    @Value("${my.RSA.private}")
    private String PRIVATE;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        RSAService rsaHelper = new RSAService();

        rsaHelper.generate();
    }

    private void generate() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        try (FileOutputStream fosPublic = new FileOutputStream("src/main/resources/new public.key");
             FileOutputStream fosPrivate = new FileOutputStream("src/main/resources/new private.key")) {
            fosPublic.write(publicKey.getEncoded());
            fosPrivate.write(privateKey.getEncoded());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public RSAPublicKey getPublicKey() throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {
        try (InputStream inputStream = getClass().getResourceAsStream(PUBLIC)) {
            assert inputStream != null;
            byte[] keyBytes = inputStream.readAllBytes();

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }
    }

    public RSAPrivateKey getPrivateKey() throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {
        try (InputStream inputStream = getClass().getResourceAsStream(PRIVATE)) {
            assert inputStream != null;
            byte[] keyBytes = inputStream.readAllBytes();

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        }
    }

}
