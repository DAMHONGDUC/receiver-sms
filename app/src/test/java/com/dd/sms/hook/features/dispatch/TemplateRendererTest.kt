package com.dd.sms.hook.features.dispatch

import com.dd.sms.hook.features.dispatch.domain.service.Escaping
import com.dd.sms.hook.features.dispatch.domain.service.TemplateRenderer
import org.junit.Assert.assertEquals
import org.junit.Test

class TemplateRendererTest {
    private val renderer = TemplateRenderer()

    @Test
    fun `replaces known placeholders and tolerates inner spaces`() {
        val result: String = renderer.render("{{sender}} said {{ body }}", mapOf("sender" to "A", "body" to "hi"), Escaping.NONE)

        assertEquals("A said hi", result)
    }

    @Test
    fun `leaves unknown placeholders untouched`() {
        assertEquals("{{nope}}", renderer.render("{{nope}}", mapOf("sender" to "A"), Escaping.NONE))
    }

    @Test
    fun `json escaping keeps the document valid for quotes, newlines and control chars`() {
        val body = "He said \"hi\"\nline2\\end\u0001"
        val result: String = renderer.render("{\"m\":\"{{body}}\"}", mapOf("body" to body), Escaping.JSON)

        assertEquals("{\"m\":\"He said \\\"hi\\\"\\nline2\\\\end\\u0001\"}", result)
    }

    @Test
    fun `url escaping encodes reserved characters`() {
        val result: String = renderer.render("https://x.io/?q={{body}}", mapOf("body" to "a b&c=d"), Escaping.URL)

        assertEquals("https://x.io/?q=a+b%26c%3Dd", result)
    }

    @Test
    fun `empty template renders empty`() {
        assertEquals("", renderer.render("", mapOf("body" to "x"), Escaping.JSON))
    }
}
