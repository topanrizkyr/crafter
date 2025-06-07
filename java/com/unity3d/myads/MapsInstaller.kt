package com.unity3d.myads



import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.collection.ArraySet
import androidx.preference.PreferenceManager
import com.unity3d.appscreen.SplashActivity
import com.unity3d.appscreen.SplashActivity.checkIfCanStartGame
import com.unity3d.appscreen.SplashActivity.splashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.io.LineNumberReader

// Installer para los mundos de Minecraft
class MapsInstaller(
    private val context: Context
) {
    // Función de instalación que ya no bloquea el hilo principal
    fun install() {
        // Ejecutar en segundo plano usando CoroutineScope sin bloquear el hilo principal
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val preferences = splashActivity.getSharedPreferences(
                    "MinecraftPreferences",
                    Context.MODE_PRIVATE
                )
                val resourcesInstalled = preferences.getBoolean("worldsInstalled", false)
                if (resourcesInstalled) {
                    return@launch
                }
                // Output path: extract archive to tmp dir
                val tmp = File(context.cacheDir, "worlds.zip")

                // Output maps: all minecraft worlds
                val worldsDir = File(context.filesDir.parentFile, "games/com.mojang/minecraftWorlds")

                if (!worldsDir.exists()) {
                    worldsDir.mkdirs()
                }

                // Read worlds.zip in app.apk
                context.assets.open("mapas/worlds.zip").use {
                    // Extract worlds.zip to minecraft maps dir
                    FileHelper.writeToFile(tmp, it)
                }

                // Extract all maps
                ZipHelper.unzip(tmp, worldsDir)

                preferences.edit().putBoolean("worldsInstalled", true).apply()
                // Delete temporary file after extraction
                tmp.delete()

                SkinsInstaller(context).install()

            } catch (e: Exception) {
                SkinsInstaller(context).install()
                e.printStackTrace()
            }
        }
    }
}


// Installer para los skins de Minecraft (copiando una carpeta directamente)
class SkinsInstaller(
    private val context: Context
) {
    // Función de instalación que ya no bloquea el hilo principal
    fun install() {
        // Ejecutar en segundo plano usando CoroutineScope sin bloquear el hilo principal
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Carpeta que contiene los skins en los assets
//                val sourceFolder = "skins"
//                // Carpeta de destino donde los skins serán copiados
//                val skinsDir = File(context.filesDir.parentFile, "games/com.mojang/skin_packs")
//
//                if (!skinsDir.exists()) {
//                    skinsDir.mkdirs()  // Crear la carpeta destino si no existe
//                }
//
//                // Llamar a la función para copiar la carpeta desde los assets
//                copyFolderFromAssets(sourceFolder, skinsDir)
                val preferences = splashActivity.getSharedPreferences(
                    "MinecraftPreferences",
                    Context.MODE_PRIVATE
                )
                val resourcesInstalled = preferences.getBoolean("skinsInstalled", false)
                if (resourcesInstalled) {
                    return@launch
                }
                val tmp = File(context.cacheDir, "skins")

                // Output maps: all minecraft worlds
                val sp = File(context.filesDir.parentFile, "games/com.mojang/skin_packs")

                if (!sp.exists()) {
                    sp.mkdirs()
                }

                // Read worlds.zip in app.apk
                context.assets.open("skins/skinsre.zip").use {
                    // Extract worlds.zip to minecraft maps dir
                    FileHelper.writeToFile(tmp, it)
                }

                // Extract all maps
                ZipHelper.unzip(tmp, sp)

                preferences.edit().putBoolean("skinsInstalled", true).apply()
                // Delete temporary file after extraction
                tmp.delete()

                BehaviorsInstaller(context).install()
            } catch (e: Exception) {
                BehaviorsInstaller(context).install()
                e.printStackTrace()
            }
        }
    }

    private fun copyFolderFromAssets(sourceFolder: String, targetFolder: File) {
        try {
            val assetManager = context.assets
            val files = assetManager.list(sourceFolder)

            if (files != null) {
                for (file in files) {
                    val sourcePath = "$sourceFolder/$file"
                    val targetFile = File(targetFolder, file)

                    if (assetManager.list(sourcePath)?.isNotEmpty() == true) {
                        targetFile.mkdirs()  // Crear la carpeta si es una subcarpeta
                        copyFolderFromAssets(sourcePath, targetFile)  // Recursión para copiar subcarpetas
                    } else {
                        context.assets.open(sourcePath).use { inputStream ->
                            FileHelper.writeToFile(targetFile, inputStream)  // Copiar archivos individuales
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
class AvatarInstaller(private val context: Context) {

    interface OnCompleteCallback {
        fun onComplete(success: Boolean)
    }

    fun install(callback: OnCompleteCallback) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sourceFolder = "skins"
                val skinsDir = File(context.filesDir.parentFile, "games/com.mojang/skin_packs")

                if (!skinsDir.exists()) {
                    skinsDir.mkdirs()
                    Log.d("AvatarInstaller", "Directorio de packs creado: ${skinsDir.absolutePath}")
                }

                copyFolderFromAssets(sourceFolder, skinsDir)

                val success = skinsDir.listFiles()?.isNotEmpty() == true
                withContext(Dispatchers.Main) {
                    callback.onComplete(success)
                }
            } catch (e: Exception) {
                Log.e("AvatarInstaller", "Error durante la instalación de packs: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback.onComplete(false)
                }
            }
        }
    }

    private fun copyFolderFromAssets(sourceFolder: String, targetFolder: File) {
        val assetManager = context.assets
        try {
            val files = assetManager.list(sourceFolder)

            if (files != null) {
                for (file in files) {
                    val sourcePath = "$sourceFolder/$file"
                    val targetFile = File(targetFolder, file)

                    if (assetManager.list(sourcePath)?.isNotEmpty() == true) {
                        if (!targetFile.exists()) {
                            targetFile.mkdirs()
                        }
                        copyFolderFromAssets(sourcePath, targetFile)
                    } else {
                        if (!targetFile.exists()) {
                            context.assets.open(sourcePath).use { inputStream ->
                                FileHelper.writeToFile(targetFile, inputStream)
                            }
                            Log.d("SkinsInstaller", "Archivo copiado: ${targetFile.absolutePath}")
                        }
                    }
                }
            } else {
                Log.w("SkinsInstaller", "La carpeta '$sourceFolder' está vacía o no existe en los assets.")
            }
        } catch (e: IOException) {
            Log.e("SkinsInstaller", "Error al copiar archivos desde assets: ${e.message}")
            e.printStackTrace()
        }
    }
}

// Installer para los behaviors de Minecraft (copiando una carpeta directamente)
class BehaviorsInstaller(
    private val context: Context
) {
    fun install() {
        // Ejecutar en segundo plano usando CoroutineScope sin bloquear el hilo principal
        CoroutineScope(Dispatchers.IO).launch {
            try {
//                val sourceFolder = "behaviors"
//                val behaviorDir = File(context.filesDir.parentFile, "games/com.mojang/behavior_packs")
//
//                if (!behaviorDir.exists()) {
//                    behaviorDir.mkdirs()
//                }
//
////                copyFolderFromAssets(sourceFolder, behaviorDir)
                val preferences = splashActivity.getSharedPreferences(
                    "MinecraftPreferences",
                    Context.MODE_PRIVATE
                )
                val resourcesInstalled = preferences.getBoolean("bpInstalled", false)
                if (resourcesInstalled) {
                    return@launch
                }
                val tmp = File(context.cacheDir, "behaviors")

                // Output maps: all minecraft worlds
                val bp = File(context.filesDir.parentFile, "games/com.mojang/behavior_packs")

                if (!bp.exists()) {
                    bp.mkdirs()
                }

                // Read worlds.zip in app.apk
                context.assets.open("behaviors/addonsbe.zip").use {
                    // Extract worlds.zip to minecraft maps dir
                    FileHelper.writeToFile(tmp, it)
                }

                // Extract all maps
                ZipHelper.unzip(tmp, bp)

                preferences.edit().putBoolean("bpInstalled", true).apply()
                // Delete temporary file after extraction
                tmp.delete()

                ResourcesInstaller(context).install()
            } catch (e: Exception) {
                ResourcesInstaller(context).install()
                e.printStackTrace()
            }
        }
    }

    private fun copyFolderFromAssets(sourceFolder: String, targetFolder: File) {
        try {
            val assetManager = context.assets
            val files = assetManager.list(sourceFolder)

            if (files != null) {
                for (file in files) {
                    val sourcePath = "$sourceFolder/$file"
                    val targetFile = File(targetFolder, file)

                    if (assetManager.list(sourcePath)?.isNotEmpty() == true) {
                        targetFile.mkdirs()
                        copyFolderFromAssets(sourcePath, targetFile)
                    } else {
                        context.assets.open(sourcePath).use { inputStream ->
                            FileHelper.writeToFile(targetFile, inputStream)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}


/// Installer para los resources de Minecraft (copiando una carpeta directamente)
class ResourcesInstaller(
    private val context: Context
) {
    fun install() {
        // Ejecutar en segundo plano usando CoroutineScope sin bloquear el hilo principal
        CoroutineScope(Dispatchers.IO).launch {
            try {
//                val sourceFolder = "resources"
//                val resourcesDir = File(context.filesDir.parentFile, "games/com.mojang/resource_packs")
//
//                if (!resourcesDir.exists()) {
//                    resourcesDir.mkdirs()
//                }
//
//                copyFolderFromAssets(sourceFolder, resourcesDir)
                val preferences = splashActivity.getSharedPreferences(
                    "MinecraftPreferences",
                    Context.MODE_PRIVATE
                )
                val resourcesInstalled = preferences.getBoolean("resourcesInstalled", false)
                if (resourcesInstalled) {
                    return@launch
                }
                val tmp = File(context.cacheDir, "resources")

                // Output maps: all minecraft worlds
                val bp = File(context.filesDir.parentFile, "games/com.mojang/resource_packs")

                if (!bp.exists()) {
                    bp.mkdirs()
                }

                // Read worlds.zip in app.apk
                context.assets.open("resources/addonsre.zip").use {
                    // Extract worlds.zip to minecraft maps dir
                    FileHelper.writeToFile(tmp, it)
                }

                // Extract all maps
                ZipHelper.unzip(tmp, bp)

                preferences.edit().putBoolean("resourcesInstalled", true).apply()
                // Delete temporary file after extraction
                tmp.delete()


                checkIfCanStartGame();
            } catch (e: Exception) {
                checkIfCanStartGame();
                e.printStackTrace()
            }
        }
    }

    private fun copyFolderFromAssets(sourceFolder: String, targetFolder: File) {
        try {
            val assetManager = context.assets
            val files = assetManager.list(sourceFolder)

            if (files != null) {
                for (file in files) {
                    val sourcePath = "$sourceFolder/$file"
                    val targetFile = File(targetFolder, file)

                    if (assetManager.list(sourcePath)?.isNotEmpty() == true) {
                        targetFile.mkdirs()
                        copyFolderFromAssets(sourcePath, targetFile)
                    } else {
                        context.assets.open(sourcePath).use { inputStream ->
                            FileHelper.writeToFile(targetFile, inputStream)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}


// Installer para los servidores de Minecraft
class ServersInstaller(
    private val context: Context
) {
    fun install() {
        // Ejecutar en segundo plano usando CoroutineScope sin bloquear el hilo principal
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                val currentVersion = context.packageManager.getPackageInfo(context.packageName, 0).versionCode
                val savedVersion = sharedPreferences.getInt("mod_version", 0)

                if (savedVersion < currentVersion) {
                    sharedPreferences.edit().putInt("mod_version", currentVersion).apply()

                    val serverList = ArraySet<String>()
                    serverList.add(":§l§a#1 Сервер (RU):s46.minesrv.ru:19132:1568120063")
                    serverList.add(":§l§a#2 Сервер (RU):s47.minesrv.ru:19132:1568120044")
                    serverList.add(":§l§a#4 Сервер (RU):s48.minesrv.ru:19132:1568120032")
                    serverList.add(":§l§a# Ѕerver Craftersmc :play.craftersmc.net:19132")
                    serverList.add(":§l§a# Ѕerver Complex Gaming :mps.mc-complex.com:19132")
                    serverList.add(":§l§a# Ѕerver WASDCRAFT :WasdCraft.aternos.me:61291")
                    serverList.add(":§l§a# Ѕerver MCHub :pe.mchub.com:19132")
                    serverList.add(":§l§a# Ѕerver AkumaMC :bedrock.akumamc.net:19132")
                    serverList.add(":§l§a# Ѕerver FadeCloud :mp.fadecloud.com:19132")

                    val minecraftDir = File(context.filesDir.parentFile, "games/com.mojang/minecraftpe")
                    if (!minecraftDir.exists()) {
                        minecraftDir.mkdirs()
                    }

                    val serversFile = File(minecraftDir, "external_servers.txt")
                    if (!serversFile.exists()) {
                        serversFile.createNewFile()
                    }

                    val existingServers = mutableSetOf<String>()
                    LineNumberReader(FileReader(serversFile)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            existingServers.add(line!!.replaceFirst("^\\d+:", ":"))
                        }
                    }

                    val updatedServers = serverList.filterNot { existingServers.contains(it) }
                    FileWriter(serversFile, true).use { writer ->
                        for (server in updatedServers) {
                            writer.write("${existingServers.size + 1}:$server\n")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}