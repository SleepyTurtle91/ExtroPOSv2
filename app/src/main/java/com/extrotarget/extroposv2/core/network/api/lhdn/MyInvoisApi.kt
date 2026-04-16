package com.extrotarget.extroposv2.core.network.api.lhdn

import retrofit2.Response
import retrofit2.http.*

interface MyInvoisApi {

    @FormUrlEncoded
    @POST("connect/token")
    suspend fun login(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("scope") scope: String = "InvoicingAPI"
    ): Response<TokenResponse>

    @POST("api/v1.0/documentsubmissions")
    suspend fun submitDocuments(
        @Header("Authorization") token: String,
        @Body body: DocumentSubmissionRequest
    ): Response<DocumentSubmissionResponse>

    @GET("api/v1.0/documentsubmissions/{submissionId}")
    suspend fun getSubmissionStatus(
        @Header("Authorization") token: String,
        @Path("submissionId") submissionId: String
    ): Response<SubmissionStatusResponse>

    @GET("api/v1.0/documents/{uuid}/details")
    suspend fun getDocumentDetails(
        @Header("Authorization") token: String,
        @Path("uuid") uuid: String
    ): Response<DocumentDetailsResponse>
}

data class TokenResponse(
    val access_token: String,
    val expires_in: Int,
    val token_type: String,
    val scope: String? = null
)

data class DocumentSubmissionRequest(
    val documents: List<DocumentItem>
)

data class DocumentItem(
    val format: String = "JSON",
    val document: String, // Base64 encoded JSON document
    val documentHash: String,
    val codeNumber: String
)

data class DocumentSubmissionResponse(
    val submissionId: String,
    val acceptedDocuments: List<AcceptedDocument>,
    val rejectedDocuments: List<RejectedDocument>
)

data class AcceptedDocument(
    val uuid: String,
    val invoiceIdentifier: String
)

data class RejectedDocument(
    val invoiceIdentifier: String,
    val error: MyInvoisError
)

data class MyInvoisError(
    val code: String,
    val message: String,
    val target: String? = null,
    val details: List<MyInvoisError>? = null
)

data class SubmissionStatusResponse(
    val submissionId: String,
    val status: String, // "InProgress", "Completed", "Failed"
    val documentCount: Int,
    val dateTimeReceived: String,
    val overallStatus: String? = null
)

data class DocumentDetailsResponse(
    val uuid: String,
    val submissionId: String,
    val status: String, // "Valid", "Invalid", "Cancelled", "Submitted"
    val validationResults: ValidationResults? = null
)

data class ValidationResults(
    val status: String,
    val validationSteps: List<ValidationStep>? = null
)

data class ValidationStep(
    val status: String,
    val error: MyInvoisError? = null
)
