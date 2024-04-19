package com.sample.edgedetection.crop

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.sample.edgedetection.EdgeDetectionHandler
import com.sample.edgedetection.R
import com.sample.edgedetection.base.BaseActivity
import com.sample.edgedetection.view.PaperRectangle

class CropActivity : BaseActivity(), ICropView.Proxy {

    private var showMenuItems = false

    private lateinit var mPresenter: CropPresenter

    private lateinit var initialBundle: Bundle

    override fun prepare() {
        this.initialBundle = intent.getBundleExtra(EdgeDetectionHandler.INITIAL_BUNDLE) as Bundle
        this.title = initialBundle.getString(EdgeDetectionHandler.CROP_TITLE)

        if(resources.getBoolean(R.bool.portrait_only)){
            Log.d("ORIENTATION changeeee","${resources.getBoolean(R.bool.portrait_only)}")
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(R.id.paper).post {
            // we have to initialize everything in post when the view has been drawn and we have the actual height and width of the whole view
            val paperView = findViewById<View>(R.id.paper)
            mPresenter.onViewsReady(paperView.width, paperView.height)
        }
    }

    override fun provideContentViewId(): Int = R.layout.activity_crop


    override fun initPresenter() {
        val initialBundle = intent.getBundleExtra(EdgeDetectionHandler.INITIAL_BUNDLE) as Bundle
        mPresenter = CropPresenter(this, initialBundle)
        val imageView : ImageView = findViewById(R.id.paper)
        if (initialBundle.getBoolean(EdgeDetectionHandler.FROM_GALLERY, false)) {
            imageView.scaleType = ImageView.ScaleType.FIT_CENTER
            imageView.adjustViewBounds = true
        } else {
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.adjustViewBounds = false
        }
        findViewById<ImageView>(R.id.crop).setOnClickListener {
            Log.e(TAG, "Crop touched!")
            mPresenter.crop()
            changeMenuVisibility(true)
        }
    }

    override fun getPaper(): ImageView = findViewById(R.id.paper)

    override fun getPaperRect() = findViewById<PaperRectangle>(R.id.paper_rect)

    override fun getCroppedPaper() = findViewById<ImageView>(R.id.picture_cropped)

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.crop_activity_menu, menu)

//        menu.setGroupVisible(R.id.enhance_group, showMenuItems)

        menu.findItem(R.id.rotation_image).isVisible = showMenuItems

//        menu.findItem(R.id.gray).title =
//            initialBundle.getString(EdgeDetectionHandler.CROP_BLACK_WHITE_TITLE) as String
//        menu.findItem(R.id.reset).title =
//            initialBundle.getString(EdgeDetectionHandler.CROP_RESET_TITLE) as String

        if (showMenuItems) {
            menu.findItem(R.id.action_label).isVisible = true
            findViewById<ImageView>(R.id.crop).visibility = View.GONE
        } else {
            menu.findItem(R.id.action_label).isVisible = false
            findViewById<ImageView>(R.id.crop).visibility = View.VISIBLE
        }

        return super.onCreateOptionsMenu(menu)
    }


    private fun changeMenuVisibility(showMenuItems: Boolean) {
        this.showMenuItems = showMenuItems
        invalidateOptionsMenu()
    }

    // handle button activities
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_label -> {
                Log.e(TAG, "Saved touched!")
                item.isEnabled = false
                mPresenter.save()
                setResult(Activity.RESULT_OK)
                System.gc()
                finish()
                return true
            }
            R.id.rotation_image -> {
                Log.e(TAG, "Rotate touched!")
                mPresenter.rotate()
                return true
            }
//            R.id.gray -> {
//                Log.e(TAG, "Black White touched!")
//                mPresenter.enhance()
//                return true
//            }
//            R.id.reset -> {
//                Log.e(TAG, "Reset touched!")
//                mPresenter.reset()
//                return true
//            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
