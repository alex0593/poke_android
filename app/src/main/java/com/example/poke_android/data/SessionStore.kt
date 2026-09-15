package com.example.poke_android.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.flow.first

private val Context.sessionData by preferencesDataStore("session")

interface Session {
    val token: String?
    val username: String?

    suspend fun restore()

    suspend fun save(user: String, accessToken: String)

    suspend fun clear()
}

class SessionStore(private val context: Context) : Session {
    @Volatile
    override var token: String? = null
        private set

    @Volatile
    override var username: String? = null
        private set

    private val tokenKey = stringPreferencesKey("encrypted_token")
    private val userKey = stringPreferencesKey("username")

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (store.getKey("poke_session", null) as? SecretKey)
            ?: KeyGenerator.getInstance("AES", "AndroidKeyStore")
                .apply {
                    init(
                        KeyGenParameterSpec.Builder(
                                "poke_session",
                                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                            )
                            .setBlockModes("GCM")
                            .setEncryptionPaddings("NoPadding")
                            .build()
                    )
                }
                .generateKey()
    }

    override suspend fun restore() {
        val prefs = context.sessionData.data.first()
        val encrypted = prefs[tokenKey] ?: return
        try {
            val bytes = Base64.decode(encrypted, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
            token = String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), Charsets.UTF_8)
            username = prefs[userKey]
        } catch (_: java.security.GeneralSecurityException) {
            clear()
        } catch (_: IllegalArgumentException) {
            clear()
        }
    }

    override suspend fun save(user: String, accessToken: String) {
        val cipher =
            Cipher.getInstance("AES/GCM/NoPadding").apply { init(Cipher.ENCRYPT_MODE, key()) }
        val encrypted =
            Base64.encodeToString(
                cipher.iv + cipher.doFinal(accessToken.toByteArray()),
                Base64.NO_WRAP,
            )
        context.sessionData.edit {
            it[tokenKey] = encrypted
            it[userKey] = user
        }
        username = user
        token = accessToken
    }

    override suspend fun clear() {
        token = null
        username = null
        context.sessionData.edit { it.clear() }
    }
}
