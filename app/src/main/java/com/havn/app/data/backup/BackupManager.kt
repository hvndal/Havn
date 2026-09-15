package com.havn.app.data.backup

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.havn.app.BuildConfig
import com.havn.app.data.db.DoseLogEntity
import com.havn.app.data.db.HavnDatabase
import com.havn.app.data.db.MedicationEntity
import com.havn.app.data.db.UserEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Export and restore the whole local database as a single JSON file.
 *
 * This is the app's answer to "is my data safe". Hävn is deliberately
 * local-only — no account, no sync, no network permission — which means the
 * medication history lives in exactly one place and would go with the phone.
 * An export the user owns is the honest way to solve that without walking back
 * the privacy promise: the file is handed to Android's share sheet, so the
 * user chooses Drive, Files, email or anything else, and Hävn itself never
 * touches a network.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: HavnDatabase,
) {

    private val json = Json {
        prettyPrint = true
        // A backup written by an older build must still load into a newer one,
        // so unknown fields are skipped rather than throwing.
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val listJson = Json { ignoreUnknownKeys = true }

    // ── Export ───────────────────────────────────────────────────────────────

    /** Builds the backup document from everything currently stored. */
    suspend fun buildBackup(): HavnBackup = withContext(Dispatchers.IO) {
        val users = database.userDao().getAllUsersSync()

        val profiles = users.map { user ->
            val meds = database.medicationDao().getAllForUserSync(user.id)
            val logs = database.doseLogDao().getAllForUserSync(user.id)

            BackupProfile(
                name = user.name,
                age = user.age,
                avatarColor = user.avatarColor,
                createdAt = user.createdAt,
                medications = meds.map { med ->
                    BackupMedication(
                        localId = med.id,
                        name = med.name,
                        dosage = med.dosage,
                        reminderTimes = runCatching {
                            listJson.decodeFromString<List<String>>(med.reminderTimesJson)
                        }.getOrDefault(emptyList()),
                        repeatType = med.repeatType,
                        colorTag = med.colorTag,
                        iconType = med.iconType,
                        isActive = med.isActive,
                        notes = med.notes,
                    )
                },
                doseLogs = logs.map { log ->
                    BackupDoseLog(
                        medicationLocalId = log.medicationId,
                        scheduledTime = log.scheduledTime,
                        takenAt = log.takenAt,
                        status = log.status,
                        scheduledSlot = log.scheduledSlot,
                    )
                },
            )
        }

        HavnBackup(
            appVersion = BuildConfig.VERSION_NAME,
            profiles = profiles,
        )
    }

    /**
     * Writes the backup to a shareable file and returns an intent for the
     * system share sheet.
     *
     * The file goes to `cacheDir/exports`, which is the correct home for
     * something handed to another app: it needs no storage permission, it is
     * reachable through FileProvider, and Android reclaims it if space runs
     * short so old exports cannot quietly accumulate.
     */
    suspend fun exportToShareIntent(): Intent = withContext(Dispatchers.IO) {
        val backup = buildBackup()
        val uri = writeToCache(backup)

        val send = Intent(Intent.ACTION_SEND).apply {
            type = MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, fileName())
            putExtra(
                Intent.EXTRA_TEXT,
                "Hävn backup · ${backup.profiles.size} profile(s), " +
                    "${backup.medicationCount} medications, ${backup.doseCount} recorded doses.",
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        Intent.createChooser(send, "Save your Hävn backup").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /** Writes the backup to a location the user picked with the system picker. */
    suspend fun writeTo(target: Uri): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val backup = buildBackup()
            context.contentResolver.openOutputStream(target)?.use { out ->
                out.write(json.encodeToString(HavnBackup.serializer(), backup).toByteArray())
            } ?: return@runCatching false
            true
        }.getOrDefault(false)
    }

    fun fileName(): String {
        val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        return "havn-backup-$stamp.json"
    }

    private fun writeToCache(backup: HavnBackup): Uri {
        val dir = File(context.cacheDir, "exports").apply {
            mkdirs()
            // Keep only the newest export around; this directory exists to
            // hand one file to another app, not to be an archive.
            listFiles()?.forEach { it.delete() }
        }
        val file = File(dir, fileName())
        file.writeText(json.encodeToString(HavnBackup.serializer(), backup))
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    // ── Restore ──────────────────────────────────────────────────────────────

    /**
     * Restores a backup **additively** — existing profiles are left alone and
     * the backup's profiles are added alongside them.
     *
     * Deliberately not a destructive "replace everything". A restore that
     * wiped current data would make an accidental tap on the wrong file
     * unrecoverable, and this is medication history. Duplicates are the
     * user's to clean up; lost history is not.
     */
    suspend fun restore(source: Uri): RestoreResult = withContext(Dispatchers.IO) {
        val text = runCatching {
            context.contentResolver.openInputStream(source)?.bufferedReader()?.use { it.readText() }
        }.getOrNull() ?: return@withContext RestoreResult.Failure("Couldn't read that file.")

        val backup = runCatching {
            json.decodeFromString(HavnBackup.serializer(), text)
        }.getOrElse {
            return@withContext RestoreResult.Failure("That doesn't look like a Hävn backup.")
        }

        if (backup.schemaVersion > HavnBackup.CURRENT_SCHEMA) {
            return@withContext RestoreResult.Failure(
                "That backup was made by a newer version of Hävn. Update the app and try again."
            )
        }
        if (backup.profiles.isEmpty()) {
            return@withContext RestoreResult.Failure("That backup is empty.")
        }

        var medCount = 0
        var doseCount = 0

        backup.profiles.forEach { profile ->
            val userId = database.userDao().insertUser(
                UserEntity(
                    name = profile.name,
                    age = profile.age,
                    avatarColor = profile.avatarColor,
                    createdAt = profile.createdAt.takeIf { it > 0 }
                        ?: System.currentTimeMillis(),
                )
            )

            // Map each backup-local medication id to the id it actually got,
            // so dose logs still point at the right medication afterwards.
            val idMap = mutableMapOf<Long, Long>()

            profile.medications.forEach { med ->
                val newId = database.medicationDao().insertMedication(
                    MedicationEntity(
                        userId = userId,
                        name = med.name,
                        dosage = med.dosage,
                        reminderTimesJson = listJson.encodeToString(med.reminderTimes),
                        repeatType = med.repeatType,
                        colorTag = med.colorTag,
                        iconType = med.iconType,
                        isActive = med.isActive,
                        notes = med.notes,
                    )
                )
                idMap[med.localId] = newId
                medCount++
            }

            val logs = profile.doseLogs.mapNotNull { log ->
                val medId = idMap[log.medicationLocalId] ?: return@mapNotNull null
                DoseLogEntity(
                    medicationId = medId,
                    userId = userId,
                    scheduledTime = log.scheduledTime,
                    takenAt = log.takenAt,
                    status = log.status,
                    scheduledSlot = log.scheduledSlot,
                )
            }
            if (logs.isNotEmpty()) {
                database.doseLogDao().insertDoseLogs(logs)
                doseCount += logs.size
            }
        }

        RestoreResult.Success(
            profiles = backup.profiles.size,
            medications = medCount,
            doses = doseCount,
        )
    }

    companion object {
        const val MIME_TYPE = "application/json"

        /**
         * What the system file picker will accept. Some providers report a
         * `.json` file as octet-stream, so both are allowed or the user's own
         * backup appears greyed out in the picker.
         */
        val IMPORT_MIME_TYPES = arrayOf("application/json", "text/plain", "application/octet-stream")
    }
}
