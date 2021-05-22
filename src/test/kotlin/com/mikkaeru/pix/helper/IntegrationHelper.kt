package com.mikkaeru.pix.helper

import com.mikkaeru.KeymanagerServiceGrpc
import com.mikkaeru.SearchManagerServiceGrpc
import com.mikkaeru.pix.shared.GrpcClientFactory
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.mockito.Mockito.mock
import javax.inject.Singleton

@MicronautTest
internal abstract class IntegrationHelper {

    @Factory
    @Replaces(factory = GrpcClientFactory::class)
    internal class StubFactory {

        @Singleton
        internal fun keymanagerStubMock(): KeymanagerServiceGrpc.KeymanagerServiceBlockingStub {
            return mock(KeymanagerServiceGrpc.KeymanagerServiceBlockingStub::class.java)
        }

        @Singleton
        internal fun searchmanagerStubMock(): SearchManagerServiceGrpc.SearchManagerServiceBlockingStub {
            return mock(SearchManagerServiceGrpc.SearchManagerServiceBlockingStub::class.java)
        }
    }
}