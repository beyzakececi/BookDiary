package com.beyzakececi.bookdiarykotlin

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beyzakececi.bookdiarykotlin.databinding.ActivityBookBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream

class BookActivity : AppCompatActivity() {

    private lateinit var binding : ActivityBookBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Books", MODE_PRIVATE, null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")

        if (info.equals("new")){
            //new book
            binding.bookName.setText("")
            binding.authorName3.setText("")
            binding.pageCount.setText("")
            binding.saveButton.visibility = View.VISIBLE

            val selectedImageBackground = BitmapFactory.decodeResource(applicationContext.resources,R.drawable.select_image)
            binding.imageView.setImageBitmap(selectedImageBackground)

        }else{
            //old book
            binding.saveButton.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",1)
            val cursor = database.rawQuery("SELECT * FROM books WHERE id = ?", arrayOf(selectedId.toString()))
            val bookNameIx = cursor.getColumnIndex("bookname")
            val authorNameIx = cursor.getColumnIndex("authorname")
            val pageCountIx = cursor.getColumnIndex("pagecount")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.bookName.setText(cursor.getString(bookNameIx))
                binding.authorName3.setText(cursor.getString(authorNameIx))
                binding.pageCount.setText(cursor.getString(pageCountIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()

        }
    }

    fun saveButtonClicked(view : View){
        val bookName = binding.bookName.text.toString()
        val bookAuthor = binding.authorName3.text.toString()
        val pageCount = binding.pageCount.text.toString()

        if(selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            //convert image to bytearray
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY, bookname VARCHAR, authorname VARCHAR, pagecount VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO books (bookname, authorname, pagecount, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, bookName)
                statement.bindString(2, bookAuthor)
                statement.bindString(3, pageCount)
                statement.bindBlob(4, byteArray)
                statement.execute()


            }catch (e : Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@BookActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) // clear all activities on top of main activity
            startActivity(intent)
        }

    }

    private fun makeSmallerBitmap(image : Bitmap,maximumSize : Int) : Bitmap{
        var width = image.width
        var height = image.height
        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if(bitmapRatio > 1){
            //landscape image
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else{
            //portrait image
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }
    fun selectImage(view : View){
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED){
            //rationale
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //show an alert dialog
                //rationale
                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                    //request permission
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                //request permission
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }


        }else{
            val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            //intent
            activityResultLauncher.launch(intentGallery)
        }
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    val imageData = intentFromResult.data
                    if(imageData != null){
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@BookActivity.contentResolver,imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        }catch (e : Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                //permission granted
                val intentGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentGallery)
            }else{
                //permission denied
                Toast.makeText(this@BookActivity,"Permission needed!",Toast.LENGTH_LONG).show()
            }
        }

    }
}