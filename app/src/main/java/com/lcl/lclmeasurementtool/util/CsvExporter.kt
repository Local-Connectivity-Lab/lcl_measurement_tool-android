package com.lcl.lclmeasurementtool.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.lcl.lclmeasurementtool.model.datamodel.ConnectivityReportModel
import com.lcl.lclmeasurementtool.model.datamodel.SignalStrengthReportModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {

    /**
     * Export signal strength data to a CSV file
     */
    suspend fun exportSignalStrengthToCsv(
        context: Context, 
        data: List<SignalStrengthReportModel>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "signal_strength_export_${getCurrentDateTime()}.csv"
            val csvContent = buildSignalStrengthCsv(data)
            return@withContext saveToFile(context, fileName, csvContent)
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Export connectivity data to a CSV file
     */
    suspend fun exportConnectivityToCsv(
        context: Context,
        data: List<ConnectivityReportModel>
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val fileName = "connectivity_export_${getCurrentDateTime()}.csv"
            val csvContent = buildConnectivityCsv(data)
            return@withContext saveToFile(context, fileName, csvContent)
        } catch (e: IOException) {
            e.printStackTrace()
            return@withContext null
        }
    }

    /**
     * Build CSV content for signal strength data
     */
    private fun buildSignalStrengthCsv(data: List<SignalStrengthReportModel>): String {
        val csvBuilder = StringBuilder()
        
        // Add CSV header
        csvBuilder.append("Timestamp,Latitude,Longitude,Cell ID,Device ID,Signal Strength (dBm),Signal Level\n")
        
        // Add data rows
        data.forEach { signal ->
            csvBuilder.append("${signal.timestamp},${signal.latitude},${signal.longitude},")
            csvBuilder.append("${signal.cellId},${signal.deviceId},${signal.dbm},${signal.levelCode}\n")
        }
        
        return csvBuilder.toString()
    }
    
    /**
     * Build CSV content for connectivity data
     */
    private fun buildConnectivityCsv(data: List<ConnectivityReportModel>): String {
        val csvBuilder = StringBuilder()
        
        // Add CSV header
        csvBuilder.append("Timestamp,Latitude,Longitude,Cell ID,Device ID,Download Speed,Upload Speed,Ping,Packet Loss\n")
        
        // Add data rows
        data.forEach { connectivity ->
            csvBuilder.append("${connectivity.timestamp},${connectivity.latitude},${connectivity.longitude},")
            csvBuilder.append("${connectivity.cellId},${connectivity.deviceId},${connectivity.downloadSpeed},")
            csvBuilder.append("${connectivity.uploadSpeed},${connectivity.ping},${connectivity.packetLoss}\n")
        }
        
        return csvBuilder.toString()
    }
    
    /**
     * Save CSV content to a file in the Downloads directory
     * 
     * This approach uses ContentResolver for compatibility across Android versions
     */
    private fun saveToFile(context: Context, fileName: String, content: String): Uri? {
        return try {
            // Use ContentValues and ContentResolver to create a file
            val contentValues = android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                
                // For API 29+, use the Downloads collection
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, "Download")
                    put(android.provider.MediaStore.Downloads.IS_PENDING, 1)
                }
            }
            
            // Choose the right content URI based on Android version
            val contentUri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                android.provider.MediaStore.Files.getContentUri("external")
            }
            
            val uri = context.contentResolver.insert(contentUri, contentValues)
            
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                    outputStream.flush()
                }
                
                // For API 29+, update IS_PENDING to 0
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(android.provider.MediaStore.Downloads.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                }
            }
            
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            
            // Use a simpler fallback method if the MediaStore approach fails
            try {
                val downloadsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(downloadsDir, fileName)
                
                java.io.FileWriter(file).use { writer ->
                    writer.write(content)
                }
                
                androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            } catch (e2: Exception) {
                e2.printStackTrace()
                null
            }
        }
    }
    
    /**
     * Get current date and time formatted for filenames
     */
    private fun getCurrentDateTime(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
