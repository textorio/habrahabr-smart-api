package com.textorio.habrahabr.smartapi.core;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.textorio.habrahabr.smartapi.core.api.UserResource;
import com.textorio.habrahabr.smartapi.core.api.UserSettings;
import com.textorio.habrahabr.smartapi.core.webdriver.WebSettings;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

public class UploadTest {

    @Test
    public void test () throws UnirestException {
        String endpoint = "https://api.imgur.com/3/image";
        String accessToken = "Bearer ";

        HttpResponse<JsonNode> uploadResponse = Unirest.post(endpoint)
                .header("Authorization", accessToken)
                .field("image", new File("/Users/olegchir/tmp/a.jpg"))
                .asJson();
        String link = uploadResponse.getBody().getObject().getJSONObject("data").getString("link");

        System.out.println(uploadResponse);
    }
}
