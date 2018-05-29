package com.revolut.interview;

import com.google.common.collect.Lists;
import com.revolut.interview.dto.AccountResponse;
import com.revolut.interview.dto.CreateAccountRequest;
import com.revolut.interview.dto.RefillRequest;
import com.revolut.interview.dto.TransferRequest;
import com.revolut.interview.model.Account;
import com.revolut.interview.repository.AccountRepository;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.Assertions;
import org.jooby.Status;
import org.jooby.test.JoobyRule;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.equalTo;

public class ApiTest {

    private static final String PATH_WITHDRAW = "/{id}/withdraw";

    private static final String PATH_DEPOSIT = "/{id}/deposit";

    private static final String PATH_TRANSFER = "/{id}/transfer";



    public static Application app = new Application();

    @ClassRule
    public static JoobyRule bootstrap = new JoobyRule(app);

    @BeforeClass
    public static void setUp() {

        RestAssured.basePath = "/api/accounts";
        RestAssured.requestSpecification =
                new RequestSpecBuilder()
                        .setContentType(ContentType.JSON)
                        .setAccept(ContentType.JSON)
                        .build();
        RestAssured
                .config()
                .objectMapperConfig(new ObjectMapperConfig(ObjectMapperType.JACKSON_2));
    }

    @After
    public void cleanUp() {
        app.require(AccountRepository.class).clear();
    }

    @Test
    public void testGetAll() {

        List<AccountResponse> expected = Lists.newArrayList(createAccount(), createAccount());
        AccountResponse[] response = get()
                .then()
                    .statusCode(Status.OK.value())
                    .extract()
                    .body().as(AccountResponse[].class);

        Assertions.assertThat(response)
                .isNotNull()
                .containsExactlyInAnyOrderElementsOf(expected);
    }


    @Test
    public void testGetAccount() {

        AccountResponse expected = createAccount();
        AccountResponse response =
                when()
                  .get("/{id}",expected.getId())
                .then()
                    .statusCode(Status.OK.value())
                .extract()
                .body().as(AccountResponse.class);

        Assertions.assertThat(response)
                .isEqualTo(expected);
        //404 on non existent account
        when()
            .get("/{id}",666)
        .then()
            .statusCode(Status.NOT_FOUND.value());


    }


    @Test
    public void testCreateAccount() {

        //400 on absent name
        given()
           .body(new CreateAccountRequest(null,BigDecimal.TEN))
        .when()
            .post()
         .then()
            .statusCode(Status.BAD_REQUEST.value());

        // 400 on negative amount
        given()
            .body(new CreateAccountRequest("name",BigDecimal.TEN.negate()))
        .when()
            .post()
        .then()
            .statusCode(Status.BAD_REQUEST.value());

        // amount zero by default
        given()
            .body(new CreateAccountRequest("name",null))
        .when()
            .post()
        .then()
            .statusCode(Status.OK.value())
            .body("amount", equalTo(0))
            .body("name", equalTo("name"));
    }


    @Test
    public void testDeposit(){
       AccountResponse account = createAccount();

       //400 on wrong amount
       given()
         .body(new RefillRequest(null))
         .pathParam("id",account.getId())
       .when()
         .post(PATH_DEPOSIT)
       .then()
         .statusCode(Status.BAD_REQUEST.value());

       given()
            .body(new RefillRequest(BigDecimal.ZERO))
            .pathParam("id",account.getId())
       .when()
            .post(PATH_DEPOSIT)
       .then()
            .statusCode(Status.BAD_REQUEST.value());

       given()
            .body(new RefillRequest(BigDecimal.TEN.negate()))
            .pathParam("id",account.getId())
       .when()
            .post(PATH_DEPOSIT)
       .then()
            .statusCode(Status.BAD_REQUEST.value());

       //404 on missing account

       given()
            .body(new RefillRequest(BigDecimal.TEN))
            .pathParam("id",666)
       .when()
            .post(PATH_DEPOSIT)
       .then()
            .statusCode(Status.NOT_FOUND.value());

       //Successful transfer

        given()
            .body(new RefillRequest(BigDecimal.TEN))
            .pathParam("id",account.getId())
       .when()
            .post(PATH_DEPOSIT)
       .then()
            .statusCode(Status.OK.value())
            .body("amount",equalTo(account.getAmount().add(BigDecimal.TEN).intValueExact()));

    }


    @Test
    public void testWithdraw(){
       AccountResponse account = createAccount();

       //400 on wrong amount
       given()
         .body(new RefillRequest(null))
         .pathParam("id",account.getId())
       .when()
         .post(PATH_WITHDRAW)
       .then()
         .statusCode(Status.BAD_REQUEST.value());

       given()
            .body(new RefillRequest(BigDecimal.ZERO))
            .pathParam("id",account.getId())
       .when()
            .post(PATH_WITHDRAW)
       .then()
            .statusCode(Status.BAD_REQUEST.value());

       given()
            .body(new RefillRequest(BigDecimal.TEN.negate()))
            .pathParam("id",account.getId())
       .when()
            .post(PATH_WITHDRAW)
       .then()
            .statusCode(Status.BAD_REQUEST.value());

       //404 on missing account

       given()
            .body(new RefillRequest(BigDecimal.TEN))
            .pathParam("id",666)
       .when()
            .post(PATH_WITHDRAW)
       .then()
            .statusCode(Status.NOT_FOUND.value());

       //400 on insufficient funds
        given()
            .body(new RefillRequest(new BigDecimal(100)))
            .pathParam("id",account.getId())
       .when()
            .post(PATH_WITHDRAW)
       .then()
            .statusCode(Status.BAD_REQUEST.value());
       //Successful transfer

        given()
            .body(new RefillRequest(BigDecimal.TEN))
            .pathParam("id",account.getId())
       .when()
            .post(PATH_WITHDRAW)
       .then()
            .statusCode(Status.OK.value())
            .body("amount",equalTo(account.getAmount().subtract(BigDecimal.TEN).intValueExact()));

    }



    @Test
    public void testTransfer(){
       AccountResponse from = createAccount();
       AccountResponse to = createAccount();


       //400 on wrong amount
       given()
         .body(new TransferRequest(null,to.getId()))
         .pathParam("id",from.getId())
       .when()
         .post(PATH_TRANSFER)
       .then()
         .statusCode(Status.BAD_REQUEST.value());

       given()
            .body(new TransferRequest(BigDecimal.ZERO,to.getId()))
            .pathParam("id",from.getId())
       .when()
            .post(PATH_TRANSFER)
       .then()
            .statusCode(Status.BAD_REQUEST.value());

       given()
            .body(new TransferRequest(BigDecimal.ZERO,to.getId()))
            .pathParam("id",from.getId())
       .when()
            .post(PATH_TRANSFER)
       .then()
            .statusCode(Status.BAD_REQUEST.value());

       //404 on missing account

       given()
            .body(new TransferRequest(BigDecimal.ZERO,to.getId()))
            .pathParam("id",666)
       .when()
            .post(PATH_TRANSFER)
       .then()
            .statusCode(Status.NOT_FOUND.value());

        given()
            .body(new TransferRequest(BigDecimal.ZERO,666L))
            .pathParam("id",from.getId())
       .when()
            .post(PATH_TRANSFER)
       .then()
            .statusCode(Status.NOT_FOUND.value());

       //400 on insufficient funds
        given()
            .body(new TransferRequest(new BigDecimal(100),to.getId()))
            .pathParam("id",from.getId())
       .when()
            .post(PATH_TRANSFER)
       .then()
            .statusCode(Status.BAD_REQUEST.value());


         //400 on same account
        given()
            .body(new TransferRequest(BigDecimal.ONE,to.getId()))
            .pathParam("id",to.getId())
       .when()
            .post(PATH_TRANSFER)
       .then()
            .statusCode(Status.BAD_REQUEST.value());
       //Successful transfer

        given()
            .body(new TransferRequest(BigDecimal.TEN,to.getId()))
            .pathParam("id",from.getId())
       .when()
            .post(PATH_TRANSFER)
       .then()
            .statusCode(Status.OK.value())
            .body("amount",equalTo(from.getAmount().subtract(BigDecimal.TEN).intValueExact()));

        get("/{id}",to.getId())
        .then()
            .statusCode(Status.OK.value())
            .body("amount",equalTo(from.getAmount().add(BigDecimal.TEN).intValueExact()));

    }




    private AccountResponse createAccount() {
        return createAccount(BigDecimal.TEN);
    }

    private AccountResponse createAccount(BigDecimal amount) {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setName("name");
        request.setAmount(amount);

        return given()
                    .body(request)
                .when()
                    .post()
                .then()
                    .statusCode(Status.OK.value())
                    .extract().body().as(AccountResponse.class);


    }
}
