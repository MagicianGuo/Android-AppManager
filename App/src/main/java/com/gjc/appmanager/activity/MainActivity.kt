package com.gjc.appmanager.activity

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.gjc.appmanager.R
import com.gjc.appmanager.adapter.PackageAdapter
import com.gjc.appmanager.base.BaseActivity
import com.gjc.appmanager.bean.BeanApp
import com.gjc.appmanager.constant.AppType
import com.gjc.appmanager.constant.RequestCode
import com.gjc.appmanager.constant.SearchType
import com.gjc.appmanager.databinding.ActivityMainBinding
import com.gjc.appmanager.util.shortToast

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val mAdapter = PackageAdapter()
    private var mCurrentSearchType = SearchType.LABEL
    private var mCurrentAppType = AppType.USER_INSTALLED
    private var mKeyWord = ""
    private var mThread: Thread? = null
    private val mTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (!TextUtils.equals(mKeyWord, s)) {
                refreshSearchTask()
                mKeyWord = s.toString()
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
    }

    override fun onResume() {
        super.onResume()
        refreshSearchTask(false)
    }

    private fun initView() {
        binding.etInput.addTextChangedListener(mTextWatcher)
        binding.ivClear.setOnClickListener {
            binding.etInput.setText("")
        }
        binding.spinSearchType.adapter = object : ArrayAdapter<String>(
            this,
            R.layout.layout_search_type_item,
            arrayOf("搜应用名", "搜包名")
        ) {}
        binding.spinSearchType.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mCurrentSearchType = when (position) {
                        0 -> {
                            binding.etInput.hint = "请输入应用名"
                            SearchType.LABEL
                        }
                        1 -> {
                            binding.etInput.hint = "请输入包名"
                            SearchType.PACKAGE_NAME
                        }
                        else -> throw RuntimeException("错误的索引：$position")
                    }
                    if (mKeyWord.isNotEmpty()) {
                        refreshSearchTask(false)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        binding.spinAppType.adapter = object : ArrayAdapter<String>(
            this,
            R.layout.layout_app_type_item,
            arrayOf("全部", "系统应用", "用户已安装应用")
        ) {}
        binding.spinAppType.setSelection(
            when (mCurrentAppType) {
                AppType.ALL -> 0
                AppType.SYSTEM -> 1
                AppType.USER_INSTALLED -> 2
                else -> throw RuntimeException("错误的类型：$mCurrentAppType")
            }
        )
        binding.spinAppType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val appType = when (position) {
                    0 -> AppType.ALL
                    1 -> AppType.SYSTEM
                    2 -> AppType.USER_INSTALLED
                    else -> throw RuntimeException("错误的索引：$position")
                }
                if (mCurrentAppType != appType) {
                    selectAppType(appType)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
        binding.rvList.layoutManager = LinearLayoutManager(this)
        binding.rvList.adapter = mAdapter
        mAdapter.setOperationListener(object : PackageAdapter.IOperationListener {
            override fun onOpen(beanApp: BeanApp) {
                try {
                    val intent = packageManager.getLaunchIntentForPackage(beanApp.packageName)
                    startActivity(intent)
                } catch (e: Exception) {
                    "无法打开这个应用。".shortToast()
                    e.printStackTrace()
                }
            }

            override fun onDetail(beanApp: BeanApp) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:${beanApp.packageName}"))
                startActivity(intent)
            }

            override fun onUninstall(beanApp: BeanApp) {
                val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                    .setData(Uri.parse("package:${beanApp.packageName}"))
                    .putExtra(Intent.EXTRA_RETURN_RESULT, true)
                startActivityForResult(intent, RequestCode.UNINSTALL)
            }
        })

    }

    override fun onDestroy() {
        binding.etInput.removeTextChangedListener(mTextWatcher)
        mThread?.interrupt()
        mThread = null
        super.onDestroy()
    }

    private fun selectAppType(@AppType type: Int) {
        mCurrentAppType = type
        refreshSearchTask(false)
    }

    private fun getUserPackageList(@AppType type: Int, keyword: String = ""): List<BeanApp> {
        val pm = packageManager
        val pkgList = mutableListOf<BeanApp>()
        val packageInfoList = pm.getInstalledPackages(0)
        for (i in 0 until packageInfoList.size) {
            val packageInfo = packageInfoList[i]
            val isSystem = packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
            val isValidAppType = when (type) {
                AppType.SYSTEM -> {
                    isSystem
                }

                AppType.USER_INSTALLED -> {
                    !isSystem
                }

                else -> true
            }
            if (!isValidAppType) {
                continue
            }
            val applicationInfo = packageInfo.applicationInfo
            val packageName = applicationInfo.packageName
            val label = pm.getApplicationLabel(applicationInfo).toString()
            val isMatchKeyword = if (keyword.isEmpty()) {
                true
            } else {
                when (mCurrentSearchType) {
                    SearchType.LABEL -> {
                        isMatchResult(keyword, label)
                    }

                    SearchType.PACKAGE_NAME -> {
                        isMatchResult(keyword, packageName)
                    }

                    else -> throw RuntimeException("错误的类型：$mCurrentSearchType")
                }
            }
            if (isMatchKeyword) {
                val icon = pm.getApplicationIcon(applicationInfo)
                val compileSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    applicationInfo.compileSdkVersion
                } else {
                    -1
                }
                pkgList.add(BeanApp(label, packageName, icon, compileSdk, applicationInfo.targetSdkVersion, applicationInfo.minSdkVersion))
            }
        }
        return pkgList
    }

    private fun isMatchResult(keyword: String, compare: String): Boolean {
        var tempCompare = compare
        for (keywordIndex in keyword.indices) {
            val ch = keyword[keywordIndex]
            if (tempCompare.contains(ch, true)) {
                val findIndex = tempCompare.indexOf(ch, 0, true)
                tempCompare = tempCompare.substring(findIndex + 1, tempCompare.length)
                continue
            }
            return false
        }
        return true
    }

    private fun refreshSearchTask(delay: Boolean = true) {
        mThread?.interrupt()
        mThread = object : Thread() {
            override fun run() {
                try {
                    if (delay) {
                        sleep(1000L)
                    }
                    runOnUiThread {
                        loading(true)
                    }
                    sleep(300L)
                    val list = getUserPackageList(mCurrentAppType, binding.etInput.text.toString())
                    runOnUiThread {
                        mAdapter.updateList(list)
                    }
                } catch (e: InterruptedException) {
                    return
                } finally {
                    runOnUiThread {
                        loading(false)
                    }
                }
            }
        }
        mThread?.start()
    }

    private fun loading(b: Boolean) {
        binding.pbLoading.isVisible = b
    }

    override fun onBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode.UNINSTALL) {
            if (resultCode == RESULT_OK){
                "卸载成功".shortToast()
                refreshSearchTask(false)
            } else{
                "卸载失败".shortToast()
            }
        }
    }
}