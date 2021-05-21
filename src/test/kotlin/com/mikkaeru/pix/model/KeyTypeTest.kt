package com.mikkaeru.pix.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class KeyTypeTest {

    @Nested
    inner class CPF {

        @Test
        fun `deve retornar verdadeiro ao fornecer um cpf valido`() {
            val isValid = KeyType.CPF.validate("14931937675")
            assertTrue(isValid)
        }

        @Test
        fun `deve retornar falso ao fornecer um cpf invalido`() {
            val isValid = KeyType.CPF.validate("32112332134")
            assertFalse(isValid)
        }

        @Test
        fun `deve retornar falso ao fornecer um cpf em branco`() {
            val isValid = KeyType.CPF.validate("")
            assertFalse(isValid)
        }

        @Test
        fun `deve retornar falso ao fornecer um cpf nulo`() {
            val isValid = KeyType.CPF.validate(null)
            assertFalse(isValid)
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve retornar verdadeiro ao fornecer um email valido`() {
            val isValid = KeyType.EMAIL.validate("teste@gmail.com")
            assertTrue(isValid)
        }

        @Test
        fun `deve retornar falso ao fornecer um email invalido`() {
            val isValid = KeyType.EMAIL.validate("testegmailcom")
            assertFalse(isValid)
        }

        @Test
        fun `deve retornar falso ao fornecer um email em branco`() {
            val isValid = KeyType.EMAIL.validate("")
            assertFalse(isValid)
        }

        @Test
        fun `deve retornar falso ao fornecer um email nulo`() {
            val isValid = KeyType.EMAIL.validate(null)
            assertFalse(isValid)
        }
    }

    @Nested
    inner class PHONE {

        @Test
        fun `deve retornar verdadeiro ao fornecer um telefone valido`() {
            val isValid = KeyType.PHONE.validate("+5538999309941")
            assertTrue(isValid)
        }

        @Test
        fun `deve retornar falso ao fornecer um telefone invalido`() {
            val isValid = KeyType.PHONE.validate("99409932")
            assertFalse(isValid)
        }

        @Test
        fun `deve retornar falso ao fornecer um telefone em branco`() {
            val isValid = KeyType.PHONE.validate("")
            assertFalse(isValid)
        }

        @Test
        fun `deve retornar falso ao fornecer um telefone nulo`() {
            val isValid = KeyType.PHONE.validate(null)
            assertFalse(isValid)
        }
    }

    @Nested
    inner class RANDOM {

        @Test
        fun `deve retornar verdadeiro se a chave for nula`() {
            val isValid = KeyType.RANDOM.validate(null)
            assertTrue(isValid)
        }

        @Test
        fun `deve retornar verdadeiro se a chave estiver em branco`() {
            val isValid = KeyType.RANDOM.validate("")
            assertTrue(isValid)
        }

        @Test
        fun `deve retornar falso se a chave estiver algum valor`() {
            val isValid = KeyType.RANDOM.validate("souUmaChaveOK")
            assertFalse(isValid)
        }
    }
}