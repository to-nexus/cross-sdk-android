package io.crosstoken.foundation.adapters

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.crosstoken.foundation.common.adapters.TtlAdapter
import io.crosstoken.foundation.common.model.Ttl
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TtlAdapterTest {
    private val moshi = Moshi.Builder()
        .add { _, _, _ ->
            TtlAdapter
        }
        .add(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun toJson() {
        val ttl = Ttl(100L)
        val expected = """"${ttl.seconds}""""

        val ttlJson = moshi.adapter(Ttl::class.java).toJson(ttl)

        assertEquals(expected, """"$ttlJson"""")
    }
}