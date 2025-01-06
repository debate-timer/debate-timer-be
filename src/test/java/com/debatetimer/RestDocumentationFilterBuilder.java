package com.debatetimer;

import static com.epages.restdocs.apispec.RestAssuredRestDocumentationWrapper.document;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;

import com.epages.restdocs.apispec.ResourceSnippetParametersBuilder;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.restassured.RestDocumentationFilter;
import org.springframework.restdocs.snippet.Snippet;

public class RestDocumentationFilterBuilder {

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
    private final ResourceSnippetParametersBuilder resourceBuilder;
    private final List<Snippet> snippets;

    public RestDocumentationFilterBuilder(String identifier) {
        this.identifier = identifier;
        this.resourceBuilder = new ResourceSnippetParametersBuilder();
        this.snippets = new ArrayList<>();
    }

    public RestDocumentationFilterBuilder tag(String tag) {
        resourceBuilder.tag(tag);
        return this;
    }

    public RestDocumentationFilterBuilder description(String description) {
        resourceBuilder.description(description)
                .summary(description);
        return this;
    }

    public RestDocumentationFilterBuilder pathParameter(ParameterDescriptor... descriptors) {
        snippets.add(pathParameters(descriptors));
        return this;
    }

    public RestDocumentationFilterBuilder queryParameter(ParameterDescriptor... descriptors) {
        snippets.add(queryParameters(descriptors));
        return this;
    }

    public RestDocumentationFilterBuilder requestHeader(HeaderDescriptor... descriptors) {
        snippets.add(requestHeaders(descriptors));
        return this;
    }

    public RestDocumentationFilterBuilder requestBodyField(FieldDescriptor... descriptors) {
        snippets.add(requestFields(descriptors));
        return this;
    }

    public RestDocumentationFilterBuilder responseHeader(HeaderDescriptor... descriptors) {
        snippets.add(responseHeaders(descriptors));
        return this;
    }

    public RestDocumentationFilterBuilder responseBodyField(FieldDescriptor... descriptors) {
        snippets.add(responseFields(descriptors));
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
