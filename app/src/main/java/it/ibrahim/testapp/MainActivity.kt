package it.ibrahim.testapp

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import net.gotev.uploadservice.data.UploadInfo
import net.gotev.uploadservice.data.UploadNotificationAction
import net.gotev.uploadservice.data.UploadNotificationConfig
import net.gotev.uploadservice.data.UploadNotificationStatusConfig
import net.gotev.uploadservice.extensions.getCancelUploadIntent
import net.gotev.uploadservice.network.ServerResponse
import net.gotev.uploadservice.observer.request.RequestObserverDelegate
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        // Every intent for result needs a unique ID in your app.
        // Choose the number which is good for you, here I'll use a random one.
        const val pickFileRequestCode = 42
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        uploadButton.setOnClickListener {
            pickFile()
        }
    }


    // Pick a file with a content provider
    fun pickFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            // Filter to only show results that can be "opened", such as files
            addCategory(Intent.CATEGORY_OPENABLE)
            // search for all documents available via installed storage providers
            type = "*/*"
            // obtain permission to read and persistable permission
            flags =
                (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, pickFileRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (requestCode == pickFileRequestCode && resultCode == Activity.RESULT_OK) {
            data?.let {
                onFilePicked(it.data.toString())
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    fun onFilePicked(filePath: String) {


        try {
            val request =
                MultipartUploadRequest(this, "http://wasa.salesforcepk.com/api/VideoAudio")
                    .setMethod("POST")
                    .addFileToUpload(filePath = filePath, fileName = "File", parameterName = "File")
                    .addParameter("Type", "Video")
                    .addParameter("ID", "115239")
                    .setNotificationConfig { context: Context?, uploadId: String? ->
                        getNotificationConfig(
                            uploadId,
                            R.string.multipart_upload
                        )!!
                    }

            request.subscribe(this, this, object : RequestObserverDelegate {
                override fun onProgress(context: Context, uploadInfo: UploadInfo) {
                    Log.e("LIFECYCLE", "Progress " + uploadInfo.progressPercent)
                }

                override fun onSuccess(
                    context: Context,
                    uploadInfo: UploadInfo,
                    serverResponse: ServerResponse
                ) {
                    Log.e("LIFECYCLE", "Success " + uploadInfo.progressPercent)
                }

                override fun onError(
                    context: Context,
                    uploadInfo: UploadInfo,
                    exception: Throwable
                ) {
                    Log.e("LIFECYCLE", "Error " + exception.message)
                }

                override fun onCompleted(context: Context, uploadInfo: UploadInfo) {
                    Log.e("LIFECYCLE", "Completed ")
                    finish()
                }

                override fun onCompletedWhileNotObserving() {
                    Log.e("LIFECYCLE", "Completed while not observing")
                    finish()
                }
            })
        } catch (exc: Exception) {
            Toast.makeText(this, exc.message, Toast.LENGTH_LONG).show()
        }
    }

    protected fun getNotificationConfig(uploadId: String?, @StringRes title: Int): UploadNotificationConfig? {
        val clickIntent = PendingIntent.getActivity(
            this, 1, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT)
        val autoClear = false
        val largeIcon: Bitmap? = null
        val clearOnAction = true
        val ringToneEnabled = true
        val noActions = ArrayList<UploadNotificationAction>(1)
        val cancelAction = UploadNotificationAction(
            R.drawable.ic_launcher_background,
            getString(R.string.cancel_upload),
            this.getCancelUploadIntent(uploadId!!)
        )
        val progressActions = ArrayList<UploadNotificationAction>(1)
        progressActions.add(cancelAction)
        val progress = UploadNotificationStatusConfig(
            getString(title) + ": " + CustomPlaceholdersProcessor.FILENAME_PLACEHOLDER,
            getString(R.string.uploading),
            R.drawable.ic_launcher_background,
            Color.BLUE,
            largeIcon,
            clickIntent,
            progressActions,
            clearOnAction,
            autoClear
        )
        val success = UploadNotificationStatusConfig(
            getString(title) + ": " + CustomPlaceholdersProcessor.FILENAME_PLACEHOLDER,
            getString(R.string.upload_success),
            R.drawable.ic_launcher_background,
            Color.GREEN,
            largeIcon,
            clickIntent,
            noActions,
            clearOnAction,
            autoClear
        )
        val error = UploadNotificationStatusConfig(
            getString(title) + ": " + CustomPlaceholdersProcessor.FILENAME_PLACEHOLDER,
            getString(R.string.upload_error),
            R.drawable.ic_launcher_background,
            Color.RED,
            largeIcon,
            clickIntent,
            noActions,
            clearOnAction,
            autoClear
        )
        val cancelled = UploadNotificationStatusConfig(
            getString(title) + ": " + CustomPlaceholdersProcessor.FILENAME_PLACEHOLDER,
            getString(R.string.upload_cancelled),
            R.drawable.ic_launcher_background,
            Color.YELLOW,
            largeIcon,
            clickIntent,
            noActions,
            clearOnAction
        )
        return UploadNotificationConfig(App.CHANNEL, ringToneEnabled, progress, success, error, cancelled)
    }
}
