package com.callshield

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService

/**
 * Android llama a onScreenCall() por cada llamada, antes de que suene.
 * Si el filtro está activo y el número no está en tus contactos (o viene oculto),
 * la llamada se rechaza sin sonar y sin notificación.
 */
class CallFilterService : CallScreeningService() {

    override fun onScreenCall(details: Call.Details) {
        val response = CallResponse.Builder()

        val isIncoming = details.callDirection == Call.Details.DIRECTION_INCOMING
        if (isIncoming && Prefs.isActive(this) && shouldBlock(details)) {
            response
                .setDisallowCall(true)
                .setRejectCall(true)
                .setSkipNotification(true)   // sin notificación de llamada perdida
                .setSkipCallLog(false)       // queda en el historial para que puedas revisarla
            Prefs.incrementBlocked(this)
        }
        respondToCall(details, response.build())
    }

    private fun shouldBlock(details: Call.Details): Boolean {
        // Si no tenemos permiso de contactos NO bloqueamos nada (mejor dejar pasar que perder llamadas).
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        val number = details.handle?.schemeSpecificPart
        if (number.isNullOrBlank()) return true       // número oculto
        return !isInContacts(number)
    }

    private fun isInContacts(number: String): Boolean {
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )
            contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)
                ?.use { it.count > 0 } ?: true
        } catch (e: Exception) {
            true // ante cualquier error, deja pasar la llamada
        }
    }
}
