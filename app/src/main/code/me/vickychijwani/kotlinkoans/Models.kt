package me.vickychijwani.kotlinkoans

// single koan
data class Koan(
        val id: String,                         // == KoanMetadata.publicId
        val name: String,                       // == KoanMetadata.name; UI label
        val help: String,                       // HTML of problem description
                                                // code blocks are marked <code data-lang="text/x-kotlin"> OR <code data-lang="text/x-java">
        val files: List<KoanFile>,
        val confType: String                    // always "junit", it seems
)

data class KoanFile(
        val publicId: String,
        val name: String,                       // UI label
        val text: String,                       // file contents (Kotlin code presumably)
                                                // initial selection == first occurrence of TODO()
        val modifiable: Boolean,                // true if the file can be edited
        val type: String,                       // "KOTLIN_FILE" or "KOTLIN_TEST_FILE"
        val solutions: List<String>?            // possible (expected) solutions
                                                // only a single file in every Koan contains this
)

// top-level list of koans
data class KoanFolder(
        val id: String,
        val name: String,
        val levels: List<KoanLevel>?,           // null if `projects` is non-empty
        val projects: List<KoanMetadata>,       // e.g., Kotlin Koans/Introduction => [Hello World, Java to Kotlin conversion, ...]
                                                // empty if `childFolders` is non-empty
        val childFolders: List<KoanFolder>      // e.g., Kotlin Koans => [Introduction, Conventions, ...]
                                                // empty if `projects` is non-empty
)

data class KoanMetadata(
        val publicId: String,                   // == Koan.id
        val name: String                        // == Koan.name; UI label
)

data class KoanLevel(
        val projectsNeeded: Int,                // total # of koans needed to finish this level
        val color: String                       // color of tick on progress bar
)
