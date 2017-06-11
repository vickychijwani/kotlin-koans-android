package me.vickychijwani.kotlinkoans

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.google.gson.annotations.SerializedName
import me.vickychijwani.kotlinkoans.features.common.textToHtml

// single koan
data class Koan(
        val id: String,
        val name: String,

        // code blocks in this are marked <code data-lang="text/x-kotlin"> OR <code data-lang="text/x-java">
        @SerializedName("help")
        val descriptionHtml: String,

        val files: List<KoanFile>,

        // custom fields added by us
        val lastRunStatus: RunStatus? = null
) {
    companion object {
        val EMPTY = Koan("", "", "", listOf())
    }

    fun getModifiableFile(): KoanFile {
        return files.first { it.modifiable }
    }

    fun getReadOnlyFiles(): List<KoanFile> {
        return files.filter { !it.modifiable }
    }
}


data class KoanFile(
        @SerializedName("publicId")
        val id: String,

        val name: String,

        @SerializedName("text")
        val contents: String,

        val modifiable: Boolean,

        // possible (expected) solutions; only one file in every Koan contains this
        val solutions: List<String>?
)

// top-level list of koans
typealias KoanFolders = List<KoanFolder>
data class KoanFolder(
        val id: String,

        val name: String,

        // TODO commented because we're currently filtering some koans, so `levels` will need to be filtered too
        // null if `projects` is non-empty
        //val levels: List<KoanLevel>?,

        // e.g., Kotlin Koans/Introduction => [Hello World, Java to Kotlin conversion, ...]
        // empty if `subfolders` is non-empty
        @SerializedName("projects")
        val koans: List<KoanMetadata>,

        // e.g., Kotlin Koans => [Introduction, Conventions, ...]
        // empty if `projects` is non-empty
        @SerializedName("childFolders")
        val subfolders: List<KoanFolder>
)

data class KoanMetadata(
        @SerializedName("publicId")
        val id: String,
        val name: String,
        val completed: Boolean?,    // if absent, it means the koan is not completed

        // custom fields added by us
        val lastRunStatus: RunStatus? = null
)

//data class KoanLevel(
//        val projectsNeeded: Int,                // total # of koans needed to finish this level
//        val color: String                       // color of tick on progress bar
//)

// run a koan
data class KoanRunInfo(
        val id: String,
        val name: String,

        // list of *modifiable* files with code to run
        val files: List<KoanFile>,

        // list of names of non-modifiable files
        val readOnlyFileNames: List<String>,

        // don't need to worry about these, defaults are fine
        val args: String = "",
        val compilerVersion: Any? = null,
        val confType: String = "junit",
        val originUrl: String = id
)

enum class RunStatus(val id: Int, val apiStatus: String, val uiLabel: String, val severity: Int) {
    OK            (0, "OK",    "Passed",            0),
    WRONG_ANSWER  (1, "FAIL",  "Wrong answer",      1),
    RUNTIME_ERROR (2, "ERROR", "Runtime error",     2),
    COMPILE_ERROR (3, "",      "Compilation error", 3);

    companion object {
        fun fromId(id: Int): RunStatus = values().first { it.id == id }
        fun fromApiStatus(s: String): RunStatus? = values().firstOrNull { it.apiStatus == s }
    }

    fun toColor(ctx: Context): Int {
        return when (this) {
            OK -> ContextCompat.getColor(ctx, R.color.status_ok)
            WRONG_ANSWER -> ContextCompat.getColor(ctx, R.color.status_warning)
            RUNTIME_ERROR -> ContextCompat.getColor(ctx, R.color.status_error)
            COMPILE_ERROR -> ContextCompat.getColor(ctx, R.color.status_error)
        }
    }

    fun toHollowIcon(ctx: Context): Drawable {
        return when (this) {
            OK -> ContextCompat.getDrawable(ctx, R.drawable.status_ok_hollow)
            WRONG_ANSWER -> ContextCompat.getDrawable(ctx, R.drawable.status_warning_hollow)
            RUNTIME_ERROR -> ContextCompat.getDrawable(ctx, R.drawable.status_error_hollow)
            COMPILE_ERROR -> ContextCompat.getDrawable(ctx, R.drawable.status_error_hollow)
        }
    }

    fun toFilledIcon(ctx: Context): Drawable {
        return when (this) {
            OK -> ContextCompat.getDrawable(ctx, R.drawable.status_ok_filled)
            WRONG_ANSWER -> ContextCompat.getDrawable(ctx, R.drawable.status_warning_filled)
            RUNTIME_ERROR -> ContextCompat.getDrawable(ctx, R.drawable.status_error_filled)
            COMPILE_ERROR -> ContextCompat.getDrawable(ctx, R.drawable.status_error_filled)
        }
    }
}

fun List<TestResult>.getRunStatus(): RunStatus {
    return this.mapNotNull { RunStatus.fromApiStatus(it.status) }
            .reduce { s, t -> if (t.severity > s.severity) t else s }
}

fun KoanFolder.getRunStatus(): RunStatus? {
    val runStatuses = this.koans.mapNotNull { it.lastRunStatus }
    return if (runStatuses.isNotEmpty()) {
        runStatuses.reduce { s, t -> if (t.severity > s.severity) t else s }
    } else {
        null
    }
}

data class KoanRunResults(
        // map from TestClass => test results
        // may not exist in case of e.g., syntax errors
        val testResults: Map<String, List<TestResult>>?,

        // map from file name => compile errors
        @SerializedName("errors")
        val compileErrors: Map<String, List<CompilationError>>
) {
    fun getStatus(): RunStatus {
        if (testResults == null) {
            return RunStatus.COMPILE_ERROR
        }
        return testResults.values.flatten().getRunStatus()
    }

    fun hasCompileErrors() = compileErrors.any { it.value.isNotEmpty() }
}

data class TestResult(
        // OK or FAIL (wrong answer) or ERROR (runtime error, or not implemented)
        val status: String,
        val className: String,
        val methodName: String,
        @SerializedName("sourceFileName")
        val testFileName: String,
        // output is wrapped in <outStream>...</outStream> and TWICE html-entity-encoded
        @SerializedName("output")
        val outputWrappedAndTwiceEncoded: String,
        // non-null when status == ERROR
        val exception: Exception?,
        // non-null when status == FAIL
        val comparisonFailure: ComparisonFailure?
) {
    fun getRunStatus(): RunStatus {
        return RunStatus.values().first { it.apiStatus == status }
    }

    fun getOutput(): String {
        // replace newlines with twice-encoded versions because they'll be decoded too
        // Author's note: this is such a goddamn mess I've run out of variable names - and I'm beyond caring at this point.
        val twiceEncodedHacked = outputWrappedAndTwiceEncoded.replace("\n", "&lt;br/&gt;")
        val onceEncoded = textToHtml(twiceEncodedHacked).toString()
        val decoded = textToHtml(onceEncoded).toString()
        return decoded.replace(Regex("""</?outStream>"""), "").trim()
    }
}

// wrong answer
data class ComparisonFailure(
        val message: String,
        val fullName: String,
        val stackTrace: List<StackFrame>,
        // TODO when are these non-null?
        val expected: Any?,
        val actual: Any?,
        val cause: Any?
) {
    fun toHtml(colors: Map<String, String>): String {
        val lines = mutableListOf<String>()
        lines.add("<font color='${colors["error"]}'>$fullName</font>")
        lines.add("Expected: $expected")
        lines.add("Actual: $actual")
        lines.addAll(stackTrace.map { "&nbsp;&nbsp;&nbsp;&nbsp;${it.toHtml(colors)}" })
        return lines.joinToString("<br />")
    }
}

// run-time exceptions
data class Exception(
        val message: String,
        val fullName: String,
        val stackTrace: List<StackFrame>,
        // TODO when is this non-null?
        val cause: Any?
) {
    fun toHtml(colors: Map<String, String>): String {
        val lines = mutableListOf<String>()
        lines.add("<font color='${colors["error"]}'>$fullName: $message</font>")
        lines.addAll(stackTrace.map { "&nbsp;&nbsp;&nbsp;&nbsp;${it.toHtml(colors)}" })
        return lines.joinToString("<br />")
    }
}

data class StackFrame(
        val fileName: String,
        val className: String,
        val methodName: String,
        val lineNumber: Int
) {
    fun toHtml(colors: Map<String, String>): String {
        return "at <font color='${colors["error"]}'>$className.$methodName</font> ($fileName:$lineNumber)"
    }
}

// compile-time errors
data class CompilationError(
        val severity: String,   // "ERROR", TODO...??
        val className: String,  // "red_wavy_line", TODO...??
        val message: String,
        val interval: CodeInterval
) {
    override fun toString(): String {
        return "$severity: (${interval.start.line+1}, ${interval.start.column+1}) $message"
    }
}

data class CodeInterval(
        val start: CodePosition,
        val end: CodePosition
)

data class CodePosition(
        val line: Int,
        @SerializedName("ch")
        val column: Int
)
