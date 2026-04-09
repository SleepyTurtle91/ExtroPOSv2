package com.extrotarget.extroposv2.core.network.api.lhdn

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface MyInvoisApi {

    @POST("connect/token")
    suspend fun login(
        @Header("Content-Type") contentType: String = "application/x-www-form-urlencoded",
        @Body body: String
    ): Response<TokenResponse>

    @POST("api/v1.0/documentsubmissions")
    suspend fun submitDocuments(
        @Header("Authorization") token: String,
        @Body body: DocumentSubmissionRequest
    ): Response<DocumentSubmissionResponse>
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
