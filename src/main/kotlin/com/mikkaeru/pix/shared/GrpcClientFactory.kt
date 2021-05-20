package com.mikkaeru.pix.shared

import com.mikkaeru.KeymanagerServiceGrpc
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
}