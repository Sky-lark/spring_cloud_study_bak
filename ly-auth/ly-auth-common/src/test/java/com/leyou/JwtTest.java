package com.leyou;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.common.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtTest {

    private static final String pubKeyPath = "C:\\tmp\\rsa\\rsa.pub";

    private static final String priKeyPath = "C:\\tmp\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        // 生成token
        String token = JwtUtils.generateToken(new UserInfo(20L, "jack"), privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiamFjayIsImV4cCI6MTU2MzI0NDQ4M30.VHykhtaBrcmZsuLrohg-GpEHHw4DATBU-_STgiFl-Gat4QzJPQaGVcXpQ8oHELGWfu4XRiw68TjSyd3-YvfwpwogZQSkBOsBSFVDp1N4zwLVYQO2oAGFLURQSyh7cPy3dTRDHl4UPh3PYs3uL1KDqELDICV6ZnY6-d1Bse4XP8Y";

        // 解析token
        UserInfo user = JwtUtils.getUserInfo( publicKey,token);
        System.out.println("id: " + user.getId());
        System.out.println("userName: " + user.getUsername());
    }
}