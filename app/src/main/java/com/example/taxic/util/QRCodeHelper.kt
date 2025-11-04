package com.example.taxic.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

/**
 * ===========================================
 * QR CODE HELPER
 * ===========================================
 *
 * This class generates QR codes from text.
 *
 * WHAT IS A QR CODE?
 * - Those square barcodes you can scan with your phone
 * - Can contain text, URLs, contact info, etc.
 *
 * HOW IT WORKS:
 * 1. Take text (like driver info)
 * 2. Encode it into a QR code matrix
 * 3. Convert matrix to a Bitmap image
 * 4. Display the image
 */
object QRCodeHelper {

    /**
     * Generate a QR code image from text
     *
     * STEPS:
     * 1. Create a QR code writer
     * 2. Encode the text into a matrix of black/white pixels
     * 3. Create a Bitmap image
     * 4. Loop through matrix and color each pixel
     * 5. Return the Bitmap
     *
     * @param text The text to encode (driver info)
     * @param size Width and height of QR code in pixels (default 512)
     * @return Bitmap image containing the QR code
     */
    fun generateQRCode(text: String, size: Int = 512): Bitmap {

        // Step 1: Create QR code writer
        val writer = QRCodeWriter()

        // Step 2: Encode text into a matrix
        // BarcodeFormat.QR_CODE = the type of barcode
        // size, size = width and height
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)

        // Step 3: Create empty Bitmap
        // RGB_565 = color format (faster than ARGB_8888)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)

        // Step 4: Loop through matrix and color pixels
        // x = horizontal position
        // y = vertical position
        for (x in 0 until size) {
            for (y in 0 until size) {
                // Check if this pixel should be black or white
                // bitMatrix.get(x, y) returns true for black pixels
                val pixelColor = if (bitMatrix.get(x, y)) {
                    Color.BLACK  // QR code module (the black squares)
                } else {
                    Color.WHITE  // Background
                }

                // Set the pixel color in bitmap
                bitmap.setPixel(x, y, pixelColor)
            }
        }

        // Step 5: Return the finished QR code image
        return bitmap
    }
}