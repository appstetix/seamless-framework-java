package com.appstetix.appstract.seamless.core.api;

import com.appstetix.appstract.seamless.core.generic.SeamlessProvider;
import com.appstetix.appstract.seamless.core.generic.UserContext;
import com.appstetix.toolbelt.locksmyth.keycore.TokenType;
import com.appstetix.toolbelt.locksmyth.keycore.exception.InvalidTokenException;
import com.appstetix.toolbelt.locksmyth.keycore.token.KeyRing;
import com.appstetix.toolbelt.locksmyth.keycore.token.TokenValidator;
import com.appstetix.toolbelt.locksmyth.keyverifier.KeyVerifier;
import io.jsonwebtoken.JwtException;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public abstract class SeamlessAPILayer<REQ, RESP> implements SeamlessProvider<REQ, RESP> {

    //VERTX SETTINGS
    protected static final String VERTX_DISABLE_FILE_CPRESOLVING = "vertx.disableFileCPResolving";
    //STATUS CODES
    protected static final int UNAUTHORIZED_STATUS_CODE = 401;
    protected static final int SERVER_ERROR_STATUS_CODE = 500;

    static {
        System.setProperty(VERTX_DISABLE_FILE_CPRESOLVING, "true");
        options = new DeploymentOptions().setWorker(true);
        vertx = Vertx.vertx();
    }

    private static List<String> bypass = new ArrayList();
    protected static DeploymentOptions options;
    protected static Logger logger;
    protected static Vertx vertx;

    public static void addToBypass(String path) {
        bypass.add(path);
    }

    protected static MessageCodec getMessageCodec() {
        return null;
    }

    protected boolean isSecureEndpoint(String path) {
        return !bypass.contains(path);
    }

    protected UserContext securityCheck(String token, boolean strict) throws InvalidTokenException {
        if(StringUtils.isNotEmpty(token)) {
            try {
                final KeyRing keyRing = KeyVerifier.getInstance().verify(token, getValidators());
                return new UserContext(keyRing.getProperties());
            } catch (JwtException ex) {
                System.out.println("Error while evaluating token: " + token);
                ex.printStackTrace();
                throw new InvalidTokenException();
            }
        } else if(!strict) {
            return null;
        }
        throw new InvalidTokenException("Unable to identify user");
    }

    protected <T> T getPostBody(String json, Class<T> clss) {
        if (StringUtils.isNotEmpty(json)) {
            if(StringUtils.isNotEmpty(json)) {
                return Json.decodeValue(json, clss);
            }
        }
        return null;
    }

    //TODO: investigate how to retrieve the DeviceID header and pass it through
    protected TokenValidator[] getValidators() {

        TokenValidator webValidator = new TokenValidator(TokenType.WEB);
        TokenValidator mobileValidator = new TokenValidator(TokenType.MOBILE);
        TokenValidator desktopValidator = new TokenValidator(TokenType.DESKTOP);

        return new TokenValidator[]{webValidator, mobileValidator, desktopValidator};
    }

    protected static void launch(String verticle) {
        launch(verticle, options);
    }

    protected static void launch(String verticle, DeploymentOptions options) {
        vertx.deployVerticle(verticle, options);
    }

}
