package com.appstetix.appstract.seamless.core.validator;

import com.appstetix.appstract.seamless.core.annotation.*;
import com.appstetix.appstract.seamless.core.api.SeamlessAPI;
import com.appstetix.appstract.seamless.core.api.SeamlessHandler;
import com.appstetix.appstract.seamless.core.api.SeamlessRequest;
import com.appstetix.appstract.seamless.core.api.SeamlessResponse;
import com.appstetix.appstract.seamless.core.exception.generic.ExceptionHandler;
import com.appstetix.appstract.seamless.core.exception.handler.DefaultExceptionHandler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.appstetix.appstract.seamless.core.validator.ValidatorProcessorTest.ValidatorProcessorTestAPIHandler.SUCCESSFULLY_PASSED_VALIDATION;
import static io.vertx.core.http.HttpMethod.*;

@RunWith(VertxUnitRunner.class)
public class ValidatorProcessorTest {

    public static final String FAIL_DEFAULT_URL = "GET:/valid/fail/default";
    public static final String FAIL_CUSTOM_URL = "GET:/valid/fail/custom";
    private static ValidatorTestAPI api;

    @BeforeClass
    public static void setUp(TestContext context) throws Exception {
        api = new ValidatorTestAPI();
    }

    @Test
    public void testValidatorWithSuccessfulCriteria(TestContext context) {
        final Async async = context.async();
        try {
            final SeamlessResponse response = api.test("GET:/valid/criteria");
            context.assertNotNull(response);
            context.assertEquals(200, response.getCode());
            context.assertEquals(SUCCESSFULLY_PASSED_VALIDATION, response.getPayload());
            async.complete();
        } catch (Exception ex) {
            ex.printStackTrace();
            context.fail();
            async.complete();
        }
    }

    @Test
    public void testValidatorWithUnsuccessfulCriteriaAndDefaultExceptionHandler(TestContext context) {
        final Async async = context.async();
        try {
            final SeamlessResponse response = api.test(FAIL_DEFAULT_URL);
            context.assertNotNull(response);
            context.assertEquals(500, response.getCode());
            context.assertEquals(DefaultExceptionHandler.DEFAULT_ERROR_MESSAGE, response.getPayload());
            async.complete();
        } catch (Exception ex) {
            ex.printStackTrace();
            context.fail();
            async.complete();
        }
    }

    @Test
    public void testValidatorWithUnsuccessfulCriteriaAndCustomExceptionHandler(TestContext context) {
        final Async async = context.async();
        try {
            final SeamlessResponse response = api.test(FAIL_CUSTOM_URL);
            context.assertNotNull(response);
            context.assertEquals(455, response.getCode());
            context.assertEquals(CustomValidator.ERROR_MESSAGE, response.getPayload());
            async.complete();
        } catch (Exception ex) {
            ex.printStackTrace();
            context.fail();
            async.complete();
        }
    }

    @API(handlers = ValidatorProcessorTestAPIHandler.class, validators = CustomValidator.class)
    @EnableExceptionHandling
    public static class ValidatorTestAPI extends SeamlessAPI<String, Object> {

        public SeamlessResponse test(String path) {
            SeamlessRequest request = convertRequest(path);
            try {
                final CompletableFuture<SeamlessResponse> future = new CompletableFuture<>();
                executeValidator(request);
                vertx.eventBus().send(path, null, result -> {
                    SeamlessResponse response = null;
                    try {
                        if(result.succeeded()) {
                            response = (SeamlessResponse) getPostBody(result.result().body().toString(), SeamlessResponse.class);
                            if(response.hasError()) {
                                response = resolveException(request, response.getErrorClass(), response.getError());
                            }
                        } else {
                            response = resolveException(request, result.cause().getClass().getName(), result.cause());
                        }
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        future.complete(response);
                    }
                });
                SeamlessResponse response = future.get();
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return resolveException(request, e.getClass().getName(), e);
            }
        }

        @Override
        public SeamlessRequest convertRequest(String path) {
            SeamlessRequest request = new SeamlessRequest();
            request.setMethod(GET.toString());
            request.setPath(path);
            return request;
        }

        @Override
        public Object convertResponse(SeamlessResponse response) {
            return null;
        }
    }

    @APIHandler(baseURL = "valid")
    public static class ValidatorProcessorTestAPIHandler extends SeamlessHandler {

        public static final String SUCCESSFULLY_PASSED_VALIDATION = "Successfully passed validation";
        public static final String SUCCESSFULLY_FAILED_DEFAULT_VALIDATION = "Successfully failed validation with default exception";
        public static final String SUCCESSFULLY_FAILED_CUSTOM_VALIDATION = "Successfully failed validation with custom exception";

        @Endpoint(path = "criteria")
        public String successfulValidation() {
            return SUCCESSFULLY_PASSED_VALIDATION;
        }

        @Endpoint(path = "fail/default")
        public String failDefaultValidation() throws Exception {
            throw new Exception(SUCCESSFULLY_FAILED_DEFAULT_VALIDATION);
        }

        @Endpoint(path = "fail/custom")
        public String failCustomValidation() throws ValidatorException {
            throw new ValidatorException(SUCCESSFULLY_FAILED_CUSTOM_VALIDATION);
        }

    }

    public static class CustomValidator implements APIValidator {

        public static final String ERROR_MESSAGE = "This url is not allowed to access this API";

        @Override
        public void validate(SeamlessRequest request, SeamlessAPI apiLayer) throws Exception {
            if(request.getPath().equalsIgnoreCase(FAIL_DEFAULT_URL)) {
                throw new Exception();
            } else if(request.getPath().equalsIgnoreCase(FAIL_CUSTOM_URL)) {
                throw new ValidatorException(ERROR_MESSAGE);
            }
        }
    }

    @APIException(ValidatorException.class)
    public static class ValidatorExceptionHandler implements ExceptionHandler<String> {

        @Override
        public int responseCode(SeamlessRequest request, Throwable exception) {
            return 455;
        }

        @Override
        public Map<String, String> headers(SeamlessRequest request, Throwable exception) {
            return null;
        }

        @Override
        public String body(SeamlessRequest request, Throwable exception) {
            return exception.getMessage();
        }
    }

    public static class ValidatorException extends Exception {

        public ValidatorException() {
        }

        public ValidatorException(String message) {
            super(message);
        }

        public ValidatorException(String message, Throwable cause) {
            super(message, cause);
        }

        public ValidatorException(Throwable cause) {
            super(cause);
        }

        public ValidatorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

}
