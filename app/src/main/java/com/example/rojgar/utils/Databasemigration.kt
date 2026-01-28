package com.example.rojgar.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log

/**
 * Database Migration Utility
 *
 * This utility helps migrate existing JobSeeker records to include the new profileViews field.
 *
 * IMPORTANT: This should be run ONCE after deploying the new code.
 *
 * Usage:
 * 1. Call DatabaseMigration.addProfileViewsToExistingRecords() from your app initialization
 * 2. Or create a temporary admin screen to trigger the migration
 * 3. Monitor logs to track migration progress
 */
object DatabaseMigration {

    private const val TAG = "DatabaseMigration"

    /**
     * Adds profileViews field to all existing JobSeeker records that don't have it
     * This is safe to run multiple times - it only updates records missing the field
     */
    fun addProfileViewsToExistingRecords(
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val jobSeekersRef = database.getReference("JobSeekers")

        Log.d(TAG, "Starting migration: Adding profileViews to existing JobSeeker records")

        jobSeekersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.d(TAG, "No JobSeeker records found")
                    onComplete(true, "No records to migrate")
                    return
                }

                var totalRecords = 0
                var recordsUpdated = 0
                var recordsSkipped = 0
                var errors = 0

                val updates = mutableMapOf<String, Any>()

                for (childSnapshot in snapshot.children) {
                    totalRecords++
                    val jobSeekerId = childSnapshot.key ?: continue

                    // Check if profileViews field already exists
                    if (childSnapshot.hasChild("profileViews")) {
                        recordsSkipped++
                        Log.d(TAG, "Skipping $jobSeekerId - already has profileViews")
                        continue
                    }

                    // Add profileViews field with initial value of 0
                    updates["$jobSeekerId/profileViews"] = 0L
                    recordsUpdated++
                    Log.d(TAG, "Queuing $jobSeekerId for update")
                }

                if (updates.isEmpty()) {
                    val message = "Migration complete: All $totalRecords records already have profileViews field"
                    Log.d(TAG, message)
                    onComplete(true, message)
                    return
                }

                // Perform batch update
                Log.d(TAG, "Updating ${updates.size} records...")
                jobSeekersRef.updateChildren(updates)
                    .addOnSuccessListener {
                        val message = """
                            Migration completed successfully:
                            - Total records: $totalRecords
                            - Records updated: $recordsUpdated
                            - Records skipped: $recordsSkipped
                        """.trimIndent()
                        Log.d(TAG, message)
                        onComplete(true, message)
                    }
                    .addOnFailureListener { exception ->
                        val message = "Migration failed: ${exception.message}"
                        Log.e(TAG, message, exception)
                        onComplete(false, message)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                val message = "Migration cancelled: ${error.message}"
                Log.e(TAG, message)
                onComplete(false, message)
            }
        })
    }

    /**
     * Verifies that all JobSeeker records have the profileViews field
     * Use this to check migration status
     */
    fun verifyMigration(
        onResult: (allRecordsHaveField: Boolean, stats: MigrationStats) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val jobSeekersRef = database.getReference("JobSeekers")

        Log.d(TAG, "Verifying migration status...")

        jobSeekersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stats = MigrationStats()

                if (!snapshot.exists()) {
                    Log.d(TAG, "No JobSeeker records found")
                    onResult(true, stats)
                    return
                }

                for (childSnapshot in snapshot.children) {
                    stats.totalRecords++

                    if (childSnapshot.hasChild("profileViews")) {
                        stats.recordsWithField++

                        // Verify the value is a valid Long
                        val value = childSnapshot.child("profileViews").getValue(Long::class.java)
                        if (value != null) {
                            stats.recordsWithValidValue++
                        }
                    } else {
                        stats.recordsMissingField++
                        stats.missingFieldIds.add(childSnapshot.key ?: "unknown")
                    }
                }

                val allHaveField = stats.recordsMissingField == 0

                val message = """
                    Migration Verification:
                    - Total records: ${stats.totalRecords}
                    - Records with profileViews: ${stats.recordsWithField}
                    - Records with valid values: ${stats.recordsWithValidValue}
                    - Records missing field: ${stats.recordsMissingField}
                    - Status: ${if (allHaveField) "✅ Complete" else "⚠️ Incomplete"}
                """.trimIndent()

                Log.d(TAG, message)

                if (!allHaveField) {
                    Log.w(TAG, "Missing field in records: ${stats.missingFieldIds}")
                }

                onResult(allHaveField, stats)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Verification failed: ${error.message}")
                onResult(false, MigrationStats())
            }
        })
    }

    /**
     * Resets all profileViews to 0 (use with caution!)
     * This is useful for testing or if you need to reset analytics
     */
    fun resetAllProfileViews(
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()
        val jobSeekersRef = database.getReference("JobSeekers")

        Log.w(TAG, "⚠️ Resetting all profileViews to 0...")

        jobSeekersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onComplete(true, "No records to reset")
                    return
                }

                val updates = mutableMapOf<String, Any>()
                var count = 0

                for (childSnapshot in snapshot.children) {
                    val jobSeekerId = childSnapshot.key ?: continue
                    updates["$jobSeekerId/profileViews"] = 0L
                    count++
                }

                jobSeekersRef.updateChildren(updates)
                    .addOnSuccessListener {
                        val message = "Reset $count profileViews to 0"
                        Log.d(TAG, message)
                        onComplete(true, message)
                    }
                    .addOnFailureListener { exception ->
                        val message = "Reset failed: ${exception.message}"
                        Log.e(TAG, message, exception)
                        onComplete(false, message)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                val message = "Reset cancelled: ${error.message}"
                Log.e(TAG, message)
                onComplete(false, message)
            }
        })
    }
}

/**
 * Data class to hold migration verification statistics
 */
data class MigrationStats(
    var totalRecords: Int = 0,
    var recordsWithField: Int = 0,
    var recordsWithValidValue: Int = 0,
    var recordsMissingField: Int = 0,
    val missingFieldIds: MutableList<String> = mutableListOf()
)

/**
 * Example usage in your Application class or MainActivity:
 *
 * class RojgarApplication : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *
 *         // Run migration once on app start (only affects records missing the field)
 *         DatabaseMigration.addProfileViewsToExistingRecords { success, message ->
 *             if (success) {
 *                 Log.i("Migration", message)
 *             } else {
 *                 Log.e("Migration", message)
 *             }
 *         }
 *     }
 * }
 *
 * Or create an admin screen:
 *
 * @Composable
 * fun AdminMigrationScreen() {
 *     var migrationStatus by remember { mutableStateOf("") }
 *
 *     Column(modifier = Modifier.padding(16.dp)) {
 *         Button(onClick = {
 *             DatabaseMigration.addProfileViewsToExistingRecords { success, message ->
 *                 migrationStatus = message
 *             }
 *         }) {
 *             Text("Run Migration")
 *         }
 *
 *         Button(onClick = {
 *             DatabaseMigration.verifyMigration { allHaveField, stats ->
 *                 migrationStatus = "Total: ${stats.totalRecords}, Missing: ${stats.recordsMissingField}"
 *             }
 *         }) {
 *             Text("Verify Migration")
 *         }
 *
 *         Text(migrationStatus)
 *     }
 * }
 */