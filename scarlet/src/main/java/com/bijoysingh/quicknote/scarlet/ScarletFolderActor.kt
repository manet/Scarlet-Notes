package com.bijoysingh.quicknote.scarlet

import com.bijoysingh.quicknote.Scarlet.Companion.firebase
import com.bijoysingh.quicknote.Scarlet.Companion.gDrive
import com.bijoysingh.quicknote.firebase.data.getFirebaseFolder
import com.maubis.scarlet.base.database.room.folder.Folder
import com.maubis.scarlet.base.core.folder.MaterialFolderActor

class ScarletFolderActor(folder: Folder) : MaterialFolderActor(folder) {

  override fun onlineSave() {
    super.onlineSave()
    firebase?.insert(folder.getFirebaseFolder())
    gDrive?.notifyInsert(folder.getFirebaseFolder())
  }

  override fun delete() {
    super.delete()
    firebase?.remove(folder.getFirebaseFolder())
    gDrive?.notifyRemove(folder.getFirebaseFolder())
  }
}