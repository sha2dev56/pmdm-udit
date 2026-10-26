package com.example.callfilter

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FilterDbHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, "filters.db", null, 3) {

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
        db.execSQL("INSERT INTO rules (type, value, enabled, priority) VALUES ('PREFIX', '400', 1, 10)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            createTables(db) // ya incluye las columnas de la v3
            db.execSQL(
                "INSERT INTO rules (type, value, enabled, priority) " +
                        "SELECT 'PREFIX', prefix, enabled, 10 FROM prefixes"
            )
            db.execSQL("DROP TABLE prefixes")
        } else if (oldVersion < 3) {
            db.execSQL("ALTER TABLE rules ADD COLUMN start_min INTEGER NOT NULL DEFAULT -1")
            db.execSQL("ALTER TABLE rules ADD COLUMN end_min INTEGER NOT NULL DEFAULT -1")
        }
    }

    private fun createTables(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE rules (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "type TEXT NOT NULL, " +
                    "value TEXT NOT NULL, " +
                    "enabled INTEGER NOT NULL DEFAULT 1, " +
                    "priority INTEGER NOT NULL DEFAULT 0, " +
                    "start_min INTEGER NOT NULL DEFAULT -1, " +
                    "end_min INTEGER NOT NULL DEFAULT -1, " +
                    "UNIQUE(type, value))"
        )
        db.execSQL(
            "CREATE TABLE call_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "ts INTEGER NOT NULL, " +
                    "number_masked TEXT NOT NULL, " +
                    "blocked INTEGER NOT NULL, " +
                    "rule_desc TEXT, " +
                    "source TEXT NOT NULL)"
        )
        db.execSQL("CREATE INDEX idx_log_ts ON call_log(ts)")
    }

    // ---------- Reglas ----------

    private fun Cursor.toRule() = Rule(
        id = getLong(getColumnIndexOrThrow("id")),
        type = RuleType.valueOf(getString(getColumnIndexOrThrow("type"))),
        value = getString(getColumnIndexOrThrow("value")),
        enabled = getInt(getColumnIndexOrThrow("enabled")) == 1,
        priority = getInt(getColumnIndexOrThrow("priority")),
        startMin = getInt(getColumnIndexOrThrow("start_min")),
        endMin = getInt(getColumnIndexOrThrow("end_min"))
    )

    fun getRules(type: RuleType? = null): List<Rule> {
        val list = mutableListOf<Rule>()
        val sel = type?.let { "type = ?" }
        val args = type?.let { arrayOf(it.name) }
        readableDatabase.query("rules", null, sel, args, null, null, "priority DESC, id ASC")
            .use { c -> while (c.moveToNext()) list.add(c.toRule()) }
        return list
    }

    fun getActiveRules(): List<Rule> {
        val list = mutableListOf<Rule>()
        readableDatabase.query("rules", null, "enabled = 1", null, null, null, null)
            .use { c -> while (c.moveToNext()) list.add(c.toRule()) }
        return list
    }

    /** Devuelve -1 si ya existe esa regla. */
    fun insertRule(
        type: RuleType,
        value: String,
        priority: Int = type.defaultPriority,
        startMin: Int = -1,
        endMin: Int = -1
    ): Long {
        val v = ContentValues().apply {
            put("type", type.name)
            put("value", value)
            put("enabled", 1)
            put("priority", priority)
            put("start_min", startMin)
            put("end_min", endMin)
        }
        return writableDatabase.insertWithOnConflict("rules", null, v, SQLiteDatabase.CONFLICT_IGNORE)
    }

    /** Devuelve 0 si no se pudo (por ejemplo, duplicado). */
    fun updateRule(
        id: Long,
        value: String,
        priority: Int? = null,
        startMin: Int? = null,
        endMin: Int? = null
    ): Int {
        val v = ContentValues().apply {
            put("value", value)
            priority?.let { put("priority", it) }
            startMin?.let { put("start_min", it) }
            endMin?.let { put("end_min", it) }
        }
        return writableDatabase.updateWithOnConflict(
            "rules", v, "id = ?", arrayOf(id.toString()), SQLiteDatabase.CONFLICT_IGNORE
        )
    }

    fun setRuleEnabled(id: Long, enabled: Boolean) {
        val v = ContentValues().apply { put("enabled", if (enabled) 1 else 0) }
        writableDatabase.update("rules", v, "id = ?", arrayOf(id.toString()))
    }

    fun deleteRule(id: Long) {
        writableDatabase.delete("rules", "id = ?", arrayOf(id.toString()))
    }

    // ---------- Compatibilidad con la UI de la Fase 2 (solo prefijos) ----------

    fun getAll(): List<PrefixFilter> =
        getRules(RuleType.PREFIX).sortedBy { it.id }.map { PrefixFilter(it.id, it.value, it.enabled) }

    fun insert(prefix: String): Long = insertRule(RuleType.PREFIX, prefix)
    fun update(id: Long, prefix: String): Int = updateRule(id, prefix)
    fun setEnabled(id: Long, enabled: Boolean) = setRuleEnabled(id, enabled)
    fun delete(id: Long) = deleteRule(id)

    // ---------- Registro ----------

    fun logCall(masked: String, blocked: Boolean, ruleDesc: String?, source: String) {
        val v = ContentValues().apply {
            put("ts", System.currentTimeMillis())
            put("number_masked", masked)
            put("blocked", if (blocked) 1 else 0)
            put("rule_desc", ruleDesc)
            put("source", source)
        }
        writableDatabase.insert("call_log", null, v)
    }

    fun getLog(limit: Int = 200): List<LogEntry> {
        val list = mutableListOf<LogEntry>()
        readableDatabase.query(
            "call_log", null, null, null, null, null, "ts DESC", limit.toString()
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    LogEntry(
                        id = c.getLong(c.getColumnIndexOrThrow("id")),
                        ts = c.getLong(c.getColumnIndexOrThrow("ts")),
                        number = c.getString(c.getColumnIndexOrThrow("number_masked")),
                        blocked = c.getInt(c.getColumnIndexOrThrow("blocked")) == 1,
                        rule = c.getString(c.getColumnIndexOrThrow("rule_desc")),
                        source = c.getString(c.getColumnIndexOrThrow("source"))
                    )
                )
            }
        }
        return list
    }

    /** Solo cuenta llamadas reales; las simuladas no ensucian las estadísticas. */
    fun getStats(): Stats {
        var total = 0
        var blocked = 0
        readableDatabase.rawQuery(
            "SELECT COUNT(*), COALESCE(SUM(blocked), 0) FROM call_log WHERE source = 'REAL'", null
        ).use { c ->
            if (c.moveToFirst()) { total = c.getInt(0); blocked = c.getInt(1) }
        }
        val byRule = mutableListOf<Pair<String, Int>>()
        readableDatabase.rawQuery(
            "SELECT rule_desc, COUNT(*) AS n FROM call_log " +
                    "WHERE source = 'REAL' AND rule_desc IS NOT NULL " +
                    "GROUP BY rule_desc ORDER BY n DESC", null
        ).use { c ->
            while (c.moveToNext()) byRule.add(c.getString(0) to c.getInt(1))
        }
        return Stats(total, blocked, byRule)
    }

    fun clearLog() {
        writableDatabase.delete("call_log", null, null)
    }

    companion object {
        @Volatile private var instance: FilterDbHelper? = null

        fun get(context: Context): FilterDbHelper =
            instance ?: synchronized(this) {
                instance ?: FilterDbHelper(context.applicationContext).also { instance = it }
            }
    }
}