package com.debatetimer.controller;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;

import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.restdocs.snippet.Snippet;

public class RestDocumentationFilterBuilder {

    private static final String IDENTIFIER_DELIMITER = "/";
    private static final OperationRequestPreprocessor REQUEST_PREPROCESSOR = Preprocessors.preprocessRequest(
            Preprocessors.prettyPrint(),
            Preprocessors.modifyHeaders()
                    .remove(HttpHeaders.HOST)
                    .remove(HttpHeaders.CONTENT_LENGTH)
    );
    private static final OperationResponsePreprocessor RESPONSE_PREPROCESSOR = Preprocessors.preprocessResponse(
            Preprocessors.prettyPrint(),
            Preprocessors.modifyHeaders()
                    .remove(HttpHeaders.TRANSFER_ENCODING)
                    .remove(HttpHeaders.DATE)
                    .remove(HttpHeaders.CONNECTION)
                    .remove(HttpHeaders.CONTENT_LENGTH)
    );

    private final String identifier;
    private final List<Snippet> snippets;
    private ResourceSnippetParametersBuilder resourceBuilder;

    private RestDocumentationFilterBuilder(String identifier) {
        this.identifier = identifier;
        this.resourceBuilder = new ResourceSnippetParametersBuilder();
        this.snippets = new ArrayList<>();
    }

    public RestDocumentationFilterBuilder(String identifierPrefix, String identifier) {
        this(identifierPrefix + IDENTIFIER_DELIMITER + identifier);
    }

    public RestDocumentationFilterBuilder request(RestDocumentationRequest request) {
        resourceBuilder = request.getResourceBuilder();
        snippets.addAll(request.getSnippets());
        return this;
    }

    public RestDocumentationFilterBuilder response(RestDocumentationResponse response) {
        snippets.addAll(response.getSnippets());
        return this;
    }

    public RestDocumentationFilter build() {
        return document(
                identifier,
                resourceBuilder,
                REQUEST_PREPROCESSOR,
                RESPONSE_PREPROCESSOR,
                snippets.toArray(Snippet[]::new)
        );
    }
}
