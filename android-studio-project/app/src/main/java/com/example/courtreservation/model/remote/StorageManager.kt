package com.example.courtreservation.model.remote

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageManager @Inject constructor(private val storage: FirebaseStorage) {
    suspend fun downloadImage(link: String): Bitmap? {
        val imageRef = storage.getReferenceFromUrl(link)
        Log.d("downloadImage","imageRef OK")
        var bitmap: Bitmap? = null
        imageRef.getBytes(16*1024*1024) //max dimensione file 16MB
            .addOnSuccessListener { bytes ->
                Log.d("downloadImage","Success OK")
                bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
            .addOnFailureListener {
                Log.d("downloadImage","Errore")
            }.await()
        return bitmap
    }

    suspend fun uploadImage(uri: Uri):String {
        val imageRef = storage.reference.child("images/${uri.lastPathSegment}")

        var uploadTask = imageRef.putFile(uri)
        var link:String?=null
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                link=task.result.toString()
                Log.d("DownloadUri","${task.result}")
            } else {
                Log.d("Upload Image: ","Error")
            }
        }.await()
        return link!!
    }
}