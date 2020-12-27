import java.io.File
import java.lang.StringBuilder

data class Module(
        val name: String,
        val version: String,
        val position: Int
)

val versionPrefix = "versions."
val versionFileName = "features/versions.gradle"

main(arrayOf("/home/suson/Android-Projects/sanima/features"))

fun main(args: Array<String>) {
    println("running version increment")
    val workingDir = "features/"
    val shouldCommit = args[0] == "true"

    val delimiter = "@f1soft@"
    val targetPath = "$workingDir/$versionFileName"
    val changedFiles = getChangedFilesFromBaseCommit(workingDir)
    val versionList = File(targetPath).readLines()
    val parsedModules = getParsedModules(delimiter, versionList)
    val changedModules = getChangedModulesName(changedFiles)
    val commitMessage = updateVersions(versionList, parsedModules, changedModules, targetPath)
    if (shouldCommit) {
        println("creating git commit for version increment")
        println("commit message: \t\n$commitMessage")
        commitVersionIncrement(commitMessage)
    } else {
        println("only executing dry run")
    }
}

fun commitVersionIncrement(commitMessage: String) {
    Runtime.getRuntime().apply {
        exec("export repoUrl=\$(grep \"@\" <<< \$CI_REPOSITORY_URL | cut -d\"@\" -f2)")
        exec("git config --global user.email \"susanthapa202@gmail.com\"")
        exec("git config --global user.name \"Susan Thapa\"")
        exec("git remote remove origin")
        exec("git remote add origin https://\$GIT_USERNAME:\$GIT_PASSWORD@\$repoUrl")
        exec("git add $versionFileName")
        exec("git commit -m $commitMessage")
        exec("git push origin \$CI_COMMIT_REF_NAME")
    }
}

fun updateVersions(
        versionList: List<String>,
        parsedModules: List<Module>,
        changedModules: List<String>,
        outputFilePath: String
): String {
    val commitMessage = StringBuilder("updated versions: \n")
    val outputList = versionList.toMutableList()
    // loop through all the parsed modules
    parsedModules.forEach {
        // check if this module is changed
        if (changedModules.contains(it.name)) {
            // increment the version
            val newVersion = getIncrementedVersion(it.version)
            val newModule = "$versionPrefix${it.name} = \"$newVersion\""
            outputList[it.position] = newModule
            commitMessage.append("\t${it.name}: ${it.version} ----> $newVersion\n")
        }
    }

    val outputFile = File(outputFilePath)
    val writer = outputFile.outputStream().bufferedWriter()
    outputList.forEach {
        writer.write(it)
        writer.newLine()
    }
    writer.close()
    // remove last empty new line
    var trimmedMessage = commitMessage.toString()
    trimmedMessage = if (trimmedMessage.lastIndexOf("\n") > 0) {
        trimmedMessage.substring(0, trimmedMessage.lastIndexOf("\n"))
    } else {
        trimmedMessage
    }

    return trimmedMessage
}

fun commitChanges() {

}

fun getIncrementedVersion(version: String): String {
    val majorIndex = version.indexOf('.')
    var major = version.substring(0, majorIndex).toInt()
    val versionPart = version.substring(majorIndex + 1)
    val minorIndex = versionPart.indexOf('.')
    var minor = versionPart.substring(0, minorIndex).toInt()
    var patch = versionPart.substring(minorIndex + 1).toInt()

    if (patch > 98) {
        patch = 0
        minor++
    } else {
        patch++
    }
    if (minor > 99) {
        minor = 0
        major++
    }

    return "$major.$minor.$patch"
}

fun getDelimiterPosition(versionList: List<String>, delimiter: String): Pair<Int, Int> {
    var delimiterCount = 0
    var startIndex = 0
    var endIndex = 0
    for (i in versionList.indices) {
        if (versionList[i].contains(delimiter)) {
            delimiterCount++
            if (delimiterCount == 1) {
                startIndex = i
            }
            if (delimiterCount == 2) {
                endIndex = i
                break
            }
        }
    }

    return Pair(startIndex, endIndex)
}


fun getParsedModules(delimiter: String, versionsList: List<String>): List<Module> {
    val delimiterRange = getDelimiterPosition(versionsList, delimiter)
    val modules = mutableListOf<Module>()
    for (i in (delimiterRange.first + 1) until delimiterRange.second) {
        val version = getVersionString(versionsList[i])
        val module = Module(version.first.trim(), version.second.trim(), i)
        modules.add(module)
    }

    return modules
}

fun getVersionString(version: String): Pair<String, String> {
    val sanitizedVersion = if (version.startsWith("//")) {
        version.replace("//", "")
    } else {
        version
    }
    val versionList = sanitizedVersion.split("=")
    return Pair(versionList[0].substring(versionPrefix.length), versionList[1].replace("\"", ""))
}

fun getChangedModulesName(changedFiles: List<String>): List<String> {
    return changedFiles.filter {
        // check for folder
        it.contains("/")
    }.map {
        it.substring(0, it.indexOf("/"))
    }.distinct()
}

fun getChangedFilesFromBaseCommit(workingDir: String): List<String> {
    // get the changed files from the base of the current branch to the HEAD
    val process = Runtime.getRuntime().exec("git diff --name-only develop..", arrayOf(), File(workingDir))
    return process.inputStream.bufferedReader().readLines()
}