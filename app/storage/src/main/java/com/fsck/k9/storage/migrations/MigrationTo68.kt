package com.fsck.k9.storage.migrations

import android.database.sqlite.SQLiteDatabase


internal object MigrationTo68 {
    @JvmStatic
    fun addOutboxStateTable(db: SQLiteDatabase) {
        createOutboxStateTable(db)
        createOutboxStateEntries(db)
    }

    private fun createOutboxStateTable(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE outbox_state (" +
                "id INTEGER PRIMARY KEY, " +
                "message_id INTEGER, " +
                "send_state TEXT, " +
                "number_of_send_attempts INTEGER DEFAULT 0, " +
                "error_timestamp INTEGER DEFAULT 0, " +
                "error TEXT" +
                ")")

        db.execSQL("CREATE INDEX outbox_state_message_id ON outbox_state (message_id)")

        db.execSQL("DROP TRIGGER IF EXISTS delete_message")
        db.execSQL("CREATE TRIGGER delete_message " +
                "BEFORE DELETE ON messages " +
                "BEGIN " +
                "DELETE FROM message_parts WHERE root = OLD.message_part_id; " +
                "DELETE FROM messages_fulltext WHERE docid = OLD.id; " +
                "DELETE FROM outbox_state WHERE message_id = OLD.id; " +
                "END")
    }

    private fun createOutboxStateEntries(db: SQLiteDatabase) {
        db.execSQL("INSERT INTO outbox_state (message_id, send_state) " +
                "SELECT messages.id, 'ready' FROM folders " +
                "JOIN messages ON (folders.id = messages.folder_id) " +
                "WHERE folders.server_id = 'K9MAIL_INTERNAL_OUTBOX'")
    }
}
