package com.mikkaeru.pix.shared

import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.SearchManagerServiceGrpc
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import javax.inject.Singleton

@Factory
class GrpcClientFactory(@GrpcChannel("keymanager") val channel: ManagedChannel) {

    @Singleton
    fun keymanagerGrpcBlockingStub(): KeymanagerServiceGrpc.KeymanagerServiceBlockingStub? {
        return KeymanagerServiceGrpc.newBlockingStub(channel)
    }

    @Singleton
    fun searchManagerGrpcBlockingStub(): SearchManagerServiceGrpc.SearchManagerServiceBlockingStub? {
        return SearchManagerServiceGrpc.newBlockingStub(channel)
    }
}