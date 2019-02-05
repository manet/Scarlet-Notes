package com.maubis.scarlet.base.export.remote

import com.github.bijoysingh.starter.util.FileManager
import com.google.gson.Gson
import com.maubis.scarlet.base.config.CoreConfig
import com.maubis.scarlet.base.export.support.KEY_EXTERNAL_FOLDER_SYNC_LAST_SCAN
import kotlinx.coroutines.experimental.launch
import java.io.File

const val LAST_MODIFIED_ERROR_MARGIN = 7 * 1000 * 60 * 60 * 24L

class RemoteFolder<T>(val folder: File,
                      val klass: Class<T>,
                      val onRemoteInsert: (T) -> Unit,
                      val onRemoteDelete: (String) -> Unit,
                      val onInitComplete: () -> Unit) {

  val deletedFolder = File(folder, "deleted")
  val uuids = HashSet<String>()
  val deletedUuids = HashSet<String>()

  val lastScanKey = "${KEY_EXTERNAL_FOLDER_SYNC_LAST_SCAN}_${folder.name}"
  var lastScan = CoreConfig.instance.store().get(lastScanKey, 0L)

  init {
    launch {
      deletedFolder.mkdirs()
      val files = folder.listFiles() ?: emptyArray()
      files.forEach {
        if (it.lastModified() > lastScan - LAST_MODIFIED_ERROR_MARGIN) {
          uuids.add(it.name)
          try {
            val item = Gson().fromJson(FileManager.readFromFile(it), klass)
            if (item !== null) {
              onRemoteInsert(item)
            }
          } catch (exception: Exception) {
          }
        }
      }

      val deletedFiles = deletedFolder.listFiles() ?: emptyArray()
      deletedFiles.forEach {
        if (it.lastModified() > lastScan - LAST_MODIFIED_ERROR_MARGIN) {
          deletedUuids.add(it.name)
          onRemoteDelete(it.name)
        }
      }

      onInitComplete()
      CoreConfig.instance.store().put(lastScanKey, System.currentTimeMillis())
    }
  }

  private fun file(uuid: String): File = File(folder, uuid)

  private fun deletedFile(uuid: String): File = File(deletedFolder, uuid)

  fun insert(uuid: String, item: T) {
    try {
      val data = Gson().toJson(item)
      val file = file(uuid)
      FileManager.writeToFile(file, data)
    } catch (exception: Exception) {
    }
  }

  fun deleteEverything() {
    uuids.forEach { delete(it) }
  }

  fun delete(uuid: String) {
    uuids.remove(uuid)
    deletedUuids.add(uuid)
    file(uuid).delete()
    FileManager.writeToFile(deletedFile(uuid), "${System.currentTimeMillis()}")
  }

  fun lock(uuid: String) {
    uuids.remove(uuid)
    file(uuid).delete()
  }
}