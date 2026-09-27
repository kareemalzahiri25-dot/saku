package com.example.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object AvatarBitmapUtil {

    private const val AVATAR_FILENAME = "avatar.png"
    private const val MAX_WIDTH = 512
    private const val MAX_HEIGHT = 512
    private const val QUALITY = 90

    /**
     * Load avatar bitmap dari internal storage.
     * Jika file tidak ada, return null.
     * WAJIB dipanggil dari background thread (atau dengan withContext(Dispatchers.IO))
     */
    suspend fun loadAvatarBitmap(context: Context): Bitmap? = withContext(Dispatchers.IO) {
        val avatarFile = File(context.filesDir, AVATAR_FILENAME)
        if (!avatarFile.exists()) return@withContext null

        try {
            BitmapFactory.decodeFile(avatarFile.absolutePath)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Save bitmap ke internal storage dengan compression.
     * - Resize max 512x512
     * - Format PNG, quality 90
     * WAJIB dipanggil dari background thread (atau dengan withContext(Dispatchers.IO))
     */
    suspend fun saveAvatarBitmap(context: Context, bitmap: Bitmap): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // Compress bitmap
                val compressedBitmap = resizeBitmap(bitmap, MAX_WIDTH, MAX_HEIGHT)

                // Save ke internal storage
                val avatarFile = File(context.filesDir, AVATAR_FILENAME)
                FileOutputStream(avatarFile).use { fos ->
                    compressedBitmap.compress(Bitmap.CompressFormat.PNG, QUALITY, fos)
                    fos.flush()
                }

                // Cleanup
                if (bitmap != compressedBitmap) {
                    compressedBitmap.recycle()
                }

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    /**
     * Delete avatar file dari internal storage.
     * WAJIB dipanggil dari background thread (atau dengan withContext(Dispatchers.IO))
     */
    suspend fun deleteAvatarFile(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val avatarFile = File(context.filesDir, AVATAR_FILENAME)
            avatarFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Decode bitmap dari URI stream, compress, dan convert to bitmap.
     * WAJIB dipanggil dari background thread (atau dengan withContext(Dispatchers.IO))
     */
    suspend fun decodeBitmapFromUri(
        context: Context,
        uri: android.net.Uri
    ): Bitmap? = withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resize bitmap untuk fit max width/height sambil maintain aspect ratio.
     */
    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }

        val scale = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}